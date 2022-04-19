
using System.Collections.Generic;
using System.Threading.Tasks;
using Zeze.Beans.LinkdBase;
using Zeze.Beans.Provider;
using Zeze.Services.ServiceManager;

namespace Zeze.Arch
{
    public class LinkdProvider : AbstractLinkdProvider
    {
        private static readonly NLog.Logger logger = NLog.LogManager.GetCurrentClassLogger();
        public LinkdApp LinkdApp { get; set; }
        public ProviderDistribute Distribute { get; set; }

        // ���ڿͻ���ѡ��Provider��ֻ֧��һ��Provider�����Ҫ֧�ֶ��֣���Ҫ�ͻ������Ӳ���������������ˡ�
        // �ڲ���ModuleRedirect ModuleRedirectAll Transmit��Я����ServiceNamePrefix���������ԣ�
        // �ڲ���Provider����֧����ȫ��ͬ��solution�������������������δ����չ�ã�
        // ��������һ����Ŀ����ʹ�ö��Prefix��
        public string ServerServiceNamePrefix { get; private set; } = "";

        protected override async Task<long> ProcessAnnounceProviderInfo(Zeze.Net.Protocol _p)
        {
            var protocol = _p as AnnounceProviderInfo;
            var session = protocol.Sender.UserState as LinkdProviderSession;
            session.Info = protocol.Argument;
            ServerServiceNamePrefix = protocol.Argument.ServiceNamePrefix;
            return Zeze.Transaction.Procedure.Success;
        }

        public int FirstModuleWithConfigTypeDefault { get; private set; } = 0;

        protected override async Task<long> ProcessBindRequest(Zeze.Net.Protocol _p)
        {
            var rpc = _p as Bind;
            if (rpc.Argument.LinkSids.Count == 0)
            {
                var providerSession = rpc.Sender.UserState as LinkdProviderSession;
                foreach (var module in rpc.Argument.Modules)
                {
                    if (FirstModuleWithConfigTypeDefault == 0
                        && module.Value.ConfigType == BModule.ConfigTypeDefault)
                    {
                        FirstModuleWithConfigTypeDefault = module.Value.ConfigType;
                    }
                    var providerModuleState = new ProviderModuleState(providerSession.SessionId,
                        module.Key, module.Value.ChoiceType, module.Value.ConfigType);
                    var serviceName = LinkdApp.LinkdProvider.Distribute.MakeServiceName(providerSession.Info.ServiceNamePrefix, module.Key);
                    var subState = await LinkdApp.Zeze.ServiceManagerAgent.SubscribeService(serviceName,
                        SubscribeInfo.SubscribeTypeReadyCommit,
                        providerModuleState);
                    // ���ĳɹ��Ժ󣬽�����Ҫ����ready��service-list��Agentά����
                    subState.SetServiceIdentityReadyState(providerSession.Info.ServiceIndentity, providerModuleState);
                    providerSession.StaticBinds.TryAdd(module.Key, module.Key);
                }
            }
            else
            {
                // ��̬��
                foreach (var linkSid in rpc.Argument.LinkSids)
                {
                    var link = LinkdApp.LinkdService.GetSocket(linkSid);
                    if (null != link)
                    {
                        var linkSession = link.UserState as LinkdUserSession;
                        linkSession.Bind(LinkdApp.LinkdProviderService, link, rpc.Argument.Modules.Keys, rpc.Sender);
                    }
                }
            }
            rpc.SendResultCode(BBind.ResultSuccess);
            return Zeze.Transaction.Procedure.Success;
        }

        protected override async Task<long> ProcessBroadcast(Zeze.Net.Protocol p)
        {
            var protocol = p as Broadcast;
            if (protocol.Argument.ConfirmSerialId != 0)
            {
                var confirm = new SendConfirm();
                confirm.Argument.ConfirmSerialId = protocol.Argument.ConfirmSerialId;
                protocol.Sender.Send(confirm);
            }

            LinkdApp.LinkdService.Foreach((socket) =>
            {
                // auth ͨ�����������͹㲥��
                // ���Ҫʵ�� role.login ��������Provider ���� SetLogin Э����ڲ�server���á�
                // ��Щ�㲥һ������Ҫͨ�棬ֻҪ��¼�ͻ��˾������յ���Ȼ����������ʱ�����ʾ�����������Ͳ������״̬�ˡ�
                var linkSession = socket.UserState as LinkdUserSession;
                if (null != linkSession && null != linkSession.Account
                    // ���״̬���ڲ�������CLogin��ʱ�����õģ��ж�һ�£����ܿ��Լ򻯿ͻ���ʵ�֡�
                    // ��Ȼ������岻�Ǻܺá����ȽϷ���һ���ʹ�á�
                    && linkSession.UserStates.Count > 0
                    )
                    socket.Send(protocol.Argument.ProtocolWholeData);
            });
            return Zeze.Transaction.Procedure.Success;
        }

        protected override async Task<long> ProcessKick(Zeze.Net.Protocol p)
        {
            var protocol = p as Kick;
            LinkdApp.LinkdService.ReportError(
                protocol.Argument.Linksid, BReportError.FromProvider,
                protocol.Argument.Code, protocol.Argument.Desc);
            return Zeze.Transaction.Procedure.Success;
        }

        protected override async Task<long> ProcessSend(Zeze.Net.Protocol _p)
        {
            var protocol = _p as Send;
            // ���������������������ģ�����߼�������֮�䣬���ͻ��˷���Э���Ŷӡ�
            // ���Բ��õȴ��������͸��ͻ��ˣ��յ��Ϳ��Է��ͽ����
            if (protocol.Argument.ConfirmSerialId != 0)
            {
                var confirm = new SendConfirm();
                confirm.Argument.ConfirmSerialId = protocol.Argument.ConfirmSerialId;
                protocol.Sender.Send(confirm);
            }

            foreach (var linkSid in protocol.Argument.LinkSids)
            {
                var link = LinkdApp.LinkdService.GetSocket(linkSid);
                logger.Debug("Send {0} {1}", Zeze.Net.Protocol.GetModuleId(protocol.Argument.ProtocolType),
                    Zeze.Net.Protocol.GetProtocolId(protocol.Argument.ProtocolType));
                // ProtocolId������hashֵ����ʾ����Ҳ���ÿ����Ժ�����û������֡�
                link?.Send(protocol.Argument.ProtocolWholeData);
            }
            return Zeze.Transaction.Procedure.Success;
        }

        protected override async Task<long> ProcessSetUserState(Zeze.Net.Protocol p)
        {
            var protocol = p as SetUserState;
            var socket = LinkdApp.LinkdService.GetSocket(protocol.Argument.LinkSid);
            var linkSession = socket?.UserState as LinkdUserSession;
            linkSession?.SetUserState(protocol.Argument.States, protocol.Argument.Statex);
            return Zeze.Transaction.Procedure.Success;
        }

        protected override async Task<long> ProcessSubscribeRequest(Zeze.Net.Protocol _p)
        {
            var rpc = (Beans.Provider.Subscribe)_p;

            var ps = (LinkdProviderSession)rpc.Sender.UserState;
            foreach (var module in rpc.Argument.Modules)
            {
                var providerModuleState = new ProviderModuleState(ps.SessionId,
                        module.Key, module.Value.ChoiceType, module.Value.ConfigType);
                var serviceName = LinkdApp.LinkdProvider.Distribute.MakeServiceName(ps.Info.ServiceNamePrefix, module.Key);
                var subState = await LinkdApp.Zeze.ServiceManagerAgent.SubscribeService(
                        serviceName, module.Value.SubscribeType, providerModuleState);
                // ���ĳɹ��Ժ󣬽�����Ҫ����ready��service-list��Agentά����
                if (SubscribeInfo.SubscribeTypeReadyCommit == module.Value.SubscribeType)
                    subState.SetServiceIdentityReadyState(ps.Info.ServiceIndentity, providerModuleState);
            }

            rpc.SendResult();
            return 0;
        }

        private void UnBindModules(Zeze.Net.AsyncSocket provider, IEnumerable<int> modules, bool isOnProviderClose = false)
        {
            var ps = provider.UserState as LinkdProviderSession;
            foreach (var moduleId in modules)
            {
                if (false == isOnProviderClose)
                    ps.StaticBinds.TryRemove(moduleId, out var _);
                var serviceName = LinkdApp.LinkdProvider.Distribute.MakeServiceName(ps.Info.ServiceNamePrefix, moduleId);
                if (false == LinkdApp.Zeze.ServiceManagerAgent.SubscribeStates.TryGetValue(
                    serviceName, out var volatileProviders))
                {
                    continue;
                }
                // UnBind ��ɾ��provider-list���������ͨ��ServiceManagerͨ����¡�
                // ����������ø�moduleId��Ӧ�ķ����״̬�����á�
                volatileProviders.SetServiceIdentityReadyState(ps.Info.ServiceIndentity, null);
            }
        }
        protected override async Task<long> ProcessUnBindRequest(Zeze.Net.Protocol p)
        {
            var rpc = p as UnBind;
            if (rpc.Argument.LinkSids.Count == 0)
            {
                UnBindModules(rpc.Sender, rpc.Argument.Modules.Keys);
            }
            else
            {
                // ��̬��
                foreach (var linkSid in rpc.Argument.LinkSids)
                {
                    var link = LinkdApp.LinkdService.GetSocket(linkSid);
                    if (null != link)
                    {
                        var linkSession = link.UserState as LinkdUserSession;
                        linkSession.UnBind(LinkdApp.LinkdProviderService, link, rpc.Argument.Modules.Keys, rpc.Sender);
                    }
                }
            }
            rpc.SendResultCode(BBind.ResultSuccess);
            return Zeze.Transaction.Procedure.Success;
        }


        public bool ChoiceProviderAndBind(int moduleId, Zeze.Net.AsyncSocket link, out long provider)
        {
            var serviceName = LinkdApp.LinkdProvider.Distribute.MakeServiceName(ServerServiceNamePrefix, moduleId);
            var linkSession = link.UserState as LinkdUserSession;

            provider = 0;
            if (false == LinkdApp.Zeze.ServiceManagerAgent.SubscribeStates.TryGetValue(
                serviceName, out var volatileProviders))
                return false;

            // ���ﱣ��� ProviderModuleState �Ǹ�moduleId�ĵ�һ��bind����ȥ����ʱ��¼�����ģ�
            // �����ʹ�������ChoiceType��ConfigType������������������ͬ��moduleId����һ���ġ�
            // �����Ҫĳ��provider.SessionId����Ҫ��ѯ ServiceInfoListSortedByIdentity ���ServiceInfo.LocalState��
            var providerModuleState = volatileProviders.SubscribeInfo.LocalState as ProviderModuleState;

            switch (providerModuleState.ChoiceType)
            {
                case BModule.ChoiceTypeHashAccount:
                    return LinkdApp.LinkdProvider.Distribute.ChoiceHash(volatileProviders, Zeze.Serialize.ByteBuffer.calc_hashnr(linkSession.Account), out provider);

                case BModule.ChoiceTypeHashRoleId:
                    if (linkSession.UserStates.Count > 0)
                    {
                        return LinkdApp.LinkdProvider.Distribute.ChoiceHash(volatileProviders, Zeze.Serialize.ByteBuffer.calc_hashnr(linkSession.UserStates[0]), out provider);
                    }
                    else
                    {
                        return false;
                    }

                case BModule.ChoiceTypeFeedFullOneByOne:
                    return LinkdApp.LinkdProvider.Distribute.ChoiceFeedFullOneByOne(volatileProviders, out provider);
            }

            // default
            if (LinkdApp.LinkdProvider.Distribute.ChoiceLoad(volatileProviders, out provider))
            {
                // ���ﲻ�ж�null�����ʧ�������ѡ��ʧ�ܣ�����ѡ���ˣ���û��Bind�Ժ�����ô�����
                var providerSocket = LinkdApp.LinkdProviderService.GetSocket(provider);
                var providerSession = providerSocket.UserState as LinkdProviderSession;
                linkSession.Bind(LinkdApp.LinkdProviderService, link, providerSession.StaticBinds.Keys, providerSocket);
                return true;
            }

            return false;
        }

        public void OnProviderClose(Zeze.Net.AsyncSocket provider)
        {
            var ps = provider.UserState as LinkdProviderSession;
            if (null == ps)
                return;

            // unbind module
            UnBindModules(provider, ps.StaticBinds.Keys, true);
            ps.StaticBinds.Clear();

            // unbind LinkSession
            lock (ps.LinkSessionIds)
            {
                foreach (var e in ps.LinkSessionIds)
                {
                    foreach (var linkSid in e.Value)
                    {
                        var link = LinkdApp.LinkdService.GetSocket(linkSid);
                        if (null != link)
                        {
                            var linkSession = link.UserState as LinkdUserSession;
                            linkSession?.UnBind(LinkdApp.LinkdProviderService, link, e.Key, provider, true);
                        }
                    }
                }
                ps.LinkSessionIds.Clear();
            }
        }

    }
}