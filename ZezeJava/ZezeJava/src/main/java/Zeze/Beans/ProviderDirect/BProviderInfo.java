// auto-generated @formatter:off
package Zeze.Beans.ProviderDirect;

import Zeze.Serialize.ByteBuffer;

public final class BProviderInfo extends Zeze.Transaction.Bean {
    private String _Ip;
    private int _Port;

    public String getIp() {
        if (!isManaged())
            return _Ip;
        var txn = Zeze.Transaction.Transaction.getCurrent();
        if (txn == null)
            return _Ip;
        txn.VerifyRecordAccessed(this, true);
        var log = (Log__Ip)txn.GetLog(this.getObjectId() + 1);
        return log != null ? log.getValue() : _Ip;
    }

    public void setIp(String value) {
        if (value == null)
            throw new IllegalArgumentException();
        if (!isManaged()) {
            _Ip = value;
            return;
        }
        var txn = Zeze.Transaction.Transaction.getCurrent();
        assert txn != null;
        txn.VerifyRecordAccessed(this);
        txn.PutLog(new Log__Ip(this, value));
    }

    public int getPort() {
        if (!isManaged())
            return _Port;
        var txn = Zeze.Transaction.Transaction.getCurrent();
        if (txn == null)
            return _Port;
        txn.VerifyRecordAccessed(this, true);
        var log = (Log__Port)txn.GetLog(this.getObjectId() + 2);
        return log != null ? log.getValue() : _Port;
    }

    public void setPort(int value) {
        if (!isManaged()) {
            _Port = value;
            return;
        }
        var txn = Zeze.Transaction.Transaction.getCurrent();
        assert txn != null;
        txn.VerifyRecordAccessed(this);
        txn.PutLog(new Log__Port(this, value));
    }

    public BProviderInfo() {
         this(0);
    }

    public BProviderInfo(int _varId_) {
        super(_varId_);
        _Ip = "";
    }

    public void Assign(BProviderInfo other) {
        setIp(other.getIp());
        setPort(other.getPort());
    }

    public BProviderInfo CopyIfManaged() {
        return isManaged() ? Copy() : this;
    }

    public BProviderInfo Copy() {
        var copy = new BProviderInfo();
        copy.Assign(this);
        return copy;
    }

    public static void Swap(BProviderInfo a, BProviderInfo b) {
        BProviderInfo save = a.Copy();
        a.Assign(b);
        b.Assign(save);
    }

    @Override
    public Zeze.Transaction.Bean CopyBean() {
        return Copy();
    }

    public static final long TYPEID = 2259458453801663225L;

    @Override
    public long getTypeId() {
        return TYPEID;
    }

    private static final class Log__Ip extends Zeze.Transaction.Log1<BProviderInfo, String> {
        public Log__Ip(BProviderInfo self, String value) { super(self, value); }
        @Override
        public long getLogKey() { return this.getBean().getObjectId() + 1; }
        @Override
        public void Commit() { this.getBeanTyped()._Ip = this.getValue(); }
    }

    private static final class Log__Port extends Zeze.Transaction.Log1<BProviderInfo, Integer> {
        public Log__Port(BProviderInfo self, Integer value) { super(self, value); }
        @Override
        public long getLogKey() { return this.getBean().getObjectId() + 2; }
        @Override
        public void Commit() { this.getBeanTyped()._Port = this.getValue(); }
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
        sb.append(Zeze.Util.Str.indent(level)).append("Zeze.Beans.ProviderDirect.BProviderInfo: {").append(System.lineSeparator());
        level += 4;
        sb.append(Zeze.Util.Str.indent(level)).append("Ip").append('=').append(getIp()).append(',').append(System.lineSeparator());
        sb.append(Zeze.Util.Str.indent(level)).append("Port").append('=').append(getPort()).append(System.lineSeparator());
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
            String _x_ = getIp();
            if (!_x_.isEmpty()) {
                _i_ = _o_.WriteTag(_i_, 1, ByteBuffer.BYTES);
                _o_.WriteString(_x_);
            }
        }
        {
            int _x_ = getPort();
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
            setIp(_o_.ReadString(_t_));
            _i_ += _o_.ReadTagSize(_t_ = _o_.ReadByte());
        }
        if (_i_ == 2) {
            setPort(_o_.ReadInt(_t_));
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
        if (getPort() < 0)
            return true;
        return false;
    }
}