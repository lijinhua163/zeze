// auto-generated @formatter:off
package Zeze.Builtin.ProviderDirect;

import Zeze.Serialize.ByteBuffer;

@SuppressWarnings({"UnusedAssignment", "RedundantIfStatement", "SwitchStatementWithTooFewBranches", "RedundantSuppression"})
public final class BTransmitAccount extends Zeze.Transaction.Bean {
    private String _ActionName;
    private Zeze.Net.Binary _Parameter; // encoded bean
    private final Zeze.Transaction.Collections.PSet1<String> _TargetAccounts; // 查询目标角色。
    private String _SenderAccount; // 结果发送给Sender。
    private String _SenderClientId; // 结果发送给Sender。

    public String getActionName() {
        if (!isManaged())
            return _ActionName;
        var txn = Zeze.Transaction.Transaction.getCurrent();
        if (txn == null)
            return _ActionName;
        txn.VerifyRecordAccessed(this, true);
        var log = (Log__ActionName)txn.GetLog(this.getObjectId() + 1);
        return log != null ? log.Value : _ActionName;
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
        txn.PutLog(new Log__ActionName(this, 1, value));
    }

    public Zeze.Net.Binary getParameter() {
        if (!isManaged())
            return _Parameter;
        var txn = Zeze.Transaction.Transaction.getCurrent();
        if (txn == null)
            return _Parameter;
        txn.VerifyRecordAccessed(this, true);
        var log = (Log__Parameter)txn.GetLog(this.getObjectId() + 2);
        return log != null ? log.Value : _Parameter;
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
        txn.PutLog(new Log__Parameter(this, 2, value));
    }

    public Zeze.Transaction.Collections.PSet1<String> getTargetAccounts() {
        return _TargetAccounts;
    }

    public String getSenderAccount() {
        if (!isManaged())
            return _SenderAccount;
        var txn = Zeze.Transaction.Transaction.getCurrent();
        if (txn == null)
            return _SenderAccount;
        txn.VerifyRecordAccessed(this, true);
        var log = (Log__SenderAccount)txn.GetLog(this.getObjectId() + 4);
        return log != null ? log.Value : _SenderAccount;
    }

    public void setSenderAccount(String value) {
        if (value == null)
            throw new IllegalArgumentException();
        if (!isManaged()) {
            _SenderAccount = value;
            return;
        }
        var txn = Zeze.Transaction.Transaction.getCurrent();
        assert txn != null;
        txn.VerifyRecordAccessed(this);
        txn.PutLog(new Log__SenderAccount(this, 4, value));
    }

    public String getSenderClientId() {
        if (!isManaged())
            return _SenderClientId;
        var txn = Zeze.Transaction.Transaction.getCurrent();
        if (txn == null)
            return _SenderClientId;
        txn.VerifyRecordAccessed(this, true);
        var log = (Log__SenderClientId)txn.GetLog(this.getObjectId() + 5);
        return log != null ? log.Value : _SenderClientId;
    }

    public void setSenderClientId(String value) {
        if (value == null)
            throw new IllegalArgumentException();
        if (!isManaged()) {
            _SenderClientId = value;
            return;
        }
        var txn = Zeze.Transaction.Transaction.getCurrent();
        assert txn != null;
        txn.VerifyRecordAccessed(this);
        txn.PutLog(new Log__SenderClientId(this, 5, value));
    }

    public BTransmitAccount() {
         this(0);
    }

    public BTransmitAccount(int _varId_) {
        super(_varId_);
        _ActionName = "";
        _Parameter = Zeze.Net.Binary.Empty;
        _TargetAccounts = new Zeze.Transaction.Collections.PSet1<>(String.class);
        _TargetAccounts.VariableId = 3;
        _SenderAccount = "";
        _SenderClientId = "";
    }

    public void Assign(BTransmitAccount other) {
        setActionName(other.getActionName());
        setParameter(other.getParameter());
        getTargetAccounts().clear();
        for (var e : other.getTargetAccounts())
            getTargetAccounts().add(e);
        setSenderAccount(other.getSenderAccount());
        setSenderClientId(other.getSenderClientId());
    }

    public BTransmitAccount CopyIfManaged() {
        return isManaged() ? Copy() : this;
    }

    public BTransmitAccount Copy() {
        var copy = new BTransmitAccount();
        copy.Assign(this);
        return copy;
    }

    public static void Swap(BTransmitAccount a, BTransmitAccount b) {
        BTransmitAccount save = a.Copy();
        a.Assign(b);
        b.Assign(save);
    }

    @Override
    public Zeze.Transaction.Bean CopyBean() {
        return Copy();
    }

    public static final long TYPEID = 2637210793748287339L;

    @Override
    public long getTypeId() {
        return TYPEID;
    }

    private static final class Log__ActionName extends Zeze.Transaction.Logs.LogString {
        public Log__ActionName(BTransmitAccount bean, int varId, String value) { super(bean, varId, value); }

        @Override
        public void Commit() { ((BTransmitAccount)getBelong())._ActionName = Value; }
    }

    private static final class Log__Parameter extends Zeze.Transaction.Logs.LogBinary {
        public Log__Parameter(BTransmitAccount bean, int varId, Zeze.Net.Binary value) { super(bean, varId, value); }

        @Override
        public void Commit() { ((BTransmitAccount)getBelong())._Parameter = Value; }
    }

    private static final class Log__SenderAccount extends Zeze.Transaction.Logs.LogString {
        public Log__SenderAccount(BTransmitAccount bean, int varId, String value) { super(bean, varId, value); }

        @Override
        public void Commit() { ((BTransmitAccount)getBelong())._SenderAccount = Value; }
    }

    private static final class Log__SenderClientId extends Zeze.Transaction.Logs.LogString {
        public Log__SenderClientId(BTransmitAccount bean, int varId, String value) { super(bean, varId, value); }

        @Override
        public void Commit() { ((BTransmitAccount)getBelong())._SenderClientId = Value; }
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
        sb.append(Zeze.Util.Str.indent(level)).append("Zeze.Builtin.ProviderDirect.BTransmitAccount: {").append(System.lineSeparator());
        level += 4;
        sb.append(Zeze.Util.Str.indent(level)).append("ActionName").append('=').append(getActionName()).append(',').append(System.lineSeparator());
        sb.append(Zeze.Util.Str.indent(level)).append("Parameter").append('=').append(getParameter()).append(',').append(System.lineSeparator());
        sb.append(Zeze.Util.Str.indent(level)).append("TargetAccounts").append("=[").append(System.lineSeparator());
        level += 4;
        for (var _item_ : getTargetAccounts()) {
            sb.append(Zeze.Util.Str.indent(level)).append("Item").append('=').append(_item_).append(',').append(System.lineSeparator());
        }
        level -= 4;
        sb.append(Zeze.Util.Str.indent(level)).append(']').append(',').append(System.lineSeparator());
        sb.append(Zeze.Util.Str.indent(level)).append("SenderAccount").append('=').append(getSenderAccount()).append(',').append(System.lineSeparator());
        sb.append(Zeze.Util.Str.indent(level)).append("SenderClientId").append('=').append(getSenderClientId()).append(System.lineSeparator());
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
            var _x_ = getParameter();
            if (_x_.size() != 0) {
                _i_ = _o_.WriteTag(_i_, 2, ByteBuffer.BYTES);
                _o_.WriteBinary(_x_);
            }
        }
        {
            var _x_ = getTargetAccounts();
            int _n_ = _x_.size();
            if (_n_ != 0) {
                _i_ = _o_.WriteTag(_i_, 3, ByteBuffer.LIST);
                _o_.WriteListType(_n_, ByteBuffer.BYTES);
                for (var _v_ : _x_)
                    _o_.WriteString(_v_);
            }
        }
        {
            String _x_ = getSenderAccount();
            if (!_x_.isEmpty()) {
                _i_ = _o_.WriteTag(_i_, 4, ByteBuffer.BYTES);
                _o_.WriteString(_x_);
            }
        }
        {
            String _x_ = getSenderClientId();
            if (!_x_.isEmpty()) {
                _i_ = _o_.WriteTag(_i_, 5, ByteBuffer.BYTES);
                _o_.WriteString(_x_);
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
            setParameter(_o_.ReadBinary(_t_));
            _i_ += _o_.ReadTagSize(_t_ = _o_.ReadByte());
        }
        if (_i_ == 3) {
            var _x_ = getTargetAccounts();
            _x_.clear();
            if ((_t_ & ByteBuffer.TAG_MASK) == ByteBuffer.LIST) {
                for (int _n_ = _o_.ReadTagSize(_t_ = _o_.ReadByte()); _n_ > 0; _n_--)
                    _x_.add(_o_.ReadString(_t_));
            } else
                _o_.SkipUnknownField(_t_);
            _i_ += _o_.ReadTagSize(_t_ = _o_.ReadByte());
        }
        if (_i_ == 4) {
            setSenderAccount(_o_.ReadString(_t_));
            _i_ += _o_.ReadTagSize(_t_ = _o_.ReadByte());
        }
        if (_i_ == 5) {
            setSenderClientId(_o_.ReadString(_t_));
            _i_ += _o_.ReadTagSize(_t_ = _o_.ReadByte());
        }
        while (_t_ != 0) {
            _o_.SkipUnknownField(_t_);
            _o_.ReadTagSize(_t_ = _o_.ReadByte());
        }
    }

    @Override
    protected void InitChildrenRootInfo(Zeze.Transaction.Record.RootInfo root) {
        _TargetAccounts.InitRootInfo(root, this);
    }

    @Override
    public boolean NegativeCheck() {
        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void FollowerApply(Zeze.Transaction.Log log) {
        var vars = ((Zeze.Transaction.Collections.LogBean)log).getVariables();
        if (vars == null)
            return;
        for (var it = vars.iterator(); it.moveToNext(); ) {
            var vlog = it.value();
            switch (vlog.getVariableId()) {
                case 1: _ActionName = ((Zeze.Transaction.Logs.LogString)vlog).Value; break;
                case 2: _Parameter = ((Zeze.Transaction.Logs.LogBinary)vlog).Value; break;
                case 3: _TargetAccounts.FollowerApply(vlog); break;
                case 4: _SenderAccount = ((Zeze.Transaction.Logs.LogString)vlog).Value; break;
                case 5: _SenderClientId = ((Zeze.Transaction.Logs.LogString)vlog).Value; break;
            }
        }
    }
}
