// auto-generated
using ByteBuffer = Zeze.Serialize.ByteBuffer;
using Environment = System.Environment;

namespace Zeze.Builtin.Provider
{
    public interface BAnnounceProviderInfoReadOnly
    {
        public long TypeId { get; }
        public void Encode(ByteBuffer _os_);
        public bool NegativeCheck();
        public Zeze.Transaction.Bean CopyBean();

        public string ServiceNamePrefix { get; }
        public string ServiceIndentity { get; }
        public string ProviderDirectIp { get; }
        public int ProviderDirectPort { get; }
    }

    public sealed class BAnnounceProviderInfo : Zeze.Transaction.Bean, BAnnounceProviderInfoReadOnly
    {
        string _ServiceNamePrefix;
        string _ServiceIndentity;
        string _ProviderDirectIp;
        int _ProviderDirectPort;

        public string ServiceNamePrefix
        {
            get
            {
                if (!IsManaged)
                    return _ServiceNamePrefix;
                var txn = Zeze.Transaction.Transaction.Current;
                if (txn == null) return _ServiceNamePrefix;
                txn.VerifyRecordAccessed(this, true);
                var log = (Log__ServiceNamePrefix)txn.GetLog(ObjectId + 1);
                return log != null ? log.Value : _ServiceNamePrefix;
            }
            set
            {
                if (value == null) throw new System.ArgumentNullException(nameof(value));
                if (!IsManaged)
                {
                    _ServiceNamePrefix = value;
                    return;
                }
                var txn = Zeze.Transaction.Transaction.Current;
                txn.VerifyRecordAccessed(this);
                txn.PutLog(new Log__ServiceNamePrefix() { Belong = this, VariableId = 1, Value = value });
            }
        }

        public string ServiceIndentity
        {
            get
            {
                if (!IsManaged)
                    return _ServiceIndentity;
                var txn = Zeze.Transaction.Transaction.Current;
                if (txn == null) return _ServiceIndentity;
                txn.VerifyRecordAccessed(this, true);
                var log = (Log__ServiceIndentity)txn.GetLog(ObjectId + 2);
                return log != null ? log.Value : _ServiceIndentity;
            }
            set
            {
                if (value == null) throw new System.ArgumentNullException(nameof(value));
                if (!IsManaged)
                {
                    _ServiceIndentity = value;
                    return;
                }
                var txn = Zeze.Transaction.Transaction.Current;
                txn.VerifyRecordAccessed(this);
                txn.PutLog(new Log__ServiceIndentity() { Belong = this, VariableId = 2, Value = value });
            }
        }

        public string ProviderDirectIp
        {
            get
            {
                if (!IsManaged)
                    return _ProviderDirectIp;
                var txn = Zeze.Transaction.Transaction.Current;
                if (txn == null) return _ProviderDirectIp;
                txn.VerifyRecordAccessed(this, true);
                var log = (Log__ProviderDirectIp)txn.GetLog(ObjectId + 3);
                return log != null ? log.Value : _ProviderDirectIp;
            }
            set
            {
                if (value == null) throw new System.ArgumentNullException(nameof(value));
                if (!IsManaged)
                {
                    _ProviderDirectIp = value;
                    return;
                }
                var txn = Zeze.Transaction.Transaction.Current;
                txn.VerifyRecordAccessed(this);
                txn.PutLog(new Log__ProviderDirectIp() { Belong = this, VariableId = 3, Value = value });
            }
        }

        public int ProviderDirectPort
        {
            get
            {
                if (!IsManaged)
                    return _ProviderDirectPort;
                var txn = Zeze.Transaction.Transaction.Current;
                if (txn == null) return _ProviderDirectPort;
                txn.VerifyRecordAccessed(this, true);
                var log = (Log__ProviderDirectPort)txn.GetLog(ObjectId + 4);
                return log != null ? log.Value : _ProviderDirectPort;
            }
            set
            {
                if (!IsManaged)
                {
                    _ProviderDirectPort = value;
                    return;
                }
                var txn = Zeze.Transaction.Transaction.Current;
                txn.VerifyRecordAccessed(this);
                txn.PutLog(new Log__ProviderDirectPort() { Belong = this, VariableId = 4, Value = value });
            }
        }

        public BAnnounceProviderInfo() : this(0)
        {
        }

        public BAnnounceProviderInfo(int _varId_) : base(_varId_)
        {
            _ServiceNamePrefix = "";
            _ServiceIndentity = "";
            _ProviderDirectIp = "";
        }

        public void Assign(BAnnounceProviderInfo other)
        {
            ServiceNamePrefix = other.ServiceNamePrefix;
            ServiceIndentity = other.ServiceIndentity;
            ProviderDirectIp = other.ProviderDirectIp;
            ProviderDirectPort = other.ProviderDirectPort;
        }

        public BAnnounceProviderInfo CopyIfManaged()
        {
            return IsManaged ? Copy() : this;
        }

        public BAnnounceProviderInfo Copy()
        {
            var copy = new BAnnounceProviderInfo();
            copy.Assign(this);
            return copy;
        }

        public static void Swap(BAnnounceProviderInfo a, BAnnounceProviderInfo b)
        {
            BAnnounceProviderInfo save = a.Copy();
            a.Assign(b);
            b.Assign(save);
        }

        public override Zeze.Transaction.Bean CopyBean()
        {
            return Copy();
        }

        public const long TYPEID = 4964769950995033065;
        public override long TypeId => TYPEID;

        sealed class Log__ServiceNamePrefix : Zeze.Transaction.Log<string>
        {
            public override void Commit() { ((BAnnounceProviderInfo)Belong)._ServiceNamePrefix = this.Value; }
        }

        sealed class Log__ServiceIndentity : Zeze.Transaction.Log<string>
        {
            public override void Commit() { ((BAnnounceProviderInfo)Belong)._ServiceIndentity = this.Value; }
        }

        sealed class Log__ProviderDirectIp : Zeze.Transaction.Log<string>
        {
            public override void Commit() { ((BAnnounceProviderInfo)Belong)._ProviderDirectIp = this.Value; }
        }

        sealed class Log__ProviderDirectPort : Zeze.Transaction.Log<int>
        {
            public override void Commit() { ((BAnnounceProviderInfo)Belong)._ProviderDirectPort = this.Value; }
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
            sb.Append(Zeze.Util.Str.Indent(level)).Append("Zeze.Builtin.Provider.BAnnounceProviderInfo: {").Append(Environment.NewLine);
            level += 4;
            sb.Append(Zeze.Util.Str.Indent(level)).Append("ServiceNamePrefix").Append('=').Append(ServiceNamePrefix).Append(',').Append(Environment.NewLine);
            sb.Append(Zeze.Util.Str.Indent(level)).Append("ServiceIndentity").Append('=').Append(ServiceIndentity).Append(',').Append(Environment.NewLine);
            sb.Append(Zeze.Util.Str.Indent(level)).Append("ProviderDirectIp").Append('=').Append(ProviderDirectIp).Append(',').Append(Environment.NewLine);
            sb.Append(Zeze.Util.Str.Indent(level)).Append("ProviderDirectPort").Append('=').Append(ProviderDirectPort).Append(Environment.NewLine);
            level -= 4;
            sb.Append(Zeze.Util.Str.Indent(level)).Append('}');
        }

        public override void Encode(ByteBuffer _o_)
        {
            int _i_ = 0;
            {
                string _x_ = ServiceNamePrefix;
                if (_x_.Length != 0)
                {
                    _i_ = _o_.WriteTag(_i_, 1, ByteBuffer.BYTES);
                    _o_.WriteString(_x_);
                }
            }
            {
                string _x_ = ServiceIndentity;
                if (_x_.Length != 0)
                {
                    _i_ = _o_.WriteTag(_i_, 2, ByteBuffer.BYTES);
                    _o_.WriteString(_x_);
                }
            }
            {
                string _x_ = ProviderDirectIp;
                if (_x_.Length != 0)
                {
                    _i_ = _o_.WriteTag(_i_, 3, ByteBuffer.BYTES);
                    _o_.WriteString(_x_);
                }
            }
            {
                int _x_ = ProviderDirectPort;
                if (_x_ != 0)
                {
                    _i_ = _o_.WriteTag(_i_, 4, ByteBuffer.INTEGER);
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
                ServiceNamePrefix = _o_.ReadString(_t_);
                _i_ += _o_.ReadTagSize(_t_ = _o_.ReadByte());
            }
            if (_i_ == 2)
            {
                ServiceIndentity = _o_.ReadString(_t_);
                _i_ += _o_.ReadTagSize(_t_ = _o_.ReadByte());
            }
            if (_i_ == 3)
            {
                ProviderDirectIp = _o_.ReadString(_t_);
                _i_ += _o_.ReadTagSize(_t_ = _o_.ReadByte());
            }
            if (_i_ == 4)
            {
                ProviderDirectPort = _o_.ReadInt(_t_);
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
            if (ProviderDirectPort < 0) return true;
            return false;
        }

        public override void FollowerApply(Zeze.Transaction.Log log)
        {
            var blog = (Zeze.Transaction.Collections.LogBean)log;
            foreach (var vlog in blog.Variables.Values)
            {
                switch (vlog.VariableId)
                {
                    case 1: _ServiceNamePrefix = ((Zeze.Transaction.Log<string>)vlog).Value; break;
                    case 2: _ServiceIndentity = ((Zeze.Transaction.Log<string>)vlog).Value; break;
                    case 3: _ProviderDirectIp = ((Zeze.Transaction.Log<string>)vlog).Value; break;
                    case 4: _ProviderDirectPort = ((Zeze.Transaction.Log<int>)vlog).Value; break;
                }
            }
        }

    }
}
