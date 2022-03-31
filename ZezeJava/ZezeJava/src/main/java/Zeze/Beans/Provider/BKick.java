// auto-generated @formatter:off
package Zeze.Beans.Provider;

import Zeze.Serialize.ByteBuffer;

public final class BKick extends Zeze.Transaction.Bean {
    public static final int ErrorProtocolUnkown = 1;
    public static final int ErrorDecode = 2;
    public static final int ErrorProtocolException = 3;

    private long _linksid;
    private int _code;
    private String _desc; // // for debug

    public long getLinksid() {
        if (!isManaged())
            return _linksid;
        var txn = Zeze.Transaction.Transaction.getCurrent();
        if (txn == null)
            return _linksid;
        txn.VerifyRecordAccessed(this, true);
        var log = (Log__linksid)txn.GetLog(this.getObjectId() + 1);
        return log != null ? log.getValue() : _linksid;
    }

    public void setLinksid(long value) {
        if (!isManaged()) {
            _linksid = value;
            return;
        }
        var txn = Zeze.Transaction.Transaction.getCurrent();
        assert txn != null;
        txn.VerifyRecordAccessed(this);
        txn.PutLog(new Log__linksid(this, value));
    }

    public int getCode() {
        if (!isManaged())
            return _code;
        var txn = Zeze.Transaction.Transaction.getCurrent();
        if (txn == null)
            return _code;
        txn.VerifyRecordAccessed(this, true);
        var log = (Log__code)txn.GetLog(this.getObjectId() + 2);
        return log != null ? log.getValue() : _code;
    }

    public void setCode(int value) {
        if (!isManaged()) {
            _code = value;
            return;
        }
        var txn = Zeze.Transaction.Transaction.getCurrent();
        assert txn != null;
        txn.VerifyRecordAccessed(this);
        txn.PutLog(new Log__code(this, value));
    }

    public String getDesc() {
        if (!isManaged())
            return _desc;
        var txn = Zeze.Transaction.Transaction.getCurrent();
        if (txn == null)
            return _desc;
        txn.VerifyRecordAccessed(this, true);
        var log = (Log__desc)txn.GetLog(this.getObjectId() + 3);
        return log != null ? log.getValue() : _desc;
    }

    public void setDesc(String value) {
        if (value == null)
            throw new IllegalArgumentException();
        if (!isManaged()) {
            _desc = value;
            return;
        }
        var txn = Zeze.Transaction.Transaction.getCurrent();
        assert txn != null;
        txn.VerifyRecordAccessed(this);
        txn.PutLog(new Log__desc(this, value));
    }

    public BKick() {
         this(0);
    }

    public BKick(int _varId_) {
        super(_varId_);
        _desc = "";
    }

    public void Assign(BKick other) {
        setLinksid(other.getLinksid());
        setCode(other.getCode());
        setDesc(other.getDesc());
    }

    public BKick CopyIfManaged() {
        return isManaged() ? Copy() : this;
    }

    public BKick Copy() {
        var copy = new BKick();
        copy.Assign(this);
        return copy;
    }

    public static void Swap(BKick a, BKick b) {
        BKick save = a.Copy();
        a.Assign(b);
        b.Assign(save);
    }

    @Override
    public Zeze.Transaction.Bean CopyBean() {
        return Copy();
    }

    public static final long TYPEID = 6126557663307790251L;

    @Override
    public long getTypeId() {
        return TYPEID;
    }

    private static final class Log__linksid extends Zeze.Transaction.Log1<BKick, Long> {
        public Log__linksid(BKick self, Long value) { super(self, value); }
        @Override
        public long getLogKey() { return this.getBean().getObjectId() + 1; }
        @Override
        public void Commit() { this.getBeanTyped()._linksid = this.getValue(); }
    }

    private static final class Log__code extends Zeze.Transaction.Log1<BKick, Integer> {
        public Log__code(BKick self, Integer value) { super(self, value); }
        @Override
        public long getLogKey() { return this.getBean().getObjectId() + 2; }
        @Override
        public void Commit() { this.getBeanTyped()._code = this.getValue(); }
    }

    private static final class Log__desc extends Zeze.Transaction.Log1<BKick, String> {
        public Log__desc(BKick self, String value) { super(self, value); }
        @Override
        public long getLogKey() { return this.getBean().getObjectId() + 3; }
        @Override
        public void Commit() { this.getBeanTyped()._desc = this.getValue(); }
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
        sb.append(Zeze.Util.Str.indent(level)).append("Zeze.Beans.Provider.BKick: {").append(System.lineSeparator());
        level += 4;
        sb.append(Zeze.Util.Str.indent(level)).append("linksid").append('=').append(getLinksid()).append(',').append(System.lineSeparator());
        sb.append(Zeze.Util.Str.indent(level)).append("code").append('=').append(getCode()).append(',').append(System.lineSeparator());
        sb.append(Zeze.Util.Str.indent(level)).append("desc").append('=').append(getDesc()).append(System.lineSeparator());
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
            long _x_ = getLinksid();
            if (_x_ != 0) {
                _i_ = _o_.WriteTag(_i_, 1, ByteBuffer.INTEGER);
                _o_.WriteLong(_x_);
            }
        }
        {
            int _x_ = getCode();
            if (_x_ != 0) {
                _i_ = _o_.WriteTag(_i_, 2, ByteBuffer.INTEGER);
                _o_.WriteInt(_x_);
            }
        }
        {
            String _x_ = getDesc();
            if (!_x_.isEmpty()) {
                _i_ = _o_.WriteTag(_i_, 3, ByteBuffer.BYTES);
                _o_.WriteString(_x_);
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
            setLinksid(_o_.ReadLong(_t_));
            _i_ += _o_.ReadTagSize(_t_ = _o_.ReadByte());
        }
        if (_i_ == 2) {
            setCode(_o_.ReadInt(_t_));
            _i_ += _o_.ReadTagSize(_t_ = _o_.ReadByte());
        }
        if (_i_ == 3) {
            setDesc(_o_.ReadString(_t_));
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
        if (getLinksid() < 0)
            return true;
        if (getCode() < 0)
            return true;
        return false;
    }
}