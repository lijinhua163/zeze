package Zeze.Arch;

import Zeze.Net.Protocol;
import Zeze.Transaction.Procedure;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import Zeze.Beans.Provider.*;
import Zeze.Beans.LinkdBase.*;

/**
 * Linkd上处理Provider协议的模块。
 */
public class LinkdProvider extends AbstractProviderLinkd {
    private static final Logger logger = LogManager.getLogger(LinkdProvider.class);

    public LinkdApp LinkdApp;
    public ProviderDistribute Distribute;

    public LinkdProvider() {
    }

    public boolean ChoiceProviderAndBind(int moduleId, Zeze.Net.AsyncSocket link, Zeze.Util.OutObject<Long> provider) {
        var serviceName = Distribute.MakeServiceName(getServerServiceNamePrefix(), moduleId);
        var linkSession = (LinkdUserSession)link.getUserState();
        provider.Value = 0L;
        var volatileProviders = Distribute.Zeze.getServiceManagerAgent().getSubscribeStates().get(serviceName);
        if (null == volatileProviders)
            return false;

        // 这里保存的 ProviderModuleState 是该moduleId的第一个bind请求去订阅时记录下来的，
        // 这里仅使用里面的ChoiceType和ConfigType。这两个参数对于相同的moduleId都是一样的。
        // 如果需要某个provider.SessionId，需要查询 ServiceInfoListSortedByIdentity 里的ServiceInfo.LocalState。
        var providerModuleState = (ProviderModuleState)volatileProviders.getSubscribeInfo().getLocalState();
        switch (providerModuleState.ChoiceType) {
        case BModule.ChoiceTypeHashAccount:
            return Distribute.ChoiceHash(volatileProviders, Zeze.Serialize.ByteBuffer.calc_hashnr(linkSession.getAccount()), provider);

        case BModule.ChoiceTypeHashRoleId:
            if (!linkSession.getUserStates().isEmpty()) {
                return Distribute.ChoiceHash(volatileProviders, Zeze.Serialize.ByteBuffer.calc_hashnr(linkSession.getUserStates().get(0)), provider);
            } else {
                return false;
            }

        case BModule.ChoiceTypeFeedFullOneByOne:
            return Distribute.ChoiceFeedFullOneByOne(volatileProviders, provider);
        }

        // default
        if (Distribute.ChoiceLoad(volatileProviders, provider)) {
            // 这里不判断null，如果失败让这次选择失败，否则选中了，又没有Bind以后更不好处理。
            var providerSocket = LinkdApp.LinkdProviderService.GetSocket(provider.Value);
            var providerSession = (LinkdProviderSession)providerSocket.getUserState();
            linkSession.Bind(LinkdApp.LinkdProviderService, link, providerSession.getStaticBinds().keySet(), providerSocket);
            return true;
        }

        return false;
    }

    public void OnProviderClose(Zeze.Net.AsyncSocket provider) {
        var providerSession = (LinkdProviderSession)provider.getUserState();
        if (null == providerSession) {
            return;
        }

        // unbind module
        UnBindModules(provider, providerSession.getStaticBinds().keySet(), true);
        providerSession.getStaticBinds().clear();

        // unbind LinkSession
        synchronized (providerSession.getLinkSessionIds()) {
            for (var e : providerSession.getLinkSessionIds().entrySet()) {
                for (var linkSid : e.getValue()) {
                    var link = LinkdApp.LinkdService.GetSocket(linkSid);
                    if (null != link) {
                        var linkSession = (LinkdUserSession)link.getUserState();
                        if (linkSession != null) {
                            linkSession.UnBind(LinkdApp.LinkdProviderService, link, e.getKey(), provider, true);
                        }
                    }
                }
            }
            providerSession.getLinkSessionIds().clear();
        }
    }

    private int FirstModuleWithConfigTypeDefault = 0;

    public int getFirstModuleWithConfigTypeDefault() {
        return FirstModuleWithConfigTypeDefault;
    }

    private void setFirstModuleWithConfigTypeDefault(int value) {
        FirstModuleWithConfigTypeDefault = value;
    }

    @Override
    public long ProcessBindRequest(Bind rpc) throws Throwable {
        if (rpc.Argument.getLinkSids().isEmpty()) {
            var providerSession = (LinkdProviderSession)rpc.getSender().getUserState();
            for (var module : rpc.Argument.getModules().entrySet()) {
                if (getFirstModuleWithConfigTypeDefault() == 0 && module.getValue().getConfigType() == BModule.ConfigTypeDefault) {
                    setFirstModuleWithConfigTypeDefault(module.getValue().getConfigType());
                }
                var providerModuleState = new ProviderModuleState(providerSession.getSessionId(),
                        module.getKey(), module.getValue().getChoiceType(), module.getValue().getConfigType());
                var serviceName = Distribute.MakeServiceName(providerSession.getInfo().getServiceNamePrefix(), module.getKey());
                var subState = Distribute.Zeze.getServiceManagerAgent().SubscribeService(
                        serviceName, Zeze.Services.ServiceManager.SubscribeInfo.SubscribeTypeReadyCommit, providerModuleState);
                // 订阅成功以后，仅仅需要设置ready。service-list由Agent维护。
                subState.SetServiceIdentityReadyState(providerSession.getInfo().getServiceIndentity(), providerModuleState);
                providerSession.getStaticBinds().putIfAbsent(module.getKey(), module.getKey());
            }
        } else {
            // 动态绑定
            for (var linkSid : rpc.Argument.getLinkSids()) {
                var link = LinkdApp.LinkdService.GetSocket(linkSid);
                if (null != link) {
                    var linkSession = (LinkdUserSession)link.getUserState();
                    linkSession.Bind(LinkdApp.LinkdProviderService, link, rpc.Argument.getModules().keySet(), rpc.getSender());
                }
            }
        }
        rpc.SendResultCode(BBind.ResultSuccess);
        return Zeze.Transaction.Procedure.Success;
    }

    @Override
    protected long ProcessSubscribeRequest(Subscribe rpc) throws Throwable {

        var providerSession = (LinkdProviderSession)rpc.getSender().getUserState();
        for (var module : rpc.Argument.getModules().entrySet()) {
            var providerModuleState = new ProviderModuleState(providerSession.getSessionId(),
                    module.getKey(), module.getValue().getChoiceType(), module.getValue().getConfigType());
            var serviceName = Distribute.MakeServiceName(providerSession.getInfo().getServiceNamePrefix(), module.getKey());
            var subState = Distribute.Zeze.getServiceManagerAgent().SubscribeService(
                    serviceName, module.getValue().getSubscribeType(), providerModuleState);
            // 订阅成功以后，仅仅需要设置ready。service-list由Agent维护。
            if (Zeze.Services.ServiceManager.SubscribeInfo.SubscribeTypeReadyCommit == module.getValue().getSubscribeType())
                subState.SetServiceIdentityReadyState(providerSession.getInfo().getServiceIndentity(), providerModuleState);
        }

        rpc.SendResult();
        return Procedure.Success;
    }

    private void UnBindModules(Zeze.Net.AsyncSocket provider, java.lang.Iterable<Integer> modules) {
        UnBindModules(provider, modules, false);
    }

    private void UnBindModules(Zeze.Net.AsyncSocket provider, java.lang.Iterable<Integer> modules, boolean isOnProviderClose) {
        var providerSession = (LinkdProviderSession)provider.getUserState();
        for (var moduleId : modules) {
            if (!isOnProviderClose) {
                providerSession.getStaticBinds().remove(moduleId);
            }
            var serviceName = Distribute.MakeServiceName(providerSession.getInfo().getServiceNamePrefix(), moduleId);
            var volatileProviders = Distribute.Zeze.getServiceManagerAgent().getSubscribeStates().get(serviceName);
            if (null == volatileProviders)
                continue;
            // UnBind 不删除provider-list，这个总是通过ServiceManager通告更新。
            // 这里仅仅设置该moduleId对应的服务的状态不可用。
            volatileProviders.SetServiceIdentityReadyState(providerSession.getInfo().getServiceIndentity(), null);
        }
    }

    @Override
    protected long ProcessUnBindRequest(UnBind rpc) {
        if (rpc.Argument.getLinkSids().size() == 0) {
            UnBindModules(rpc.getSender(), rpc.Argument.getModules().keySet());
        } else {
            // 动态绑定
            for (var linkSid : rpc.Argument.getLinkSids()) {
                var link = LinkdApp.LinkdService.GetSocket(linkSid);
                if (null != link) {
                    var linkSession = (LinkdUserSession)link.getUserState();
                    linkSession.UnBind(LinkdApp.LinkdProviderService, link, rpc.Argument.getModules().keySet(), rpc.getSender());
                }
            }
        }
        rpc.SendResultCode(BBind.ResultSuccess);
        return Zeze.Transaction.Procedure.Success;
    }

    @Override
    protected long ProcessSend(Send protocol) {
        // 这个是拿来处理乱序问题的：多个逻辑服务器之间，给客户端发送协议排队。
        // 所以不用等待真正发送给客户端，收到就可以发送结果。
        if (protocol.Argument.getConfirmSerialId() != 0) {
            var confirm = new SendConfirm();
            confirm.Argument.setConfirmSerialId(protocol.Argument.getConfirmSerialId());
            protocol.getSender().Send(confirm);
        }

        for (var linkSid : protocol.Argument.getLinkSids()) {
            var link = LinkdApp.LinkdService.GetSocket(linkSid);
            var ptype = protocol.Argument.getProtocolType();
            logger.debug("Send {} {}", Protocol.GetModuleId(ptype), Protocol.GetProtocolId(ptype));
            // ProtocolId现在是hash值，显示出来也不好看，以后加配置换成名字。
            if (link != null) {
                link.Send(protocol.Argument.getProtocolWholeData());
            }
        }
        return Zeze.Transaction.Procedure.Success;
    }

    @Override
    protected long ProcessBroadcast(Broadcast protocol) throws Throwable {
        if (protocol.Argument.getConfirmSerialId() != 0) {
            var confirm = new SendConfirm();
            confirm.Argument.setConfirmSerialId(protocol.Argument.getConfirmSerialId());
            protocol.getSender().Send(confirm);
        }

        LinkdApp.LinkdService.Foreach((socket) -> {
            // auth 通过就允许发送广播。
            // 如果要实现 role.login 才允许，Provider 增加 SetLogin 协议给内部server调用。
            // 这些广播一般是重要通告，只要登录客户端就允许收到，然后进入世界的时候才显示。这样处理就不用这个状态了。
            var linkSession = (LinkdUserSession)socket.getUserState();
            if (null != linkSession && linkSession.getAccount() == null && !linkSession.getUserStates().isEmpty()) {
                socket.Send(protocol.Argument.getProtocolWholeData());
            }
        });
        return Zeze.Transaction.Procedure.Success;
    }

    @Override
    protected long ProcessKick(Kick protocol) {
        LinkdApp.LinkdService.ReportError(
                protocol.Argument.getLinksid(),
                BReportError.FromProvider,
                protocol.Argument.getCode(),
                protocol.Argument.getDesc());
        return Zeze.Transaction.Procedure.Success;
    }

    @Override
    protected long ProcessSetUserState(SetUserState protocol) {
        var socket = LinkdApp.LinkdService.GetSocket(protocol.Argument.getLinkSid());
        var linkSession = (LinkdUserSession)socket.getUserState();
        if (linkSession != null) {
            linkSession.SetUserState(protocol.Argument.getStates(), protocol.Argument.getStatex());
        }
        return Zeze.Transaction.Procedure.Success;
    }

    // 用于客户端选择Provider，只支持一种Provider。如果要支持多种，需要客户端增加参数，这个不考虑了。
    // 内部的ModuleRedirect ModuleRedirectAll Transmit都携带了ServiceNamePrefix参数，所以，
    // 内部的Provider可以支持完全不同的solution，不过这个仅仅保留给未来扩展用，
    // 不建议在一个项目里面使用多个Prefix。
    private String ServerServiceNamePrefix = "";

    public String getServerServiceNamePrefix() {
        return ServerServiceNamePrefix;
    }

    private void setServerServiceNamePrefix(String value) {
        ServerServiceNamePrefix = value;
    }

    @Override
    protected long ProcessAnnounceProviderInfo(AnnounceProviderInfo protocol) {
        var session = (LinkdProviderSession)protocol.getSender().getUserState();
        session.setInfo(protocol.Argument);
        setServerServiceNamePrefix(protocol.Argument.getServiceNamePrefix());
        return Zeze.Transaction.Procedure.Success;
    }
}