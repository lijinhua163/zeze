// auto-generated @formatter:off
package Zeze.Beans.Game.Online;

import Zeze.Serialize.ByteBuffer;

public final class BAccount extends Zeze.Transaction.Bean {
    private String _Name;
    private final Zeze.Transaction.Collections.PList1<Long> _Roles; // roleid list
    private long _LastLoginRoleId;

    public String getName() {
        if (!isManaged())
            return _Name;
        var txn = Zeze.Transaction.Transaction.getCurrent();
        if (txn == null)
            return _Name;
        txn.VerifyRecordAccessed(this, true);
        var log = (Log__Name)txn.GetLog(this.getObjectId() + 1);
        return log != null ? log.getValue() : _Name;
    }

    public void setName(String value) {
        if (value == null)
            throw new IllegalArgumentException();
        if (!isManaged()) {
            _Name = value;
            return;
        }
        var txn = Zeze.Transaction.Transaction.getCurrent();
        assert txn != null;
        txn.VerifyRecordAccessed(this);
        txn.PutLog(new Log__Name(this, value));
    }

    public Zeze.Transaction.Collections.PList1<Long> getRoles() {
        return _Roles;
    }

    public long getLastLoginRoleId() {
        if (!isManaged())
            return _LastLoginRoleId;
        var txn = Zeze.Transaction.Transaction.getCurrent();
        if (txn == null)
            return _LastLoginRoleId;
        txn.VerifyRecordAccessed(this, true);
        var log = (Log__LastLoginRoleId)txn.GetLog(this.getObjectId() + 3);
        return log != null ? log.getValue() : _LastLoginRoleId;
    }

    public void setLastLoginRoleId(long value) {
        if (!isManaged()) {
            _LastLoginRoleId = value;
            return;
        }
        var txn = Zeze.Transaction.Transaction.getCurrent();
        assert txn != null;
        txn.VerifyRecordAccessed(this);
        txn.PutLog(new Log__LastLoginRoleId(this, value));
    }

    public BAccount() {
         this(0);
    }

    public BAccount(int _varId_) {
        super(_varId_);
        _Name = "";
        _Roles = new Zeze.Transaction.Collections.PList1<>(getObjectId() + 2, (_v) -> new Log__Roles(this, _v));
    }

    public void Assign(BAccount other) {
        setName(other.getName());
        getRoles().clear();
        for (var e : other.getRoles())
            getRoles().add(e);
        setLastLoginRoleId(other.getLastLoginRoleId());
    }

    public BAccount CopyIfManaged() {
        return isManaged() ? Copy() : this;
    }

    public BAccount Copy() {
        var copy = new BAccount();
        copy.Assign(this);
        return copy;
    }

    public static void Swap(BAccount a, BAccount b) {
        BAccount save = a.Copy();
        a.Assign(b);
        b.Assign(save);
    }

    @Override
    public Zeze.Transaction.Bean CopyBean() {
        return Copy();
    }

    public static final long TYPEID = 8206234522906707244L;

    @Override
    public long getTypeId() {
        return TYPEID;
    }

    private static final class Log__Name extends Zeze.Transaction.Log1<BAccount, String> {
        public Log__Name(BAccount self, String value) { super(self, value); }
        @Override
        public long getLogKey() { return this.getBean().getObjectId() + 1; }
        @Override
        public void Commit() { this.getBeanTyped()._Name = this.getValue(); }
    }

    private static final class Log__Roles extends Zeze.Transaction.Collections.PList.LogV<Long> {
        public Log__Roles(BAccount host, org.pcollections.PVector<Long> value) { super(host, value); }
        @Override
        public long getLogKey() { return getBean().getObjectId() + 2; }
        public BAccount getBeanTyped() { return (BAccount)getBean(); }
        @Override
        public void Commit() { Commit(getBeanTyped()._Roles); }
    }

    private static final class Log__LastLoginRoleId extends Zeze.Transaction.Log1<BAccount, Long> {
        public Log__LastLoginRoleId(BAccount self, Long value) { super(self, value); }
        @Override
        public long getLogKey() { return this.getBean().getObjectId() + 3; }
        @Override
        public void Commit() { this.getBeanTyped()._LastLoginRoleId = this.getValue(); }
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
        sb.append(Zeze.Util.Str.indent(level)).append("Zeze.Beans.Game.Online.BAccount: {").append(System.lineSeparator());
        level += 4;
        sb.append(Zeze.Util.Str.indent(level)).append("Name").append('=').append(getName()).append(',').append(System.lineSeparator());
        sb.append(Zeze.Util.Str.indent(level)).append("Roles").append("=[").append(System.lineSeparator());
        level += 4;
        for (var _item_ : getRoles()) {
            sb.append(Zeze.Util.Str.indent(level)).append("Item").append('=').append(_item_).append(',').append(System.lineSeparator());
        }
        level -= 4;
        sb.append(Zeze.Util.Str.indent(level)).append(']').append(',').append(System.lineSeparator());
        sb.append(Zeze.Util.Str.indent(level)).append("LastLoginRoleId").append('=').append(getLastLoginRoleId()).append(System.lineSeparator());
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
            String _x_ = getName();
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
            long _x_ = getLastLoginRoleId();
            if (_x_ != 0) {
                _i_ = _o_.WriteTag(_i_, 3, ByteBuffer.INTEGER);
                _o_.WriteLong(_x_);
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
            setName(_o_.ReadString(_t_));
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
            setLastLoginRoleId(_o_.ReadLong(_t_));
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

    @SuppressWarnings("RedundantIfStatement")
    @Override
    public boolean NegativeCheck() {
        for (var _v_ : getRoles()) {
            if (_v_ < 0)
                return true;
        }
        if (getLastLoginRoleId() < 0)
            return true;
        return false;
    }
}