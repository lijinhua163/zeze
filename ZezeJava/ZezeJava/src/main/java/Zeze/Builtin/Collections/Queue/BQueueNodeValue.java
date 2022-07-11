// auto-generated @formatter:off
package Zeze.Builtin.Collections.Queue;

import Zeze.Serialize.ByteBuffer;

@SuppressWarnings({"UnusedAssignment", "RedundantIfStatement", "SwitchStatementWithTooFewBranches", "RedundantSuppression"})
public final class BQueueNodeValue extends Zeze.Transaction.Bean {
    private long _Timestamp;
    private final Zeze.Transaction.DynamicBean _Value;
        public static long GetSpecialTypeIdFromBean_Value(Zeze.Transaction.Bean bean) {
            var _typeId_ = bean.getTypeId();
            if (_typeId_ == Zeze.Transaction.EmptyBean.TYPEID)
                return Zeze.Transaction.EmptyBean.TYPEID;
            throw new RuntimeException("Unknown Bean! dynamic@Zeze.Builtin.Collections.Queue.BQueueNodeValue:Value");
        }

        public static Zeze.Transaction.Bean CreateBeanFromSpecialTypeId_Value(long typeId) {
            return null;
        }


    public long getTimestamp() {
        if (!isManaged())
            return _Timestamp;
        var txn = Zeze.Transaction.Transaction.getCurrent();
        if (txn == null)
            return _Timestamp;
        txn.VerifyRecordAccessed(this, true);
        var log = (Log__Timestamp)txn.GetLog(this.getObjectId() + 1);
        return log != null ? log.Value : _Timestamp;
    }

    public void setTimestamp(long value) {
        if (!isManaged()) {
            _Timestamp = value;
            return;
        }
        var txn = Zeze.Transaction.Transaction.getCurrent();
        assert txn != null;
        txn.VerifyRecordAccessed(this);
        txn.PutLog(new Log__Timestamp(this, 1, value));
    }

    public Zeze.Transaction.DynamicBean getValue() {
        return _Value;
    }

    public BQueueNodeValue() {
         this(0);
    }

    public BQueueNodeValue(int _varId_) {
        super(_varId_);
        _Value = new Zeze.Transaction.DynamicBean(2, Zeze.Collections.Queue::GetSpecialTypeIdFromBean, Zeze.Collections.Queue::CreateBeanFromSpecialTypeId);
    }

    public void Assign(BQueueNodeValue other) {
        setTimestamp(other.getTimestamp());
        getValue().Assign(other.getValue());
    }

    public BQueueNodeValue CopyIfManaged() {
        return isManaged() ? Copy() : this;
    }

    public BQueueNodeValue Copy() {
        var copy = new BQueueNodeValue();
        copy.Assign(this);
        return copy;
    }

    public static void Swap(BQueueNodeValue a, BQueueNodeValue b) {
        BQueueNodeValue save = a.Copy();
        a.Assign(b);
        b.Assign(save);
    }

    @Override
    public Zeze.Transaction.Bean CopyBean() {
        return Copy();
    }

    public static final long TYPEID = 486912310764000976L;

    @Override
    public long getTypeId() {
        return TYPEID;
    }

    private static final class Log__Timestamp extends Zeze.Transaction.Logs.LogLong {
        public Log__Timestamp(BQueueNodeValue bean, int varId, long value) { super(bean, varId, value); }

        @Override
        public void Commit() { ((BQueueNodeValue)getBelong())._Timestamp = Value; }
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
        sb.append(Zeze.Util.Str.indent(level)).append("Zeze.Builtin.Collections.Queue.BQueueNodeValue: {").append(System.lineSeparator());
        level += 4;
        sb.append(Zeze.Util.Str.indent(level)).append("Timestamp").append('=').append(getTimestamp()).append(',').append(System.lineSeparator());
        sb.append(Zeze.Util.Str.indent(level)).append("Value").append('=').append(System.lineSeparator());
        getValue().getBean().BuildString(sb, level + 4);
        sb.append(System.lineSeparator());
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
            long _x_ = getTimestamp();
            if (_x_ != 0) {
                _i_ = _o_.WriteTag(_i_, 1, ByteBuffer.INTEGER);
                _o_.WriteLong(_x_);
            }
        }
        {
            var _x_ = getValue();
            if (!_x_.isEmpty()) {
                _i_ = _o_.WriteTag(_i_, 2, ByteBuffer.DYNAMIC);
                _x_.Encode(_o_);
            }
        }
        _o_.WriteByte(0);
    }

    @Override
    public void Decode(ByteBuffer _o_) {
        int _t_ = _o_.ReadByte();
        int _i_ = _o_.ReadTagSize(_t_);
        if (_i_ == 1) {
            setTimestamp(_o_.ReadLong(_t_));
            _i_ += _o_.ReadTagSize(_t_ = _o_.ReadByte());
        }
        if (_i_ == 2) {
            _o_.ReadDynamic(getValue(), _t_);
            _i_ += _o_.ReadTagSize(_t_ = _o_.ReadByte());
        }
        while (_t_ != 0) {
            _o_.SkipUnknownField(_t_);
            _o_.ReadTagSize(_t_ = _o_.ReadByte());
        }
    }

    @Override
    protected void InitChildrenRootInfo(Zeze.Transaction.Record.RootInfo root) {
        _Value.InitRootInfo(root, this);
    }

    @Override
    public boolean NegativeCheck() {
        if (getTimestamp() < 0)
            return true;
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
                case 1: _Timestamp = ((Zeze.Transaction.Logs.LogLong)vlog).Value; break;
                case 2: _Value.FollowerApply(vlog); break;
            }
        }
    }
}
