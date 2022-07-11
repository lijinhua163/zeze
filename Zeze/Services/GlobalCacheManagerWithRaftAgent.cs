
using Zeze.Builtin.GlobalCacheManagerWithRaft;
using System.Threading.Tasks;
using System;
using Zeze.Transaction;
using Zeze.Net;
using Zeze.Serialize;

namespace Zeze.Services
{
    public class GlobalCacheManagerWithRaftAgent : AbstractGlobalCacheManagerWithRaftAgent, Zeze.Transaction.IGlobalAgent
    {
        static readonly NLog.Logger logger = NLog.LogManager.GetCurrentClassLogger();
        public Application Zeze { get; }

        public void Dispose()
        {
            GC.SuppressFinalize(this);
            Stop();
        }

        public GlobalCacheManagerWithRaftAgent(Application zeze)
        {
            Zeze = zeze;
        }

        public async Task Start(string[] hosts)
        {
            if (null != Agents)
                return;

            Agents = new RaftAgent[hosts.Length];
            for (int i = 0; i < hosts.Length; ++i)
            {
                var raftconf = Raft.RaftConfig.Load(hosts[i]);
                Agents[i] = new RaftAgent(this, Zeze, i, raftconf);
            }

            foreach (var agent in Agents)
            {
                agent.RaftClient.Client.Start();
            }

            foreach (var agent in Agents)
            {
                // raft 登录需要选择leader，所以总是会起新的登录，第一次等待会失败，所以下面尝试两次。
                for (int i = 0; i < 2; ++i)
                {
                    try
                    {
                        await agent.WaitLoginSuccess();
                    }
                    catch (Exception)
                    {
                        // skip
                    }
                }
            }
        }

        public void Stop()
        {
            if (null == Agents)
                return;

            foreach (var agent in Agents)
            {
                agent.Close();
            }
            Agents = null;
        }

        public class ReduceBridge : GlobalCacheManager.Reduce
        {
            public Reduce Real { get; }

            public ReduceBridge(Reduce real)
            {
                Real = real;
                Argument.GlobalKey = real.Argument.GlobalKey;
                Argument.State = real.Argument.State;
                ResultCode = real.ResultCode;
            }

            public override void SendResult(Zeze.Net.Binary result = null)
            {
                Real.Result.GlobalKey = Real.Argument.GlobalKey; // no change
                Real.Result.State = Result.State;
                Real.ResultCode = ResultCode;
                Real.SendResult(result);
            }
        }

        protected override async Task<long> ProcessReduceRequest(Zeze.Net.Protocol _p)
        {
            var rpc = _p as Zeze.Builtin.GlobalCacheManagerWithRaft.Reduce;
            switch (rpc.Argument.State)
            {
                case GlobalCacheManagerServer.StateInvalid:
                    {
                        var bb = ByteBuffer.Wrap(rpc.Argument.GlobalKey);
                        var tableId = bb.ReadInt4();
                        var table = Zeze.GetTable(tableId);
                        if (table == null)
                        {
                            logger.Warn($"ReduceInvalid Table Not Found={tableId},ServerId={Zeze.Config.ServerId}");
                            // 本地没有找到表格看作成功。
                            rpc.Result.GlobalKey = rpc.Argument.GlobalKey;
                            rpc.Result.State = GlobalCacheManagerServer.StateInvalid;
                            rpc.SendResultCode(0);
                            return 0;
                        }
                        return await table.ReduceInvalid(new ReduceBridge(rpc), bb);
                    }

                case GlobalCacheManagerServer.StateShare:
                    {
                        var bb = ByteBuffer.Wrap(rpc.Argument.GlobalKey);
                        var tableId = bb.ReadInt4();
                        var table = Zeze.GetTable(tableId);
                        if (table == null)
                        {
                            logger.Warn($"ReduceShare Table Not Found={tableId},ServerId={Zeze.Config.ServerId}");
                            // 本地没有找到表格看作成功。
                            rpc.Result.GlobalKey = rpc.Argument.GlobalKey;
                            rpc.Result.State = GlobalCacheManagerServer.StateInvalid;
                            rpc.SendResultCode(0);
                            return 0;
                        }
                        return await table.ReduceShare(new ReduceBridge(rpc), bb);
                    }

                default:
                    rpc.Result = rpc.Argument;
                    rpc.SendResultCode(GlobalCacheManagerServer.ReduceErrorState);
                    return 0;
            }
        }

        internal RaftAgent[] Agents;

        public int GetGlobalCacheManagerHashIndex(Binary gkey)
        {
            return gkey.GetHashCode() % Agents.Length;
        }

        public async Task<(long, int)> Acquire(Binary gkey, int state, bool fresh)
        {
            if (null != Agents)
            {
                var agent = Agents[GetGlobalCacheManagerHashIndex(gkey)]; // hash
                if (agent.IsReleasing())
                {
                    agent.SetFastFail(); // 一般是超时失败，此时必须进入快速失败模式。
                    if (null == Transaction.Transaction.Current)
                        throw new Exception("GlobalAgent.Acquire Exception");
                    Transaction.Transaction.Current.ThrowAbort("GlobalAgent.Acquire Exception");
                }
                agent.VerifyFastFail();
                try
                {
                    await agent.WaitLoginSuccess();
                }
                catch (Exception )
                {
                    agent.SetFastFail();
                    throw;
                }

                var rpc = new Acquire();
                if (fresh)
                    rpc.ResultCode = GlobalCacheManagerServer.AcquireFreshSource;
                rpc.Argument.GlobalKey = gkey;
                rpc.Argument.State = state;
                rpc.Timeout = agent.Config.AcquireTimeout;
                // 让协议包更小，这里仅仅把ServerId当作ClientId。
                // Global是专用服务，用这个够区分了。
                rpc.Unique.ClientId = Zeze.Config.ServerId.ToString();
                try
                {
                    await agent.RaftClient.SendAsync(rpc);
                }
                catch (Exception e)
                {
                    agent.SetFastFail();
                    if (null == Transaction.Transaction.Current)
                        throw new Exception("GlobalAgent.Acquire Exception", e);
                    Transaction.Transaction.Current.ThrowAbort("GlobalAgent.Acquire Exception", e);
                }

                if (false == rpc.IsTimeout)
                    agent.SetActiveTime(Util.Time.NowUnixMillis);
                if (rpc.ResultCode < 0)
                {
                    Transaction.Transaction.Current.ThrowAbort("GlobalAgent.Acquire Failed");
                    // never got here
                }
                switch (rpc.ResultCode)
                {
                    case GlobalCacheManagerServer.AcquireModifyFailed:
                    case GlobalCacheManagerServer.AcquireShareFailed:
                        Transaction.Transaction.Current.ThrowAbort("GlobalAgent.Acquire Failed");
                        // never got here
                        break;
                }
                return (rpc.ResultCode, rpc.Result.State);
            }
            logger.Debug("Acquire local ++++++");
            return (0, state);
        }

        // 1. 【Login|ReLogin|NormalClose】会被Raft.Agent重发处理，这要求GlobalRaft能处理重复请求。
        // 2. 【Login|NormalClose】有多个事务处理，这跟rpc.UniqueRequestId唯一性有矛盾。【可行方法：去掉唯一判断，让流程正确处理重复请求。】
        // 3. 【ReLogin】没有数据修改，完全允许重复，并且不判断唯一性。
        // 4. Raft 高可用性，所以认为服务器永远不会关闭，就不需要处理服务器关闭时清理本地状态。
        public class RaftAgent : GlobalAgentBase
        {
            public GlobalCacheManagerWithRaftAgent GlobalCacheManagerWithRaftAgent { get; }
            public Zeze.Raft.Agent RaftClient { get; }
            public Util.AtomicLong LoginTimes { get; } = new Util.AtomicLong();
            private long LastErrorTime;

            public void SetFastFail()
            {
                // 并发的等待，简单用个规则：在间隔期内不再设置。
                long now = global::Zeze.Util.Time.NowUnixMillis;
                lock (this)
                {
                    if (now - LastErrorTime > Config.ServerFastErrorPeriod)
                        LastErrorTime = now;
                }
            }

            public void VerifyFastFail()
            {
                long now = global::Zeze.Util.Time.NowUnixMillis;
                lock (this)
                {
                    if (now - LastErrorTime < Config.ServerFastErrorPeriod)
                        ThrowException("GlobalAgent.FastFail");
                }
            }

            private static void ThrowException(string msg, Exception cause = null)
            {
                var txn = Transaction.Transaction.Current;
                if (txn != null)
                    txn.ThrowAbort(msg, cause);
                throw new Exception(msg, cause);
            }

            public RaftAgent(GlobalCacheManagerWithRaftAgent global,
                Application zeze, int _GlobalCacheManagerHashIndex,
                Zeze.Raft.RaftConfig raftconf = null)
                : base(zeze)
            {
                GlobalCacheManagerWithRaftAgent = global;
                base.GlobalCacheManagerHashIndex = _GlobalCacheManagerHashIndex;
                RaftClient = new Raft.Agent("global.raft", zeze, raftconf) { OnSetLeader = RaftOnSetLeader };
                GlobalCacheManagerWithRaftAgent.RegisterProtocols(RaftClient.Client);
            }

            protected override void CancelPending()
            {
                LoginFuture?.TrySetCanceled();
                RaftClient.CancelPending();
            }

            public override void KeepAlive()
            {
                var rpc = new KeepAlive();
                RaftClient.Send(rpc, p =>
                {
                    if (false == rpc.IsTimeout && (rpc.ResultCode == 0 || rpc.ResultCode == Procedure.RaftApplied))
                        SetActiveTime(Util.Time.NowUnixMillis);
                    return Task.FromResult(0L);
                });
            }

            public void Close()
            {
                if (LoginTimes.Get() > 0)
                {
                    if (false == RaftClient.SendAsync(new NormalClose()).Wait(10 * 1000))
                    {
                        // 10s
                        logger.Warn($"{RaftClient.Name} Leader={RaftClient.Leader} NormalClose Timeout");
                    }
                }

                RaftClient.Stop();
            }

            private volatile TaskCompletionSource<bool> LoginFuture = new (TaskCreationOptions.RunContinuationsAsynchronously);

            public async Task WaitLoginSuccess()
            {
                var volatiletmp = LoginFuture;
                if (volatiletmp.Task.IsCompleted)
                {
                    if (volatiletmp.Task.Result)
                        return;
                    throw new Exception("login fail");
                }
                await Task.WhenAny(volatiletmp.Task, Task.Delay(Config.LoginTimeout));
                if (volatiletmp.Task.IsCompletedSuccessfully && volatiletmp.Task.Result)
                    return;
                throw new Exception("login timeout");
            }

            private TaskCompletionSource<bool> StartNewLogin()
            {
                lock (this)
                {
                    LoginFuture.TrySetCanceled(); // 如果旧的Future上面有人在等，让他们失败。
                    LoginFuture = new TaskCompletionSource<bool>(TaskCreationOptions.RunContinuationsAsynchronously);
                    return LoginFuture;
                }
            }

            private void RaftOnSetLeader(Zeze.Raft.Agent agent)
            {
                var future = StartNewLogin();

                if (LoginTimes.Get() == 0)
                {
                    var login = new Login();
                    login.Argument.ServerId = agent.Client.Zeze.Config.ServerId;
                    login.Argument.GlobalCacheManagerHashIndex = GlobalCacheManagerHashIndex;

                    agent.Send(login,
                        (p) =>
                        {
                            var rpc = p as Login;
                            if (rpc.IsTimeout || rpc.ResultCode != 0)
                            {
                                logger.Error($"Login Timeout Or ResultCode != 0. Code={rpc.ResultCode}");
                                // 这里不记录future失败，等待raft通知新的Leader启动新的Login。让外面等待的线程一直等待。
                            }
                            else
                            {
                                SetActiveTime(Util.Time.NowUnixMillis);
                                LoginTimes.IncrementAndGet();
                                this.Initialize(login.Result.MaxNetPing, login.Result.ServerProcessTime, login.Result.ServerReleaseTimeout);
                                future.TrySetResult(true);
                            }
                            return Task.FromResult(0L);
                        }, true);
                }
                else
                {
                    var relogin = new ReLogin();
                    relogin.Argument.ServerId = agent.Client.Zeze.Config.ServerId;
                    relogin.Argument.GlobalCacheManagerHashIndex = GlobalCacheManagerHashIndex;
                    agent.Send(relogin,
                        (p) =>
                        {
                            var rpc = p as ReLogin;
                            if (rpc.IsTimeout || rpc.ResultCode != 0)
                            {
                                logger.Error($"Login Timeout Or ResultCode != 0. Code={rpc.ResultCode}");
                                // 这里不记录future失败，等待raft通知新的Leader启动新的Login。让外面等待的线程一直等待。
                            }
                            else
                            {
                                SetActiveTime(Util.Time.NowUnixMillis);
                                LoginTimes.IncrementAndGet();
                                future.TrySetResult(true);
                            }
                            return Task.FromResult(0L);
                        }, true);
                }
            }
        }
    }
}
