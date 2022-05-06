// auto-generated
using ByteBuffer = Zeze.Serialize.ByteBuffer;
using Environment = System.Environment;

namespace Zeze.Builtin.Game.Online
{
    public interface BLocalReadOnly
    {
        public long TypeId { get; }
        public void Encode(ByteBuffer _os_);
        public bool NegativeCheck();
        public Zeze.Transaction.Bean CopyBean();

        public long LoginVersion { get; }
        public System.Collections.Generic.IReadOnlyDictionary<string,Zeze.Builtin.Game.Online.BAnyReadOnly> Datas { get; }
    }

    public sealed class BLocal : Zeze.Transaction.Bean, BLocalReadOnly
    {
        long _LoginVersion;
        readonly Zeze.Transaction.Collections.CollMap2<string, Zeze.Builtin.Game.Online.BAny> _Datas;
        Zeze.Transaction.Collections.CollMapReadOnly<string,Zeze.Builtin.Game.Online.BAnyReadOnly,Zeze.Builtin.Game.Online.BAny> _DatasReadOnly;

        public long LoginVersion
        {
            get
            {
                if (!IsManaged)
                    return _LoginVersion;
                var txn = Zeze.Transaction.Transaction.Current;
                if (txn == null) return _LoginVersion;
                txn.VerifyRecordAccessed(this, true);
                var log = (Log__LoginVersion)txn.GetLog(ObjectId + 1);
                return log != null ? log.Value : _LoginVersion;
            }
            set
            {
                if (!IsManaged)
                {
                    _LoginVersion = value;
                    return;
                }
                var txn = Zeze.Transaction.Transaction.Current;
                txn.VerifyRecordAccessed(this);
                txn.PutLog(new Log__LoginVersion() { Belong = this, VariableId = 1, Value = value });
            }
        }

        public Zeze.Transaction.Collections.CollMap2<string, Zeze.Builtin.Game.Online.BAny> Datas => _Datas;
        System.Collections.Generic.IReadOnlyDictionary<string,Zeze.Builtin.Game.Online.BAnyReadOnly> Zeze.Builtin.Game.Online.BLocalReadOnly.Datas => _DatasReadOnly;

        public BLocal() : this(0)
        {
        }

        public BLocal(int _varId_) : base(_varId_)
        {
            _Datas = new Zeze.Transaction.Collections.CollMap2<string, Zeze.Builtin.Game.Online.BAny>() { VariableId = 2 };
            _DatasReadOnly = new Zeze.Transaction.Collections.CollMapReadOnly<string,Zeze.Builtin.Game.Online.BAnyReadOnly,Zeze.Builtin.Game.Online.BAny>(_Datas);
        }

        public void Assign(BLocal other)
        {
            LoginVersion = other.LoginVersion;
            Datas.Clear();
            foreach (var e in other.Datas)
                Datas.Add(e.Key, e.Value.Copy());
        }

        public BLocal CopyIfManaged()
        {
            return IsManaged ? Copy() : this;
        }

        public BLocal Copy()
        {
            var copy = new BLocal();
            copy.Assign(this);
            return copy;
        }

        public static void Swap(BLocal a, BLocal b)
        {
            BLocal save = a.Copy();
            a.Assign(b);
            b.Assign(save);
        }

        public override Zeze.Transaction.Bean CopyBean()
        {
            return Copy();
        }

        public const long TYPEID = 1038509325594826174;
        public override long TypeId => TYPEID;

        sealed class Log__LoginVersion : Zeze.Transaction.Log<long>
        {
            public override void Commit() { ((BLocal)Belong)._LoginVersion = this.Value; }
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
            sb.Append(Zeze.Util.Str.Indent(level)).Append("Zeze.Builtin.Game.Online.BLocal: {").Append(Environment.NewLine);
            level += 4;
            sb.Append(Zeze.Util.Str.Indent(level)).Append("LoginVersion").Append('=').Append(LoginVersion).Append(',').Append(Environment.NewLine);
            sb.Append(Zeze.Util.Str.Indent(level)).Append("Datas").Append("=[").Append(Environment.NewLine);
            level += 4;
            foreach (var _kv_ in Datas)
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
                long _x_ = LoginVersion;
                if (_x_ != 0)
                {
                    _i_ = _o_.WriteTag(_i_, 1, ByteBuffer.INTEGER);
                    _o_.WriteLong(_x_);
                }
            }
            {
                var _x_ = Datas;
                int _n_ = _x_.Count;
                if (_n_ != 0)
                {
                    _i_ = _o_.WriteTag(_i_, 2, ByteBuffer.MAP);
                    _o_.WriteMapType(_n_, ByteBuffer.BYTES, ByteBuffer.BEAN);
                    foreach (var _e_ in _x_)
                    {
                        _o_.WriteString(_e_.Key);
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
                LoginVersion = _o_.ReadLong(_t_);
                _i_ += _o_.ReadTagSize(_t_ = _o_.ReadByte());
            }
            if (_i_ == 2)
            {
                var _x_ = Datas;
                _x_.Clear();
                if ((_t_ & ByteBuffer.TAG_MASK) == ByteBuffer.MAP)
                {
                    int _s_ = (_t_ = _o_.ReadByte()) >> ByteBuffer.TAG_SHIFT;
                    for (int _n_ = _o_.ReadUInt(); _n_ > 0; _n_--)
                    {
                        var _k_ = _o_.ReadString(_s_);
                        var _v_ = _o_.ReadBean(new Zeze.Builtin.Game.Online.BAny(), _t_);
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
            _Datas.InitRootInfo(root, this);
        }

        public override bool NegativeCheck()
        {
            if (LoginVersion < 0) return true;
            return false;
        }
    }
}
