// auto-generated @formatter:off
package Zeze.Builtin.Collections.LinkedMap;

import Zeze.Serialize.ByteBuffer;

@SuppressWarnings({"UnusedAssignment", "RedundantIfStatement", "SwitchStatementWithTooFewBranches", "RedundantSuppression"})
public final class BLinkedMapNode extends Zeze.Transaction.Bean {
    private long _PrevNodeId; // 前一个节点ID. 0表示已到达开头。
    private long _NextNodeId; // 后一个节点ID. 0表示已到达结尾。
    private final Zeze.Transaction.Collections.PList2<Zeze.Builtin.Collections.LinkedMap.BLinkedMapNodeValue> _Values; // 多个KeyValue对,容量由LinkedMap构造时的nodeSize决定

    public long getPrevNodeId() {
        if (!isManaged())
            return _PrevNodeId;
        var txn = Zeze.Transaction.Transaction.getCurrent();
        if (txn == null)
            return _PrevNodeId;
        txn.VerifyRecordAccessed(this, true);
        var log = (Log__PrevNodeId)txn.GetLog(this.getObjectId() + 1);
        return log != null ? log.Value : _PrevNodeId;
    }

    public void setPrevNodeId(long value) {
        if (!isManaged()) {
            _PrevNodeId = value;
            return;
        }
        var txn = Zeze.Transaction.Transaction.getCurrent();
        assert txn != null;
        txn.VerifyRecordAccessed(this);
        txn.PutLog(new Log__PrevNodeId(this, 1, value));
    }

    public long getNextNodeId() {
        if (!isManaged())
            return _NextNodeId;
        var txn = Zeze.Transaction.Transaction.getCurrent();
        if (txn == null)
            return _NextNodeId;
        txn.VerifyRecordAccessed(this, true);
        var log = (Log__NextNodeId)txn.GetLog(this.getObjectId() + 2);
        return log != null ? log.Value : _NextNodeId;
    }

    public void setNextNodeId(long value) {
        if (!isManaged()) {
            _NextNodeId = value;
            return;
        }
        var txn = Zeze.Transaction.Transaction.getCurrent();
        assert txn != null;
        txn.VerifyRecordAccessed(this);
        txn.PutLog(new Log__NextNodeId(this, 2, value));
    }

    public Zeze.Transaction.Collections.PList2<Zeze.Builtin.Collections.LinkedMap.BLinkedMapNodeValue> getValues() {
        return _Values;
    }

    public BLinkedMapNode() {
         this(0);
    }

    public BLinkedMapNode(int _varId_) {
        super(_varId_);
        _Values = new Zeze.Transaction.Collections.PList2<>(Zeze.Builtin.Collections.LinkedMap.BLinkedMapNodeValue.class);
        _Values.VariableId = 3;
    }

    public void Assign(BLinkedMapNode other) {
        setPrevNodeId(other.getPrevNodeId());
        setNextNodeId(other.getNextNodeId());
        getValues().clear();
        for (var e : other.getValues())
            getValues().add(e.Copy());
    }

    public BLinkedMapNode CopyIfManaged() {
        return isManaged() ? Copy() : this;
    }

    public BLinkedMapNode Copy() {
        var copy = new BLinkedMapNode();
        copy.Assign(this);
        return copy;
    }

    public static void Swap(BLinkedMapNode a, BLinkedMapNode b) {
        BLinkedMapNode save = a.Copy();
        a.Assign(b);
        b.Assign(save);
    }

    @Override
    public Zeze.Transaction.Bean CopyBean() {
        return Copy();
    }

    public static final long TYPEID = 3432187612551867839L;

    @Override
    public long getTypeId() {
        return TYPEID;
    }

    private static final class Log__PrevNodeId extends Zeze.Transaction.Logs.LogLong {
        public Log__PrevNodeId(BLinkedMapNode bean, int varId, long value) { super(bean, varId, value); }

        @Override
        public void Commit() { ((BLinkedMapNode)getBelong())._PrevNodeId = Value; }
    }

    private static final class Log__NextNodeId extends Zeze.Transaction.Logs.LogLong {
        public Log__NextNodeId(BLinkedMapNode bean, int varId, long value) { super(bean, varId, value); }

        @Override
        public void Commit() { ((BLinkedMapNode)getBelong())._NextNodeId = Value; }
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
        sb.append(Zeze.Util.Str.indent(level)).append("Zeze.Builtin.Collections.LinkedMap.BLinkedMapNode: {").append(System.lineSeparator());
        level += 4;
        sb.append(Zeze.Util.Str.indent(level)).append("PrevNodeId").append('=').append(getPrevNodeId()).append(',').append(System.lineSeparator());
        sb.append(Zeze.Util.Str.indent(level)).append("NextNodeId").append('=').append(getNextNodeId()).append(',').append(System.lineSeparator());
        sb.append(Zeze.Util.Str.indent(level)).append("Values").append("=[").append(System.lineSeparator());
        level += 4;
        for (var _item_ : getValues()) {
            sb.append(Zeze.Util.Str.indent(level)).append("Item").append('=').append(System.lineSeparator());
            _item_.BuildString(sb, level + 4);
            sb.append(',').append(System.lineSeparator());
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

    @Override
    public void Encode(ByteBuffer _o_) {
        int _i_ = 0;
        {
            long _x_ = getPrevNodeId();
            if (_x_ != 0) {
                _i_ = _o_.WriteTag(_i_, 1, ByteBuffer.INTEGER);
                _o_.WriteLong(_x_);
            }
        }
        {
            long _x_ = getNextNodeId();
            if (_x_ != 0) {
                _i_ = _o_.WriteTag(_i_, 2, ByteBuffer.INTEGER);
                _o_.WriteLong(_x_);
            }
        }
        {
            var _x_ = getValues();
            int _n_ = _x_.size();
            if (_n_ != 0) {
                _i_ = _o_.WriteTag(_i_, 3, ByteBuffer.LIST);
                _o_.WriteListType(_n_, ByteBuffer.BEAN);
                for (var _v_ : _x_)
                    _v_.Encode(_o_);
            }
        }
        _o_.WriteByte(0);
    }

    @Override
    public void Decode(ByteBuffer _o_) {
        int _t_ = _o_.ReadByte();
        int _i_ = _o_.ReadTagSize(_t_);
        if (_i_ == 1) {
            setPrevNodeId(_o_.ReadLong(_t_));
            _i_ += _o_.ReadTagSize(_t_ = _o_.ReadByte());
        }
        if (_i_ == 2) {
            setNextNodeId(_o_.ReadLong(_t_));
            _i_ += _o_.ReadTagSize(_t_ = _o_.ReadByte());
        }
        if (_i_ == 3) {
            var _x_ = getValues();
            _x_.clear();
            if ((_t_ & ByteBuffer.TAG_MASK) == ByteBuffer.LIST) {
                for (int _n_ = _o_.ReadTagSize(_t_ = _o_.ReadByte()); _n_ > 0; _n_--)
                    _x_.add(_o_.ReadBean(new Zeze.Builtin.Collections.LinkedMap.BLinkedMapNodeValue(), _t_));
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
        _Values.InitRootInfo(root, this);
    }

    @Override
    public boolean NegativeCheck() {
        if (getPrevNodeId() < 0)
            return true;
        if (getNextNodeId() < 0)
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
                case 1: _PrevNodeId = ((Zeze.Transaction.Logs.LogLong)vlog).Value; break;
                case 2: _NextNodeId = ((Zeze.Transaction.Logs.LogLong)vlog).Value; break;
                case 3: _Values.FollowerApply(vlog); break;
            }
        }
    }
}
