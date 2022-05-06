// auto-generated
using ByteBuffer = Zeze.Serialize.ByteBuffer;
using Environment = System.Environment;

namespace Zeze.Builtin.Game.Bag
{
    public interface BBagReadOnly
    {
        public long TypeId { get; }
        public void Encode(ByteBuffer _os_);
        public bool NegativeCheck();
        public Zeze.Transaction.Bean CopyBean();

        public int Capacity { get; }
        public System.Collections.Generic.IReadOnlyDictionary<int,Zeze.Builtin.Game.Bag.BItemReadOnly> Items { get; }
    }

    public sealed class BBag : Zeze.Transaction.Bean, BBagReadOnly
    {
        int _Capacity;
        readonly Zeze.Transaction.Collections.PMap2<int, Zeze.Builtin.Game.Bag.BItem> _Items; // key is bag position
        Zeze.Transaction.Collections.PMapReadOnly<int,Zeze.Builtin.Game.Bag.BItemReadOnly,Zeze.Builtin.Game.Bag.BItem> _ItemsReadOnly;

        public int Capacity
        {
            get
            {
                if (!IsManaged)
                    return _Capacity;
                var txn = Zeze.Transaction.Transaction.Current;
                if (txn == null) return _Capacity;
                txn.VerifyRecordAccessed(this, true);
                var log = (Log__Capacity)txn.GetLog(ObjectId + 1);
                return log != null ? log.Value : _Capacity;
            }
            set
            {
                if (!IsManaged)
                {
                    _Capacity = value;
                    return;
                }
                var txn = Zeze.Transaction.Transaction.Current;
                txn.VerifyRecordAccessed(this);
                txn.PutLog(new Log__Capacity(this, value));
            }
        }

        public Zeze.Transaction.Collections.PMap2<int, Zeze.Builtin.Game.Bag.BItem> Items => _Items;
        System.Collections.Generic.IReadOnlyDictionary<int,Zeze.Builtin.Game.Bag.BItemReadOnly> Zeze.Builtin.Game.Bag.BBagReadOnly.Items => _ItemsReadOnly;

        public BBag() : this(0)
        {
        }

        public BBag(int _varId_) : base(_varId_)
        {
            _Items = new Zeze.Transaction.Collections.PMap2<int, Zeze.Builtin.Game.Bag.BItem>(ObjectId + 2, _v => new Log__Items(this, _v));
            _ItemsReadOnly = new Zeze.Transaction.Collections.PMapReadOnly<int,Zeze.Builtin.Game.Bag.BItemReadOnly,Zeze.Builtin.Game.Bag.BItem>(_Items);
        }

        public void Assign(BBag other)
        {
            Capacity = other.Capacity;
            Items.Clear();
            foreach (var e in other.Items)
                Items.Add(e.Key, e.Value.Copy());
        }

        public BBag CopyIfManaged()
        {
            return IsManaged ? Copy() : this;
        }

        public BBag Copy()
        {
            var copy = new BBag();
            copy.Assign(this);
            return copy;
        }

        public static void Swap(BBag a, BBag b)
        {
            BBag save = a.Copy();
            a.Assign(b);
            b.Assign(save);
        }

        public override Zeze.Transaction.Bean CopyBean()
        {
            return Copy();
        }

        public const long TYPEID = -5051317137860806350;
        public override long TypeId => TYPEID;

        sealed class Log__Capacity : Zeze.Transaction.Log<BBag, int>
        {
            public Log__Capacity(BBag self, int value) : base(self, value) {}
            public override long LogKey => this.Belong.ObjectId + 1;
            public override void Commit() { this.BeanTyped._Capacity = this.Value; }
        }

        sealed class Log__Items : Zeze.Transaction.Collections.PMap2<int, Zeze.Builtin.Game.Bag.BItem>.LogV
        {
            public Log__Items(BBag host, System.Collections.Immutable.ImmutableDictionary<int, Zeze.Builtin.Game.Bag.BItem> value) : base(host, value) {}
            public override long LogKey => Belong.ObjectId + 2;
            public BBag BeanTyped => (BBag)Belong;
            public override void Commit() { Commit(BeanTyped._Items); }
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
            sb.Append(Zeze.Util.Str.Indent(level)).Append("Zeze.Builtin.Game.Bag.BBag: {").Append(Environment.NewLine);
            level += 4;
            sb.Append(Zeze.Util.Str.Indent(level)).Append("Capacity").Append('=').Append(Capacity).Append(',').Append(Environment.NewLine);
            sb.Append(Zeze.Util.Str.Indent(level)).Append("Items").Append("=[").Append(Environment.NewLine);
            level += 4;
            foreach (var _kv_ in Items)
            {
                sb.Append(Zeze.Util.Str.Indent(level)).Append('(').Append(Environment.NewLine);
                var Key = _kv_.Key;
                sb.Append(Zeze.Util.Str.Indent(level)).Append("Key").Append('=').Append(Key).Append(',').Append(Environment.NewLine);
                var Value = _kv_.Value;
                sb.Append(Zeze.Util.Str.Indent(level)).Append("Value").Append('=').Append(Environment.NewLine);
                Value.BuildString(sb, level + 4);
                sb.Append(',').Append(Environment.NewLine);
                sb.Append(Zeze.Util.Str.Indent(level)).Append(')').Append(Environment.NewLine);
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
                int _x_ = Capacity;
                if (_x_ != 0)
                {
                    _i_ = _o_.WriteTag(_i_, 1, ByteBuffer.INTEGER);
                    _o_.WriteInt(_x_);
                }
            }
            {
                var _x_ = Items;
                int _n_ = _x_.Count;
                if (_n_ != 0)
                {
                    _i_ = _o_.WriteTag(_i_, 2, ByteBuffer.MAP);
                    _o_.WriteMapType(_n_, ByteBuffer.INTEGER, ByteBuffer.BEAN);
                    foreach (var _e_ in _x_)
                    {
                        _o_.WriteLong(_e_.Key);
                        _e_.Value.Encode(_o_);
                    }
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
                Capacity = _o_.ReadInt(_t_);
                _i_ += _o_.ReadTagSize(_t_ = _o_.ReadByte());
            }
            if (_i_ == 2)
            {
                var _x_ = Items;
                _x_.Clear();
                if ((_t_ & ByteBuffer.TAG_MASK) == ByteBuffer.MAP)
                {
                    int _s_ = (_t_ = _o_.ReadByte()) >> ByteBuffer.TAG_SHIFT;
                    for (int _n_ = _o_.ReadUInt(); _n_ > 0; _n_--)
                    {
                        var _k_ = _o_.ReadInt(_s_);
                        var _v_ = _o_.ReadBean(new Zeze.Builtin.Game.Bag.BItem(), _t_);
                        _x_.Add(_k_, _v_);
                    }
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
            _Items.InitRootInfo(root, this);
        }

        public override bool NegativeCheck()
        {
            if (Capacity < 0) return true;
            foreach (var _v_ in Items.Values)
            {
                if (_v_.NegativeCheck()) return true;
            }
            return false;
        }
    }
}
