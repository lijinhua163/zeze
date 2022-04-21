// auto-generated
using Zeze.Serialize;

namespace Zeze.Builtin.RedoQueue
{
    public sealed class tQueueLastTaskId : Zeze.Transaction.Table<string, Zeze.Builtin.RedoQueue.BTaskId>
    {
        public tQueueLastTaskId() : base("Zeze_Builtin_RedoQueue_tQueueLastTaskId")
        {
        }

        public override bool IsMemory => false;
        public override bool IsAutoKey => false;

        public const int VAR_All = 0;
        public const int VAR_TaskId = 1;

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
                _ => null,
            };
        }
    }
}