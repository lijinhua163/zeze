// auto-generated
using ByteBuffer = Zeze.Serialize.ByteBuffer;
using Environment = System.Environment;

namespace Zeze.Builtin.Online
{
    public interface BLocalsReadOnly
    {
        public long TypeId { get; }
        public void Encode(ByteBuffer _os_);
        public bool NegativeCheck();
        public Zeze.Transaction.Bean CopyBean();

        public System.Collections.Generic.IReadOnlyDictionary<string,Zeze.Builtin.Online.BLocalReadOnly> Logins { get; }
    }

    public sealed class BLocals : Zeze.Transaction.Bean, BLocalsReadOnly
    {
        readonly Zeze.Transaction.Collections.CollMap2<string, Zeze.Builtin.Online.BLocal> _Logins; // key is ClientId
        Zeze.Transaction.Collections.CollMapReadOnly<string,Zeze.Builtin.Online.BLocalReadOnly,Zeze.Builtin.Online.BLocal> _LoginsReadOnly;

        public Zeze.Transaction.Collections.CollMap2<string, Zeze.Builtin.Online.BLocal> Logins => _Logins;
        System.Collections.Generic.IReadOnlyDictionary<string,Zeze.Builtin.Online.BLocalReadOnly> Zeze.Builtin.Online.BLocalsReadOnly.Logins => _LoginsReadOnly;

        public BLocals() : this(0)
        {
        }

        public BLocals(int _varId_) : base(_varId_)
        {
            _Logins = new Zeze.Transaction.Collections.CollMap2<string, Zeze.Builtin.Online.BLocal>() { VariableId = 1 };
            _LoginsReadOnly = new Zeze.Transaction.Collections.CollMapReadOnly<string,Zeze.Builtin.Online.BLocalReadOnly,Zeze.Builtin.Online.BLocal>(_Logins);
        }

        public void Assign(BLocals other)
        {
            Logins.Clear();
            foreach (var e in other.Logins)
                Logins.Add(e.Key, e.Value.Copy());
        }

        public BLocals CopyIfManaged()
        {
            return IsManaged ? Copy() : this;
        }

        public BLocals Copy()
        {
            var copy = new BLocals();
            copy.Assign(this);
            return copy;
        }

        public static void Swap(BLocals a, BLocals b)
        {
            BLocals save = a.Copy();
            a.Assign(b);
            b.Assign(save);
        }

        public override Zeze.Transaction.Bean CopyBean()
        {
            return Copy();
        }

        public const long TYPEID = -3801167864121993305;
        public override long TypeId => TYPEID;


        public override string ToString()
        {
            var sb = new System.Text.StringBuilder();
            BuildString(sb, 0);
            sb.Append(Environment.NewLine);
            return sb.ToString();
        }

        public override void BuildString(System.Text.StringBuilder sb, int level)
        {
            sb.Append(Zeze.Util.Str.Indent(level)).Append("Zeze.Builtin.Online.BLocals: {").Append(Environment.NewLine);
            level += 4;
            sb.Append(Zeze.Util.Str.Indent(level)).Append("Logins").Append("=[").Append(Environment.NewLine);
            level += 4;
            foreach (var _kv_ in Logins)
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
                var _x_ = Logins;
                int _n_ = _x_.Count;
                if (_n_ != 0)
                {
                    _i_ = _o_.WriteTag(_i_, 1, ByteBuffer.MAP);
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
                var _x_ = Logins;
                _x_.Clear();
                if ((_t_ & ByteBuffer.TAG_MASK) == ByteBuffer.MAP)
                {
                    int _s_ = (_t_ = _o_.ReadByte()) >> ByteBuffer.TAG_SHIFT;
                    for (int _n_ = _o_.ReadUInt(); _n_ > 0; _n_--)
                    {
                        var _k_ = _o_.ReadString(_s_);
                        var _v_ = _o_.ReadBean(new Zeze.Builtin.Online.BLocal(), _t_);
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
            _Logins.InitRootInfo(root, this);
        }

        public override bool NegativeCheck()
        {
            foreach (var _v_ in Logins.Values)
            {
                if (_v_.NegativeCheck()) return true;
            }
            return false;
        }

        public override void FollowerApply(Zeze.Transaction.Log log)
        {
            var blog = (Zeze.Transaction.Collections.LogBean)log;
            foreach (var vlog in blog.Variables.Values)
            {
                switch (vlog.VariableId)
                {
                    case 1: _Logins.FollowerApply(vlog); break;
                }
            }
        }

    }
}
