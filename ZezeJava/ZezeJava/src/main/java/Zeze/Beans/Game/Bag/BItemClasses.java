// auto-generated @formatter:off
package Zeze.Beans.Game.Bag;

import Zeze.Serialize.ByteBuffer;

public final class BItemClasses extends Zeze.Transaction.Bean {
    private final Zeze.Transaction.Collections.PSet1<String> _ItemClasses;

    public Zeze.Transaction.Collections.PSet1<String> getItemClasses() {
        return _ItemClasses;
    }

    public BItemClasses() {
         this(0);
    }

    public BItemClasses(int _varId_) {
        super(_varId_);
        _ItemClasses = new Zeze.Transaction.Collections.PSet1<>(getObjectId() + 1, (_v) -> new Log__ItemClasses(this, _v));
    }

    public void Assign(BItemClasses other) {
        getItemClasses().clear();
        for (var e : other.getItemClasses())
            getItemClasses().add(e);
    }

    public BItemClasses CopyIfManaged() {
        return isManaged() ? Copy() : this;
    }

    public BItemClasses Copy() {
        var copy = new BItemClasses();
        copy.Assign(this);
        return copy;
    }

    public static void Swap(BItemClasses a, BItemClasses b) {
        BItemClasses save = a.Copy();
        a.Assign(b);
        b.Assign(save);
    }

    @Override
    public Zeze.Transaction.Bean CopyBean() {
        return Copy();
    }

    public static final long TYPEID = 7263821517053449735L;

    @Override
    public long getTypeId() {
        return TYPEID;
    }

    private static final class Log__ItemClasses extends Zeze.Transaction.Collections.PSet.LogV<String> {
        public Log__ItemClasses(BItemClasses host, org.pcollections.PSet<String> value) { super(host, value); }
        @Override
        public long getLogKey() { return getBean().getObjectId() + 1; }
        public BItemClasses getBeanTyped() { return (BItemClasses)getBean(); }
        @Override
        public void Commit() { Commit(getBeanTyped()._ItemClasses); }
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
        sb.append(Zeze.Util.Str.indent(level)).append("Zeze.Beans.Game.Bag.BItemClasses: {").append(System.lineSeparator());
        level += 4;
        sb.append(Zeze.Util.Str.indent(level)).append("ItemClasses").append("=[").append(System.lineSeparator());
        level += 4;
        for (var _item_ : getItemClasses()) {
            sb.append(Zeze.Util.Str.indent(level)).append("Item").append('=').append(_item_).append(',').append(System.lineSeparator());
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
            var _x_ = getItemClasses();
            int _n_ = _x_.size();
            if (_n_ != 0) {
                _i_ = _o_.WriteTag(_i_, 1, ByteBuffer.LIST);
                _o_.WriteListType(_n_, ByteBuffer.BYTES);
                for (var _v_ : _x_)
                    _o_.WriteString(_v_);
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
            var _x_ = getItemClasses();
            _x_.clear();
            if ((_t_ & ByteBuffer.TAG_MASK) == ByteBuffer.LIST) {
                for (int _n_ = _o_.ReadTagSize(_t_ = _o_.ReadByte()); _n_ > 0; _n_--)
                    _x_.add(_o_.ReadString(_t_));
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
        _ItemClasses.InitRootInfo(root, this);
    }

    @SuppressWarnings("RedundantIfStatement")
    @Override
    public boolean NegativeCheck() {
        return false;
    }
}
