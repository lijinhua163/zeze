// auto-generated @formatter:off
package Zeze.Builtin.Provider;

public class UnBind extends Zeze.Net.Rpc<Zeze.Builtin.Provider.BBind, Zeze.Transaction.EmptyBean> {
    public static final int ModuleId_ = 11008;
    public static final int ProtocolId_ = 2107584596;
    public static final long TypeId_ = Zeze.Net.Protocol.MakeTypeId(ModuleId_, ProtocolId_); // 47281107578964

    @Override
    public int getModuleId() {
        return ModuleId_;
    }

    @Override
    public int getProtocolId() {
        return ProtocolId_;
    }

    public UnBind() {
        Argument = new Zeze.Builtin.Provider.BBind();
        Result = new Zeze.Transaction.EmptyBean();
    }
}
