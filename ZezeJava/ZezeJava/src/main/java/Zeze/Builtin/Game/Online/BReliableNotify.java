// auto-generated @formatter:off
package Zeze.Builtin.Game.Online;

import Zeze.Serialize.ByteBuffer;

@SuppressWarnings({"UnusedAssignment", "RedundantIfStatement", "SwitchStatementWithTooFewBranches", "RedundantSuppression"})
public final class BReliableNotify extends Zeze.Transaction.Bean {
    private final Zeze.Transaction.Collections.PList1<Zeze.Net.Binary> _Notifies; // full encoded protocol list
    private long _ReliableNotifyTotalCountStart; // Notify的计数开始。客户端收到的总计数为：start + Notifies.Count

    public Zeze.Transaction.Collections.PList1<Zeze.Net.Binary> getNotifies() {
        return _Notifies;
    }

    public long getReliableNotifyTotalCountStart() {
        if (!isManaged())
            return _ReliableNotifyTotalCountStart;
        var txn = Zeze.Transaction.Transaction.getCurrent();
        if (txn == null)
            return _ReliableNotifyTotalCountStart;
        txn.VerifyRecordAccessed(this, true);
        var log = (Log__ReliableNotifyTotalCountStart)txn.GetLog(this.getObjectId() + 2);
        return log != null ? log.getValue() : _ReliableNotifyTotalCountStart;
    }

    public void setReliableNotifyTotalCountStart(long value) {
        if (!isManaged()) {
            _ReliableNotifyTotalCountStart = value;
            return;
        }
        var txn = Zeze.Transaction.Transaction.getCurrent();
        assert txn != null;
        txn.VerifyRecordAccessed(this);
        txn.PutLog(new Log__ReliableNotifyTotalCountStart(this, value));
    }

    public BReliableNotify() {
         this(0);
    }

    public BReliableNotify(int _varId_) {
        super(_varId_);
        _Notifies = new Zeze.Transaction.Collections.PList1<>(getObjectId() + 1, (_v) -> new Log__Notifies(this, _v));
    }

    public void Assign(BReliableNotify other) {
        getNotifies().clear();
        for (var e : other.getNotifies())
            getNotifies().add(e);
        setReliableNotifyTotalCountStart(other.getReliableNotifyTotalCountStart());
    }

    public BReliableNotify CopyIfManaged() {
        return isManaged() ? Copy() : this;
    }

    public BReliableNotify Copy() {
        var copy = new BReliableNotify();
        copy.Assign(this);
        return copy;
    }

    public static void Swap(BReliableNotify a, BReliableNotify b) {
        BReliableNotify save = a.Copy();
        a.Assign(b);
        b.Assign(save);
    }

    @Override
    public Zeze.Transaction.Bean CopyBean() {
        return Copy();
    }

    public static final long TYPEID = -6166834646872658332L;

    @Override
    public long getTypeId() {
        return TYPEID;
    }

    private static final class Log__Notifies extends Zeze.Transaction.Collections.PList.LogV<Zeze.Net.Binary> {
        public Log__Notifies(BReliableNotify host, org.pcollections.PVector<Zeze.Net.Binary> value) { super(host, value); }
        @Override
        public long getLogKey() { return getBean().getObjectId() + 1; }
        public BReliableNotify getBeanTyped() { return (BReliableNotify)getBean(); }
        @Override
        public void Commit() { Commit(getBeanTyped()._Notifies); }
    }

    private static final class Log__ReliableNotifyTotalCountStart extends Zeze.Transaction.Log1<BReliableNotify, Long> {
        public Log__ReliableNotifyTotalCountStart(BReliableNotify self, Long value) { super(self, value); }
        @Override
        public long getLogKey() { return this.getBean().getObjectId() + 2; }
        @Override
        public void Commit() { this.getBeanTyped()._ReliableNotifyTotalCountStart = this.getValue(); }
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
        sb.append(Zeze.Util.Str.indent(level)).append("Zeze.Builtin.Game.Online.BReliableNotify: {").append(System.lineSeparator());
        level += 4;
        sb.append(Zeze.Util.Str.indent(level)).append("Notifies").append("=[").append(System.lineSeparator());
        level += 4;
        for (var _item_ : getNotifies()) {
            sb.append(Zeze.Util.Str.indent(level)).append("Item").append('=').append(_item_).append(',').append(System.lineSeparator());
        }
        level -= 4;
        sb.append(Zeze.Util.Str.indent(level)).append(']').append(',').append(System.lineSeparator());
        sb.append(Zeze.Util.Str.indent(level)).append("ReliableNotifyTotalCountStart").append('=').append(getReliableNotifyTotalCountStart()).append(System.lineSeparator());
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
            var _x_ = getNotifies();
            int _n_ = _x_.size();
            if (_n_ != 0) {
                _i_ = _o_.WriteTag(_i_, 1, ByteBuffer.LIST);
                _o_.WriteListType(_n_, ByteBuffer.BYTES);
                for (var _v_ : _x_)
                    _o_.WriteBinary(_v_);
            }
        }
        {
            long _x_ = getReliableNotifyTotalCountStart();
            if (_x_ != 0) {
                _i_ = _o_.WriteTag(_i_, 2, ByteBuffer.INTEGER);
                _o_.WriteLong(_x_);
            }
        }
        _o_.WriteByte(0);
    }

    @Override
    public void Decode(ByteBuffer _o_) {
        int _t_ = _o_.ReadByte();
        int _i_ = _o_.ReadTagSize(_t_);
        if (_i_ == 1) {
            var _x_ = getNotifies();
            _x_.clear();
            if ((_t_ & ByteBuffer.TAG_MASK) == ByteBuffer.LIST) {
                for (int _n_ = _o_.ReadTagSize(_t_ = _o_.ReadByte()); _n_ > 0; _n_--)
                    _x_.add(_o_.ReadBinary(_t_));
            } else
                _o_.SkipUnknownField(_t_);
            _i_ += _o_.ReadTagSize(_t_ = _o_.ReadByte());
        }
        if (_i_ == 2) {
            setReliableNotifyTotalCountStart(_o_.ReadLong(_t_));
            _i_ += _o_.ReadTagSize(_t_ = _o_.ReadByte());
        }
        while (_t_ != 0) {
            _o_.SkipUnknownField(_t_);
            _o_.ReadTagSize(_t_ = _o_.ReadByte());
        }
    }

    @Override
    protected void InitChildrenRootInfo(Zeze.Transaction.Record.RootInfo root) {
        _Notifies.InitRootInfo(root, this);
    }

    @Override
    public boolean NegativeCheck() {
        if (getReliableNotifyTotalCountStart() < 0)
            return true;
        return false;
    }
}
