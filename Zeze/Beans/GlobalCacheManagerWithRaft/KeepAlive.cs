// auto-generated

namespace Zeze.Beans.GlobalCacheManagerWithRaft
{
    public sealed class KeepAlive : Zeze.Raft.RaftRpc<Zeze.Transaction.EmptyBean, Zeze.Transaction.EmptyBean>
    {
        public const int ModuleId_ = 11001;
        public const int ProtocolId_ = 1204080176;
        public const long TypeId_ = (long)ModuleId_ << 32 | (ProtocolId_ & 0xffff_ffff);

        public override int ModuleId => ModuleId_;
        public override int ProtocolId => ProtocolId_;
    }
}