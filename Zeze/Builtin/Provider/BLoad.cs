// auto-generated
using ByteBuffer = Zeze.Serialize.ByteBuffer;
using Environment = System.Environment;

namespace Zeze.Builtin.Provider
{
    public interface BLoadReadOnly
    {
        public long TypeId { get; }
        public void Encode(ByteBuffer _os_);
        public bool NegativeCheck();
        public Zeze.Transaction.Bean CopyBean();

        public int Online { get; }
        public int ProposeMaxOnline { get; }
        public int OnlineNew { get; }
    }

    public sealed class BLoad : Zeze.Transaction.Bean, BLoadReadOnly
    {
        int _Online; // 用户数量
        int _ProposeMaxOnline; // 建议最大用户数量
        int _OnlineNew; // 最近上线用户数量，一般是一秒内的。用来防止短时间内给同一个gs分配太多用户。

        public int Online
        {
            get
            {
                if (!IsManaged)
                    return _Online;
                var txn = Zeze.Transaction.Transaction.Current;
                if (txn == null) return _Online;
                txn.VerifyRecordAccessed(this, true);
                var log = (Log__Online)txn.GetLog(ObjectId + 1);
                return log != null ? log.Value : _Online;
            }
            set
            {
                if (!IsManaged)
                {
                    _Online = value;
                    return;
                }
                var txn = Zeze.Transaction.Transaction.Current;
                txn.VerifyRecordAccessed(this);
                txn.PutLog(new Log__Online() { Belong = this, VariableId = 1, Value = value });
            }
        }

        public int ProposeMaxOnline
        {
            get
            {
                if (!IsManaged)
                    return _ProposeMaxOnline;
                var txn = Zeze.Transaction.Transaction.Current;
                if (txn == null) return _ProposeMaxOnline;
                txn.VerifyRecordAccessed(this, true);
                var log = (Log__ProposeMaxOnline)txn.GetLog(ObjectId + 2);
                return log != null ? log.Value : _ProposeMaxOnline;
            }
            set
            {
                if (!IsManaged)
                {
                    _ProposeMaxOnline = value;
                    return;
                }
                var txn = Zeze.Transaction.Transaction.Current;
                txn.VerifyRecordAccessed(this);
                txn.PutLog(new Log__ProposeMaxOnline() { Belong = this, VariableId = 2, Value = value });
            }
        }

        public int OnlineNew
        {
            get
            {
                if (!IsManaged)
                    return _OnlineNew;
                var txn = Zeze.Transaction.Transaction.Current;
                if (txn == null) return _OnlineNew;
                txn.VerifyRecordAccessed(this, true);
                var log = (Log__OnlineNew)txn.GetLog(ObjectId + 3);
                return log != null ? log.Value : _OnlineNew;
            }
            set
            {
                if (!IsManaged)
                {
                    _OnlineNew = value;
                    return;
                }
                var txn = Zeze.Transaction.Transaction.Current;
                txn.VerifyRecordAccessed(this);
                txn.PutLog(new Log__OnlineNew() { Belong = this, VariableId = 3, Value = value });
            }
        }

        public BLoad() : this(0)
        {
        }

        public BLoad(int _varId_) : base(_varId_)
        {
        }

        public void Assign(BLoad other)
        {
            Online = other.Online;
            ProposeMaxOnline = other.ProposeMaxOnline;
            OnlineNew = other.OnlineNew;
        }

        public BLoad CopyIfManaged()
        {
            return IsManaged ? Copy() : this;
        }

        public BLoad Copy()
        {
            var copy = new BLoad();
            copy.Assign(this);
            return copy;
        }

        public static void Swap(BLoad a, BLoad b)
        {
            BLoad save = a.Copy();
            a.Assign(b);
            b.Assign(save);
        }

        public override Zeze.Transaction.Bean CopyBean()
        {
            return Copy();
        }

        public const long TYPEID = 8972064501607813483;
        public override long TypeId => TYPEID;

        sealed class Log__Online : Zeze.Transaction.Log<int>
        {
            public override void Commit() { ((BLoad)Belong)._Online = this.Value; }
        }

        sealed class Log__ProposeMaxOnline : Zeze.Transaction.Log<int>
        {
            public override void Commit() { ((BLoad)Belong)._ProposeMaxOnline = this.Value; }
        }

        sealed class Log__OnlineNew : Zeze.Transaction.Log<int>
        {
            public override void Commit() { ((BLoad)Belong)._OnlineNew = this.Value; }
        }

        public override string ToString()
        {
            var sb = new System.Text.StringBuilder();
            BuildString(sb, 0);
            sb.Append(Environment.NewLine);
            return sb.ToString();
        }

        public override void BuildString(System.Text.StringBuilder sb, int level)
        {
            sb.Append(Zeze.Util.Str.Indent(level)).Append("Zeze.Builtin.Provider.BLoad: {").Append(Environment.NewLine);
            level += 4;
            sb.Append(Zeze.Util.Str.Indent(level)).Append("Online").Append('=').Append(Online).Append(',').Append(Environment.NewLine);
            sb.Append(Zeze.Util.Str.Indent(level)).Append("ProposeMaxOnline").Append('=').Append(ProposeMaxOnline).Append(',').Append(Environment.NewLine);
            sb.Append(Zeze.Util.Str.Indent(level)).Append("OnlineNew").Append('=').Append(OnlineNew).Append(Environment.NewLine);
            level -= 4;
            sb.Append(Zeze.Util.Str.Indent(level)).Append('}');
        }

        public override void Encode(ByteBuffer _o_)
        {
            int _i_ = 0;
            {
                int _x_ = Online;
                if (_x_ != 0)
                {
                    _i_ = _o_.WriteTag(_i_, 1, ByteBuffer.INTEGER);
                    _o_.WriteInt(_x_);
                }
            }
            {
                int _x_ = ProposeMaxOnline;
                if (_x_ != 0)
                {
                    _i_ = _o_.WriteTag(_i_, 2, ByteBuffer.INTEGER);
                    _o_.WriteInt(_x_);
                }
            }
            {
                int _x_ = OnlineNew;
                if (_x_ != 0)
                {
                    _i_ = _o_.WriteTag(_i_, 3, ByteBuffer.INTEGER);
                    _o_.WriteInt(_x_);
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
                Online = _o_.ReadInt(_t_);
                _i_ += _o_.ReadTagSize(_t_ = _o_.ReadByte());
            }
            if (_i_ == 2)
            {
                ProposeMaxOnline = _o_.ReadInt(_t_);
                _i_ += _o_.ReadTagSize(_t_ = _o_.ReadByte());
            }
            if (_i_ == 3)
            {
                OnlineNew = _o_.ReadInt(_t_);
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
            if (Online < 0) return true;
            if (ProposeMaxOnline < 0) return true;
            if (OnlineNew < 0) return true;
            return false;
        }

        public override void FollowerApply(Zeze.Transaction.Log log)
        {
            var blog = (Zeze.Transaction.Collections.LogBean)log;
            foreach (var vlog in blog.Variables.Values)
            {
                switch (vlog.VariableId)
                {
                    case 1: _Online = ((Zeze.Transaction.Log<int>)vlog).Value; break;
                    case 2: _ProposeMaxOnline = ((Zeze.Transaction.Log<int>)vlog).Value; break;
                    case 3: _OnlineNew = ((Zeze.Transaction.Log<int>)vlog).Value; break;
                }
            }
        }

    }
}
