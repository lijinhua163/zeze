// auto-generated @formatter:off
package Zeze.Builtin.GlobalCacheManagerWithRaft;

import Zeze.Serialize.ByteBuffer;

public final class LoginParam extends Zeze.Transaction.Bean {
    private int _ServerId;
    private int _GlobalCacheManagerHashIndex;

    public int getServerId() {
        if (!isManaged())
            return _ServerId;
        var txn = Zeze.Transaction.Transaction.getCurrent();
        if (txn == null)
            return _ServerId;
        txn.VerifyRecordAccessed(this, true);
        var log = (Log__ServerId)txn.GetLog(this.getObjectId() + 1);
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

    public int getGlobalCacheManagerHashIndex() {
        if (!isManaged())
            return _GlobalCacheManagerHashIndex;
        var txn = Zeze.Transaction.Transaction.getCurrent();
        if (txn == null)
            return _GlobalCacheManagerHashIndex;
        txn.VerifyRecordAccessed(this, true);
        var log = (Log__GlobalCacheManagerHashIndex)txn.GetLog(this.getObjectId() + 2);
        return log != null ? log.getValue() : _GlobalCacheManagerHashIndex;
    }

    public void setGlobalCacheManagerHashIndex(int value) {
        if (!isManaged()) {
            _GlobalCacheManagerHashIndex = value;
            return;
        }
        var txn = Zeze.Transaction.Transaction.getCurrent();
        assert txn != null;
        txn.VerifyRecordAccessed(this);
        txn.PutLog(new Log__GlobalCacheManagerHashIndex(this, value));
    }

    public LoginParam() {
         this(0);
    }

    public LoginParam(int _varId_) {
        super(_varId_);
    }

    public void Assign(LoginParam other) {
        setServerId(other.getServerId());
        setGlobalCacheManagerHashIndex(other.getGlobalCacheManagerHashIndex());
    }

    public LoginParam CopyIfManaged() {
        return isManaged() ? Copy() : this;
    }

    public LoginParam Copy() {
        var copy = new LoginParam();
        copy.Assign(this);
        return copy;
    }

    public static void Swap(LoginParam a, LoginParam b) {
        LoginParam save = a.Copy();
        a.Assign(b);
        b.Assign(save);
    }

    @Override
    public Zeze.Transaction.Bean CopyBean() {
        return Copy();
    }

    public static final long TYPEID = 8338257265267188489L;

    @Override
    public long getTypeId() {
        return TYPEID;
    }

    private static final class Log__ServerId extends Zeze.Transaction.Log1<LoginParam, Integer> {
        public Log__ServerId(LoginParam self, Integer value) { super(self, value); }
        @Override
        public long getLogKey() { return this.getBean().getObjectId() + 1; }
        @Override
        public void Commit() { this.getBeanTyped()._ServerId = this.getValue(); }
    }

    private static final class Log__GlobalCacheManagerHashIndex extends Zeze.Transaction.Log1<LoginParam, Integer> {
        public Log__GlobalCacheManagerHashIndex(LoginParam self, Integer value) { super(self, value); }
        @Override
        public long getLogKey() { return this.getBean().getObjectId() + 2; }
        @Override
        public void Commit() { this.getBeanTyped()._GlobalCacheManagerHashIndex = this.getValue(); }
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
        sb.append(Zeze.Util.Str.indent(level)).append("Zeze.Builtin.GlobalCacheManagerWithRaft.LoginParam: {").append(System.lineSeparator());
        level += 4;
        sb.append(Zeze.Util.Str.indent(level)).append("ServerId").append('=').append(getServerId()).append(',').append(System.lineSeparator());
        sb.append(Zeze.Util.Str.indent(level)).append("GlobalCacheManagerHashIndex").append('=').append(getGlobalCacheManagerHashIndex()).append(System.lineSeparator());
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
            int _x_ = getServerId();
            if (_x_ != 0) {
                _i_ = _o_.WriteTag(_i_, 1, ByteBuffer.INTEGER);
                _o_.WriteInt(_x_);
            }
        }
        {
            int _x_ = getGlobalCacheManagerHashIndex();
            if (_x_ != 0) {
                _i_ = _o_.WriteTag(_i_, 2, ByteBuffer.INTEGER);
                _o_.WriteInt(_x_);
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
            setServerId(_o_.ReadInt(_t_));
            _i_ += _o_.ReadTagSize(_t_ = _o_.ReadByte());
        }
        if (_i_ == 2) {
            setGlobalCacheManagerHashIndex(_o_.ReadInt(_t_));
            _i_ += _o_.ReadTagSize(_t_ = _o_.ReadByte());
        }
        while (_t_ != 0) {
            _o_.SkipUnknownField(_t_);
            _o_.ReadTagSize(_t_ = _o_.ReadByte());
        }
    }

    @Override
    protected void InitChildrenRootInfo(Zeze.Transaction.Record.RootInfo root) {
    }

    @SuppressWarnings("RedundantIfStatement")
    @Override
    public boolean NegativeCheck() {
        if (getServerId() < 0)
            return true;
        if (getGlobalCacheManagerHashIndex() < 0)
            return true;
        return false;
    }
}