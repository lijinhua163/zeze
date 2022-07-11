// auto-generated @formatter:off
package Zeze.Builtin.Collections.DepartmentTree;

import Zeze.Serialize.ByteBuffer;

@SuppressWarnings({"UnusedAssignment", "RedundantIfStatement", "SwitchStatementWithTooFewBranches", "RedundantSuppression"})
public final class BDepartmentTreeNode extends Zeze.Transaction.Bean {
    private long _ParentDepartment; // 0表示第一级部门
    private final Zeze.Transaction.Collections.PMap1<String, Long> _Childs; // name 2 id。采用整体保存，因为需要排序和重名判断。需要加数量上限。
    private String _Name;
    private final Zeze.Transaction.Collections.PMap1<String, Zeze.Transaction.DynamicBean> _Managers;
        public static long GetSpecialTypeIdFromBean_Managers(Zeze.Transaction.Bean bean) {
            var _typeId_ = bean.getTypeId();
            if (_typeId_ == Zeze.Transaction.EmptyBean.TYPEID)
                return Zeze.Transaction.EmptyBean.TYPEID;
            throw new RuntimeException("Unknown Bean! dynamic@Zeze.Builtin.Collections.DepartmentTree.BDepartmentTreeNode:Managers");
        }

        public static Zeze.Transaction.Bean CreateBeanFromSpecialTypeId_Managers(long typeId) {
            return null;
        }


    public long getParentDepartment() {
        if (!isManaged())
            return _ParentDepartment;
        var txn = Zeze.Transaction.Transaction.getCurrent();
        if (txn == null)
            return _ParentDepartment;
        txn.VerifyRecordAccessed(this, true);
        var log = (Log__ParentDepartment)txn.GetLog(this.getObjectId() + 1);
        return log != null ? log.Value : _ParentDepartment;
    }

    public void setParentDepartment(long value) {
        if (!isManaged()) {
            _ParentDepartment = value;
            return;
        }
        var txn = Zeze.Transaction.Transaction.getCurrent();
        assert txn != null;
        txn.VerifyRecordAccessed(this);
        txn.PutLog(new Log__ParentDepartment(this, 1, value));
    }

    public Zeze.Transaction.Collections.PMap1<String, Long> getChilds() {
        return _Childs;
    }

    public String getName() {
        if (!isManaged())
            return _Name;
        var txn = Zeze.Transaction.Transaction.getCurrent();
        if (txn == null)
            return _Name;
        txn.VerifyRecordAccessed(this, true);
        var log = (Log__Name)txn.GetLog(this.getObjectId() + 3);
        return log != null ? log.Value : _Name;
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
        txn.PutLog(new Log__Name(this, 3, value));
    }

    public Zeze.Transaction.Collections.PMap1<String, Zeze.Transaction.DynamicBean> getManagers() {
        return _Managers;
    }

    public BDepartmentTreeNode() {
         this(0);
    }

    public BDepartmentTreeNode(int _varId_) {
        super(_varId_);
        _Childs = new Zeze.Transaction.Collections.PMap1<>(String.class, Long.class);
        _Childs.VariableId = 2;
        _Name = "";
        _Managers = new Zeze.Transaction.Collections.PMap1<>(String.class, Zeze.Transaction.DynamicBean.class);
        _Managers.VariableId = 4;
    }

    public void Assign(BDepartmentTreeNode other) {
        setParentDepartment(other.getParentDepartment());
        getChilds().clear();
        for (var e : other.getChilds().entrySet())
            getChilds().put(e.getKey(), e.getValue());
        setName(other.getName());
        getManagers().clear();
        for (var e : other.getManagers().entrySet())
            getManagers().put(e.getKey(), e.getValue());
    }

    public BDepartmentTreeNode CopyIfManaged() {
        return isManaged() ? Copy() : this;
    }

    public BDepartmentTreeNode Copy() {
        var copy = new BDepartmentTreeNode();
        copy.Assign(this);
        return copy;
    }

    public static void Swap(BDepartmentTreeNode a, BDepartmentTreeNode b) {
        BDepartmentTreeNode save = a.Copy();
        a.Assign(b);
        b.Assign(save);
    }

    @Override
    public Zeze.Transaction.Bean CopyBean() {
        return Copy();
    }

    public static final long TYPEID = 2712461973987809351L;

    @Override
    public long getTypeId() {
        return TYPEID;
    }

    private static final class Log__ParentDepartment extends Zeze.Transaction.Logs.LogLong {
        public Log__ParentDepartment(BDepartmentTreeNode bean, int varId, long value) { super(bean, varId, value); }

        @Override
        public void Commit() { ((BDepartmentTreeNode)getBelong())._ParentDepartment = Value; }
    }

    private static final class Log__Name extends Zeze.Transaction.Logs.LogString {
        public Log__Name(BDepartmentTreeNode bean, int varId, String value) { super(bean, varId, value); }

        @Override
        public void Commit() { ((BDepartmentTreeNode)getBelong())._Name = Value; }
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
        sb.append(Zeze.Util.Str.indent(level)).append("Zeze.Builtin.Collections.DepartmentTree.BDepartmentTreeNode: {").append(System.lineSeparator());
        level += 4;
        sb.append(Zeze.Util.Str.indent(level)).append("ParentDepartment").append('=').append(getParentDepartment()).append(',').append(System.lineSeparator());
        sb.append(Zeze.Util.Str.indent(level)).append("Childs").append("=[").append(System.lineSeparator());
        level += 4;
        for (var _kv_ : getChilds().entrySet()) {
            sb.append(Zeze.Util.Str.indent(level)).append('(').append(System.lineSeparator());
            sb.append(Zeze.Util.Str.indent(level)).append("Key").append('=').append(_kv_.getKey()).append(',').append(System.lineSeparator());
            sb.append(Zeze.Util.Str.indent(level)).append("Value").append('=').append(_kv_.getValue()).append(',').append(System.lineSeparator());
            sb.append(Zeze.Util.Str.indent(level)).append(')').append(System.lineSeparator());
        }
        level -= 4;
        sb.append(Zeze.Util.Str.indent(level)).append(']').append(',').append(System.lineSeparator());
        sb.append(Zeze.Util.Str.indent(level)).append("Name").append('=').append(getName()).append(',').append(System.lineSeparator());
        sb.append(Zeze.Util.Str.indent(level)).append("Managers").append("=[").append(System.lineSeparator());
        level += 4;
        for (var _kv_ : getManagers().entrySet()) {
            sb.append(Zeze.Util.Str.indent(level)).append('(').append(System.lineSeparator());
            sb.append(Zeze.Util.Str.indent(level)).append("Key").append('=').append(_kv_.getKey()).append(',').append(System.lineSeparator());
            sb.append(Zeze.Util.Str.indent(level)).append("Value").append('=').append(System.lineSeparator());
            _kv_.getValue().getBean().BuildString(sb, level + 4);
            sb.append(',').append(System.lineSeparator());
            sb.append(Zeze.Util.Str.indent(level)).append(')').append(System.lineSeparator());
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
            long _x_ = getParentDepartment();
            if (_x_ != 0) {
                _i_ = _o_.WriteTag(_i_, 1, ByteBuffer.INTEGER);
                _o_.WriteLong(_x_);
            }
        }
        {
            var _x_ = getChilds();
            int _n_ = _x_.size();
            if (_n_ != 0) {
                _i_ = _o_.WriteTag(_i_, 2, ByteBuffer.MAP);
                _o_.WriteMapType(_n_, ByteBuffer.BYTES, ByteBuffer.INTEGER);
                for (var _e_ : _x_.entrySet()) {
                    _o_.WriteString(_e_.getKey());
                    _o_.WriteLong(_e_.getValue());
                }
            }
        }
        {
            String _x_ = getName();
            if (!_x_.isEmpty()) {
                _i_ = _o_.WriteTag(_i_, 3, ByteBuffer.BYTES);
                _o_.WriteString(_x_);
            }
        }
        {
            var _x_ = getManagers();
            int _n_ = _x_.size();
            if (_n_ != 0) {
                _i_ = _o_.WriteTag(_i_, 4, ByteBuffer.MAP);
                _o_.WriteMapType(_n_, ByteBuffer.BYTES, ByteBuffer.DYNAMIC);
                for (var _e_ : _x_.entrySet()) {
                    _o_.WriteString(_e_.getKey());
                    _x_.Encode(_o_);
                }
            }
        }
        _o_.WriteByte(0);
    }

    @Override
    public void Decode(ByteBuffer _o_) {
        int _t_ = _o_.ReadByte();
        int _i_ = _o_.ReadTagSize(_t_);
        if (_i_ == 1) {
            setParentDepartment(_o_.ReadLong(_t_));
            _i_ += _o_.ReadTagSize(_t_ = _o_.ReadByte());
        }
        if (_i_ == 2) {
            var _x_ = getChilds();
            _x_.clear();
            if ((_t_ & ByteBuffer.TAG_MASK) == ByteBuffer.MAP) {
                int _s_ = (_t_ = _o_.ReadByte()) >> ByteBuffer.TAG_SHIFT;
                for (int _n_ = _o_.ReadUInt(); _n_ > 0; _n_--) {
                    var _k_ = _o_.ReadString(_s_);
                    var _v_ = _o_.ReadLong(_t_);
                    _x_.put(_k_, _v_);
                }
            } else
                _o_.SkipUnknownField(_t_);
            _i_ += _o_.ReadTagSize(_t_ = _o_.ReadByte());
        }
        if (_i_ == 3) {
            setName(_o_.ReadString(_t_));
            _i_ += _o_.ReadTagSize(_t_ = _o_.ReadByte());
        }
        if (_i_ == 4) {
            var _x_ = getManagers();
            _x_.clear();
            if ((_t_ & ByteBuffer.TAG_MASK) == ByteBuffer.MAP) {
                int _s_ = (_t_ = _o_.ReadByte()) >> ByteBuffer.TAG_SHIFT;
                for (int _n_ = _o_.ReadUInt(); _n_ > 0; _n_--) {
                    var _k_ = _o_.ReadString(_s_);
                    Zeze.Transaction.DynamicBean _v_ = new Zeze.Transaction.DynamicBean(0, Zeze.Collections.DepartmentTree::GetSpecialTypeIdFromBean, Zeze.Collections.DepartmentTree::CreateBeanFromSpecialTypeId);
                    _o_.ReadDynamic(_v_, _t_);
                    _x_.put(_k_, _v_);
                }
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
        _Childs.InitRootInfo(root, this);
        _Managers.InitRootInfo(root, this);
    }

    @Override
    public boolean NegativeCheck() {
        if (getParentDepartment() < 0)
            return true;
        for (var _v_ : getChilds().values()) {
            if (_v_ < 0)
                return true;
        }
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
                case 1: _ParentDepartment = ((Zeze.Transaction.Logs.LogLong)vlog).Value; break;
                case 2: _Childs.FollowerApply(vlog); break;
                case 3: _Name = ((Zeze.Transaction.Logs.LogString)vlog).Value; break;
                case 4: _Managers.FollowerApply(vlog); break;
            }
        }
    }
}
