// auto-generated
using ByteBuffer = Zeze.Serialize.ByteBuffer;
using Environment = System.Environment;

namespace Zeze.Builtin.ProviderDirect
{
    public interface BModuleRedirectAllHashReadOnly
    {
        public long TypeId { get; }
        public void Encode(ByteBuffer _os_);
        public bool NegativeCheck();
        public Zeze.Transaction.Bean CopyBean();

        public long ReturnCode { get; }
        public Zeze.Net.Binary Params { get; }
    }

    public sealed class BModuleRedirectAllHash : Zeze.Transaction.Bean, BModuleRedirectAllHashReadOnly
    {
        long _ReturnCode;
        Zeze.Net.Binary _Params;

        public long ReturnCode
        {
            get
            {
                if (!IsManaged)
                    return _ReturnCode;
                var txn = Zeze.Transaction.Transaction.Current;
                if (txn == null) return _ReturnCode;
                txn.VerifyRecordAccessed(this, true);
                var log = (Log__ReturnCode)txn.GetLog(ObjectId + 1);
                return log != null ? log.Value : _ReturnCode;
            }
            set
            {
                if (!IsManaged)
                {
                    _ReturnCode = value;
                    return;
                }
                var txn = Zeze.Transaction.Transaction.Current;
                txn.VerifyRecordAccessed(this);
                txn.PutLog(new Log__ReturnCode(this, value));
            }
        }

        public Zeze.Net.Binary Params
        {
            get
            {
                if (!IsManaged)
                    return _Params;
                var txn = Zeze.Transaction.Transaction.Current;
                if (txn == null) return _Params;
                txn.VerifyRecordAccessed(this, true);
                var log = (Log__Params)txn.GetLog(ObjectId + 2);
                return log != null ? log.Value : _Params;
            }
            set
            {
                if (value == null) throw new System.ArgumentNullException();
                if (!IsManaged)
                {
                    _Params = value;
                    return;
                }
                var txn = Zeze.Transaction.Transaction.Current;
                txn.VerifyRecordAccessed(this);
                txn.PutLog(new Log__Params(this, value));
            }
        }

        public BModuleRedirectAllHash() : this(0)
        {
        }

        public BModuleRedirectAllHash(int _varId_) : base(_varId_)
        {
            _Params = Zeze.Net.Binary.Empty;
        }

        public void Assign(BModuleRedirectAllHash other)
        {
            ReturnCode = other.ReturnCode;
            Params = other.Params;
        }

        public BModuleRedirectAllHash CopyIfManaged()
        {
            return IsManaged ? Copy() : this;
        }

        public BModuleRedirectAllHash Copy()
        {
            var copy = new BModuleRedirectAllHash();
            copy.Assign(this);
            return copy;
        }

        public static void Swap(BModuleRedirectAllHash a, BModuleRedirectAllHash b)
        {
            BModuleRedirectAllHash save = a.Copy();
            a.Assign(b);
            b.Assign(save);
        }

        public override Zeze.Transaction.Bean CopyBean()
        {
            return Copy();
        }

        public const long TYPEID = 5611412794338295457;
        public override long TypeId => TYPEID;

        sealed class Log__ReturnCode : Zeze.Transaction.Log<BModuleRedirectAllHash, long>
        {
            public Log__ReturnCode(BModuleRedirectAllHash self, long value) : base(self, value) {}
            public override long LogKey => this.Bean.ObjectId + 1;
            public override void Commit() { this.BeanTyped._ReturnCode = this.Value; }
        }

        sealed class Log__Params : Zeze.Transaction.Log<BModuleRedirectAllHash, Zeze.Net.Binary>
        {
            public Log__Params(BModuleRedirectAllHash self, Zeze.Net.Binary value) : base(self, value) {}
            public override long LogKey => this.Bean.ObjectId + 2;
            public override void Commit() { this.BeanTyped._Params = this.Value; }
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
            sb.Append(Zeze.Util.Str.Indent(level)).Append("Zeze.Builtin.ProviderDirect.BModuleRedirectAllHash: {").Append(Environment.NewLine);
            level += 4;
            sb.Append(Zeze.Util.Str.Indent(level)).Append("ReturnCode").Append('=').Append(ReturnCode).Append(',').Append(Environment.NewLine);
            sb.Append(Zeze.Util.Str.Indent(level)).Append("Params").Append('=').Append(Params).Append(Environment.NewLine);
            level -= 4;
            sb.Append(Zeze.Util.Str.Indent(level)).Append('}');
        }

        public override void Encode(ByteBuffer _o_)
        {
            int _i_ = 0;
            {
                long _x_ = ReturnCode;
                if (_x_ != 0)
                {
                    _i_ = _o_.WriteTag(_i_, 1, ByteBuffer.INTEGER);
                    _o_.WriteLong(_x_);
                }
            }
            {
                var _x_ = Params;
                if (_x_.Count != 0)
                {
                    _i_ = _o_.WriteTag(_i_, 2, ByteBuffer.BYTES);
                    _o_.WriteBinary(_x_);
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
                ReturnCode = _o_.ReadLong(_t_);
                _i_ += _o_.ReadTagSize(_t_ = _o_.ReadByte());
            }
            if (_i_ == 2)
            {
                Params = _o_.ReadBinary(_t_);
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
            if (ReturnCode < 0) return true;
            return false;
        }
    }
}