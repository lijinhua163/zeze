// auto-generated @formatter:off
package Zeze.Beans.Provider2;

import Zeze.Serialize.ByteBuffer;

public final class BModuleRedirectAllResult extends Zeze.Transaction.Bean {
    private int _ModuleId;
    private int _ServerId; // 目标server的id。
    private long _SourceProvider; // 从BModuleRedirectAllRequest里面得到。
    private String _MethodFullName; // format="ModuleFullName:MethodName"
    private long _SessionId; // 发起请求者初始化，返回结果时带回。
    private final Zeze.Transaction.Collections.PMap2<Integer, Zeze.Beans.Provider2.BModuleRedirectAllHash> _Hashs; // 发送给具体进程时需要处理的分组hash-index（目前由linkd填写）

    public int getModuleId() {
        if (!isManaged())
            return _ModuleId;
        var txn = Zeze.Transaction.Transaction.getCurrent();
        if (txn == null)
            return _ModuleId;
        txn.VerifyRecordAccessed(this, true);
        var log = (Log__ModuleId)txn.GetLog(this.getObjectId() + 1);
        return log != null ? log.getValue() : _ModuleId;
    }

    public void setModuleId(int value) {
        if (!isManaged()) {
            _ModuleId = value;
            return;
        }
        var txn = Zeze.Transaction.Transaction.getCurrent();
        assert txn != null;
        txn.VerifyRecordAccessed(this);
        txn.PutLog(new Log__ModuleId(this, value));
    }

    public int getServerId() {
        if (!isManaged())
            return _ServerId;
        var txn = Zeze.Transaction.Transaction.getCurrent();
        if (txn == null)
            return _ServerId;
        txn.VerifyRecordAccessed(this, true);
        var log = (Log__ServerId)txn.GetLog(this.getObjectId() + 2);
        return log != null ? log.getValue() : _ServerId;
    }

    public void setServerId(int value) {
        if (!isManaged()) {
            _ServerId = value;
            return;
        }
        var txn = Zeze.Transaction.Transaction.getCurrent();
        assert txn != null;
        txn.VerifyRecordAccessed(this);
        txn.PutLog(new Log__ServerId(this, value));
    }

    public long getSourceProvider() {
        if (!isManaged())
            return _SourceProvider;
        var txn = Zeze.Transaction.Transaction.getCurrent();
        if (txn == null)
            return _SourceProvider;
        txn.VerifyRecordAccessed(this, true);
        var log = (Log__SourceProvider)txn.GetLog(this.getObjectId() + 3);
        return log != null ? log.getValue() : _SourceProvider;
    }

    public void setSourceProvider(long value) {
        if (!isManaged()) {
            _SourceProvider = value;
            return;
        }
        var txn = Zeze.Transaction.Transaction.getCurrent();
        assert txn != null;
        txn.VerifyRecordAccessed(this);
        txn.PutLog(new Log__SourceProvider(this, value));
    }

    public String getMethodFullName() {
        if (!isManaged())
            return _MethodFullName;
        var txn = Zeze.Transaction.Transaction.getCurrent();
        if (txn == null)
            return _MethodFullName;
        txn.VerifyRecordAccessed(this, true);
        var log = (Log__MethodFullName)txn.GetLog(this.getObjectId() + 4);
        return log != null ? log.getValue() : _MethodFullName;
    }

    public void setMethodFullName(String value) {
        if (value == null)
            throw new IllegalArgumentException();
        if (!isManaged()) {
            _MethodFullName = value;
            return;
        }
        var txn = Zeze.Transaction.Transaction.getCurrent();
        assert txn != null;
        txn.VerifyRecordAccessed(this);
        txn.PutLog(new Log__MethodFullName(this, value));
    }

    public long getSessionId() {
        if (!isManaged())
            return _SessionId;
        var txn = Zeze.Transaction.Transaction.getCurrent();
        if (txn == null)
            return _SessionId;
        txn.VerifyRecordAccessed(this, true);
        var log = (Log__SessionId)txn.GetLog(this.getObjectId() + 5);
        return log != null ? log.getValue() : _SessionId;
    }

    public void setSessionId(long value) {
        if (!isManaged()) {
            _SessionId = value;
            return;
        }
        var txn = Zeze.Transaction.Transaction.getCurrent();
        assert txn != null;
        txn.VerifyRecordAccessed(this);
        txn.PutLog(new Log__SessionId(this, value));
    }

    public Zeze.Transaction.Collections.PMap2<Integer, Zeze.Beans.Provider2.BModuleRedirectAllHash> getHashs() {
        return _Hashs;
    }

    public BModuleRedirectAllResult() {
         this(0);
    }

    public BModuleRedirectAllResult(int _varId_) {
        super(_varId_);
        _MethodFullName = "";
        _Hashs = new Zeze.Transaction.Collections.PMap2<>(getObjectId() + 6, (_v) -> new Log__Hashs(this, _v));
    }

    public void Assign(BModuleRedirectAllResult other) {
        setModuleId(other.getModuleId());
        setServerId(other.getServerId());
        setSourceProvider(other.getSourceProvider());
        setMethodFullName(other.getMethodFullName());
        setSessionId(other.getSessionId());
        getHashs().clear();
        for (var e : other.getHashs().entrySet())
            getHashs().put(e.getKey(), e.getValue().Copy());
    }

    public BModuleRedirectAllResult CopyIfManaged() {
        return isManaged() ? Copy() : this;
    }

    public BModuleRedirectAllResult Copy() {
        var copy = new BModuleRedirectAllResult();
        copy.Assign(this);
        return copy;
    }

    public static void Swap(BModuleRedirectAllResult a, BModuleRedirectAllResult b) {
        BModuleRedirectAllResult save = a.Copy();
        a.Assign(b);
        b.Assign(save);
    }

    @Override
    public Zeze.Transaction.Bean CopyBean() {
        return Copy();
    }

    public static final long TYPEID = -2705661491278303195L;

    @Override
    public long getTypeId() {
        return TYPEID;
    }

    private static final class Log__ModuleId extends Zeze.Transaction.Log1<BModuleRedirectAllResult, Integer> {
        public Log__ModuleId(BModuleRedirectAllResult self, Integer value) { super(self, value); }
        @Override
        public long getLogKey() { return this.getBean().getObjectId() + 1; }
        @Override
        public void Commit() { this.getBeanTyped()._ModuleId = this.getValue(); }
    }

    private static final class Log__ServerId extends Zeze.Transaction.Log1<BModuleRedirectAllResult, Integer> {
        public Log__ServerId(BModuleRedirectAllResult self, Integer value) { super(self, value); }
        @Override
        public long getLogKey() { return this.getBean().getObjectId() + 2; }
        @Override
        public void Commit() { this.getBeanTyped()._ServerId = this.getValue(); }
    }

    private static final class Log__SourceProvider extends Zeze.Transaction.Log1<BModuleRedirectAllResult, Long> {
        public Log__SourceProvider(BModuleRedirectAllResult self, Long value) { super(self, value); }
        @Override
        public long getLogKey() { return this.getBean().getObjectId() + 3; }
        @Override
        public void Commit() { this.getBeanTyped()._SourceProvider = this.getValue(); }
    }

    private static final class Log__MethodFullName extends Zeze.Transaction.Log1<BModuleRedirectAllResult, String> {
        public Log__MethodFullName(BModuleRedirectAllResult self, String value) { super(self, value); }
        @Override
        public long getLogKey() { return this.getBean().getObjectId() + 4; }
        @Override
        public void Commit() { this.getBeanTyped()._MethodFullName = this.getValue(); }
    }

    private static final class Log__SessionId extends Zeze.Transaction.Log1<BModuleRedirectAllResult, Long> {
        public Log__SessionId(BModuleRedirectAllResult self, Long value) { super(self, value); }
        @Override
        public long getLogKey() { return this.getBean().getObjectId() + 5; }
        @Override
        public void Commit() { this.getBeanTyped()._SessionId = this.getValue(); }
    }

    private static final class Log__Hashs extends Zeze.Transaction.Collections.PMap.LogV<Integer, Zeze.Beans.Provider2.BModuleRedirectAllHash> {
        public Log__Hashs(BModuleRedirectAllResult host, org.pcollections.PMap<Integer, Zeze.Beans.Provider2.BModuleRedirectAllHash> value) { super(host, value); }
        @Override
        public long getLogKey() { return getBean().getObjectId() + 6; }
        public BModuleRedirectAllResult getBeanTyped() { return (BModuleRedirectAllResult)getBean(); }
        @Override
        public void Commit() { Commit(getBeanTyped()._Hashs); }
    }

    @Override
    public String toString() {
        var sb = new StringBuilder();
        BuildString(sb, 0);
        sb.append(System.lineSeparator());
        return sb.toString();
    }

    @Override
    public void BuildString(StringBuilder sb, int level) {
        sb.append(Zeze.Util.Str.indent(level)).append("Zeze.Beans.Provider2.BModuleRedirectAllResult: {").append(System.lineSeparator());
        level += 4;
        sb.append(Zeze.Util.Str.indent(level)).append("ModuleId").append('=').append(getModuleId()).append(',').append(System.lineSeparator());
        sb.append(Zeze.Util.Str.indent(level)).append("ServerId").append('=').append(getServerId()).append(',').append(System.lineSeparator());
        sb.append(Zeze.Util.Str.indent(level)).append("SourceProvider").append('=').append(getSourceProvider()).append(',').append(System.lineSeparator());
        sb.append(Zeze.Util.Str.indent(level)).append("MethodFullName").append('=').append(getMethodFullName()).append(',').append(System.lineSeparator());
        sb.append(Zeze.Util.Str.indent(level)).append("SessionId").append('=').append(getSessionId()).append(',').append(System.lineSeparator());
        sb.append(Zeze.Util.Str.indent(level)).append("Hashs").append("=[").append(System.lineSeparator());
        level += 4;
        for (var _kv_ : getHashs().entrySet()) {
            sb.append(Zeze.Util.Str.indent(level)).append('(').append(System.lineSeparator());
            sb.append(Zeze.Util.Str.indent(level)).append("Key").append('=').append(_kv_.getKey()).append(',').append(System.lineSeparator());
            sb.append(Zeze.Util.Str.indent(level)).append("Value").append('=').append(System.lineSeparator());
            _kv_.getValue().BuildString(sb, level + 4);
            sb.append(',').append(System.lineSeparator());
            sb.append(Zeze.Util.Str.indent(level)).append(')').append(System.lineSeparator());
        }
        level -= 4;
        sb.append(Zeze.Util.Str.indent(level)).append(']').append(System.lineSeparator());
        level -= 4;
        sb.append(Zeze.Util.Str.indent(level)).append('}');
    }

    private static int _PRE_ALLOC_SIZE_ = 16;

    @Override
    public int getPreAllocSize() {
        return _PRE_ALLOC_SIZE_;
    }

    @Override
    public void setPreAllocSize(int size) {
        _PRE_ALLOC_SIZE_ = size;
    }

    @SuppressWarnings("UnusedAssignment")
    @Override
    public void Encode(ByteBuffer _o_) {
        int _i_ = 0;
        {
            int _x_ = getModuleId();
            if (_x_ != 0) {
                _i_ = _o_.WriteTag(_i_, 1, ByteBuffer.INTEGER);
                _o_.WriteInt(_x_);
            }
        }
        {
            int _x_ = getServerId();
            if (_x_ != 0) {
                _i_ = _o_.WriteTag(_i_, 2, ByteBuffer.INTEGER);
                _o_.WriteInt(_x_);
            }
        }
        {
            long _x_ = getSourceProvider();
            if (_x_ != 0) {
                _i_ = _o_.WriteTag(_i_, 3, ByteBuffer.INTEGER);
                _o_.WriteLong(_x_);
            }
        }
        {
            String _x_ = getMethodFullName();
            if (!_x_.isEmpty()) {
                _i_ = _o_.WriteTag(_i_, 4, ByteBuffer.BYTES);
                _o_.WriteString(_x_);
            }
        }
        {
            long _x_ = getSessionId();
            if (_x_ != 0) {
                _i_ = _o_.WriteTag(_i_, 5, ByteBuffer.INTEGER);
                _o_.WriteLong(_x_);
            }
        }
        {
            var _x_ = getHashs();
            int _n_ = _x_.size();
            if (_n_ != 0) {
                _i_ = _o_.WriteTag(_i_, 6, ByteBuffer.MAP);
                _o_.WriteMapType(_n_, ByteBuffer.INTEGER, ByteBuffer.BEAN);
                for (var _e_ : _x_.entrySet()) {
                    _o_.WriteLong(_e_.getKey());
                    _e_.getValue().Encode(_o_);
                }
            }
        }
        _o_.WriteByte(0);
    }

    @SuppressWarnings("UnusedAssignment")
    @Override
    public void Decode(ByteBuffer _o_) {
        int _t_ = _o_.ReadByte();
        int _i_ = _o_.ReadTagSize(_t_);
        if (_i_ == 1) {
            setModuleId(_o_.ReadInt(_t_));
            _i_ += _o_.ReadTagSize(_t_ = _o_.ReadByte());
        }
        if (_i_ == 2) {
            setServerId(_o_.ReadInt(_t_));
            _i_ += _o_.ReadTagSize(_t_ = _o_.ReadByte());
        }
        if (_i_ == 3) {
            setSourceProvider(_o_.ReadLong(_t_));
            _i_ += _o_.ReadTagSize(_t_ = _o_.ReadByte());
        }
        if (_i_ == 4) {
            setMethodFullName(_o_.ReadString(_t_));
            _i_ += _o_.ReadTagSize(_t_ = _o_.ReadByte());
        }
        if (_i_ == 5) {
            setSessionId(_o_.ReadLong(_t_));
            _i_ += _o_.ReadTagSize(_t_ = _o_.ReadByte());
        }
        if (_i_ == 6) {
            var _x_ = getHashs();
            _x_.clear();
            if ((_t_ & ByteBuffer.TAG_MASK) == ByteBuffer.MAP) {
                int _s_ = (_t_ = _o_.ReadByte()) >> ByteBuffer.TAG_SHIFT;
                for (int _n_ = _o_.ReadUInt(); _n_ > 0; _n_--) {
                    var _k_ = _o_.ReadInt(_s_);
                    var _v_ = _o_.ReadBean(new Zeze.Beans.Provider2.BModuleRedirectAllHash(), _t_);
                    _x_.put(_k_, _v_);
                }
            } else
                _o_.SkipUnknownField(_t_);
            _i_ += _o_.ReadTagSize(_t_ = _o_.ReadByte());
        }
        while (_t_ != 0) {
            _o_.SkipUnknownField(_t_);
            _o_.ReadTagSize(_t_ = _o_.ReadByte());
        }
    }

    @Override
    protected void InitChildrenRootInfo(Zeze.Transaction.Record.RootInfo root) {
        _Hashs.InitRootInfo(root, this);
    }

    @SuppressWarnings("RedundantIfStatement")
    @Override
    public boolean NegativeCheck() {
        if (getModuleId() < 0)
            return true;
        if (getServerId() < 0)
            return true;
        if (getSourceProvider() < 0)
            return true;
        if (getSessionId() < 0)
            return true;
        for (var _v_ : getHashs().values()) {
            if (_v_.NegativeCheck())
                return true;
        }
        return false;
    }
}