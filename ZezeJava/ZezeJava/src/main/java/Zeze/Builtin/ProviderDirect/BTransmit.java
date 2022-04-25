// auto-generated @formatter:off
package Zeze.Builtin.ProviderDirect;

import Zeze.Serialize.ByteBuffer;

@SuppressWarnings({"UnusedAssignment", "RedundantIfStatement", "SwitchStatementWithTooFewBranches", "RedundantSuppression"})
public final class BTransmit extends Zeze.Transaction.Bean {
    private String _ActionName;
    private final Zeze.Transaction.Collections.PSet1<Long> _Roles; // 查询目标角色。
    private long _Sender; // 结果发送给Sender。
    private Zeze.Net.Binary _Parameter; // encoded bean

    public String getActionName() {
        if (!isManaged())
            return _ActionName;
        var txn = Zeze.Transaction.Transaction.getCurrent();
        if (txn == null)
            return _ActionName;
        txn.VerifyRecordAccessed(this, true);
        var log = (Log__ActionName)txn.GetLog(this.getObjectId() + 1);
        return log != null ? log.getValue() : _ActionName;
    }

    public void setActionName(String value) {
        if (value == null)
            throw new IllegalArgumentException();
        if (!isManaged()) {
            _ActionName = value;
            return;
        }
        var txn = Zeze.Transaction.Transaction.getCurrent();
        assert txn != null;
        txn.VerifyRecordAccessed(this);
        txn.PutLog(new Log__ActionName(this, value));
    }

    public Zeze.Transaction.Collections.PSet1<Long> getRoles() {
        return _Roles;
    }

    public long getSender() {
        if (!isManaged())
            return _Sender;
        var txn = Zeze.Transaction.Transaction.getCurrent();
        if (txn == null)
            return _Sender;
        txn.VerifyRecordAccessed(this, true);
        var log = (Log__Sender)txn.GetLog(this.getObjectId() + 3);
        return log != null ? log.getValue() : _Sender;
    }

    public void setSender(long value) {
        if (!isManaged()) {
            _Sender = value;
            return;
        }
        var txn = Zeze.Transaction.Transaction.getCurrent();
        assert txn != null;
        txn.VerifyRecordAccessed(this);
        txn.PutLog(new Log__Sender(this, value));
    }

    public Zeze.Net.Binary getParameter() {
        if (!isManaged())
            return _Parameter;
        var txn = Zeze.Transaction.Transaction.getCurrent();
        if (txn == null)
            return _Parameter;
        txn.VerifyRecordAccessed(this, true);
        var log = (Log__Parameter)txn.GetLog(this.getObjectId() + 4);
        return log != null ? log.getValue() : _Parameter;
    }

    public void setParameter(Zeze.Net.Binary value) {
        if (value == null)
            throw new IllegalArgumentException();
        if (!isManaged()) {
            _Parameter = value;
            return;
        }
        var txn = Zeze.Transaction.Transaction.getCurrent();
        assert txn != null;
        txn.VerifyRecordAccessed(this);
        txn.PutLog(new Log__Parameter(this, value));
    }

    public BTransmit() {
         this(0);
    }

    public BTransmit(int _varId_) {
        super(_varId_);
        _ActionName = "";
        _Roles = new Zeze.Transaction.Collections.PSet1<>(getObjectId() + 2, (_v) -> new Log__Roles(this, _v));
        _Parameter = Zeze.Net.Binary.Empty;
    }

    public void Assign(BTransmit other) {
        setActionName(other.getActionName());
        getRoles().clear();
        for (var e : other.getRoles())
            getRoles().add(e);
        setSender(other.getSender());
        setParameter(other.getParameter());
    }

    public BTransmit CopyIfManaged() {
        return isManaged() ? Copy() : this;
    }

    public BTransmit Copy() {
        var copy = new BTransmit();
        copy.Assign(this);
        return copy;
    }

    public static void Swap(BTransmit a, BTransmit b) {
        BTransmit save = a.Copy();
        a.Assign(b);
        b.Assign(save);
    }

    @Override
    public Zeze.Transaction.Bean CopyBean() {
        return Copy();
    }

    public static final long TYPEID = 7395081565293443928L;

    @Override
    public long getTypeId() {
        return TYPEID;
    }

    private static final class Log__ActionName extends Zeze.Transaction.Log1<BTransmit, String> {
        public Log__ActionName(BTransmit self, String value) { super(self, value); }
        @Override
        public long getLogKey() { return this.getBean().getObjectId() + 1; }
        @Override
        public void Commit() { this.getBeanTyped()._ActionName = this.getValue(); }
    }

    private static final class Log__Roles extends Zeze.Transaction.Collections.PSet.LogV<Long> {
        public Log__Roles(BTransmit host, org.pcollections.PSet<Long> value) { super(host, value); }
        @Override
        public long getLogKey() { return getBean().getObjectId() + 2; }
        public BTransmit getBeanTyped() { return (BTransmit)getBean(); }
        @Override
        public void Commit() { Commit(getBeanTyped()._Roles); }
    }

    private static final class Log__Sender extends Zeze.Transaction.Log1<BTransmit, Long> {
        public Log__Sender(BTransmit self, Long value) { super(self, value); }
        @Override
        public long getLogKey() { return this.getBean().getObjectId() + 3; }
        @Override
        public void Commit() { this.getBeanTyped()._Sender = this.getValue(); }
    }

    private static final class Log__Parameter extends Zeze.Transaction.Log1<BTransmit, Zeze.Net.Binary> {
        public Log__Parameter(BTransmit self, Zeze.Net.Binary value) { super(self, value); }
        @Override
        public long getLogKey() { return this.getBean().getObjectId() + 4; }
        @Override
        public void Commit() { this.getBeanTyped()._Parameter = this.getValue(); }
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
        sb.append(Zeze.Util.Str.indent(level)).append("Zeze.Builtin.ProviderDirect.BTransmit: {").append(System.lineSeparator());
        level += 4;
        sb.append(Zeze.Util.Str.indent(level)).append("ActionName").append('=').append(getActionName()).append(',').append(System.lineSeparator());
        sb.append(Zeze.Util.Str.indent(level)).append("Roles").append("=[").append(System.lineSeparator());
        level += 4;
        for (var _item_ : getRoles()) {
            sb.append(Zeze.Util.Str.indent(level)).append("Item").append('=').append(_item_).append(',').append(System.lineSeparator());
        }
        level -= 4;
        sb.append(Zeze.Util.Str.indent(level)).append(']').append(',').append(System.lineSeparator());
        sb.append(Zeze.Util.Str.indent(level)).append("Sender").append('=').append(getSender()).append(',').append(System.lineSeparator());
        sb.append(Zeze.Util.Str.indent(level)).append("Parameter").append('=').append(getParameter()).append(System.lineSeparator());
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

    @Override
    public void Encode(ByteBuffer _o_) {
        int _i_ = 0;
        {
            String _x_ = getActionName();
            if (!_x_.isEmpty()) {
                _i_ = _o_.WriteTag(_i_, 1, ByteBuffer.BYTES);
                _o_.WriteString(_x_);
            }
        }
        {
            var _x_ = getRoles();
            int _n_ = _x_.size();
            if (_n_ != 0) {
                _i_ = _o_.WriteTag(_i_, 2, ByteBuffer.LIST);
                _o_.WriteListType(_n_, ByteBuffer.INTEGER);
                for (var _v_ : _x_)
                    _o_.WriteLong(_v_);
            }
        }
        {
            long _x_ = getSender();
            if (_x_ != 0) {
                _i_ = _o_.WriteTag(_i_, 3, ByteBuffer.INTEGER);
                _o_.WriteLong(_x_);
            }
        }
        {
            var _x_ = getParameter();
            if (_x_.size() != 0) {
                _i_ = _o_.WriteTag(_i_, 4, ByteBuffer.BYTES);
                _o_.WriteBinary(_x_);
            }
        }
        _o_.WriteByte(0);
    }

    @Override
    public void Decode(ByteBuffer _o_) {
        int _t_ = _o_.ReadByte();
        int _i_ = _o_.ReadTagSize(_t_);
        if (_i_ == 1) {
            setActionName(_o_.ReadString(_t_));
            _i_ += _o_.ReadTagSize(_t_ = _o_.ReadByte());
        }
        if (_i_ == 2) {
            var _x_ = getRoles();
            _x_.clear();
            if ((_t_ & ByteBuffer.TAG_MASK) == ByteBuffer.LIST) {
                for (int _n_ = _o_.ReadTagSize(_t_ = _o_.ReadByte()); _n_ > 0; _n_--)
                    _x_.add(_o_.ReadLong(_t_));
            } else
                _o_.SkipUnknownField(_t_);
            _i_ += _o_.ReadTagSize(_t_ = _o_.ReadByte());
        }
        if (_i_ == 3) {
            setSender(_o_.ReadLong(_t_));
            _i_ += _o_.ReadTagSize(_t_ = _o_.ReadByte());
        }
        if (_i_ == 4) {
            setParameter(_o_.ReadBinary(_t_));
            _i_ += _o_.ReadTagSize(_t_ = _o_.ReadByte());
        }
        while (_t_ != 0) {
            _o_.SkipUnknownField(_t_);
            _o_.ReadTagSize(_t_ = _o_.ReadByte());
        }
    }

    @Override
    protected void InitChildrenRootInfo(Zeze.Transaction.Record.RootInfo root) {
        _Roles.InitRootInfo(root, this);
    }

    @Override
    public boolean NegativeCheck() {
        for (var _v_ : getRoles()) {
            if (_v_ < 0)
                return true;
        }
        if (getSender() < 0)
            return true;
        return false;
    }
}
