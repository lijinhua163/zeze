// auto-generated @formatter:off
package Zeze.Beans.ProviderDirect;

public class ModuleRedirect extends Zeze.Net.Rpc<Zeze.Beans.ProviderDirect.BModuleRedirectArgument, Zeze.Beans.ProviderDirect.BModuleRedirectResult> {
    public static final int ModuleId_ = 11009;
    public static final int ProtocolId_ = -881551061;
    public static final long TypeId_ = Zeze.Net.Protocol.MakeTypeId(ModuleId_, ProtocolId_);

    @Override
    public int getModuleId() {
        return ModuleId_;
    }

    @Override
    public int getProtocolId() {
        return ProtocolId_;
    }

    public static final int RedirectTypeWithHash = 0;
    public static final int RedirectTypeToServer = 1;
    public static final int ResultCodeSuccess = 0;
    public static final int ResultCodeMethodFullNameNotFound = 1;
    public static final int ResultCodeHandleException = 2;
    public static final int ResultCodeHandleError = 3;
    public static final int ResultCodeLinkdTimeout = 10;
    public static final int ResultCodeLinkdNoProvider = 11;
    public static final int ResultCodeRequestTimeout = 12;

    public ModuleRedirect() {
        Argument = new Zeze.Beans.ProviderDirect.BModuleRedirectArgument();
        Result = new Zeze.Beans.ProviderDirect.BModuleRedirectResult();
    }
}