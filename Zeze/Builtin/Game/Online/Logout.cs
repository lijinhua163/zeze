// auto-generated

namespace Zeze.Builtin.Game.Online
{
    public sealed class Logout : Zeze.Net.Rpc<Zeze.Transaction.EmptyBean, Zeze.Transaction.EmptyBean>
    {
        public const int ModuleId_ = 11013;
        public const int ProtocolId_ = -563842687;
        public const long TypeId_ = (long)ModuleId_ << 32 | (ProtocolId_ & 0xffff_ffff);

        public override int ModuleId => ModuleId_;
        public override int ProtocolId => ProtocolId_;
    }
}