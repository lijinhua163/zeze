
using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Threading.Tasks;
using Zeze.Arch;
using Zeze.Builtin.Online;
using Zeze.Builtin.Provider;
using Zeze.Builtin.ProviderDirect;
using Zeze.Net;
using Zeze.Serialize;
using Zeze.Transaction;

namespace Zeze.Arch
{
    public class Online : AbstractOnline
    {
        public static long GetSpecialTypeIdFromBean(Bean bean)
        {
            return bean.TypeId;
        }

        public static Bean CreateBeanFromSpecialTypeId(long typeId)
        {
            throw new InvalidOperationException("Online Memory Table Dynamic Only.");
        }

        private static readonly NLog.Logger logger = NLog.LogManager.GetCurrentClassLogger();
        public ProviderApp ProviderApp { get; }
        //public LoadReporter LoadReporter { get; }
        public taccount TableAccount => _taccount;

        public Online(ProviderApp app)
        {
            this.ProviderApp = app;

            RegisterProtocols(ProviderApp.ProviderService);
            RegisterZezeTables(ProviderApp.Zeze);

            //LoadReporter = new(this);
        }

        public override void UnRegister()
        {
            UnRegisterZezeTables(ProviderApp.Zeze);
            UnRegisterProtocols(ProviderApp.ProviderService);
        }
        public void Start()
        {
            //LoadReporter.StartTimerTask();
            //Util.Scheduler.ScheduleAt(VerifyLocal, 3 + Util.Random.Instance.Next(3), 10); // at 3:10 - 6:10
        }

        public int LocalCount => _tlocal.Cache.DataMap.Count;

        public long WalkLocal(Func<string, BLocals, bool> walker)
        {
            return _tlocal.WalkCache(walker);
        }

        public async Task SetLocalBean(string account, string clientId, string key, Bean bean)
        {
            var bLocals = await _tlocal.GetAsync(account);
            if (null == bLocals)
                throw new Exception("roleid not online. " + account);
            if (false == bLocals.Logins.TryGetValue(clientId, out var login))
            {
                login = new BLocal();
                bLocals.Logins.Add(clientId, login);
            }
            var bAny = new BAny();
            bAny.Any.Bean = bean;
            login.Datas[key] = bAny;
        }

        public async Task<T> GetLocalBean<T>(string account, string clientId, string key)
            where T : Bean
        {
            var bLocals = await _tlocal.GetAsync(account);
            if (null == bLocals)
                return null;
            if (!bLocals.Logins.TryGetValue(clientId, out var login))
                return null;
            if (!login.Datas.TryGetValue(key, out var data))
                return null;
            return (T)data.Any.Bean;

        }

        public Zeze.Util.EventDispatcher LoginEvents { get; } = new("Online.Login");
        public Zeze.Util.EventDispatcher ReloginEvents { get; } = new("Online.Relogin");
        public Zeze.Util.EventDispatcher LogoutEvents { get; } = new("Online.Logout");
        public Zeze.Util.EventDispatcher LocalRemoveEvents { get; } = new("Online.Local.Remove");

        private Util.AtomicLong _LoginTimes = new();

        public long LoginTimes => _LoginTimes.Get();

        private async Task RemoveLocalAndTrigger(string account, string clientId)
        {
            var bLocals = await _tlocal.GetAsync(account);
            bLocals.Logins.Remove(clientId, out var localData);
            var arg = new LocalRemoveEventArgument()
            {
                Account = account,
                ClientId = clientId,
                LocalData = localData?.Copy(),
            };

            if (bLocals.Logins.Count == 0)
                await _tlocal.RemoveAsync(account); // remove first

            await LocalRemoveEvents.TriggerEmbed(this, arg);
            await LocalRemoveEvents.TriggerProcedure(ProviderApp.Zeze, this, arg);
            Transaction.Transaction.Current.RunWhileCommit(() => LocalRemoveEvents.TriggerThread(this, arg));
        }

        private async Task RemoveOnlineAndTrigger(string account, string clientId)
        {
            var bOnlines = await _tonline.GetAsync(account);
            bOnlines.Logins.Remove(clientId, out var onlineData);

            var arg = new LogoutEventArgument()
            {
                Account = account,
                ClientId = clientId,
                OnlineData = onlineData?.Copy(),
            };

            if (bOnlines.Logins.Count == 0)
                await _tonline.RemoveAsync(account); // remove first

            await LogoutEvents.TriggerEmbed(this, arg);
            await LogoutEvents.TriggerProcedure(ProviderApp.Zeze, this, arg);
            Transaction.Transaction.Current.RunWhileCommit(() => LogoutEvents.TriggerThread(this, arg));
        }

        private async Task LoginTrigger(string account, string clientId)
        {
            var arg = new LoginArgument()
            {
                Account = account,
                ClientId = clientId,
            };

            await LoginEvents.TriggerEmbed(this, arg);
            await LoginEvents.TriggerProcedure(ProviderApp.Zeze, this, arg);
            Transaction.Transaction.Current.RunWhileCommit(() => LoginEvents.TriggerThread(this, arg));
            _LoginTimes.IncrementAndGet();
        }

        private async Task ReloginTrigger(string account, string clientId)
        {
            var arg = new LoginArgument()
            {
                Account = account,
                ClientId = clientId,
            };

            await ReloginEvents.TriggerEmbed(this, arg);
            await ReloginEvents.TriggerProcedure(ProviderApp.Zeze, this, arg);
            Transaction.Transaction.Current.RunWhileCommit(() => ReloginEvents.TriggerThread(this, arg));
            _LoginTimes.IncrementAndGet();
        }

        public async Task OnLinkBroken(string account, string clientId, BLinkBroken arg)
        {
            long currentLoginVersion = 0;
            {
                var online = await _tonline.GetAsync(account);
                if (false == online.Logins.TryGetValue(clientId, out var loginOnline))
                    return;
                // skip not owner: �������LinkSid�ǲ���ֵġ�����������LoginVersion��
                if (loginOnline.LinkSid != arg.LinkSid)
                    return;

                var version = await _tversion.GetOrAddAsync(account);
                var local = await _tlocal.GetAsync(account);
                if (local == null || false == local.Logins.TryGetValue(clientId, out var loginLocal))
                    return; // ���ڱ�����¼��
                if (false == version.Logins.TryGetValue(clientId, out var loginVersion))
                    return; // �����ڵ�¼��
                currentLoginVersion = loginLocal.LoginVersion;
                if (loginVersion.LoginVersion != currentLoginVersion)
                    await RemoveLocalAndTrigger(account, clientId); // ���������Ѿ���ʱ������ɾ����
            }
            await Task.Delay(10 * 60 * 1000);

            // TryRemove
            await ProviderApp.Zeze.NewProcedure(async () =>
            {
                // local online �����ж�version�ֱ���ɾ����
                var local = await _tlocal.GetAsync(account);
                if (null != local
                    && local.Logins.TryGetValue(clientId, out var loginLocal)
                    && loginLocal.LoginVersion == currentLoginVersion)
                {
                    await RemoveLocalAndTrigger(account, clientId);
                }
                // ���������ӳ��ڼ佨�����µĵ�¼������汾���жϻ�ʧ�ܡ�
                var online = await _tonline.GetAsync(account);
                var version = await _tversion.GetOrAddAsync(account);
                if (null != online
                    && version.Logins.TryGetValue(clientId, out var loginVersion)
                    && loginVersion.LoginVersion == currentLoginVersion)
                {
                    await RemoveOnlineAndTrigger(account, clientId);
                }
                return Procedure.Success;
            }, "Onlines.OnLinkBroken").CallAsync();
        }

        public async Task AddReliableNotifyMark(string account, string clientId, string listenerName)
        {
            var online = await _tonline.GetAsync(account);
            if (null == online)
                throw new Exception("Not Online. AddReliableNotifyMark: " + listenerName);
            var version = await _tversion.GetOrAddAsync(account);
            version.Logins.GetOrAdd(clientId).ReliableNotifyMark.Add(listenerName);
        }

        public async Task RemoveReliableNotifyMark(string account, string clientId, string listenerName)
        {
            // �Ƴ�����ͨ���������κ��жϡ�
            if ((await _tversion.GetOrAddAsync(account)).Logins.TryGetValue(clientId, out var login))
                login.ReliableNotifyMark.Remove(listenerName);
        }

        public void SendReliableNotifyWhileCommit(
            string account, string clientId, string listenerName, Protocol p)
        {
            Transaction.Transaction.Current.RunWhileCommit(
                () => SendReliableNotify(account, clientId, listenerName, p)
                );
        }

        public void SendReliableNotifyWhileCommit(
            string account, string clientId, string listenerName, int typeId, Binary fullEncodedProtocol)
        {
            Transaction.Transaction.Current.RunWhileCommit(
                () => SendReliableNotify(account, clientId, listenerName, typeId, fullEncodedProtocol)
                );
        }

        public void SendReliableNotifyWhileRollback(string account, string clientId, string listenerName, Protocol p)
        {
            Transaction.Transaction.Current.RunWhileRollback(
                () => SendReliableNotify(account, clientId, listenerName, p)
                );
        }

        public void SendReliableNotifyWhileRollback(
            string account, string clientId, string listenerName, int typeId, Binary fullEncodedProtocol)
        {
            Transaction.Transaction.Current.RunWhileRollback(
                () => SendReliableNotify(account, clientId, listenerName, typeId, fullEncodedProtocol)
                );
        }

        public void SendReliableNotify(string account, string clientId, string listenerName, Protocol p)
        {
            SendReliableNotify(account, clientId, listenerName, p.TypeId, new Binary(p.Encode()));
        }

        /// <summary>
        /// �������߿ɿ�Э�飬��������ߵȣ���Ȼ���ᷢ��Ŷ��
        /// </summary>
        /// <param name="roleId"></param>
        /// <param name="listenerName"></param>
        /// <param name="fullEncodedProtocol">Э������ȱ��룬��Ϊ�������</param>
        public void SendReliableNotify(
            string account, string clientId, string listenerName, long typeId, Binary fullEncodedProtocol)
        {
            ProviderApp.Zeze.TaskOneByOneByKey.Execute(
                listenerName,
                ProviderApp.Zeze.NewProcedure(async () =>
                {
                    var online = await _tonline.GetAsync(account);
                    if (null == online)
                    {
                        // ��ȫ���ߣ����Կɿ���Ϣ���ͣ��ɿ���Ϣ����Ϊ�����ṩ���񣬲����ṩȫ�ֿɿ���Ϣ��
                        return Procedure.Success;
                    }
                    var version = await _tversion.GetOrAddAsync(account);
                    if (false == version.Logins.TryGetValue(clientId, out var login)
                        || false == login.ReliableNotifyMark.Contains(listenerName))
                    {
                        return Procedure.Success; // �������װ�ص�ʱ��Ҫͬ�����������
                    }

                    // �ȱ������ٷ��ͣ�Ȼ��ͻ��˻���ȷ�ϡ�
                    // see Game.Login.Module: CLogin CReLogin CReliableNotifyConfirm ��ʵ�֡�
                    login.ReliableNotifyQueue.Add(fullEncodedProtocol);

                    var notify = new SReliableNotify(); // ��ֱ�ӷ���Э�飬����Ϊ�ͻ�����Ҫʶ��ReliableNotify�����д�������������
                    notify.Argument.ReliableNotifyTotalCountStart = login.ReliableNotifyTotalCount;
                    notify.Argument.Notifies.Add(fullEncodedProtocol);

                    await SendInProcedure(account, clientId, notify.TypeId, new Binary(notify.Encode()));
                    login.ReliableNotifyTotalCount += 1; // ��ӣ�start �� Queue.Add ֮ǰ�ġ�
                    return Procedure.Success;
                },
                "SendReliableNotify." + listenerName
                ));
        }

        public class RoleOnLink
        {
            public string LinkName { get; set; } = ""; // empty when not online
            public AsyncSocket LinkSocket { get; set; } // null if not online
            public int ServerId { get; set; } = -1;
            public long ProviderSessionId { get; set; }
            public Dictionary<ALogin, long> Logins { get; } = new();
        }

        public class ALogin
        {
            public string Account { get; set; }
            public string ClientId { get; set; }

            public ALogin(string account, string clientId)
            {
                Account = account;
                ClientId = clientId;
            }

            public override int GetHashCode()
            {
                const int _prime_ = 31;
                int _h_ = 0;
                _h_ = _h_ * _prime_ + Account.GetHashCode();
                _h_ = _h_ * _prime_ + ClientId.GetHashCode();
                return _h_;
            }

            public override bool Equals(object obj)
            {
                if (obj == this)
                    return true;
                if (obj is ALogin login)
                {
                    return Account.Equals(login.Account) && ClientId.Equals(login.ClientId);
                }
                return false;
            }
        }

        public async Task<ICollection<RoleOnLink>> GroupByLink(ICollection<ALogin> logins)
        {
            var groups = new Dictionary<string, RoleOnLink>();
            var groupNotOnline = new RoleOnLink(); // LinkName is Empty And Socket is null.
            groups.Add(groupNotOnline.LinkName, groupNotOnline);

            foreach (var alogin in logins)
            {
                var online = await _tonline.GetAsync(alogin.Account);
                if (null == online)
                {
                    groupNotOnline.Logins.TryAdd(alogin, 0);
                    continue;
                }
                if (false == online.Logins.TryGetValue(alogin.ClientId, out var login))
                {
                    groupNotOnline.Logins.TryAdd(alogin, 0);
                    continue;
                }
                if (false == ProviderApp.ProviderService.Links.TryGetValue(login.LinkName, out var connector))
                {
                    groupNotOnline.Logins.TryAdd(alogin, 0);
                    continue;
                }

                if (false == connector.IsHandshakeDone)
                {
                    groupNotOnline.Logins.TryAdd(alogin, 0);
                    continue;
                }
                // ���汣��connector.Socket��ʹ�ã����֮�����ӱ��رգ��Ժ���Э��ʧ�ܡ�
                if (false == groups.TryGetValue(login.LinkName, out var group))
                {
                    group = new RoleOnLink()
                    {
                        LinkName = login.LinkName,
                        LinkSocket = connector.Socket,
                        // ����online����Login��ʱ������versionҲ�϶�������Ӧ��Login��
                        ServerId = (await _tversion.GetOrAddAsync(alogin.Account)).Logins.GetOrAdd(alogin.ClientId).ServerId,
                    };
                    groups.Add(group.LinkName, group);
                }
                group.Logins.TryAdd(alogin, login.LinkSid);
            }
            return groups.Values;
        }

        private async Task SendInProcedure(
            string account, string clientId, long typeId, Binary fullEncodedProtocol)
        {
            // ������ϢΪ������TaskOneByOne��ֻ��һ��һ�����ͣ�Ϊ���ٸĴ��룬��ʹ�þɵ�GroupByLink�ӿڡ�
            var groups = await GroupByLink(new List<ALogin> { new ALogin(account, clientId) });
            foreach (var group in groups)
            {
                if (group.LinkSocket == null)
                    continue; // skip not online

                var send = new Send();
                send.Argument.ProtocolType = typeId;
                send.Argument.ProtocolWholeData = fullEncodedProtocol;
                send.Argument.LinkSids.UnionWith(group.Logins.Values);
                group.LinkSocket.Send(send);
            }
        }

        private void Send(string account, string clientId, long typeId, Binary fullEncodedProtocol)
        {
            // ����Э�������������������ִ�С�
            ProviderApp.Zeze.TaskOneByOneByKey.Execute(account, () =>
                ProviderApp.Zeze.NewProcedure(async () =>
                {
                    await SendInProcedure(account, clientId, typeId, fullEncodedProtocol);
                    return Procedure.Success;
                }, "Onlines.Send"));
        }

        public void Send(string account, string clientId, Protocol p)
        {
            Send(account, clientId, p.TypeId, new Binary(p.Encode()));
        }

        protected override Task<long> ProcessLoginRequest(Protocol p)
        {
            throw new NotImplementedException();
        }

        protected override Task<long> ProcessLogoutRequest(Protocol p)
        {
            throw new NotImplementedException();
        }

        protected override Task<long> ProcessReliableNotifyConfirmRequest(Protocol p)
        {
            throw new NotImplementedException();
        }

        protected override Task<long> ProcessReLoginRequest(Protocol p)
        {
            throw new NotImplementedException();
        }
        /*
public void Send(ICollection<ALogin> logins, Protocol p)
{
   Send(logins, p.TypeId, new Binary(p.Encode()));
}

public void SendWhileCommit(string account, Protocol p)
{
   Transaction.Transaction.Current.RunWhileCommit(() => Send(account, p));
}

public void SendWhileCommit(ICollection<string> accounts, Protocol p)
{
   Transaction.Transaction.Current.RunWhileCommit(() => Send(accounts, p));
}

public void SendWhileRollback(string account, Protocol p, bool WaitConfirm = false)
{
   Transaction.Transaction.Current.RunWhileRollback(() => Send(account, p, WaitConfirm));
}

public void SendWhileRollback(ICollection<string> accounts, Protocol p)
{
   Transaction.Transaction.Current.RunWhileRollback(() => Send(accounts, p));
}

/// <summary>
/// Func<sender, target, result>
/// sender: ��ѯ�����ߣ�������͸�����
/// target: ��ѯĿ���ɫ��
/// result: ����ֵ��int������ͨ���������̷���ֵ������
/// </summary>
public ConcurrentDictionary<string, Func<long, long, Binary, Task<long>>> TransmitActions { get; } = new();

/// <summary>
/// ת����ѯ�����RoleId��
/// </summary>
/// <param name="sender">��ѯ�����ߣ�������͸�����</param>
/// <param name="actionName">��ѯ������ʵ��</param>
/// <param name="roleId">Ŀ���ɫ</param>
public void Transmit(long sender, string actionName, long roleId, Serializable parameter = null)
{
   Transmit(sender, actionName, new List<long>() { roleId }, parameter);
}

public void ProcessTransmit(long sender, string actionName, IEnumerable<long> roleIds, Binary parameter)
{
   if (TransmitActions.TryGetValue(actionName, out var handle))
   {
       foreach (var target in roleIds)
       {
           ProviderApp.Zeze.NewProcedure(async () => await handle(sender, target, parameter), "Game.Online.Transmit:" + actionName).Execute();
       }
   }
}

public class RoleOnServer
{
   public int ServerId { get; set; } = -1; // empty when not online
   public HashSet<long> Roles { get; } = new();
   public void AddAll(HashSet<long> roles)
   {
       foreach (var role in roles)
           Roles.Add(role);
   }
}

public async Task<ICollection<RoleOnServer>> GroupByServer(ICollection<string> accounts)
{
   var groups = new Dictionary<int, RoleOnServer>();
   var groupNotOnline = new RoleOnServer(); // LinkName is Empty And Socket is null.
   groups.Add(groupNotOnline.ServerId, groupNotOnline);

   foreach (var account in accounts)
   {
       var online = await _tonline.GetAsync(account);
       if (null == online)
       {
           groupNotOnline.Roles.Add(account);
           continue;
       }
       var version = await _tversion.GetOrAddAsync(account);
       // ���汣��connector.Socket��ʹ�ã����֮�����ӱ��رգ��Ժ���Э��ʧ�ܡ�
       if (false == groups.TryGetValue(version.ServerId, out var group))
       {
           group = new RoleOnServer()
           {
               ServerId = version.ServerId
           };
           groups.Add(group.ServerId, group);
       }
       group.Roles.Add(account);
   }
   return groups.Values;
}

private RoleOnServer Merge(RoleOnServer current, RoleOnServer m)
{
   if (null == current)
       return m;
   foreach (var roleId in m.Roles)
       current.Roles.Add(roleId);
   return current;
}

private async Task TransmitInProcedure(long sender, string actionName, ICollection<long> roleIds, Binary parameter)
{
   if (ProviderApp.Zeze.Config.GlobalCacheManagerHostNameOrAddress.Length == 0)
   {
       // û������cache-sync�����ϴ�����������
       ProcessTransmit(sender, actionName, roleIds, parameter);
       return;
   }

   var groups = await GroupByServer(roleIds);
   RoleOnServer groupLocal = null;
   foreach (var group in groups)
   {
       if (group.ServerId == -1 || group.ServerId == ProviderApp.Zeze.Config.ServerId)
       {
           // loopback ���ǵ�ǰgs.
           // ���ڲ����ߵĽ�ɫ��ֱ���ڱ������С�
           groupLocal = Merge(groupLocal, group);
           continue;
       }

       var transmit = new Transmit();
       transmit.Argument.ActionName = actionName;
       transmit.Argument.Sender = sender;
       transmit.Argument.Roles.AddAll(group.Roles);
       if (null != parameter)
       {
           transmit.Argument.Parameter = parameter;
       }

       if (false == ProviderApp.ProviderDirectService.ProviderByServerId.TryGetValue(group.ServerId, out var ps))
       {
           groupLocal.AddAll(group.Roles);
           continue;
       }
       var socket = ProviderApp.ProviderDirectService.GetSocket(ps.SessionId);
       if (null == socket)
       {
           groupLocal.AddAll(group.Roles);
           continue;
       }
       transmit.Send(socket);
   }
   if (groupLocal.Roles.Count > 0)
       ProcessTransmit(sender, actionName, groupLocal.Roles, parameter);
}

public void Transmit(long sender, string actionName, ICollection<long> roleIds, Serializable parameter = null)
{
   if (false == TransmitActions.ContainsKey(actionName))
       throw new Exception("Unkown Action Name: " + actionName);

   var binaryParam = parameter == null ? Binary.Empty : new Binary(ByteBuffer.Encode(parameter));
   // ����Э�������������������ִ�С�
   _ = ProviderApp.Zeze.NewProcedure(async () =>
   {
       await TransmitInProcedure(sender, actionName, roleIds, binaryParam);
       return Procedure.Success;
   }, "Onlines.Transmit").CallAsync();
}

public void TransmitWhileCommit(long sender, string actionName, long roleId, Serializable parameter = null)
{
   if (false == TransmitActions.ContainsKey(actionName))
       throw new Exception("Unkown Action Name: " + actionName);
   Transaction.Transaction.Current.RunWhileCommit(() => Transmit(sender, actionName, roleId, parameter));
}

public void TransmitWhileCommit(long sender, string actionName, ICollection<long> roleIds, Serializable parameter = null)
{
   if (false == TransmitActions.ContainsKey(actionName))
       throw new Exception("Unkown Action Name: " + actionName);
   Transaction.Transaction.Current.RunWhileCommit(() => Transmit(sender, actionName, roleIds, parameter));
}

public void TransmitWhileRollback(long sender, string actionName, long roleId, Serializable parameter = null)
{
   if (false == TransmitActions.ContainsKey(actionName))
       throw new Exception("Unkown Action Name: " + actionName);
   Transaction.Transaction.Current.RunWhileRollback(() => Transmit(sender, actionName, roleId, parameter));
}

public void TransmitWhileRollback(long sender, string actionName, ICollection<long> roleIds, Serializable parameter = null)
{
   if (false == TransmitActions.ContainsKey(actionName))
       throw new Exception("Unkown Action Name: " + actionName);
   Transaction.Transaction.Current.RunWhileRollback(() => Transmit(sender, actionName, roleIds, parameter));
}

public class ConfirmContext : Service.ManualContext
{
   public HashSet<string> LinkNames { get; } = new HashSet<string>();
   public TaskCompletionSource<long> Future { get; }
   public ProviderApp App { get; }

   public ConfirmContext(ProviderApp app, TaskCompletionSource<long> future)
   {
       App = app;
       Future = future;
   }

   public override void OnRemoved()
   {
       lock (this)
       {
           Future.SetResult(base.SessionId);
       }
   }

   public long ProcessLinkConfirm(string linkName)
   {
       lock (this)
       {
           LinkNames.Remove(linkName);
           if (LinkNames.Count == 0)
           {
               App.ProviderService.TryRemoveManualContext<ConfirmContext>(SessionId);
           }
           return Procedure.Success;
       }
   }
}

private void Broadcast(long typeId, Binary fullEncodedProtocol, int time, bool WaitConfirm)
{
   TaskCompletionSource<long> future = null;
   long serialId = 0;
   if (WaitConfirm)
   {
       future = new TaskCompletionSource<long>();
       var confirmContext = new ConfirmContext(ProviderApp, future);
       foreach (var link in ProviderApp.ProviderService.Links.Values)
       {
           if (link.Socket != null)
               confirmContext.LinkNames.Add(link.Name);
       }
       serialId = ProviderApp.ProviderService.AddManualContextWithTimeout(confirmContext, 5000);
   }

   var broadcast = new Broadcast();
   broadcast.Argument.ProtocolType = typeId;
   broadcast.Argument.ProtocolWholeData = fullEncodedProtocol;
   broadcast.Argument.ConfirmSerialId = serialId;
   broadcast.Argument.Time = time;

   foreach (var link in ProviderApp.ProviderService.Links.Values)
   {
       link.Socket?.Send(broadcast);
   }

   future?.Task.Wait();
}

public void Broadcast(Protocol p, int time = 60 * 1000, bool WaitConfirm = false)
{
   Broadcast(p.TypeId, new Binary(p.Encode()), time, WaitConfirm);
}

private void VerifyLocal(Util.SchedulerTask thisTask)
{
   string account = null;
   _tlocal.WalkCache(
       (k, v) =>
       {
           // �ȵõ�roleId
           account = k;
           return true;
       },
       () =>
       {
           // ����ִ������
           try
           {
               ProviderApp.Zeze.NewProcedure(async () =>
               {
                   await TryRemoveLocal(account);
                   return 0L;
               }, "VerifyLocal:" + account).CallSynchronously();
           }
           catch (Exception e)
           {
               logger.Error(e);
           }
       });
   // �����ʼʱ�䣬������֤�������ڼ��С�3:10 - 5:10
   Util.Scheduler.ScheduleAt(VerifyLocal, 3 + Util.Random.Instance.Next(3), 10); // at 3:10 - 6:10
}

private async Task TryRemoveLocal(string account)
{
   var online = await _tonline.GetAsync(account);
   var local = await _tlocal.GetAsync(account);
   var version = await _tversion.GetOrAddAsync(account);
   if (null == local)
       return;
   // null == online && null == local -> do nothing
   // null != online && null == local -> do nothing

   if (null == online)
   {
       // remove all
       foreach (var loginLocal in local.Logins)
           await RemoveLocalAndTrigger(account, loginLocal.Key);
   }
   else
   {
       // ��ȫ�������в���login-local��ɾ�������ڻ��߰汾��ƥ��ġ�
       foreach (var loginLocal in local.Logins)
       {
           if (false == version.Logins.TryGetValue(loginLocal.Key, out var loginVersion)
               || loginVersion.LoginVersion != loginLocal.Value.LoginVersion)
           {
               await RemoveLocalAndTrigger(account, loginLocal.Key);
           }
       }
   }

}

[RedirectToServer]
protected async Task RedirectNotify(int serverId, string account)
{
   await TryRemoveLocal(account);
}

protected override async Task<long> ProcessLoginRequest(Zeze.Net.Protocol p)
{
   var rpc = p as Login;
   var session = ProviderUserSession.Get(rpc);

   var account = await _taccount.GetOrAddAsync(session.Account);
   var online = await _tonline.GetOrAddAsync(rpc.Argument.RoleId);
   var local = await _tlocal.GetOrAddAsync(rpc.Argument.RoleId);
   var version = await _tversion.GetOrAddAsync(rpc.Argument.RoleId);

   // login exist && not local
   if (version.LoginVersion != 0 && version.LoginVersion != local.LoginVersion)
   {
       // nowait
       _ = RedirectNotify(version.ServerId, rpc.Argument.RoleId);
   }
   var loginVersion = account.LastLoginVersion + 1;
   account.LastLoginVersion = loginVersion;
   version.LoginVersion = loginVersion;
   local.LoginVersion = loginVersion;

   if (!online.LinkName.Equals(session.LinkName) || online.LinkSid == session.LinkSid)
   {
       ProviderApp.ProviderService.Kick(online.LinkName, online.LinkSid,
               BKick.ErrorDuplicateLogin, "duplicate role login");
   }

   /////////////////////////////////////////////////////////////
   // ��LinkName,LinkSidû�б仯��ʱ�򣬱��ּ�¼�Ƕ�ȡ״̬����������д����
   // ��ΪOnline���ݿ��ܻᱻ�ܶ�ط����棬д��������ɻ���ʧЧ��
   // see Linkd.StableLinkSid
   if (false == online.LinkName.Equals(session.LinkName))
       online.LinkName = session.LinkName;
   if (online.LinkSid != session.LinkSid)
       online.LinkSid = session.LinkSid;
   /////////////////////////////////////////////////////////////

   version.ReliableNotifyConfirmCount = 0;
   version.ReliableNotifyTotalCount = 0;
   version.ReliableNotifyMark.Clear();
   version.ReliableNotifyQueue.Clear();

   var linkSession = session.Link.UserState as ProviderService.LinkSession;
   version.ServerId = ProviderApp.Zeze.Config.ServerId;

   await LoginTrigger(rpc.Argument.RoleId);

   // ���ύ���������״̬��
   // see linkd::Zezex.Provider.ModuleProvider��ProcessBroadcast
   session.SendResponseWhileCommit(rpc);
   Transaction.Transaction.Current.RunWhileCommit(() =>
   {
       var setUserState = new SetUserState();
       setUserState.Argument.LinkSid = session.LinkSid;
       setUserState.Argument.States.Add(rpc.Argument.RoleId);
       rpc.Sender.Send(setUserState); // ֱ��ʹ��link���ӡ�
   });
   //App.Load.LoginCount.IncrementAndGet();
   return Procedure.Success;
}

protected override async Task<long> ProcessReLoginRequest(Zeze.Net.Protocol p)
{
   var rpc = p as ReLogin;
   var session = ProviderUserSession.Get(rpc);

   BAccount account = await _taccount.GetAsync(session.Account);
   if (null == account)
       return ErrorCode(ResultCodeAccountNotExist);

   if (account.LastLoginRoleId != rpc.Argument.RoleId)
       return ErrorCode(ResultCodeNotLastLoginRoleId);

   if (!account.Roles.Contains(rpc.Argument.RoleId))
       return ErrorCode(ResultCodeRoleNotExist);

   BOnline online = await _tonline.GetAsync(rpc.Argument.RoleId);
   if (null == online)
       return ErrorCode(ResultCodeOnlineDataNotFound);

   var local = await _tlocal.GetOrAddAsync(rpc.Argument.RoleId);
   var version = await _tversion.GetOrAddAsync(rpc.Argument.RoleId);

   // login exist && not local
   if (version.LoginVersion != 0 && version.LoginVersion != local.LoginVersion)
   {
       // nowait
       _ = RedirectNotify(version.ServerId, rpc.Argument.RoleId);
   }
   var loginVersion = account.LastLoginVersion + 1;
   account.LastLoginVersion = loginVersion;
   version.LoginVersion = loginVersion;
   local.LoginVersion = loginVersion;

   /////////////////////////////////////////////////////////////
   // ��LinkName,LinkSidû�б仯��ʱ�򣬱��ּ�¼�Ƕ�ȡ״̬����������д����
   // ��ΪOnline���ݿ��ܻᱻ�ܶ�ط����棬д��������ɻ���ʧЧ��
   // see Linkd.StableLinkSid
   if (false == online.LinkName.Equals(session.LinkName))
       online.LinkName = session.LinkName;
   if (online.LinkSid != session.LinkSid)
       online.LinkSid = session.LinkSid;
   /////////////////////////////////////////////////////////////

   await ReloginTrigger(session.RoleId.Value);

   // �ȷ�������ٷ���ͬ�����ݣ�ReliableNotifySync����
   // ��ʹ�� WhileCommit������ɹ������ύ��˳���ͣ�ʧ��ȫ�����ᷢ�͡�
   session.SendResponseWhileCommit(rpc);
   Transaction.Transaction.Current.RunWhileCommit(() =>
   {
       var setUserState = new SetUserState();
       setUserState.Argument.LinkSid = session.LinkSid;
       setUserState.Argument.States.Add(rpc.Argument.RoleId);
       rpc.Sender.Send(setUserState); // ֱ��ʹ��link���ӡ�
   });

   var syncResultCode = await ReliableNotifySync(session.RoleId.Value,
       session, rpc.Argument.ReliableNotifyConfirmCount);

   if (syncResultCode != ResultCodeSuccess)
       return ErrorCode((ushort)syncResultCode);

   //App.Load.LoginCount.IncrementAndGet();
   return Procedure.Success;
}

protected override async Task<long> ProcessLogoutRequest(Zeze.Net.Protocol p)
{
   var rpc = p as Logout;
   var session = ProviderUserSession.Get(rpc);

   if (session.RoleId == null)
       return ErrorCode(ResultCodeNotLogin);

   var local = await _tlocal.GetAsync(session.RoleId.Value);
   var online = await _tonline.GetAsync(session.RoleId.Value);
   var version = await _tversion.GetOrAddAsync(session.RoleId.Value);
   // ��¼�����������ϡ�
   if (local == null && online != null)
       _ = RedirectNotify(version.ServerId, session.RoleId.Value); // nowait
   if (null != local)
       await RemoveLocalAndTrigger(session.RoleId.Value);
   if (null != online)
       await RemoveOnlineAndTrigger(session.RoleId.Value);

   // ������״̬���ٷ���Logout�����
   Transaction.Transaction.Current.RunWhileCommit(() =>
   {
       var setUserState = new SetUserState();
       setUserState.Argument.LinkSid = session.LinkSid;
       rpc.Sender.Send(setUserState); // ֱ��ʹ��link���ӡ�
   });
   session.SendResponseWhileCommit(rpc);
   // �� OnLinkBroken ʱ����������ͬʱ���������쳣�������
   // App.Load.LogoutCount.IncrementAndGet();
   return Procedure.Success;
}

private async Task<int> ReliableNotifySync(long roleId, ProviderUserSession session, long ReliableNotifyConfirmCount, bool sync = true)
{
   var online = await _tversion.GetOrAddAsync(roleId);
   if (ReliableNotifyConfirmCount < online.ReliableNotifyConfirmCount
       || ReliableNotifyConfirmCount > online.ReliableNotifyTotalCount
       || ReliableNotifyConfirmCount - online.ReliableNotifyConfirmCount > online.ReliableNotifyQueue.Count)
   {
       return ResultCodeReliableNotifyConfirmCountOutOfRange;
   }

   int confirmCount = (int)(ReliableNotifyConfirmCount - online.ReliableNotifyConfirmCount);

   if (sync)
   {
       var notify = new SReliableNotify();
       notify.Argument.ReliableNotifyTotalCountStart = ReliableNotifyConfirmCount;
       for (int i = confirmCount; i < online.ReliableNotifyQueue.Count; ++i)
           notify.Argument.Notifies.Add(online.ReliableNotifyQueue[i]);
       session.SendResponseWhileCommit(notify);
   }
   online.ReliableNotifyQueue.RemoveRange(0, confirmCount);
   online.ReliableNotifyConfirmCount = ReliableNotifyConfirmCount;
   return ResultCodeSuccess;
}

protected override async Task<long> ProcessReliableNotifyConfirmRequest(Zeze.Net.Protocol p)
{
   var rpc = p as ReliableNotifyConfirm;
   var session = ProviderUserSession.Get(rpc);

   BOnline online = await _tonline.GetAsync(session.RoleId.Value);
   if (null == online)
       return ErrorCode(ResultCodeOnlineDataNotFound);

   session.SendResponseWhileCommit(rpc); // ͬ��ǰ�ύ��
   var syncResultCode = await ReliableNotifySync(session.RoleId.Value,
       session, rpc.Argument.ReliableNotifyConfirmCount, false);

   if (ResultCodeSuccess != syncResultCode)
       return ErrorCode((ushort)syncResultCode);

   return Procedure.Success;
}
*/
    }
}