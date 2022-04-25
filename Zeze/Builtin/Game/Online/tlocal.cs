// auto-generated
using Zeze.Serialize;

namespace Zeze.Builtin.Game.Online
{
    public sealed class tlocal : Zeze.Transaction.Table<long, Zeze.Builtin.Game.Online.BLocal>
    {
        public tlocal() : base("Zeze_Builtin_Game_Online_tlocal")
        {
        }

        public override bool IsMemory => true;
        public override bool IsAutoKey => false;

        public const int VAR_All = 0;
        public const int VAR_Datas = 1;

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

        public override Zeze.Transaction.ChangeVariableCollector CreateChangeVariableCollector(int variableId)
        {
            return variableId switch
            {
                0 => new Zeze.Transaction.ChangeVariableCollectorChanged(),
                1 => new Zeze.Transaction.ChangeVariableCollectorMap(() => new Zeze.Transaction.ChangeNoteMap2<int, Zeze.Builtin.Game.Online.BAny>(null)),
                _ => null,
            };
        }
    }
}