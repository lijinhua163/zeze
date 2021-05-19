﻿using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Diagnostics.CodeAnalysis;
using System.Linq;
using System.Net;
using System.Text;
using System.Threading.Tasks;
using System.Xml;
using Zeze.Net;
using Zeze.Serialize;
using Zeze.Transaction;

namespace Zeze.Services
{
    public sealed class ServiceManager : IDisposable
    {
        //////////////////////////////////////////////////////////////////////////////
        /// 服务管理：注册和订阅
        /// 【名词】
        /// 动态服务(gs)
        ///     动态服务器一般指启用cache-sync的逻辑服务器。比如gs。
        /// 注册服务器（ServiceManager）
        ///     支持更新服务器，这个服务一开始是为了启用cache-sync的服务器的查找。
        /// 动态服务器列表使用者(linkd)
        ///     当前使用动态服务的客户端主要是Game2/linkd，linkd在hash分配请求的时候需要一致动态服务器列表。
        ///
        /// 【下面的流程都是用现有的服务名字（上面括号中的名字）】
        /// 
        /// 【本控制功能的目标】
        /// 所有的linkd的可用动态服务列表的更新并不是原子的。
        /// 1. 让所有的linkd的列表保持最新；
        /// 2. 尽可能减少linkd上的服务列表不一致的时间（通过ready-commit机制）；
        /// 3. 列表不一致时，分发请求可能引起cache不命中，但不影响正确性（cache-sync保证了正确性）；
        /// 
        /// 【主要事件和流程】
        /// 1. gs停止时调用 RegisterService,UnRegisterService 向ServiceManager声明自己服务状态。
        /// 2. linkd启动时调用 UseService, UnUseService 向ServiceManager申请使用gs-list。
        /// 3. ServiceManager在RegisterService,UnRegisterService处理时发送 NotifyServiceList 给所有的 linkd。
        /// 4. linkd收到NotifyServiceList先记录到本地，同时持续关注自己和gs之间的连接，
        ///    当列表中的所有serivce都准备完成时调用 ReadyServiceList。
        /// 5. ServiceManager收到所有的linkd的ReadyServiceList后，向所有的linkd广播 CommitServiceList。
        /// 6. linkd 收到 CommitServiceList 时，启用新的服务列表。
        /// 
        /// 【特别规则和错误处理】
        /// 1. linkd 异常停止，ServiceManager 按 UnUseService 处理，仅仅简单移除use-list。相当于减少了以后请求来源。
        /// 2. gs 异常停止，ServiceManager 按 UnRegisterService 处理，移除可用服务，并启动列表更新流程（NotifyServiceList）。
        /// 3. linkd 处理 gs 关闭（在NotifyServiceList之前），仅仅更新本地服务列表状态，让该服务暂时不可用，但不改变列表。
        ///    linkd总是使用ServiceManager提交给他的服务列表，自己不主动增删。
        ///    linkd在NotifyServiceList的列表减少的处理：一般总是立即进入ready（因为其他gs都是可用状态）。
        /// 4. ServiceManager 异常关闭：
        ///    a) 启用raft以后，新的master会有正确列表数据，但服务状态（连接）未知，此时等待gs的RegisterService一段时间,
        ///       然后开启新的一轮NotifyServiceList，等待时间内没有再次注册的gs以后当作新的处理。
        ///    b) 启用raft的好处是raft的非master服务器会识别这种状态，并重定向请求到master，使得系统内只有一个master启用服务。
        ///       实际上raft不需要维护相同数据状态（gs-list），从空的开始即可，启用raft的话仅使用他的选举功能。
        ///    #) 由于ServiceManager可以较快恢复，暂时不考虑使用Raft，实现无聊了再来加这个吧
        /// 5. ServiceManager开启一轮变更通告过程中，有新的gs启动停止，将开启新的通告(NotifyServiceList)。
        ///    ReadyServiceList时会检查ready中的列表是否和当前ServiceManagerlist一致，不一致直接忽略。
        ///    新的通告流程会促使linkd继续发送ready。
        ///    另外为了更健壮的处理通告，通告加一个超时机制。超时没有全部ready，就启动一次新的通告。
        ///    原则是：总按最新的gs-list通告。中间不一致的ready全部忽略。

        // ServiceInfo.Name -> ServiceState
        private ConcurrentDictionary<string, ServerState> ServerStates = new ConcurrentDictionary<string, ServerState>();
        public NetServer Server { get; private set; }
        private AsyncSocket ServerSocket;
        private volatile Util.SchedulerTask StartNotifyDelayTask;

        public sealed class Conf : Zeze.Config.ICustomize
        {
            public string Name => "Zeze.Services.ServiceManager";

            public int KeepAlivePeriod { get; set; } = 300 * 1000; // 5 mins

            /// <summary>
            /// 启动以后接收注册和订阅，一段时间内不进行通知。
            /// 用来处理ServiceManager异常重启导致服务列表重置的问题。
            /// 在Delay时间内，希望所有的服务都重新连接上来并注册和订阅。
            /// Delay到达时，全部通知一遍，以后正常工作。
            /// </summary>
            public int StartNotifyDelay { get; set; } = 12 * 1000; // 12s

            public int RetryNotifyDelayWhenNotAllReady { get; set; } = 30 * 1000; // 30s

            public void Parse(XmlElement self)
            {
                string attr = self.GetAttribute("KeepAlivePeriod");
                if (!string.IsNullOrEmpty(attr))
                    KeepAlivePeriod = int.Parse(attr);
                attr = self.GetAttribute("StartNotifyDelay");
                if (!string.IsNullOrEmpty(attr))
                    StartNotifyDelay = int.Parse(attr);
                attr = self.GetAttribute("RetryNotifyDelayWhenNotAllReady");
                if (!string.IsNullOrEmpty(attr))
                    RetryNotifyDelayWhenNotAllReady = int.Parse(attr);
            }
        }

        /// <summary>
        /// 需要从配置文件中读取，把这个引用加入： Zeze.Config.AddCustomize
        /// </summary>
        public Conf Config { get; } = new Conf();

        public sealed class ServerState
        {
            public ServiceManager ServiceManager { get; }
            public string ServiceName { get; }

            // identity ->
            // 记录一下SessionId，方便以后找到服务所在的连接。
            public ConcurrentDictionary<string, ServiceInfo> ServiceInfos { get; }
                = new ConcurrentDictionary<string, ServiceInfo>();
            public ConcurrentDictionary<long, SubscribeState> Simple { get; }
                = new ConcurrentDictionary<long, SubscribeState>();
            public ConcurrentDictionary<long, SubscribeState> ReadyCommit { get; }
                = new ConcurrentDictionary<long, SubscribeState>();

            private Zeze.Util.SchedulerTask NotifyTimeoutTask;
            private long SerialId;

            public ServerState(ServiceManager sm, string serviceName)
            {
                ServiceManager = sm;
                ServiceName = serviceName;
            }

            public void StartNotify()
            {
                lock (this)
                {
                    if (null != ServiceManager.StartNotifyDelayTask)
                        return;
                    var notify = new NotifyServiceList()
                    {
                        Argument = new ServiceManager.ServiceInfos(ServiceName, this, ++SerialId),
                    };
                    logger.Debug($"StartNotify {notify.Argument}");
                    var notifyBytes = notify.Encode();

                    foreach (var e in Simple)
                    {
                        ServiceManager.Server.GetSocket(e.Key)?.Send(notifyBytes);
                    }
                    foreach (var e in ReadyCommit)
                    {
                        e.Value.Ready = false;
                        ServiceManager.Server.GetSocket(e.Key)?.Send(notifyBytes);
                    }
                    if (ReadyCommit.Count > 0)
                    {
                        // 只有两段公告模式需要回应处理。
                        NotifyTimeoutTask = Zeze.Util.Scheduler.Instance.Schedule(
                            (ThisTask) =>
                            {
                                if (NotifyTimeoutTask == ThisTask)
                                {
                                    // NotifyTimeoutTask 会在下面两种情况下被修改：
                                    // 1. 在 Notify.ReadyCommit 完成以后会被清空。
                                    // 2. 启动了新的 Notify。
                                    StartNotify(); // restart
                                }
                            },
                            ServiceManager.Config.RetryNotifyDelayWhenNotAllReady);
                    }
                }
            }

            public void TryCommit()
            {
                lock (this)
                {
                    if (NotifyTimeoutTask == null)
                        return; // no pending notify

                    foreach (var e in ReadyCommit)
                    {
                        if (false == e.Value.Ready)
                        {
                            return;
                        }
                    }
                    var commit = new CommitServiceList()
                    {
                        Argument = new ServiceInfos(ServiceName, this, 0),
                    };
                    foreach (var e in ReadyCommit)
                    {
                        ServiceManager.Server.GetSocket(e.Key)?.Send(commit);
                    }
                    NotifyTimeoutTask?.Cancel();
                    NotifyTimeoutTask = null;
                }
            }

            /// <summary>
            /// 订阅时候返回的ServiceInfos，必须和Notify流程互斥。
            /// 原子的得到当前信息并发送，然后加入订阅(simple or readycommit)。
            /// </summary>
            public int SubscribeAndSend(Subscribe r, Session session)
            {
                lock (this)
                {
                    // 外面会话的 TryAdd 加入成功，下面TryAdd肯定也成功。
                    switch (r.Argument.SubscribeType)
                    {
                        case SubscribeInfo.SubscribeTypeSimple:
                            Simple.TryAdd(session.SessionId, new SubscribeState(session.SessionId));
                            break;
                        case SubscribeInfo.SubscribeTypeReadyCommit:
                            ReadyCommit.TryAdd(session.SessionId, new SubscribeState(session.SessionId));
                            break;
                        default:
                            r.ResultCode = Subscribe.UnknownSubscribeType;
                            r.SendResult();
                            return Procedure.LogicError;
                    }
                    r.SendResultCode(Subscribe.Success);
                    if (null == ServiceManager.StartNotifyDelayTask)
                    {
                        var arg = new ServiceInfos(ServiceName, this, ++SerialId);
                        new SubscribeFirstCommit() { Argument = arg }.Send(r.Sender);
                    }
                    return Procedure.Success;
                }
            }

            public void SetReady(ReadyServiceList p, Session session)
            {
                lock(this)
                {
                    if (p.Argument.SerialId != SerialId)
                    {
                        logger.Debug("Skip Ready: SerialId Not Equal.");
                        return;
                    }
                    var ordered = new ServiceInfos(ServiceName, this, 0);

                    // 忽略旧的Ready。
                    if (!Enumerable.SequenceEqual(ordered.ServiceInfoListSortedByIdentity, p.Argument.ServiceInfoListSortedByIdentity))
                    {
                        var sb = new StringBuilder();
                        sb.Append("SequenceNotEqual:");
                        sb.Append(" Current=").Append(ordered);
                        sb.Append(" Ready=").Append(p.Argument);
                        logger.Debug(sb.ToString());
                        return;
                    }

                    if (!ReadyCommit.TryGetValue(session.SessionId, out var subcribeState))
                        return;

                    subcribeState.Ready = true;
                    TryCommit();
                }
            }
        }

        public sealed class SubscribeState
        {
            public long SessionId { get; }
            public bool Ready { get; set; } // ReadyCommit时才被使用。
            public SubscribeState(long ssid)
            {
                SessionId = ssid;
            }
        }

        public sealed class Session
        {
            public ServiceManager ServiceManager { get; }
            public long SessionId { get; }
            public ConcurrentDictionary<ServiceInfo, ServiceInfo> Registers { get; }
                = new ConcurrentDictionary<ServiceInfo, ServiceInfo>(new ServiceInfoEqualityComparer());
            // key is ServiceName: 会话订阅
            public ConcurrentDictionary<string, SubscribeInfo> Subscribes { get; }
                = new ConcurrentDictionary<string, SubscribeInfo>();

            public Session(ServiceManager sm, long ssid)
            {
                ServiceManager = sm;
                SessionId = ssid;
                Util.Scheduler.Instance.Schedule(
                    (ThisTask) =>
                    {
                        try
                        {
                            var r = new Keepalive();
                            var s = ServiceManager.Server.GetSocket(SessionId);
                            r.SendAndWaitCheckResultCode(s);
                        }
                        catch (Exception ex)
                        {
                            ServiceManager.Server.GetSocket(SessionId)?.Dispose();
                            logger.Error(ex, "ServiceManager.KeepAlive");
                        }
                    },
                    Util.Random.Instance.Next(ServiceManager.Config.KeepAlivePeriod),
                ServiceManager.Config.KeepAlivePeriod);
            }

            public void OnClose()
            {
                foreach (var info in Subscribes.Values)
                {
                    ServiceManager.UnSubscribeNow(SessionId, info);
                }

                Dictionary<string, ServerState> changed
                    = new Dictionary<string, ServerState>(Registers.Count);

                foreach (var info in Registers.Values)
                {
                    var state = ServiceManager.UnRegisterNow(SessionId, info);
                    if (null != state)
                    {
                        changed.TryAdd(state.ServiceName, state);
                    }
                }

                foreach (var state in changed.Values)
                {
                    state.StartNotify();
                }
            }
        }

        private static readonly NLog.Logger logger = NLog.LogManager.GetCurrentClassLogger();

        private int ProcessRegister(Protocol p)
        {
            var r = p as Register;
            var session = r.Sender.UserState as Session;
            if (false == session.Registers.TryAdd(r.Argument, r.Argument))
            {
                r.SendResultCode(Register.DuplicateRegister);
                return Procedure.LogicError;
            }
            var state = ServerStates.GetOrAdd(r.Argument.ServiceName, (name) => new ServerState(this, name));

            // 【警告】
            // 为了简单，这里没有创建新的对象，直接修改并引用了r.Argument。
            // 这个破坏了r.Argument只读的属性。另外引用同一个对象，也有点风险。
            // 在目前没有问题，因为r.Argument主要记录在state.ServiceInfos中，
            // 另外它也被Session引用（用于连接关闭时，自动注销）。
            // 这是专用程序，不是一个库，以后有修改时，小心就是了。
            r.Argument.LocalState = r.Sender.SessionId;

            // AddOrUpdate，否则重连重新注册很难恢复到正确的状态。
            state.ServiceInfos.AddOrUpdate(r.Argument.ServiceIdentity, r.Argument, (key, value) => r.Argument);
            r.SendResultCode(Register.Success);
            state.StartNotify();
            return Procedure.Success;
        }

        internal ServerState UnRegisterNow(long sessionId, ServiceInfo info)
        {
            if (ServerStates.TryGetValue(info.ServiceName, out var state))
            {
                if (state.ServiceInfos.TryGetValue(info.ServiceIdentity, out var exist))
                {
                    // 这里存在一个时间窗口，可能使得重复的注销会成功。注销一般比较特殊，忽略这个问题。
                    long? existSessionId = exist.LocalState as long?;
                    if (existSessionId == null || sessionId == existSessionId.Value)
                    {
                        // 有可能当前连接没有注销，新的注册已经AddOrUpdate，此时忽略当前连接的注销。
                        state.ServiceInfos.TryRemove(info.ServiceIdentity, out var _);
                        return state;
                    }
                }
            }
            return null;
        }

        private int ProcessUnRegister(Protocol p)
        {
            var r = p as UnRegister;
            var session = r.Sender.UserState as Session;
            if (null != UnRegisterNow(r.Sender.SessionId, r.Argument))
            {
                // ignore TryRemove failed.
                session.Registers.TryRemove(r.Argument, out var _);
                //r.SendResultCode(UnRegister.Success);
                //return Procedure.Success;
            }
            // 注销不存在也返回成功，否则Agent处理比较麻烦。
            r.SendResultCode(UnRegister.Success);
            return Procedure.Success;
        }

        private int ProcessSubscribe(Protocol p)
        {
            var r = p as Subscribe;
            var session = r.Sender.UserState as Session;
            if (!session.Subscribes.TryAdd(r.Argument.ServiceName, r.Argument))
            {
                r.ResultCode = Subscribe.DuplicateSubscribe;
                r.SendResult();
                return Procedure.LogicError;
            }
            var state = ServerStates.GetOrAdd(r.Argument.ServiceName, (name) => new ServerState(this, name));
            return state.SubscribeAndSend(r, session);
        }

        internal ServerState UnSubscribeNow(long sessionId, SubscribeInfo info)
        {
            if (ServerStates.TryGetValue(info.ServiceName, out var state))
            {
                switch (info.SubscribeType)
                {
                    case SubscribeInfo.SubscribeTypeSimple:
                        if (state.Simple.TryRemove(sessionId, out var _))
                            return state;
                        break;
                    case SubscribeInfo.SubscribeTypeReadyCommit:
                        if (state.ReadyCommit.TryRemove(sessionId, out var _))
                            return state;
                        break;
                }
            }
            return null;
        }

        private int ProcessUnSubscribe(Protocol p)
        {
            var r = p as UnSubscribe;
            var session = r.Sender.UserState as Session;
            if (session.Subscribes.TryRemove(r.Argument.ServiceName, out var sub))
            {
                if (r.Argument.SubscribeType == sub.SubscribeType)
                {
                    var changed = UnSubscribeNow(r.Sender.SessionId, r.Argument);
                    if (null != changed)
                    {
                        r.ResultCode = UnSubscribe.Success;
                        r.SendResult();
                        changed.TryCommit();
                        return Procedure.Success;
                    }
                }
            }
            // 取消订阅不能存在返回成功。否则Agent比较麻烦。
            //r.ResultCode = UnSubscribe.NotExist;
            //r.SendResult();
            //return Procedure.LogicError;
            r.ResultCode = UnRegister.Success;
            r.SendResult();
            return Procedure.Success;
        }

        private int ProcessReadyServiceList(Protocol p)
        {
            var r = p as ReadyServiceList;
            var session = r.Sender.UserState as Session;
            var state = ServerStates.GetOrAdd(r.Argument.ServiceName, (name) => new ServerState(this, name));
            state?.SetReady(r, session);
            return Procedure.Success;
        }

        public void Dispose()
        {
            Stop();
        }

        public ServiceManager(IPAddress ipaddress, int port, Config config, int startNotifyDelay = -1)
        {
            if (config.GetCustomize<Conf>(out var tmpconf))
                Config = tmpconf;

            if (startNotifyDelay >= 0)
                Config.StartNotifyDelay = startNotifyDelay;

            Server = new NetServer(this, config);

            Server.AddFactoryHandle(new Register().TypeId, new Service.ProtocolFactoryHandle()
            {
                Factory = () => new Register(),
                Handle = ProcessRegister,
            });

            Server.AddFactoryHandle(new UnRegister().TypeId, new Service.ProtocolFactoryHandle()
            {
                Factory = () => new UnRegister(),
                Handle = ProcessUnRegister,
            });

            Server.AddFactoryHandle(new Subscribe().TypeId, new Service.ProtocolFactoryHandle()
            {
                Factory = () => new Subscribe(),
                Handle = ProcessSubscribe,
            });

            Server.AddFactoryHandle(new UnSubscribe().TypeId, new Service.ProtocolFactoryHandle()
            {
                Factory = () => new UnSubscribe(),
                Handle = ProcessUnSubscribe,
            });

            Server.AddFactoryHandle(new ReadyServiceList().TypeId, new Service.ProtocolFactoryHandle()
            {
                Factory = () => new ReadyServiceList(),
                Handle = ProcessReadyServiceList,
            });

            Server.AddFactoryHandle(new Keepalive().TypeId, new Service.ProtocolFactoryHandle()
            {
                Factory = () => new Keepalive(),
            });

            if (Config.StartNotifyDelay > 0)
            {
                StartNotifyDelayTask = Util.Scheduler.Instance.Schedule(
                    StartNotifyAll, Config.StartNotifyDelay);
            }

            // 允许配置多个acceptor，如果有冲突，通过日志查看。
            ServerSocket = Server.NewServerSocket(ipaddress, port);
            Server.Start();
        }

        private void StartNotifyAll(Util.SchedulerTask ThisTask)
        {
            StartNotifyDelayTask = null;
            foreach (var e in ServerStates)
            {
                e.Value.StartNotify();
            }
        }

        public void Stop()
        {
            lock (this)
            {
                if (null == Server)
                    return;
                StartNotifyDelayTask?.Cancel();
                ServerSocket.Dispose();
                ServerSocket = null;
                Server.Close();
                Server = null;
            }
        }

        public sealed class NetServer : HandshakeServer
        {
            public ServiceManager ServiceManager { get; }

            public NetServer(ServiceManager sm, Config config)
                : base("Zeze.Services.ServiceManager", config)
            {
                ServiceManager = sm;
            }

            public override void OnSocketAccept(AsyncSocket so)
            {
                so.UserState = new Session(ServiceManager, so.SessionId);
                base.OnSocketAccept(so);
            }

            public override void OnSocketClose(AsyncSocket so, Exception e)
            {
                var session = so.UserState as Session;
                session?.OnClose();
                base.OnSocketClose(so, e);
            }
        }

        public sealed class ServiceInfo : Zeze.Transaction.Bean
        {
            /// <summary>
            /// 服务名，比如"GameServer"
            /// </summary>
            public string ServiceName { get; private set; }

            /// <summary>
            /// 服务id，对于 Zeze.Application，一般就是 Config.AutoKeyLocalId.
            /// 这里使用类型 string 是为了更好的支持扩展。
            /// </summary>
            public string ServiceIdentity { get; private set; }

            /// <summary>
            /// 服务ip-port，如果没有，保持空和0.
            /// </summary>
            public string PassiveIp { get; private set; } = "";
            public int PassivePort { get; private set; } = 0;

            // 服务扩展信息，可选。
            public Binary ExtraInfo { get; private set; } = Binary.Empty;

            // ServiceManager或者ServiceManager.Agent用来保存本地状态，不是协议一部分，不会被系列化。
            // 算是一个简单的策略，不怎么优美。一般仅设置一次，线程保护由使用者自己管理。
            public object LocalState { get; internal set; }

            public ServiceInfo()
            { 
            }

            public ServiceInfo(
                string name, string identity,
                string ip = null, int port = 0,
                Binary extrainfo = null)
            {
                ServiceName = name;
                ServiceIdentity = identity;
                if (null != ip)
                    PassiveIp = ip;
                PassivePort = port;
                if (extrainfo != null)
                    ExtraInfo = extrainfo;
            }

            public override void Decode(ByteBuffer bb)
            {
                ServiceName = bb.ReadString();
                ServiceIdentity = bb.ReadString();
                PassiveIp = bb.ReadString();
                PassivePort = bb.ReadInt();
                ExtraInfo = bb.ReadBinary();
            }

            public override void Encode(ByteBuffer bb)
            {
                bb.WriteString(ServiceName);
                bb.WriteString(ServiceIdentity);
                bb.WriteString(PassiveIp);
                bb.WriteInt(PassivePort);
                bb.WriteBinary(ExtraInfo);
            }

            protected override void InitChildrenRootInfo(Record.RootInfo root)
            {
                throw new NotImplementedException();
            }

            public override int GetHashCode()
            {
                const int prime = 31;
                int result = 17;
                result = prime * result + ServiceName.GetHashCode();
                result = prime * result + ServiceIdentity.GetHashCode();
                return result;
            }

            public override bool Equals(object obj)
            {
                if (obj == this)
                    return true;

                if (obj is ServiceInfo other)
                {
                    return ServiceName.Equals(other.ServiceName)
                        && ServiceIdentity.Equals(other.ServiceIdentity);
                }
                return false;
            }
        }

        /// <summary>
        /// 动态服务启动时通过这个rpc注册自己。
        /// </summary>
        public sealed class Register : Rpc<ServiceInfo, EmptyBean>
        {
            public readonly static int ProtocolId_ = Bean.Hash16(typeof(Register).FullName);

            public const int Success = 0;
            public const int DuplicateRegister = 1;

            public override int ModuleId => 0;
            public override int ProtocolId => ProtocolId_;

        }

        /// <summary>
        /// 动态服务关闭时，注销自己，当与本服务器的连接关闭时，默认也会注销。
        /// 最好主动注销，方便以后错误处理。
        /// </summary>
        public sealed class UnRegister : Rpc<ServiceInfo, EmptyBean>
        {
            public readonly static int ProtocolId_ = Bean.Hash16(typeof(UnRegister).FullName);

            public const int Success = 0;
            public const int NotExist = 1;

            public override int ModuleId => 0;
            public override int ProtocolId => ProtocolId_;
        }

        public sealed class SubscribeInfo : Bean
        {
            public const int SubscribeTypeSimple = 0;
            public const int SubscribeTypeReadyCommit = 1;

            public string ServiceName { get; set; }
            public int SubscribeType { get; set; }
            public object LocalState { get; set; }

            public override void Decode(ByteBuffer bb)
            {
                ServiceName = bb.ReadString();
                SubscribeType = bb.ReadInt();
            }

            public override void Encode(ByteBuffer bb)
            {
                bb.WriteString(ServiceName);
                bb.WriteInt(SubscribeType);
            }

            protected override void InitChildrenRootInfo(Record.RootInfo root)
            {
                throw new NotImplementedException();
            }

            public override string ToString()
            {
                return $"{ServiceName}:{SubscribeType}";
            }
        }

        public sealed class Subscribe : Rpc<SubscribeInfo, EmptyBean>
        {
            public readonly static int ProtocolId_ = Bean.Hash16(typeof(Subscribe).FullName);

            public const int Success = 0;
            public const int DuplicateSubscribe = 1;
            public const int UnknownSubscribeType = 2;

            public override int ModuleId => 0;
            public override int ProtocolId => ProtocolId_;
        }

        public sealed class UnSubscribe : Rpc<SubscribeInfo, EmptyBean>
        {
            public readonly static int ProtocolId_ = Bean.Hash16(typeof(UnSubscribe).FullName);

            public const int Success = 0;
            public const int NotExist = 1;

            public override int ModuleId => 0;
            public override int ProtocolId => ProtocolId_;
        }

        public sealed class ServiceInfos : Bean
        {
            // ServiceList maybe empty. need a ServiceName
            public string ServiceName { get; private set; }
            // sorted by ServiceIdentity
            private List<ServiceInfo> _ServiceInfoListSortedByIdentity { get; } = new List<ServiceInfo>();
            public IReadOnlyList<ServiceInfo> ServiceInfoListSortedByIdentity => _ServiceInfoListSortedByIdentity;
            public long SerialId { get; set; }

            public ServiceInfos()
            { 
            }

            public ServiceInfos(string serviceName)
            {
                ServiceName = serviceName;
            }

            public ServiceInfos(string serviceName, ServerState state, long serialId)
            {
                ServiceName = serviceName;
                foreach (var e in state.ServiceInfos)
                {
                    _ServiceInfoListSortedByIdentity.Add(e.Value);
                }
                SerialId = serialId;
            }

            public bool TryGetServiceInfo(string identity, out ServiceInfo info)
            {
                var cur = new ServiceInfo(ServiceName, identity);
                int index = _ServiceInfoListSortedByIdentity.BinarySearch(cur, new ServiceInfoIdentityComparer());
                if (index >= 0)
                {
                    info = ServiceInfoListSortedByIdentity[index];
                    return true;
                }
                info = null;
                return false;
            }
            public override void Decode(ByteBuffer bb)
            {
                ServiceName = bb.ReadString();
                _ServiceInfoListSortedByIdentity.Clear();
                for (int c = bb.ReadInt(); c > 0; --c)
                {
                    var service = new ServiceInfo();
                    service.Decode(bb);
                    _ServiceInfoListSortedByIdentity.Add(service);
                }
                SerialId = bb.ReadLong();
            }

            public override void Encode(ByteBuffer bb)
            {
                bb.WriteString(ServiceName);
                bb.WriteInt(_ServiceInfoListSortedByIdentity.Count);
                foreach (var service in _ServiceInfoListSortedByIdentity)
                {
                    service.Encode(bb);
                }
                bb.WriteLong(SerialId);
            }

            protected override void InitChildrenRootInfo(Record.RootInfo root)
            {
                throw new NotImplementedException();
            }

            public override string ToString()
            {
                var sb = new StringBuilder();
                sb.Append(ServiceName).Append("=");
                sb.Append("[");
                foreach (var e in ServiceInfoListSortedByIdentity)
                {
                    sb.Append(e.ServiceIdentity);
                    sb.Append(",");
                }
                sb.Append("]");
                return sb.ToString();
            }
        }

        public sealed class NotifyServiceList : Protocol<ServiceInfos>
        {
            public readonly static int ProtocolId_ = Bean.Hash16(typeof(NotifyServiceList).FullName);

            public override int ModuleId => 0;
            public override int ProtocolId => ProtocolId_;
        }

        public sealed class ReadyServiceList : Protocol<ServiceInfos>
        {
            public readonly static int ProtocolId_ = Bean.Hash16(typeof(ReadyServiceList).FullName);

            public override int ModuleId => 0;
            public override int ProtocolId => ProtocolId_;
        }

        public sealed class CommitServiceList : Protocol<ServiceInfos>
        {
            public readonly static int ProtocolId_ = Bean.Hash16(typeof(CommitServiceList).FullName);

            public override int ModuleId => 0;
            public override int ProtocolId => ProtocolId_;
        }

        // 实际上可以不用这个类，为了保持以后ServiceInfo的比较可能改变，写一个这个类。
        public sealed class ServiceInfoEqualityComparer : IEqualityComparer<ServiceInfo>
        {
            public bool Equals(ServiceInfo x, ServiceInfo y)
            {
                return x.Equals(y);
            }

            public int GetHashCode([DisallowNull] ServiceInfo obj)
            {
                return obj.GetHashCode();
            }
        }

        public sealed class ServiceInfoIdentityComparer : IComparer<ServiceInfo>
        {
            public int Compare(ServiceInfo x, ServiceInfo y)
            {
                return x.ServiceIdentity.CompareTo(y.ServiceIdentity);
            }
        }

        public sealed class Keepalive : Rpc<EmptyBean, EmptyBean>
        {
            public readonly static int ProtocolId_ = Bean.Hash16(typeof(Keepalive).FullName);

            public const int Success = 0;

            public override int ModuleId => 0;
            public override int ProtocolId => ProtocolId_;
        }

        public sealed class SubscribeFirstCommit : Protocol<ServiceInfos>
        {
            public readonly static int ProtocolId_ = Bean.Hash16(typeof(SubscribeFirstCommit).FullName);

            public override int ModuleId => 0;
            public override int ProtocolId => ProtocolId_;
        }

        public sealed class Agent : IDisposable
        {
            // key is ServiceName。对于一个Agent，一个服务只能有一个订阅。
            // ServiceName ->
            public ConcurrentDictionary<string, SubscribeState> SubscribeStates { get; }
                = new ConcurrentDictionary<string, SubscribeState>();
            public NetClient Client { get; private set; }
            /// <summary>
            /// 如果应用一开始就知道自己需要注册和订阅的服务，可以在这个回调里面集中处理。
            /// 如果应用执行过程中动态注册和订阅，则可不处理，但是调用注册订阅的函数时要检查连接好了没。
            /// </summary>
            public Action<Agent> OnConnected { get; private set; }
            public Action<SubscribeState> OnChanged { get; private set; }

            // 应用可以在这个Action内起一个测试事务并执行一次。也可以实现其他检测。
            // ServiceManager 定时发送KeepAlive给Agent，并等待结果。超时则认为服务失效。
            public Action OnKeepAlive { get; set; }

            // key is (ServiceName, ServideIdentity)
            private ConcurrentDictionary<ServiceInfo, ServiceInfo> Registers { get; }
                = new ConcurrentDictionary<ServiceInfo, ServiceInfo>(new ServiceInfoEqualityComparer());

            // 【警告】
            // 记住当前已经注册和订阅信息，当ServiceManager连接发生重连时，重新发送请求。
            // 维护这些状态数据都是先更新本地再发送远程请求，在失败的时候rollback。
            // 当同一个Key(比如ServiceName)存在并发时，现在处理所有情况，但不保证都是合理的。
            public sealed class SubscribeState
            {
                public Agent Agent { get; }
                public SubscribeInfo SubscribeInfo { get; }
                public int SubscribeType => SubscribeInfo.SubscribeType;
                public string ServiceName => SubscribeInfo.ServiceName;

                public ServiceInfos ServiceInfos { get; private set; }
                public ServiceInfos ServiceInfosPending { get; private set; }

                /// <summary>
                /// 刚初始化时为false，任何修改ServiceInfos都会设置成true。
                /// 用来处理Subscribe返回的第一份数据和Commit可能乱序的问题。
                /// 目前的实现不会发生乱序。
                /// </summary>
                public bool Committed { get; internal set; } = false;

                // 服务准备好。
                public ConcurrentDictionary<string, object> ServiceIdentityReadyStates { get; }
                    = new ConcurrentDictionary<string, object>();

                public SubscribeState(Agent ag, SubscribeInfo info)
                {
                    Agent = ag;
                    SubscribeInfo = info;
                    ServiceInfos = new ServiceInfos(info.ServiceName);
                }

                // NOT UNDER LOCK
                private bool TrySendReadyServiceList()
                {
                    if (null == ServiceInfosPending)
                        return false;

                    foreach (var pending in ServiceInfosPending.ServiceInfoListSortedByIdentity)
                    {
                        if (!ServiceIdentityReadyStates.ContainsKey(pending.ServiceIdentity))
                            return false;
                    }
                    var r = new ReadyServiceList() { Argument = ServiceInfosPending };
                    Agent.Client.Socket?.Send(r);
                    return true;
                }

                public void SetServiceIdentityReadyState(string identity, object state)
                {
                    if (null == state)
                    {
                        ServiceIdentityReadyStates.TryRemove(identity, out var _);
                    }
                    else
                    {
                        ServiceIdentityReadyStates[identity] = state;
                    }

                    lock (this)
                    {
                        // 把 state 复制到当前版本的服务列表中。允许列表不变，服务状态改变。
                        if (ServiceInfos != null && ServiceInfos.TryGetServiceInfo(identity, out var info))
                        {
                            info.LocalState = state;
                        }
                        // 尝试发送Ready，如果有pending.
                        TrySendReadyServiceList();
                    }
                }

                private void PrepareAndTriggerOnchanged()
                {
                    foreach (var info in ServiceInfos.ServiceInfoListSortedByIdentity)
                    {
                        if (ServiceIdentityReadyStates.TryGetValue(info.ServiceIdentity, out var state))
                        {
                            info.LocalState = state;
                        }
                    }
                    Agent.OnChanged(this);
                }

                internal void OnNotify(ServiceInfos infos)
                {
                    lock (this)
                    {
                        switch (SubscribeType)
                        {
                            case SubscribeInfo.SubscribeTypeSimple:
                                ServiceInfos = infos;
                                Committed = true;
                                PrepareAndTriggerOnchanged();
                                break;

                            case SubscribeInfo.SubscribeTypeReadyCommit:
                                if (null == ServiceInfosPending
                                    // 忽略过期的Notify，防止乱序。long不可能溢出，不做回绕处理了。
                                    || infos.SerialId > ServiceInfosPending.SerialId
                                    )
                                {
                                    ServiceInfosPending = infos;
                                    TrySendReadyServiceList();
                                }
                                break;
                        }
                    }
                }

                internal void OnCommit(ServiceInfos infos)
                {
                    lock (this)
                    {
                        // ServiceInfosPending 和 Commit.infos 应该一样，否则肯定哪里出错了。
                        // 这里总是使用最新的 Commit.infos，检查记录日志。
                        if (!Enumerable.SequenceEqual(infos.ServiceInfoListSortedByIdentity, ServiceInfosPending.ServiceInfoListSortedByIdentity))
                        {
                            Agent.logger.Warn("OnCommit: ServiceInfosPending Miss Match.");
                        }
                        ServiceInfos = infos;
                        ServiceInfosPending = null;
                        Committed = true;
                        PrepareAndTriggerOnchanged();
                    }
                }

                internal void OnFirstCommit(ServiceInfos infos)
                {
                    lock (this)
                    {
                        if (Committed)
                            return;
                        Committed = true;
                        ServiceInfos = infos;
                        ServiceInfosPending = null;
                        PrepareAndTriggerOnchanged();
                    }
                }
            }

            private static readonly NLog.Logger logger = NLog.LogManager.GetCurrentClassLogger();

            public ServiceInfo RegisterService(
                string name, string identity,
                string ip = null, int port = 0,
                Binary extrainfo = null)
            {
                return RegisterService(new ServiceInfo(name, identity, ip, port, extrainfo));
            }

            private ServiceInfo RegisterService(ServiceInfo info)
            {
                bool regNew = false;
                var regServInfo = Registers.GetOrAdd(info,
                    (key) =>
                    {
                        regNew = true;
                        return info;
                    });

                if (regNew)
                {
                    try
                    {
                        var r = new Register() { Argument = info };
                        r.SendAndWaitCheckResultCode(Client.Socket);
                    }
                    catch (Exception)
                    {
                        Registers.TryRemove(KeyValuePair.Create(info, info)); // rollback
                        throw;
                    }
                }
                return regServInfo;
            }

            public void UnRegisterService(string name, string identity)
            {
                UnRegisterService(new ServiceInfo(name, identity));
            }

            private void UnRegisterService(ServiceInfo info)
            {
                if (Registers.TryRemove(info, out var exist))
                {
                    try
                    {
                        var r = new UnRegister() { Argument = info };
                        r.SendAndWaitCheckResultCode(Client.Socket);
                    }
                    catch (Exception)
                    {
                        Registers.TryAdd(exist, exist); // rollback
                        throw;
                    }
                }
            }

            public SubscribeState SubscribeService(string serviceName, int type, object state = null)
            {
                if (type != SubscribeInfo.SubscribeTypeSimple
                    && type != SubscribeInfo.SubscribeTypeReadyCommit)
                    throw new Exception("Unkown SubscribeType");

                return SubscribeService(new SubscribeInfo()
                {
                    ServiceName = serviceName,
                    SubscribeType = type,
                    LocalState = state,
                });
            }

            private SubscribeState SubscribeService(SubscribeInfo info)
            {
                bool newAdd = false;
                var subState = SubscribeStates.GetOrAdd(info.ServiceName,
                    (_) =>
                    {
                        newAdd = true;
                        return new SubscribeState(this, info);
                    });

                if (newAdd)
                {
                    var r = new Subscribe() { Argument = info };
                    r.SendAndWaitCheckResultCode(Client.Socket);
                }
                return subState;
            }

            private int ProcessSubscribeFirstCommit(Protocol p)
            {
                var r = p as SubscribeFirstCommit;
                if (SubscribeStates.TryGetValue(r.Argument.ServiceName, out var state))
                {
                    state.OnFirstCommit(r.Argument);
                }
                return Procedure.Success;
            }

            public void UnSubscribeService(string serviceName)
            {
                if (SubscribeStates.TryRemove(serviceName, out var state))
                {
                    try
                    {
                        var r = new UnSubscribe() { Argument = state.SubscribeInfo };
                        r.SendAndWaitCheckResultCode(Client.Socket);
                    }
                    catch (Exception)
                    {
                        SubscribeStates.TryAdd(serviceName, state); // rollback
                        throw;
                    }
                }
            }

            private int ProcessNotifyServiceList(Protocol p)
            {
                var r = p as NotifyServiceList;
                if (SubscribeStates.TryGetValue(r.Argument.ServiceName, out var state))
                {
                    state.OnNotify(r.Argument);
                }
                else
                {
                    Agent.logger.Warn("NotifyServiceList But SubscribeState Not Found.");
                }
                return Procedure.Success;
            }

            private int ProcessCommitServiceList(Protocol p)
            {
                var r = p as CommitServiceList;
                if (SubscribeStates.TryGetValue(r.Argument.ServiceName, out var state))
                {
                    state.OnCommit(r.Argument);
                }
                else
                {
                    Agent.logger.Warn("CommitServiceList But SubscribeState Not Found.");
                }
                return Procedure.Success;
            }

            private int ProcessKeepalive(Protocol p)
            {
                var r = p as Keepalive;
                OnKeepAlive?.Invoke();
                r.SendResultCode(Keepalive.Success);
                return Procedure.Success;
            }

            public void Stop()
            {
                lock(this)
                {
                    if (null == Client)
                        return;
                    Client.Close();
                    Client = null;
                }
            }

            internal void _OnConnected()
            {
                foreach (var e in Registers)
                {
                    try
                    {
                        var r = new Register() { Argument = e.Value };
                        r.SendAndWaitCheckResultCode(Client.Socket);
                    }
                    catch (Exception ex)
                    {
                        logger.Debug(ex, $"_OnConnected.Register={e.Value}");
                    }
                }
                foreach (var e in SubscribeStates)
                {
                    try
                    {
                        e.Value.Committed = false;
                        var r = new Subscribe() { Argument = e.Value.SubscribeInfo };
                        r.SendAndWaitCheckResultCode(Client.Socket);
                    }
                    catch (Exception ex)
                    {
                        logger.Debug(ex, $"_OnConnected.Subscribe={e.Value.SubscribeInfo}");
                    }
                }
                OnConnected(this);
            }

            /// <summary>
            /// 使用Config配置连接信息，可以配置是否支持重连。
            /// 用于测试：Agent.Client.NewClientSocket(...)，不会自动重连，不要和Config混用。
            /// </summary>
            public Agent(Config config, Action<Agent> onConnected, Action<SubscribeState> onChanged, string netServiceName = null)
            {
                if (null == config)
                    throw new Exception("Config is null");

                Client = string.IsNullOrEmpty(netServiceName)
                    ? new NetClient(this, config) : new NetClient(this, config, netServiceName);

                OnConnected = onConnected;
                OnChanged = onChanged;

                Client.AddFactoryHandle(new Register().TypeId, new Service.ProtocolFactoryHandle()
                {
                    Factory = () => new Register(),
                });

                Client.AddFactoryHandle(new UnRegister().TypeId, new Service.ProtocolFactoryHandle()
                {
                    Factory = () => new UnRegister(),
                });

                Client.AddFactoryHandle(new Subscribe().TypeId, new Service.ProtocolFactoryHandle()
                {
                    Factory = () => new Subscribe(),
                });

                Client.AddFactoryHandle(new UnSubscribe().TypeId, new Service.ProtocolFactoryHandle()
                {
                    Factory = () => new UnSubscribe(),
                });

                Client.AddFactoryHandle(new NotifyServiceList().TypeId, new Service.ProtocolFactoryHandle()
                {
                    Factory = () => new NotifyServiceList(),
                    Handle = ProcessNotifyServiceList,
                });

                Client.AddFactoryHandle(new CommitServiceList().TypeId, new Service.ProtocolFactoryHandle()
                {
                    Factory = () => new CommitServiceList(),
                    Handle = ProcessCommitServiceList,
                });

                Client.AddFactoryHandle(new Keepalive().TypeId, new Service.ProtocolFactoryHandle()
                {
                    Factory = () => new Keepalive(),
                    Handle = ProcessKeepalive,
                });

                Client.AddFactoryHandle(new SubscribeFirstCommit().TypeId, new Service.ProtocolFactoryHandle()
                {
                    Factory = () => new SubscribeFirstCommit(),
                    Handle = ProcessSubscribeFirstCommit,
                });

                Client.Start();
            }

            public void Dispose()
            {
                Stop();
            }

            public sealed class NetClient : HandshakeClient
            {
                public Agent Agent { get; }
                /// <summary>
                /// 和注册服务器之间只保持一个连接。并且不处理任何协议状态。
                /// </summary>
                public AsyncSocket Socket { get; private set; }

                public NetClient(Agent agent, Config config)
                    : base("Zeze.Services.ServiceManager.Agent", config)
                {
                    Agent = agent;
                }
                public NetClient(Agent agent, Config config, string name) : base(name, config)
                {
                    Agent = agent;
                }

                public override void OnHandshakeDone(AsyncSocket sender)
                {
                    base.OnHandshakeDone(sender);
                    if (null == Socket)
                    {
                        Socket = sender;
                        Util.Task.Run(Agent._OnConnected, "ServiceManager.Agent._OnConnected");
                    }
                    else
                    {
                        Agent.logger.Error("Has Connected.");
                    }
                }

                public override void OnSocketClose(AsyncSocket so, Exception e)
                {
                    if (Socket == so)
                        Socket = null;
                    base.OnSocketClose(so, e);
                }
            }
        }
    }
}