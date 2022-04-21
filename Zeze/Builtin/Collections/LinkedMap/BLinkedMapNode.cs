// auto-generated
using ByteBuffer = Zeze.Serialize.ByteBuffer;
using Environment = System.Environment;

namespace Zeze.Builtin.Collections.LinkedMap
{
    public interface BLinkedMapNodeReadOnly
    {
        public long TypeId { get; }
        public void Encode(ByteBuffer _os_);
        public bool NegativeCheck();
        public Zeze.Transaction.Bean CopyBean();

        public long PrevNodeId { get; }
        public long NextNodeId { get; }
        public System.Collections.Generic.IReadOnlyList<Zeze.Builtin.Collections.LinkedMap.BLinkedMapNodeValueReadOnly>Values { get; }
    }

    public sealed class BLinkedMapNode : Zeze.Transaction.Bean, BLinkedMapNodeReadOnly
    {
        long _PrevNodeId; // 前一个节点ID. 0表示已到达开头。
        long _NextNodeId; // 后一个节点ID. 0表示已到达结尾。
        readonly Zeze.Transaction.Collections.PList2<Zeze.Builtin.Collections.LinkedMap.BLinkedMapNodeValue> _Values; // 多个KeyValue对,容量由LinkedMap构造时的nodeSize决定

        public long PrevNodeId
        {
            get
            {
                if (!IsManaged)
                    return _PrevNodeId;
                var txn = Zeze.Transaction.Transaction.Current;
                if (txn == null) return _PrevNodeId;
                txn.VerifyRecordAccessed(this, true);
                var log = (Log__PrevNodeId)txn.GetLog(ObjectId + 1);
                return log != null ? log.Value : _PrevNodeId;
            }
            set
            {
                if (!IsManaged)
                {
                    _PrevNodeId = value;
                    return;
                }
                var txn = Zeze.Transaction.Transaction.Current;
                txn.VerifyRecordAccessed(this);
                txn.PutLog(new Log__PrevNodeId(this, value));
            }
        }

        public long NextNodeId
        {
            get
            {
                if (!IsManaged)
                    return _NextNodeId;
                var txn = Zeze.Transaction.Transaction.Current;
                if (txn == null) return _NextNodeId;
                txn.VerifyRecordAccessed(this, true);
                var log = (Log__NextNodeId)txn.GetLog(ObjectId + 2);
                return log != null ? log.Value : _NextNodeId;
            }
            set
            {
                if (!IsManaged)
                {
                    _NextNodeId = value;
                    return;
                }
                var txn = Zeze.Transaction.Transaction.Current;
                txn.VerifyRecordAccessed(this);
                txn.PutLog(new Log__NextNodeId(this, value));
            }
        }

        public Zeze.Transaction.Collections.PList2<Zeze.Builtin.Collections.LinkedMap.BLinkedMapNodeValue> Values => _Values;
        System.Collections.Generic.IReadOnlyList<Zeze.Builtin.Collections.LinkedMap.BLinkedMapNodeValueReadOnly> Zeze.Builtin.Collections.LinkedMap.BLinkedMapNodeReadOnly.Values => _Values;

        public BLinkedMapNode() : this(0)
        {
        }

        public BLinkedMapNode(int _varId_) : base(_varId_)
        {
            _Values = new Zeze.Transaction.Collections.PList2<Zeze.Builtin.Collections.LinkedMap.BLinkedMapNodeValue>(ObjectId + 3, _v => new Log__Values(this, _v));
        }

        public void Assign(BLinkedMapNode other)
        {
            PrevNodeId = other.PrevNodeId;
            NextNodeId = other.NextNodeId;
            Values.Clear();
            foreach (var e in other.Values)
                Values.Add(e.Copy());
        }

        public BLinkedMapNode CopyIfManaged()
        {
            return IsManaged ? Copy() : this;
        }

        public BLinkedMapNode Copy()
        {
            var copy = new BLinkedMapNode();
            copy.Assign(this);
            return copy;
        }

        public static void Swap(BLinkedMapNode a, BLinkedMapNode b)
        {
            BLinkedMapNode save = a.Copy();
            a.Assign(b);
            b.Assign(save);
        }

        public override Zeze.Transaction.Bean CopyBean()
        {
            return Copy();
        }

        public const long TYPEID = 3432187612551867839;
        public override long TypeId => TYPEID;

        sealed class Log__PrevNodeId : Zeze.Transaction.Log<BLinkedMapNode, long>
        {
            public Log__PrevNodeId(BLinkedMapNode self, long value) : base(self, value) {}
            public override long LogKey => this.Bean.ObjectId + 1;
            public override void Commit() { this.BeanTyped._PrevNodeId = this.Value; }
        }

        sealed class Log__NextNodeId : Zeze.Transaction.Log<BLinkedMapNode, long>
        {
            public Log__NextNodeId(BLinkedMapNode self, long value) : base(self, value) {}
            public override long LogKey => this.Bean.ObjectId + 2;
            public override void Commit() { this.BeanTyped._NextNodeId = this.Value; }
        }

        sealed class Log__Values : Zeze.Transaction.Collections.PList2<Zeze.Builtin.Collections.LinkedMap.BLinkedMapNodeValue>.LogV
        {
            public Log__Values(BLinkedMapNode host, System.Collections.Immutable.ImmutableList<Zeze.Builtin.Collections.LinkedMap.BLinkedMapNodeValue> value) : base(host, value) {}
            public override long LogKey => Bean.ObjectId + 3;
            public BLinkedMapNode BeanTyped => (BLinkedMapNode)Bean;
            public override void Commit() { Commit(BeanTyped._Values); }
        }

        public override string ToString()
        {
            System.Text.StringBuilder sb = new System.Text.StringBuilder();
            BuildString(sb, 0);
            sb.Append(Environment.NewLine);
            return sb.ToString();
        }

        public override void BuildString(System.Text.StringBuilder sb, int level)
        {
            sb.Append(Zeze.Util.Str.Indent(level)).Append("Zeze.Builtin.Collections.LinkedMap.BLinkedMapNode: {").Append(Environment.NewLine);
            level += 4;
            sb.Append(Zeze.Util.Str.Indent(level)).Append("PrevNodeId").Append('=').Append(PrevNodeId).Append(',').Append(Environment.NewLine);
            sb.Append(Zeze.Util.Str.Indent(level)).Append("NextNodeId").Append('=').Append(NextNodeId).Append(',').Append(Environment.NewLine);
            sb.Append(Zeze.Util.Str.Indent(level)).Append("Values").Append("=[").Append(Environment.NewLine);
            level += 4;
            foreach (var Item in Values)
            {
                sb.Append(Zeze.Util.Str.Indent(level)).Append("Item").Append('=').Append(Environment.NewLine);
                Item.BuildString(sb, level + 4);
                sb.Append(',').Append(Environment.NewLine);
            }
            level -= 4;
            sb.Append(Zeze.Util.Str.Indent(level)).Append(']').Append(Environment.NewLine);
            level -= 4;
            sb.Append(Zeze.Util.Str.Indent(level)).Append('}');
        }

        public override void Encode(ByteBuffer _o_)
        {
            int _i_ = 0;
            {
                long _x_ = PrevNodeId;
                if (_x_ != 0)
                {
                    _i_ = _o_.WriteTag(_i_, 1, ByteBuffer.INTEGER);
                    _o_.WriteLong(_x_);
                }
            }
            {
                long _x_ = NextNodeId;
                if (_x_ != 0)
                {
                    _i_ = _o_.WriteTag(_i_, 2, ByteBuffer.INTEGER);
                    _o_.WriteLong(_x_);
                }
            }
            {
                var _x_ = Values;
                int _n_ = _x_.Count;
                if (_n_ != 0)
                {
                    _i_ = _o_.WriteTag(_i_, 3, ByteBuffer.LIST);
                    _o_.WriteListType(_n_, ByteBuffer.BEAN);
                    foreach (var _v_ in _x_)
                        _v_.Encode(_o_);
                }
            }
            _o_.WriteByte(0);
        }

        public override void Decode(ByteBuffer _o_)
        {
            int _t_ = _o_.ReadByte();
            int _i_ = _o_.ReadTagSize(_t_);
            if (_i_ == 1)
            {
                PrevNodeId = _o_.ReadLong(_t_);
                _i_ += _o_.ReadTagSize(_t_ = _o_.ReadByte());
            }
            if (_i_ == 2)
            {
                NextNodeId = _o_.ReadLong(_t_);
                _i_ += _o_.ReadTagSize(_t_ = _o_.ReadByte());
            }
            if (_i_ == 3)
            {
                var _x_ = Values;
                _x_.Clear();
                if ((_t_ & ByteBuffer.TAG_MASK) == ByteBuffer.LIST)
                {
                    for (int _n_ = _o_.ReadTagSize(_t_ = _o_.ReadByte()); _n_ > 0; _n_--)
                        _x_.Add(_o_.ReadBean(new Zeze.Builtin.Collections.LinkedMap.BLinkedMapNodeValue(), _t_));
                }
                else
                    _o_.SkipUnknownField(_t_);
                _i_ += _o_.ReadTagSize(_t_ = _o_.ReadByte());
            }
            while (_t_ != 0)
            {
                _o_.SkipUnknownField(_t_);
                _o_.ReadTagSize(_t_ = _o_.ReadByte());
            }
        }

        protected override void InitChildrenRootInfo(Zeze.Transaction.Record.RootInfo root)
        {
            _Values.InitRootInfo(root, this);
        }

        public override bool NegativeCheck()
        {
            if (PrevNodeId < 0) return true;
            if (NextNodeId < 0) return true;
            return false;
        }
    }
}