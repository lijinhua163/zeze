// auto-generated
using Zeze.Serialize;

namespace Zeze.Builtin.Game.Online
{
    public sealed class tversion : Zeze.Transaction.Table<long, Zeze.Builtin.Game.Online.BVersion>
    {
        public tversion() : base("Zeze_Builtin_Game_Online_tversion")
        {
        }

        public override int Id => -1673876055;
        public override bool IsMemory => false;
        public override bool IsAutoKey => false;

        public const int VAR_LoginVersion = 1;
        public const int VAR_ReliableNotifyMark = 2;
        public const int VAR_ReliableNotifyConfirmIndex = 3;
        public const int VAR_ReliableNotifyIndex = 4;
        public const int VAR_ServerId = 5;

        public override long DecodeKey(ByteBuffer _os_)
        {
            long _v_;
            _v_ = _os_.ReadLong();
            return _v_;
        }

        public override ByteBuffer EncodeKey(long _v_)
        {
            ByteBuffer _os_ = ByteBuffer.Allocate();
            _os_.WriteLong(_v_);
            return _os_;
        }
    }
}
