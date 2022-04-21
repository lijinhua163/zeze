// auto-generated
using Zeze.Serialize;

namespace Zeze.Builtin.Collections.Queue
{
    public sealed class tQueueNodes : Zeze.Transaction.Table<Zeze.Builtin.Collections.Queue.BQueueNodeKey, Zeze.Builtin.Collections.Queue.BQueueNode>
    {
        public tQueueNodes() : base("Zeze_Builtin_Collections_Queue_tQueueNodes")
        {
        }

        public override bool IsMemory => false;
        public override bool IsAutoKey => false;

        public const int VAR_All = 0;
        public const int VAR_NextNodeId = 1;
        public const int VAR_Values = 2;

        public override Zeze.Builtin.Collections.Queue.BQueueNodeKey DecodeKey(ByteBuffer _os_)
        {
            Zeze.Builtin.Collections.Queue.BQueueNodeKey _v_ = new Zeze.Builtin.Collections.Queue.BQueueNodeKey();
            _v_.Decode(_os_);
            return _v_;
        }

        public override ByteBuffer EncodeKey(Zeze.Builtin.Collections.Queue.BQueueNodeKey _v_)
        {
            ByteBuffer _os_ = ByteBuffer.Allocate();
            _v_.Encode(_os_);
            return _os_;
        }

        public override Zeze.Transaction.ChangeVariableCollector CreateChangeVariableCollector(int variableId)
        {
            return variableId switch
            {
                0 => new Zeze.Transaction.ChangeVariableCollectorChanged(),
                1 => new Zeze.Transaction.ChangeVariableCollectorChanged(),
                2 => new Zeze.Transaction.ChangeVariableCollectorChanged(),
                _ => null,
            };
        }
    }
}