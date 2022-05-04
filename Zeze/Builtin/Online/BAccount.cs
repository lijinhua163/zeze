// auto-generated
using ByteBuffer = Zeze.Serialize.ByteBuffer;
using Environment = System.Environment;

namespace Zeze.Builtin.Online
{
    public interface BAccountReadOnly
    {
        public long TypeId { get; }
        public void Encode(ByteBuffer _os_);
        public bool NegativeCheck();
        public Zeze.Transaction.Bean CopyBean();

        public long LastLoginVersion { get; }
    }

    public sealed class BAccount : Zeze.Transaction.Bean, BAccountReadOnly
    {
        long _LastLoginVersion; // 用来生成 role 登录版本号。每次递增。

        public long LastLoginVersion
        {
            get
            {
                if (!IsManaged)
                    return _LastLoginVersion;
                var txn = Zeze.Transaction.Transaction.Current;
                if (txn == null) return _LastLoginVersion;
                txn.VerifyRecordAccessed(this, true);
                var log = (Log__LastLoginVersion)txn.GetLog(ObjectId + 1);
                return log != null ? log.Value : _LastLoginVersion;
            }
            set
            {
                if (!IsManaged)
                {
                    _LastLoginVersion = value;
                    return;
                }
                var txn = Zeze.Transaction.Transaction.Current;
                txn.VerifyRecordAccessed(this);
                txn.PutLog(new Log__LastLoginVersion(this, value));
            }
        }

        public BAccount() : this(0)
        {
        }

        public BAccount(int _varId_) : base(_varId_)
        {
        }

        public void Assign(BAccount other)
        {
            LastLoginVersion = other.LastLoginVersion;
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

        public const long TYPEID = 3220082739597459764;
        public override long TypeId => TYPEID;

        sealed class Log__LastLoginVersion : Zeze.Transaction.Log<BAccount, long>
        {
            public Log__LastLoginVersion(BAccount self, long value) : base(self, value) {}
            public override long LogKey => this.Bean.ObjectId + 1;
            public override void Commit() { this.BeanTyped._LastLoginVersion = this.Value; }
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
            sb.Append(Zeze.Util.Str.Indent(level)).Append("Zeze.Builtin.Online.BAccount: {").Append(Environment.NewLine);
            level += 4;
            sb.Append(Zeze.Util.Str.Indent(level)).Append("LastLoginVersion").Append('=').Append(LastLoginVersion).Append(Environment.NewLine);
            level -= 4;
            sb.Append(Zeze.Util.Str.Indent(level)).Append('}');
        }

        public override void Encode(ByteBuffer _o_)
        {
            int _i_ = 0;
            {
                long _x_ = LastLoginVersion;
                if (_x_ != 0)
                {
                    _i_ = _o_.WriteTag(_i_, 1, ByteBuffer.INTEGER);
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
                LastLoginVersion = _o_.ReadLong(_t_);
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
        }

        public override bool NegativeCheck()
        {
            if (LastLoginVersion < 0) return true;
            return false;
        }
    }
}