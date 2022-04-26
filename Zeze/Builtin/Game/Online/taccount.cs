// auto-generated
using Zeze.Serialize;

namespace Zeze.Builtin.Game.Online
{
    public sealed class taccount : Zeze.Transaction.Table<string, Zeze.Builtin.Game.Online.BAccount>
    {
        public taccount() : base("Zeze_Builtin_Game_Online_taccount")
        {
        }

        public override bool IsMemory => false;
        public override bool IsAutoKey => false;

        public const int VAR_All = 0;
        public const int VAR_Name = 1;
        public const int VAR_Roles = 2;
        public const int VAR_LastLoginRoleId = 3;
        public const int VAR_LastLoginVersion = 4;

        public override string DecodeKey(ByteBuffer _os_)
        {
            string _v_;
            _v_ = _os_.ReadString();
            return _v_;
        }

        public override ByteBuffer EncodeKey(string _v_)
        {
            ByteBuffer _os_ = ByteBuffer.Allocate();
            _os_.WriteString(_v_);
            return _os_;
        }

        public override Zeze.Transaction.ChangeVariableCollector CreateChangeVariableCollector(int variableId)
        {
            return variableId switch
            {
                0 => new Zeze.Transaction.ChangeVariableCollectorChanged(),
                1 => new Zeze.Transaction.ChangeVariableCollectorChanged(),
                2 => new Zeze.Transaction.ChangeVariableCollectorChanged(),
                3 => new Zeze.Transaction.ChangeVariableCollectorChanged(),
                4 => new Zeze.Transaction.ChangeVariableCollectorChanged(),
                _ => null,
            };
        }
    }
}
