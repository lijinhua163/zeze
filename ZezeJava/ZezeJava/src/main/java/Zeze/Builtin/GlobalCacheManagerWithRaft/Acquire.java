// auto-generated @formatter:off
package Zeze.Builtin.GlobalCacheManagerWithRaft;

public class Acquire extends Zeze.Raft.RaftRpc<Zeze.Builtin.GlobalCacheManagerWithRaft.AcquireParam, Zeze.Builtin.GlobalCacheManagerWithRaft.ReduceParam> {
    public static final int ModuleId_ = 11001;
    public static final int ProtocolId_ = -1825434690; // 2469532606
    public static final long TypeId_ = Zeze.Net.Protocol.MakeTypeId(ModuleId_, ProtocolId_); // 47251404755902

    @Override
    public int getModuleId() {
        return ModuleId_;
    }

    @Override
    public int getProtocolId() {
        return ProtocolId_;
    }

    public Acquire() {
        Argument = new Zeze.Builtin.GlobalCacheManagerWithRaft.AcquireParam();
        Result = new Zeze.Builtin.GlobalCacheManagerWithRaft.ReduceParam();
    }
}
