
using Zeze.Beans.GlobalCacheManagerWithRaft;

namespace Zeze.Services
{
    public class GlobalCacheManagerWithRaftAgent : AbstractGlobalCacheManagerWithRaftAgent, Zeze.Transaction.IGlobalAgent
    {
        static readonly NLog.Logger logger = NLog.LogManager.GetCurrentClassLogger();
        public Application Zeze { get; }

        public void Dispose()
        {
            Stop();
        }

        public GlobalCacheManagerWithRaftAgent(Application zeze)
        {
            Zeze = zeze;
        }

        public void Start(string[] hosts)
        {
            lock (this)
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
            }
        }

        public void Stop()
        {
            lock (this)
            {
                if (null == Agents)
                    return;

                foreach (var agent in Agents)
                {
                    agent.Close();
                }
                Agents = null;
            }
        }

        public class ReduceBridge : GlobalCacheManager.Reduce
        {
            public Reduce Real { get; }

            public ReduceBridge(Reduce real)
            {
                Real = real;
                Argument.GlobalTableKey = real.Argument.GlobalTableKey;
                Argument.State = real.Argument.State;
                Argument.GlobalSerialId = real.Argument.GlobalSerialId;
            }

            public override void SendResult(Zeze.Net.Binary result = null)
            {
                Real.Result.GlobalTableKey = Real.Argument.GlobalTableKey; // no change
                Real.Result.GlobalSerialId = Result.GlobalSerialId;
                Real.Result.State = Result.State;

                Real.SendResult(result);
            }

            public override void SendResultCode(long code, Zeze.Net.Binary result = null)
            {
                Real.Result.GlobalTableKey = Real.Argument.GlobalTableKey; // no change
                Real.Result.GlobalSerialId = Result.GlobalSerialId;
                Real.Result.State = Result.State;

                Real.SendResultCode(code, result);
            }
        }

        protected override long ProcessReduceRequest(Zeze.Net.Protocol _p)
        {
            var rpc = _p as Zeze.Beans.GlobalCacheManagerWithRaft.Reduce;
            switch (rpc.Argument.State)
            {
                case GlobalCacheManagerServer.StateInvalid:
                    {
                        var table = Zeze.GetTable(rpc.Argument.GlobalTableKey.TableName);
                        if (table == null)
                        {
                            logger.Warn($"ReduceInvalid Table Not Found={rpc.Argument.GlobalTableKey.TableName},ServerId={Zeze.Config.ServerId}");
                            // ����û���ҵ��������ɹ���
                            rpc.Result.GlobalTableKey = rpc.Argument.GlobalTableKey;
                            rpc.Result.State = GlobalCacheManagerServer.StateInvalid;
                            rpc.SendResultCode(0);
                            return 0;
                        }
                        return table.ReduceInvalid(new ReduceBridge(rpc));
                    }

                case GlobalCacheManagerServer.StateShare:
                    {
                        var table = Zeze.GetTable(rpc.Argument.GlobalTableKey.TableName);
                        if (table == null)
                        {
                            logger.Warn($"ReduceShare Table Not Found={rpc.Argument.GlobalTableKey.TableName},ServerId={Zeze.Config.ServerId}");
                            // ����û���ҵ��������ɹ���
                            rpc.Result.GlobalTableKey = rpc.Argument.GlobalTableKey;
                            rpc.Result.State = GlobalCacheManagerServer.StateInvalid;
                            rpc.SendResultCode(0);
                            return 0;
                        }
                        return table.ReduceShare(new ReduceBridge(rpc));
                    }

                default:
                    rpc.Result = rpc.Argument;
                    rpc.SendResultCode(GlobalCacheManagerServer.ReduceErrorState);
                    return 0;
            }
        }

        internal RaftAgent[] Agents;

        public int GetGlobalCacheManagerHashIndex(GlobalTableKey gkey)
        {
            return gkey.GetHashCode() % Agents.Length;
        }

        public (long, int, long) Acquire(GlobalTableKey gkey, int state)
        {
            if (null != Agents)
            {
                var agent = Agents[GetGlobalCacheManagerHashIndex(gkey)]; // hash
                var rpc = new Acquire();
                rpc.Argument.GlobalTableKey = gkey;
                rpc.Argument.State = state;
                agent.RaftClient.SendForWait(rpc).Task.Wait();

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
                return (rpc.ResultCode, rpc.Result.State, rpc.Result.GlobalSerialId);
            }
            logger.Debug("Acquire local ++++++");
            return (0, state, 0);
        }

        // 1. ��Login|ReLogin|NormalClose���ᱻRaft.Agent�ط���������Ҫ��GlobalRaft�ܴ����ظ�����
        // 2. ��Login|NormalClose���ж�������������rpc.UniqueRequestIdΨһ����ì�ܡ������з�����ȥ��Ψһ�жϣ���������ȷ�����ظ����󡣡�
        // 3. ��ReLogin��û�������޸ģ���ȫ�����ظ������Ҳ��ж�Ψһ�ԡ�
        // 4. Raft �߿����ԣ�������Ϊ��������Զ����رգ��Ͳ���Ҫ�����������ر�ʱ��������״̬��
        public class RaftAgent
        {
            public GlobalCacheManagerWithRaftAgent GlobalCacheManagerWithRaftAgent { get; }
            public Zeze.Raft.Agent RaftClient { get; }
            public bool ActiveClose { get; private set; } = false;
            public Util.AtomicLong LoginTimes { get; } = new Util.AtomicLong();
            public int GlobalCacheManagerHashIndex { get; }

            public RaftAgent(GlobalCacheManagerWithRaftAgent global,
                Application zeze, int _GlobalCacheManagerHashIndex,
                Zeze.Raft.RaftConfig raftconf = null)
            {
                GlobalCacheManagerWithRaftAgent = global;
                GlobalCacheManagerHashIndex = _GlobalCacheManagerHashIndex;
                RaftClient = new Raft.Agent("Zeze.GlobalRaft.Agent", zeze, raftconf) { OnSetLeader = RaftOnSetLeader };
                GlobalCacheManagerWithRaftAgent.RegisterProtocols(RaftClient.Client);
            }

            public void Close()
            {
                lock (this)
                {
                    // �򵥱���һ�£�Close ���������˳���ʱ��ŵ��������Ӧ�ò��ñ�����
                    if (ActiveClose)
                        return;
                    ActiveClose = true;
                }
                RaftClient.SendForWait(new NormalClose()).Task.Wait();
                RaftClient.Client.Stop();
            }

            private Zeze.Net.Protocol LoginPending = null;

            private void RaftOnSetLeader(Zeze.Raft.Agent agent)
            {
                if (LoginPending != null)
                    return;

                if (LoginTimes.Get() == 0)
                {
                    var login = new Login();
                    LoginPending = login;
                    login.Argument.ServerId = agent.Client.Zeze.Config.ServerId;
                    login.Argument.GlobalCacheManagerHashIndex = 0; // agent.GlobalCacheManagerHashIndex;

                    agent.Send(login,
                        (p) =>
                        {
                            var rpc = p as Login;
                            if (rpc.IsTimeout || rpc.ResultCode != 0)
                            {
                                logger.Error($"Login Timeout Or ResultCode != 0. Code={rpc.ResultCode}");
                            }
                            else
                            {
                                LoginTimes.IncrementAndGet();
                            }
                            LoginPending = null;
                            return 0;
                        }, true);
                }
                else
                {
                    var relogin = new ReLogin();
                    LoginPending = relogin;
                    relogin.Argument.ServerId = agent.Client.Zeze.Config.ServerId;
                    relogin.Argument.GlobalCacheManagerHashIndex = 0; // agent.GlobalCacheManagerHashIndex;
                    agent.Send(relogin,
                        (p) =>
                        {
                            var rpc = p as ReLogin;
                            if (rpc.IsTimeout || rpc.ResultCode != 0)
                            {
                                logger.Error($"Login Timeout Or ResultCode != 0. Code={rpc.ResultCode}");
                            }
                            else
                            {
                                LoginTimes.IncrementAndGet();
                            }
                            LoginPending = null;
                            return 0;
                        }, true);
                }
            }
        }
    }
}