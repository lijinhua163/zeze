// auto-generated @formatter:off
package Zeze.Beans.Provider;

import Zeze.Serialize.ByteBuffer;

public final class BBind extends Zeze.Transaction.Bean {
    public static final int ResultSuccess = 0;
    public static final int ResultFaild = 1;

    private final Zeze.Transaction.Collections.PMap2<Integer, Zeze.Beans.Provider.BModule> _modules; // moduleId -> BModule
    private final Zeze.Transaction.Collections.PSet1<Long> _linkSids;

    public Zeze.Transaction.Collections.PMap2<Integer, Zeze.Beans.Provider.BModule> getModules() {
        return _modules;
    }

    public Zeze.Transaction.Collections.PSet1<Long> getLinkSids() {
        return _linkSids;
    }

    public BBind() {
         this(0);
    }

    public BBind(int _varId_) {
        super(_varId_);
        _modules = new Zeze.Transaction.Collections.PMap2<>(getObjectId() + 1, (_v) -> new Log__modules(this, _v));
        _linkSids = new Zeze.Transaction.Collections.PSet1<>(getObjectId() + 2, (_v) -> new Log__linkSids(this, _v));
    }

    public void Assign(BBind other) {
        getModules().clear();
        for (var e : other.getModules().entrySet())
            getModules().put(e.getKey(), e.getValue().Copy());
        getLinkSids().clear();
        for (var e : other.getLinkSids())
            getLinkSids().add(e);
    }

    public BBind CopyIfManaged() {
        return isManaged() ? Copy() : this;
    }

    public BBind Copy() {
        var copy = new BBind();
        copy.Assign(this);
        return copy;
    }

    public static void Swap(BBind a, BBind b) {
        BBind save = a.Copy();
        a.Assign(b);
        b.Assign(save);
    }

    @Override
    public Zeze.Transaction.Bean CopyBean() {
        return Copy();
    }

    public static final long TYPEID = -5146452617331422012L;

    @Override
    public long getTypeId() {
        return TYPEID;
    }

    private static final class Log__modules extends Zeze.Transaction.Collections.PMap.LogV<Integer, Zeze.Beans.Provider.BModule> {
        public Log__modules(BBind host, org.pcollections.PMap<Integer, Zeze.Beans.Provider.BModule> value) { super(host, value); }
        @Override
        public long getLogKey() { return getBean().getObjectId() + 1; }
        public BBind getBeanTyped() { return (BBind)getBean(); }
        @Override
        public void Commit() { Commit(getBeanTyped()._modules); }
    }

    private static final class Log__linkSids extends Zeze.Transaction.Collections.PSet.LogV<Long> {
        public Log__linkSids(BBind host, org.pcollections.PSet<Long> value) { super(host, value); }
        @Override
        public long getLogKey() { return getBean().getObjectId() + 2; }
        public BBind getBeanTyped() { return (BBind)getBean(); }
        @Override
        public void Commit() { Commit(getBeanTyped()._linkSids); }
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
        sb.append(Zeze.Util.Str.indent(level)).append("Zeze.Beans.Provider.BBind: {").append(System.lineSeparator());
        level += 4;
        sb.append(Zeze.Util.Str.indent(level)).append("modules").append("=[").append(System.lineSeparator());
        level += 4;
        for (var _kv_ : getModules().entrySet()) {
            sb.append(Zeze.Util.Str.indent(level)).append('(').append(System.lineSeparator());
            sb.append(Zeze.Util.Str.indent(level)).append("Key").append('=').append(_kv_.getKey()).append(',').append(System.lineSeparator());
            sb.append(Zeze.Util.Str.indent(level)).append("Value").append('=').append(System.lineSeparator());
            _kv_.getValue().BuildString(sb, level + 4);
            sb.append(',').append(System.lineSeparator());
            sb.append(Zeze.Util.Str.indent(level)).append(')').append(System.lineSeparator());
        }
        level -= 4;
        sb.append(Zeze.Util.Str.indent(level)).append(']').append(',').append(System.lineSeparator());
        sb.append(Zeze.Util.Str.indent(level)).append("linkSids").append("=[").append(System.lineSeparator());
        level += 4;
        for (var _item_ : getLinkSids()) {
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
            var _x_ = getModules();
            int _n_ = _x_.size();
            if (_n_ != 0) {
                _i_ = _o_.WriteTag(_i_, 1, ByteBuffer.MAP);
                _o_.WriteMapType(_n_, ByteBuffer.INTEGER, ByteBuffer.BEAN);
                for (var _e_ : _x_.entrySet()) {
                    _o_.WriteLong(_e_.getKey());
                    _e_.getValue().Encode(_o_);
                }
            }
        }
        {
            var _x_ = getLinkSids();
            int _n_ = _x_.size();
            if (_n_ != 0) {
                _i_ = _o_.WriteTag(_i_, 2, ByteBuffer.LIST);
                _o_.WriteListType(_n_, ByteBuffer.INTEGER);
                for (var _v_ : _x_)
                    _o_.WriteLong(_v_);
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
            var _x_ = getModules();
            _x_.clear();
            if ((_t_ & ByteBuffer.TAG_MASK) == ByteBuffer.MAP) {
                int _s_ = (_t_ = _o_.ReadByte()) >> ByteBuffer.TAG_SHIFT;
                for (int _n_ = _o_.ReadUInt(); _n_ > 0; _n_--) {
                    var _k_ = _o_.ReadInt(_s_);
                    var _v_ = _o_.ReadBean(new Zeze.Beans.Provider.BModule(), _t_);
                    _x_.put(_k_, _v_);
                }
            } else
                _o_.SkipUnknownField(_t_);
            _i_ += _o_.ReadTagSize(_t_ = _o_.ReadByte());
        }
        if (_i_ == 2) {
            var _x_ = getLinkSids();
            _x_.clear();
            if ((_t_ & ByteBuffer.TAG_MASK) == ByteBuffer.LIST) {
                for (int _n_ = _o_.ReadTagSize(_t_ = _o_.ReadByte()); _n_ > 0; _n_--)
                    _x_.add(_o_.ReadLong(_t_));
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
        _modules.InitRootInfo(root, this);
        _linkSids.InitRootInfo(root, this);
    }

    @SuppressWarnings("RedundantIfStatement")
    @Override
    public boolean NegativeCheck() {
        for (var _v_ : getModules().values()) {
            if (_v_.NegativeCheck())
                return true;
        }
        for (var _v_ : getLinkSids()) {
            if (_v_ < 0)
                return true;
        }
        return false;
    }
}