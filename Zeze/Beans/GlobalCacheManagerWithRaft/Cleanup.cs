// auto-generated

namespace Zeze.Beans.GlobalCacheManagerWithRaft
{
    public sealed class Cleanup : Zeze.Raft.RaftRpc<Zeze.Beans.GlobalCacheManagerWithRaft.AchillesHeel, Zeze.Transaction.EmptyBean>
    {
        public const int ModuleId_ = 11001;
        public const int ProtocolId_ = 754579307;
        public const long TypeId_ = (long)ModuleId_ << 32 | (ProtocolId_ & 0xffff_ffff);

        public override int ModuleId => ModuleId_;
        public override int ProtocolId => ProtocolId_;
    }
}