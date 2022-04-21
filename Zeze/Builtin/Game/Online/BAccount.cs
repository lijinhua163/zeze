// auto-generated
using ByteBuffer = Zeze.Serialize.ByteBuffer;
using Environment = System.Environment;

namespace Zeze.Builtin.Game.Online
{
    public interface BAccountReadOnly
    {
        public long TypeId { get; }
        public void Encode(ByteBuffer _os_);
        public bool NegativeCheck();
        public Zeze.Transaction.Bean CopyBean();

        public string Name { get; }
        public System.Collections.Generic.IReadOnlyList<long>Roles { get; }
        public long LastLoginRoleId { get; }
    }

    public sealed class BAccount : Zeze.Transaction.Bean, BAccountReadOnly
    {
        string _Name;
        readonly Zeze.Transaction.Collections.PList1<long> _Roles; // roleid list
        long _LastLoginRoleId;

        public string Name
        {
            get
            {
                if (!IsManaged)
                    return _Name;
                var txn = Zeze.Transaction.Transaction.Current;
                if (txn == null) return _Name;
                txn.VerifyRecordAccessed(this, true);
                var log = (Log__Name)txn.GetLog(ObjectId + 1);
                return log != null ? log.Value : _Name;
            }
            set
            {
                if (value == null) throw new System.ArgumentNullException();
                if (!IsManaged)
                {
                    _Name = value;
                    return;
                }
                var txn = Zeze.Transaction.Transaction.Current;
                txn.VerifyRecordAccessed(this);
                txn.PutLog(new Log__Name(this, value));
            }
        }

        public Zeze.Transaction.Collections.PList1<long> Roles => _Roles;
        System.Collections.Generic.IReadOnlyList<long> Zeze.Builtin.Game.Online.BAccountReadOnly.Roles => _Roles;

        public long LastLoginRoleId
        {
            get
            {
                if (!IsManaged)
                    return _LastLoginRoleId;
                var txn = Zeze.Transaction.Transaction.Current;
                if (txn == null) return _LastLoginRoleId;
                txn.VerifyRecordAccessed(this, true);
                var log = (Log__LastLoginRoleId)txn.GetLog(ObjectId + 3);
                return log != null ? log.Value : _LastLoginRoleId;
            }
            set
            {
                if (!IsManaged)
                {
                    _LastLoginRoleId = value;
                    return;
                }
                var txn = Zeze.Transaction.Transaction.Current;
                txn.VerifyRecordAccessed(this);
                txn.PutLog(new Log__LastLoginRoleId(this, value));
            }
        }

        public BAccount() : this(0)
        {
        }

        public BAccount(int _varId_) : base(_varId_)
        {
            _Name = "";
            _Roles = new Zeze.Transaction.Collections.PList1<long>(ObjectId + 2, _v => new Log__Roles(this, _v));
        }

        public void Assign(BAccount other)
        {
            Name = other.Name;
            Roles.Clear();
            foreach (var e in other.Roles)
                Roles.Add(e);
            LastLoginRoleId = other.LastLoginRoleId;
        }

        public BAccount CopyIfManaged()
        {
            return IsManaged ? Copy() : this;
        }

        public BAccount Copy()
        {
            var copy = new BAccount();
            copy.Assign(this);
            return copy;
        }

        public static void Swap(BAccount a, BAccount b)
        {
            BAccount save = a.Copy();
            a.Assign(b);
            b.Assign(save);
        }

        public override Zeze.Transaction.Bean CopyBean()
        {
            return Copy();
        }

        public const long TYPEID = -6071732171172452068;
        public override long TypeId => TYPEID;

        sealed class Log__Name : Zeze.Transaction.Log<BAccount, string>
        {
            public Log__Name(BAccount self, string value) : base(self, value) {}
            public override long LogKey => this.Bean.ObjectId + 1;
            public override void Commit() { this.BeanTyped._Name = this.Value; }
        }

        sealed class Log__Roles : Zeze.Transaction.Collections.PList1<long>.LogV
        {
            public Log__Roles(BAccount host, System.Collections.Immutable.ImmutableList<long> value) : base(host, value) {}
            public override long LogKey => Bean.ObjectId + 2;
            public BAccount BeanTyped => (BAccount)Bean;
            public override void Commit() { Commit(BeanTyped._Roles); }
        }

        sealed class Log__LastLoginRoleId : Zeze.Transaction.Log<BAccount, long>
        {
            public Log__LastLoginRoleId(BAccount self, long value) : base(self, value) {}
            public override long LogKey => this.Bean.ObjectId + 3;
            public override void Commit() { this.BeanTyped._LastLoginRoleId = this.Value; }
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
            sb.Append(Zeze.Util.Str.Indent(level)).Append("Zeze.Builtin.Game.Online.BAccount: {").Append(Environment.NewLine);
            level += 4;
            sb.Append(Zeze.Util.Str.Indent(level)).Append("Name").Append('=').Append(Name).Append(',').Append(Environment.NewLine);
            sb.Append(Zeze.Util.Str.Indent(level)).Append("Roles").Append("=[").Append(Environment.NewLine);
            level += 4;
            foreach (var Item in Roles)
            {
                sb.Append(Zeze.Util.Str.Indent(level)).Append("Item").Append('=').Append(Item).Append(',').Append(Environment.NewLine);
            }
            level -= 4;
            sb.Append(Zeze.Util.Str.Indent(level)).Append(']').Append(',').Append(Environment.NewLine);
            sb.Append(Zeze.Util.Str.Indent(level)).Append("LastLoginRoleId").Append('=').Append(LastLoginRoleId).Append(Environment.NewLine);
            level -= 4;
            sb.Append(Zeze.Util.Str.Indent(level)).Append('}');
        }

        public override void Encode(ByteBuffer _o_)
        {
            int _i_ = 0;
            {
                string _x_ = Name;
                if (_x_.Length != 0)
                {
                    _i_ = _o_.WriteTag(_i_, 1, ByteBuffer.BYTES);
                    _o_.WriteString(_x_);
                }
            }
            {
                var _x_ = Roles;
                int _n_ = _x_.Count;
                if (_n_ != 0)
                {
                    _i_ = _o_.WriteTag(_i_, 2, ByteBuffer.LIST);
                    _o_.WriteListType(_n_, ByteBuffer.INTEGER);
                    foreach (var _v_ in _x_)
                        _o_.WriteLong(_v_);
                }
            }
            {
                long _x_ = LastLoginRoleId;
                if (_x_ != 0)
                {
                    _i_ = _o_.WriteTag(_i_, 3, ByteBuffer.INTEGER);
                    _o_.WriteLong(_x_);
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
                Name = _o_.ReadString(_t_);
                _i_ += _o_.ReadTagSize(_t_ = _o_.ReadByte());
            }
            if (_i_ == 2)
            {
                var _x_ = Roles;
                _x_.Clear();
                if ((_t_ & ByteBuffer.TAG_MASK) == ByteBuffer.LIST)
                {
                    for (int _n_ = _o_.ReadTagSize(_t_ = _o_.ReadByte()); _n_ > 0; _n_--)
                        _x_.Add(_o_.ReadLong(_t_));
                }
                else
                    _o_.SkipUnknownField(_t_);
                _i_ += _o_.ReadTagSize(_t_ = _o_.ReadByte());
            }
            if (_i_ == 3)
            {
                LastLoginRoleId = _o_.ReadLong(_t_);
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
            _Roles.InitRootInfo(root, this);
        }

        public override bool NegativeCheck()
        {
            foreach (var _v_ in Roles)
            {
                if (_v_ < 0) return true;
            }
            if (LastLoginRoleId < 0) return true;
            return false;
        }
    }
}