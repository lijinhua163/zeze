// auto-generated @formatter:off
package Zeze.Builtin.Game.Bag;

import Zeze.Serialize.ByteBuffer;

public final class BMove extends Zeze.Transaction.Bean {
    private String _BagName;
    private int _PositionFrom;
    private int _PositionTo;
    private int _number; // -1 表示全部

    public String getBagName() {
        if (!isManaged())
            return _BagName;
        var txn = Zeze.Transaction.Transaction.getCurrent();
        if (txn == null)
            return _BagName;
        txn.VerifyRecordAccessed(this, true);
        var log = (Log__BagName)txn.GetLog(this.getObjectId() + 1);
        return log != null ? log.getValue() : _BagName;
    }

    public void setBagName(String value) {
        if (value == null)
            throw new IllegalArgumentException();
        if (!isManaged()) {
            _BagName = value;
            return;
        }
        var txn = Zeze.Transaction.Transaction.getCurrent();
        assert txn != null;
        txn.VerifyRecordAccessed(this);
        txn.PutLog(new Log__BagName(this, value));
    }

    public int getPositionFrom() {
        if (!isManaged())
            return _PositionFrom;
        var txn = Zeze.Transaction.Transaction.getCurrent();
        if (txn == null)
            return _PositionFrom;
        txn.VerifyRecordAccessed(this, true);
        var log = (Log__PositionFrom)txn.GetLog(this.getObjectId() + 2);
        return log != null ? log.getValue() : _PositionFrom;
    }

    public void setPositionFrom(int value) {
        if (!isManaged()) {
            _PositionFrom = value;
            return;
        }
        var txn = Zeze.Transaction.Transaction.getCurrent();
        assert txn != null;
        txn.VerifyRecordAccessed(this);
        txn.PutLog(new Log__PositionFrom(this, value));
    }

    public int getPositionTo() {
        if (!isManaged())
            return _PositionTo;
        var txn = Zeze.Transaction.Transaction.getCurrent();
        if (txn == null)
            return _PositionTo;
        txn.VerifyRecordAccessed(this, true);
        var log = (Log__PositionTo)txn.GetLog(this.getObjectId() + 3);
        return log != null ? log.getValue() : _PositionTo;
    }

    public void setPositionTo(int value) {
        if (!isManaged()) {
            _PositionTo = value;
            return;
        }
        var txn = Zeze.Transaction.Transaction.getCurrent();
        assert txn != null;
        txn.VerifyRecordAccessed(this);
        txn.PutLog(new Log__PositionTo(this, value));
    }

    public int getNumber() {
        if (!isManaged())
            return _number;
        var txn = Zeze.Transaction.Transaction.getCurrent();
        if (txn == null)
            return _number;
        txn.VerifyRecordAccessed(this, true);
        var log = (Log__number)txn.GetLog(this.getObjectId() + 4);
        return log != null ? log.getValue() : _number;
    }

    public void setNumber(int value) {
        if (!isManaged()) {
            _number = value;
            return;
        }
        var txn = Zeze.Transaction.Transaction.getCurrent();
        assert txn != null;
        txn.VerifyRecordAccessed(this);
        txn.PutLog(new Log__number(this, value));
    }

    public BMove() {
         this(0);
    }

    public BMove(int _varId_) {
        super(_varId_);
        _BagName = "";
    }

    public void Assign(BMove other) {
        setBagName(other.getBagName());
        setPositionFrom(other.getPositionFrom());
        setPositionTo(other.getPositionTo());
        setNumber(other.getNumber());
    }

    public BMove CopyIfManaged() {
        return isManaged() ? Copy() : this;
    }

    public BMove Copy() {
        var copy = new BMove();
        copy.Assign(this);
        return copy;
    }

    public static void Swap(BMove a, BMove b) {
        BMove save = a.Copy();
        a.Assign(b);
        b.Assign(save);
    }

    @Override
    public Zeze.Transaction.Bean CopyBean() {
        return Copy();
    }

    public static final long TYPEID = -7346236832819011963L;

    @Override
    public long getTypeId() {
        return TYPEID;
    }

    private static final class Log__BagName extends Zeze.Transaction.Log1<BMove, String> {
        public Log__BagName(BMove self, String value) { super(self, value); }
        @Override
        public long getLogKey() { return this.getBean().getObjectId() + 1; }
        @Override
        public void Commit() { this.getBeanTyped()._BagName = this.getValue(); }
    }

    private static final class Log__PositionFrom extends Zeze.Transaction.Log1<BMove, Integer> {
        public Log__PositionFrom(BMove self, Integer value) { super(self, value); }
        @Override
        public long getLogKey() { return this.getBean().getObjectId() + 2; }
        @Override
        public void Commit() { this.getBeanTyped()._PositionFrom = this.getValue(); }
    }

    private static final class Log__PositionTo extends Zeze.Transaction.Log1<BMove, Integer> {
        public Log__PositionTo(BMove self, Integer value) { super(self, value); }
        @Override
        public long getLogKey() { return this.getBean().getObjectId() + 3; }
        @Override
        public void Commit() { this.getBeanTyped()._PositionTo = this.getValue(); }
    }

    private static final class Log__number extends Zeze.Transaction.Log1<BMove, Integer> {
        public Log__number(BMove self, Integer value) { super(self, value); }
        @Override
        public long getLogKey() { return this.getBean().getObjectId() + 4; }
        @Override
        public void Commit() { this.getBeanTyped()._number = this.getValue(); }
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
        sb.append(Zeze.Util.Str.indent(level)).append("Zeze.Builtin.Game.Bag.BMove: {").append(System.lineSeparator());
        level += 4;
        sb.append(Zeze.Util.Str.indent(level)).append("BagName").append('=').append(getBagName()).append(',').append(System.lineSeparator());
        sb.append(Zeze.Util.Str.indent(level)).append("PositionFrom").append('=').append(getPositionFrom()).append(',').append(System.lineSeparator());
        sb.append(Zeze.Util.Str.indent(level)).append("PositionTo").append('=').append(getPositionTo()).append(',').append(System.lineSeparator());
        sb.append(Zeze.Util.Str.indent(level)).append("number").append('=').append(getNumber()).append(System.lineSeparator());
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
            String _x_ = getBagName();
            if (!_x_.isEmpty()) {
                _i_ = _o_.WriteTag(_i_, 1, ByteBuffer.BYTES);
                _o_.WriteString(_x_);
            }
        }
        {
            int _x_ = getPositionFrom();
            if (_x_ != 0) {
                _i_ = _o_.WriteTag(_i_, 2, ByteBuffer.INTEGER);
                _o_.WriteInt(_x_);
            }
        }
        {
            int _x_ = getPositionTo();
            if (_x_ != 0) {
                _i_ = _o_.WriteTag(_i_, 3, ByteBuffer.INTEGER);
                _o_.WriteInt(_x_);
            }
        }
        {
            int _x_ = getNumber();
            if (_x_ != 0) {
                _i_ = _o_.WriteTag(_i_, 4, ByteBuffer.INTEGER);
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
            setBagName(_o_.ReadString(_t_));
            _i_ += _o_.ReadTagSize(_t_ = _o_.ReadByte());
        }
        if (_i_ == 2) {
            setPositionFrom(_o_.ReadInt(_t_));
            _i_ += _o_.ReadTagSize(_t_ = _o_.ReadByte());
        }
        if (_i_ == 3) {
            setPositionTo(_o_.ReadInt(_t_));
            _i_ += _o_.ReadTagSize(_t_ = _o_.ReadByte());
        }
        if (_i_ == 4) {
            setNumber(_o_.ReadInt(_t_));
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
        if (getPositionFrom() < 0)
            return true;
        if (getPositionTo() < 0)
            return true;
        if (getNumber() < 0)
            return true;
        return false;
    }
}