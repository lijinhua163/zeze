
using System;
using System.Threading.Tasks;
using Zeze.Builtin.Provider;
using Zeze.Net;
using Zeze.Transaction;
using Zeze.Services.ServiceManager;
using System.Collections.Generic;

namespace Zeze.Arch
{
    public abstract class ProviderImplement : AbstractProviderImplement
    {
        public ProviderApp ProviderApp { get; set; }

        internal void ApplyOnChanged(Agent.SubscribeState subState)
        {
            if (subState.ServiceName.Equals(ProviderApp.LinkdServiceName))
            {
                // Linkd info
                ProviderApp.ProviderService.ApplyLinksChanged(subState.ServiceInfos);
            }
            else if (subState.ServiceName.StartsWith(ProviderApp.ServerServiceNamePrefix))
            {
                // Provider info
                // ���� SubscribeTypeSimple �ǲ���Ҫ SetReady �ģ�Ϊ����һ�´����Ͷ��������ˡ�
                // ���� SubscribeTypeReadyCommit �� ApplyOnPrepare �д���
                if (subState.SubscribeType == SubscribeInfo.SubscribeTypeSimple)
                    this.ProviderApp.ProviderDirectService.TryConnectAndSetReady(subState, subState.ServiceInfos);
            }
        }

        internal void ApplyOnPrepare(Agent.SubscribeState subState)
        {
            var pending = subState.ServiceInfosPending;
            if (pending != null && pending.ServiceName.StartsWith(ProviderApp.ServerServiceNamePrefix))
            {
                this.ProviderApp.ProviderDirectService.TryConnectAndSetReady(subState, pending);
            }
        }

        /**
         * ע������֧�ֵ�ģ�����
         * ������̬��̬��
         * ע���ģ��ʱ��������Provider֮�����ӵ�ip��port��
         * <p>
         * ����Linkd����
         * Provider��������Linkd��
         */
        public async Task RegisterModulesAndSubscribeLinkd()
        {
            var sm = ProviderApp.Zeze.ServiceManagerAgent;
            var services = new Dictionary<string, BModule>();

            // ע�᱾provider�ľ�̬����
            foreach (var it in ProviderApp.StaticBinds)
            {
                var name = $"{ProviderApp.ServerServiceNamePrefix}{it.Key}";
                var identity = ProviderApp.Zeze.Config.ServerId.ToString();
                await sm.RegisterService(name, identity, ProviderApp.DirectIp, ProviderApp.DirectPort);
                services.Add(name, it.Value);
            }
            // ע�᱾provider�Ķ�̬����
            foreach (var it in ProviderApp.DynamicModules)
            {
                var name = $"{ProviderApp.ServerServiceNamePrefix}{it.Key}";
                var identity = ProviderApp.Zeze.Config.ServerId.ToString();
                await sm.RegisterService(name, identity, ProviderApp.DirectIp, ProviderApp.DirectPort);
                services.Add(name, it.Value);
            }

            // ����providerֱ�����ַ���
            foreach (var e in services)
            {
                await sm.SubscribeService(e.Key, e.Value.SubscribeType);
            }

            // ����linkd���ַ���
            await sm.SubscribeService(ProviderApp.LinkdServiceName, SubscribeInfo.SubscribeTypeSimple);
        }


        public static void SendKick(AsyncSocket sender, long linkSid, int code, string desc)
        {
            var p = new Kick();
            p.Argument.Linksid = linkSid;
            p.Argument.Code = code;
            p.Argument.Desc = desc;
            p.Send(sender);
        }

        protected override async Task<long> ProcessDispatch(Protocol _p)
        {
            var p = _p as Dispatch;
            try
            {
                var factoryHandle = ProviderApp.ProviderService.FindProtocolFactoryHandle(p.Argument.ProtocolType);
                if (null == factoryHandle)
                {
                    SendKick(p.Sender, p.Argument.LinkSid, BKick.ErrorProtocolUnkown, "unknown protocol");
                    return Procedure.LogicError;
                }
                var p2 = factoryHandle.Factory();
                p2.Service = p.Service;
                p2.Decode(Zeze.Serialize.ByteBuffer.Wrap(p.Argument.ProtocolData));
                p2.Sender = p.Sender;

                var session = new ProviderUserSession(
                    ProviderApp.ProviderService,
                    p.Argument.Account,
                    p.Argument.Context,
                    p.Sender,
                    p.Argument.LinkSid);

                p2.UserState = session;
                if (Transaction.Transaction.Current != null)
                {
                    // �Ѿ��������У�Ƕ��ִ�С���ʱ����p2��NoProcedure���á�
                    Transaction.Transaction.Current.TopProcedure.ActionName = p2.GetType().FullName;
                    Transaction.Transaction.Current.TopProcedure.UserState = p2.UserState;
                    return await Zeze.Util.Mission.CallAsync(
                        factoryHandle.Handle,
                        p2,
                        (p, code) => { p.ResultCode = code; session.SendResponse(p); });
                }

                if (p2.Sender.Service.Zeze == null || factoryHandle.NoProcedure)
                {
                    // Ӧ�ÿ�ܲ�֧���������Э�������ˡ�����Ҫ����
                    return await Zeze.Util.Mission.CallAsync(
                        factoryHandle.Handle,
                        p2,
                        (p, code) => { p.ResultCode = code; session.SendResponse(p); });
                }

                // �����洢���̲����ڵ�ǰ�߳��е��á�
                return await Zeze.Util.Mission.CallAsync(
                    p2.Sender.Service.Zeze.NewProcedure(
                        () => factoryHandle.Handle(p2),
                        p2.GetType().FullName,
                        factoryHandle.TransactionLevel,
                        p2.UserState),
                    p2,
                    (p, code) => { p.ResultCode = code; session.SendResponse(p); }
                    );
            }
            catch (Exception ex)
            {
                SendKick(p.Sender, p.Argument.LinkSid, BKick.ErrorProtocolException, ex.ToString());
                throw;
            }
        }

        protected override Task<long> ProcessAnnounceLinkInfo(Zeze.Net.Protocol _p)
        {
            // reserve
            /*
            var protocol = _p as AnnounceLinkInfo;
            var linkSession = protocol.Sender.UserState as ProviderService.LinkSession;
            */
            return Task.FromResult(Procedure.Success);
        }
    }
}
