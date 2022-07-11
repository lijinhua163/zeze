// auto-generated rocks @formatter:off
package Zeze.Builtin.TestRocks;

import Zeze.Serialize.ByteBuffer;

@SuppressWarnings({"UnusedAssignment", "RedundantIfStatement", "SwitchStatementWithTooFewBranches", "RedundantSuppression"})
public final class Value extends Zeze.Raft.RocksRaft.Bean {
    private int _Int;
    private boolean _Bool;
    private float _Float;
    private double _double;
    private String _String;
    private Zeze.Net.Binary _Binary;
    private final Zeze.Raft.RocksRaft.CollSet1<Integer> _SetInt;
    private final Zeze.Raft.RocksRaft.CollSet1<Zeze.Builtin.TestRocks.BeanKey> _SetBeankey;
    private final Zeze.Raft.RocksRaft.CollMap1<Integer, Integer> _MapInt;
    private final Zeze.Raft.RocksRaft.CollMap2<Integer, Zeze.Builtin.TestRocks.Value> _MapBean;
    private Zeze.Builtin.TestRocks.BeanKey _Beankey;

    private Object __zeze_map_key__;

    @Override
    public Object getMapKey() {
        return __zeze_map_key__;
    }

    @Override
    public void setMapKey(Object value) {
        __zeze_map_key__ = value;
    }

    public int getInt() {
        if (!isManaged())
            return _Int;
        var txn = Zeze.Raft.RocksRaft.Transaction.getCurrent();
        if (txn == null)
            return _Int;
        var log = txn.GetLog(getObjectId() + 1);
        if (log == null)
            return _Int;
        return ((Zeze.Raft.RocksRaft.Log1.LogInt)log).Value;
    }

    public void setInt(int value) {
        if (!isManaged()) {
            _Int = value;
            return;
        }
        var txn = Zeze.Raft.RocksRaft.Transaction.getCurrent();
        assert txn != null;
        txn.PutLog(new Zeze.Raft.RocksRaft.Log1.LogInt(this, 1, value));
    }

    public boolean isBool() {
        if (!isManaged())
            return _Bool;
        var txn = Zeze.Raft.RocksRaft.Transaction.getCurrent();
        if (txn == null)
            return _Bool;
        var log = txn.GetLog(getObjectId() + 2);
        if (log == null)
            return _Bool;
        return ((Zeze.Raft.RocksRaft.Log1.LogBool)log).Value;
    }

    public void setBool(boolean value) {
        if (!isManaged()) {
            _Bool = value;
            return;
        }
        var txn = Zeze.Raft.RocksRaft.Transaction.getCurrent();
        assert txn != null;
        txn.PutLog(new Zeze.Raft.RocksRaft.Log1.LogBool(this, 2, value));
    }

    public float getFloat() {
        if (!isManaged())
            return _Float;
        var txn = Zeze.Raft.RocksRaft.Transaction.getCurrent();
        if (txn == null)
            return _Float;
        var log = txn.GetLog(getObjectId() + 3);
        if (log == null)
            return _Float;
        return ((Zeze.Raft.RocksRaft.Log1.LogFloat)log).Value;
    }

    public void setFloat(float value) {
        if (!isManaged()) {
            _Float = value;
            return;
        }
        var txn = Zeze.Raft.RocksRaft.Transaction.getCurrent();
        assert txn != null;
        txn.PutLog(new Zeze.Raft.RocksRaft.Log1.LogFloat(this, 3, value));
    }

    public double getDouble() {
        if (!isManaged())
            return _double;
        var txn = Zeze.Raft.RocksRaft.Transaction.getCurrent();
        if (txn == null)
            return _double;
        var log = txn.GetLog(getObjectId() + 4);
        if (log == null)
            return _double;
        return ((Zeze.Raft.RocksRaft.Log1.LogDouble)log).Value;
    }

    public void setDouble(double value) {
        if (!isManaged()) {
            _double = value;
            return;
        }
        var txn = Zeze.Raft.RocksRaft.Transaction.getCurrent();
        assert txn != null;
        txn.PutLog(new Zeze.Raft.RocksRaft.Log1.LogDouble(this, 4, value));
    }

    public String getString() {
        if (!isManaged())
            return _String;
        var txn = Zeze.Raft.RocksRaft.Transaction.getCurrent();
        if (txn == null)
            return _String;
        var log = txn.GetLog(getObjectId() + 5);
        if (log == null)
            return _String;
        return ((Zeze.Raft.RocksRaft.Log1.LogString)log).Value;
    }

    public void setString(String value) {
        if (value == null)
            throw new IllegalArgumentException();
        if (!isManaged()) {
            _String = value;
            return;
        }
        var txn = Zeze.Raft.RocksRaft.Transaction.getCurrent();
        assert txn != null;
        txn.PutLog(new Zeze.Raft.RocksRaft.Log1.LogString(this, 5, value));
    }

    public Zeze.Net.Binary getBinary() {
        if (!isManaged())
            return _Binary;
        var txn = Zeze.Raft.RocksRaft.Transaction.getCurrent();
        if (txn == null)
            return _Binary;
        var log = txn.GetLog(getObjectId() + 6);
        if (log == null)
            return _Binary;
        return ((Zeze.Raft.RocksRaft.Log1.LogBinary)log).Value;
    }

    public void setBinary(Zeze.Net.Binary value) {
        if (value == null)
            throw new IllegalArgumentException();
        if (!isManaged()) {
            _Binary = value;
            return;
        }
        var txn = Zeze.Raft.RocksRaft.Transaction.getCurrent();
        assert txn != null;
        txn.PutLog(new Zeze.Raft.RocksRaft.Log1.LogBinary(this, 6, value));
    }

    public Zeze.Raft.RocksRaft.CollSet1<Integer> getSetInt() {
        return _SetInt;
    }

    public Zeze.Raft.RocksRaft.CollSet1<Zeze.Builtin.TestRocks.BeanKey> getSetBeankey() {
        return _SetBeankey;
    }

    public Zeze.Raft.RocksRaft.CollMap1<Integer, Integer> getMapInt() {
        return _MapInt;
    }

    public Zeze.Raft.RocksRaft.CollMap2<Integer, Zeze.Builtin.TestRocks.Value> getMapBean() {
        return _MapBean;
    }

    @SuppressWarnings("unchecked")
    public Zeze.Builtin.TestRocks.BeanKey getBeankey() {
        if (!isManaged())
            return _Beankey;
        var txn = Zeze.Raft.RocksRaft.Transaction.getCurrent();
        if (txn == null)
            return _Beankey;
        var log = txn.GetLog(getObjectId() + 11);
        if (null == log)
            return _Beankey;
        return ((Zeze.Raft.RocksRaft.Log1.LogBeanKey<Zeze.Builtin.TestRocks.BeanKey>)log).Value;
    }

    public void setBeankey(Zeze.Builtin.TestRocks.BeanKey value) {
        if (value == null)
            throw new IllegalArgumentException();
        if (!isManaged()) {
            _Beankey = value;
            return;
        }
        var txn = Zeze.Raft.RocksRaft.Transaction.getCurrent();
        assert txn != null;
        txn.PutLog(new Zeze.Raft.RocksRaft.Log1.LogBeanKey<>(Zeze.Builtin.TestRocks.BeanKey.class, this, 11, value));
    }

    public Value() {
         this(0);
    }

    public Value(int _varId_) {
        super(_varId_);
        _String = "";
        _Binary = Zeze.Net.Binary.Empty;
        _SetInt = new Zeze.Raft.RocksRaft.CollSet1<>(Integer.class);
        _SetInt.VariableId = 7;
        _SetBeankey = new Zeze.Raft.RocksRaft.CollSet1<>(Zeze.Builtin.TestRocks.BeanKey.class);
        _SetBeankey.VariableId = 8;
        _MapInt = new Zeze.Raft.RocksRaft.CollMap1<>(Integer.class, Integer.class);
        _MapInt.VariableId = 9;
        _MapBean = new Zeze.Raft.RocksRaft.CollMap2<>(Integer.class, Zeze.Builtin.TestRocks.Value.class);
        _MapBean.VariableId = 10;
        _Beankey = new Zeze.Builtin.TestRocks.BeanKey();
    }

    public void Assign(Value other) {
        setInt(other.getInt());
        setBool(other.isBool());
        setFloat(other.getFloat());
        setDouble(other.getDouble());
        setString(other.getString());
        setBinary(other.getBinary());
        getSetInt().clear();
        for (var e : other.getSetInt())
            getSetInt().add(e);
        getSetBeankey().clear();
        for (var e : other.getSetBeankey())
            getSetBeankey().add(e);
        getMapInt().clear();
        for (var e : other.getMapInt().entrySet())
            getMapInt().put(e.getKey(), e.getValue());
        getMapBean().clear();
        for (var e : other.getMapBean().entrySet())
            getMapBean().put(e.getKey(), e.getValue());
        setBeankey(other.getBeankey());
    }

    public Value CopyIfManaged() {
        return isManaged() ? Copy() : this;
    }

    public Value Copy() {
        var copy = new Value();
        copy.Assign(this);
        return copy;
    }

    public static void Swap(Value a, Value b) {
        Value save = a.Copy();
        a.Assign(b);
        b.Assign(save);
    }

    @Override
    public Zeze.Raft.RocksRaft.Bean CopyBean() {
        return Copy();
    }

    public static final long TYPEID = 7725276190606291579L;

    @Override
    public long getTypeId() {
        return TYPEID;
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
        sb.append(Zeze.Util.Str.indent(level)).append("Zeze.Builtin.TestRocks.Value: {").append(System.lineSeparator());
        level += 4;
        sb.append(Zeze.Util.Str.indent(level)).append("Int").append('=').append(getInt()).append(',').append(System.lineSeparator());
        sb.append(Zeze.Util.Str.indent(level)).append("Bool").append('=').append(isBool()).append(',').append(System.lineSeparator());
        sb.append(Zeze.Util.Str.indent(level)).append("Float").append('=').append(getFloat()).append(',').append(System.lineSeparator());
        sb.append(Zeze.Util.Str.indent(level)).append("double").append('=').append(getDouble()).append(',').append(System.lineSeparator());
        sb.append(Zeze.Util.Str.indent(level)).append("String").append('=').append(getString()).append(',').append(System.lineSeparator());
        sb.append(Zeze.Util.Str.indent(level)).append("Binary").append('=').append(getBinary()).append(',').append(System.lineSeparator());
        sb.append(Zeze.Util.Str.indent(level)).append("SetInt").append("=[").append(System.lineSeparator());
        level += 4;
        for (var _item_ : getSetInt()) {
            sb.append(Zeze.Util.Str.indent(level)).append("Item").append('=').append(_item_).append(',').append(System.lineSeparator());
        }
        level -= 4;
        sb.append(Zeze.Util.Str.indent(level)).append(']').append(',').append(System.lineSeparator());
        sb.append(Zeze.Util.Str.indent(level)).append("SetBeankey").append("=[").append(System.lineSeparator());
        level += 4;
        for (var _item_ : getSetBeankey()) {
            sb.append(Zeze.Util.Str.indent(level)).append("Item").append('=').append(System.lineSeparator());
            _item_.BuildString(sb, level + 4);
            sb.append(',').append(System.lineSeparator());
        }
        level -= 4;
        sb.append(Zeze.Util.Str.indent(level)).append(']').append(',').append(System.lineSeparator());
        sb.append(Zeze.Util.Str.indent(level)).append("MapInt").append("=[").append(System.lineSeparator());
        level += 4;
        for (var _kv_ : getMapInt().entrySet()) {
            sb.append(Zeze.Util.Str.indent(level)).append('(').append(System.lineSeparator());
            sb.append(Zeze.Util.Str.indent(level)).append("Key").append('=').append(_kv_.getKey()).append(',').append(System.lineSeparator());
            sb.append(Zeze.Util.Str.indent(level)).append("Value").append('=').append(_kv_.getValue()).append(',').append(System.lineSeparator());
            sb.append(Zeze.Util.Str.indent(level)).append(')').append(System.lineSeparator());
        }
        level -= 4;
        sb.append(Zeze.Util.Str.indent(level)).append(']').append(',').append(System.lineSeparator());
        sb.append(Zeze.Util.Str.indent(level)).append("MapBean").append("=[").append(System.lineSeparator());
        level += 4;
        for (var _kv_ : getMapBean().entrySet()) {
            sb.append(Zeze.Util.Str.indent(level)).append('(').append(System.lineSeparator());
            sb.append(Zeze.Util.Str.indent(level)).append("Key").append('=').append(_kv_.getKey()).append(',').append(System.lineSeparator());
            sb.append(Zeze.Util.Str.indent(level)).append("Value").append('=').append(System.lineSeparator());
            _kv_.getValue().BuildString(sb, level + 4);
            sb.append(',').append(System.lineSeparator());
            sb.append(Zeze.Util.Str.indent(level)).append(')').append(System.lineSeparator());
        }
        level -= 4;
        sb.append(Zeze.Util.Str.indent(level)).append(']').append(',').append(System.lineSeparator());
        sb.append(Zeze.Util.Str.indent(level)).append("Beankey").append('=').append(System.lineSeparator());
        getBeankey().BuildString(sb, level + 4);
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
            int _x_ = getInt();
            if (_x_ != 0) {
                _i_ = _o_.WriteTag(_i_, 1, ByteBuffer.INTEGER);
                _o_.WriteInt(_x_);
            }
        }
        {
            boolean _x_ = isBool();
            if (_x_) {
                _i_ = _o_.WriteTag(_i_, 2, ByteBuffer.INTEGER);
                _o_.WriteByte(1);
            }
        }
        {
            float _x_ = getFloat();
            if (_x_ != 0) {
                _i_ = _o_.WriteTag(_i_, 3, ByteBuffer.FLOAT);
                _o_.WriteFloat(_x_);
            }
        }
        {
            double _x_ = getDouble();
            if (_x_ != 0) {
                _i_ = _o_.WriteTag(_i_, 4, ByteBuffer.DOUBLE);
                _o_.WriteDouble(_x_);
            }
        }
        {
            String _x_ = getString();
            if (!_x_.isEmpty()) {
                _i_ = _o_.WriteTag(_i_, 5, ByteBuffer.BYTES);
                _o_.WriteString(_x_);
            }
        }
        {
            var _x_ = getBinary();
            if (_x_.size() != 0) {
                _i_ = _o_.WriteTag(_i_, 6, ByteBuffer.BYTES);
                _o_.WriteBinary(_x_);
            }
        }
        {
            var _x_ = getSetInt();
            int _n_ = _x_.size();
            if (_n_ != 0) {
                _i_ = _o_.WriteTag(_i_, 7, ByteBuffer.LIST);
                _o_.WriteListType(_n_, ByteBuffer.INTEGER);
                for (var _v_ : _x_)
                    _o_.WriteLong(_v_);
            }
        }
        {
            var _x_ = getSetBeankey();
            int _n_ = _x_.size();
            if (_n_ != 0) {
                _i_ = _o_.WriteTag(_i_, 8, ByteBuffer.LIST);
                _o_.WriteListType(_n_, ByteBuffer.BEAN);
                for (var _v_ : _x_)
                    _v_.Encode(_o_);
            }
        }
        {
            var _x_ = getMapInt();
            int _n_ = _x_.size();
            if (_n_ != 0) {
                _i_ = _o_.WriteTag(_i_, 9, ByteBuffer.MAP);
                _o_.WriteMapType(_n_, ByteBuffer.INTEGER, ByteBuffer.INTEGER);
                for (var _e_ : _x_.entrySet()) {
                    _o_.WriteLong(_e_.getKey());
                    _o_.WriteLong(_e_.getValue());
                }
            }
        }
        {
            var _x_ = getMapBean();
            int _n_ = _x_.size();
            if (_n_ != 0) {
                _i_ = _o_.WriteTag(_i_, 10, ByteBuffer.MAP);
                _o_.WriteMapType(_n_, ByteBuffer.INTEGER, ByteBuffer.BEAN);
                for (var _e_ : _x_.entrySet()) {
                    _o_.WriteLong(_e_.getKey());
                    _e_.getValue().Encode(_o_);
                }
            }
        }
        {
            int _a_ = _o_.WriteIndex;
            int _j_ = _o_.WriteTag(_i_, 11, ByteBuffer.BEAN);
            int _b_ = _o_.WriteIndex;
            getBeankey().Encode(_o_);
            if (_b_ + 1 == _o_.WriteIndex)
                _o_.WriteIndex = _a_;
            else
                _i_ = _j_;
        }
        _o_.WriteByte(0);
    }

    @Override
    public void Decode(ByteBuffer _o_) {
        int _t_ = _o_.ReadByte();
        int _i_ = _o_.ReadTagSize(_t_);
        if (_i_ == 1) {
            _Int = _o_.ReadInt(_t_);
            _i_ += _o_.ReadTagSize(_t_ = _o_.ReadByte());
        }
        if (_i_ == 2) {
            _Bool = _o_.ReadBool(_t_);
            _i_ += _o_.ReadTagSize(_t_ = _o_.ReadByte());
        }
        if (_i_ == 3) {
            _Float = _o_.ReadFloat(_t_);
            _i_ += _o_.ReadTagSize(_t_ = _o_.ReadByte());
        }
        if (_i_ == 4) {
            _double = _o_.ReadDouble(_t_);
            _i_ += _o_.ReadTagSize(_t_ = _o_.ReadByte());
        }
        if (_i_ == 5) {
            _String = _o_.ReadString(_t_);
            _i_ += _o_.ReadTagSize(_t_ = _o_.ReadByte());
        }
        if (_i_ == 6) {
            _Binary = _o_.ReadBinary(_t_);
            _i_ += _o_.ReadTagSize(_t_ = _o_.ReadByte());
        }
        if (_i_ == 7) {
            var _x_ = getSetInt();
            _x_.clear();
            if ((_t_ & ByteBuffer.TAG_MASK) == ByteBuffer.LIST) {
                for (int _n_ = _o_.ReadTagSize(_t_ = _o_.ReadByte()); _n_ > 0; _n_--)
                    _x_.add(_o_.ReadInt(_t_));
            } else
                _o_.SkipUnknownField(_t_);
            _i_ += _o_.ReadTagSize(_t_ = _o_.ReadByte());
        }
        if (_i_ == 8) {
            var _x_ = getSetBeankey();
            _x_.clear();
            if ((_t_ & ByteBuffer.TAG_MASK) == ByteBuffer.LIST) {
                for (int _n_ = _o_.ReadTagSize(_t_ = _o_.ReadByte()); _n_ > 0; _n_--)
                    _x_.add(_o_.ReadBean(new Zeze.Builtin.TestRocks.BeanKey(), _t_));
            } else
                _o_.SkipUnknownField(_t_);
            _i_ += _o_.ReadTagSize(_t_ = _o_.ReadByte());
        }
        if (_i_ == 9) {
            var _x_ = getMapInt();
            _x_.clear();
            if ((_t_ & ByteBuffer.TAG_MASK) == ByteBuffer.MAP) {
                int _s_ = (_t_ = _o_.ReadByte()) >> ByteBuffer.TAG_SHIFT;
                for (int _n_ = _o_.ReadUInt(); _n_ > 0; _n_--) {
                    var _k_ = _o_.ReadInt(_s_);
                    var _v_ = _o_.ReadInt(_t_);
                    _x_.put(_k_, _v_);
                }
            } else
                _o_.SkipUnknownField(_t_);
            _i_ += _o_.ReadTagSize(_t_ = _o_.ReadByte());
        }
        if (_i_ == 10) {
            var _x_ = getMapBean();
            _x_.clear();
            if ((_t_ & ByteBuffer.TAG_MASK) == ByteBuffer.MAP) {
                int _s_ = (_t_ = _o_.ReadByte()) >> ByteBuffer.TAG_SHIFT;
                for (int _n_ = _o_.ReadUInt(); _n_ > 0; _n_--) {
                    var _k_ = _o_.ReadInt(_s_);
                    var _v_ = _o_.ReadBean(new Zeze.Builtin.TestRocks.Value(), _t_);
                    _x_.put(_k_, _v_);
                }
            } else
                _o_.SkipUnknownField(_t_);
            _i_ += _o_.ReadTagSize(_t_ = _o_.ReadByte());
        }
        if (_i_ == 11) {
            _o_.ReadBean(_Beankey, _t_);
            _i_ += _o_.ReadTagSize(_t_ = _o_.ReadByte());
        }
        while (_t_ != 0) {
            _o_.SkipUnknownField(_t_);
            _o_.ReadTagSize(_t_ = _o_.ReadByte());
        }
    }

    @Override
    protected void InitChildrenRootInfo(Zeze.Raft.RocksRaft.Record.RootInfo root) {
        _SetInt.InitRootInfo(root, this);
        _SetBeankey.InitRootInfo(root, this);
        _MapInt.InitRootInfo(root, this);
        _MapBean.InitRootInfo(root, this);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void LeaderApplyNoRecursive(Zeze.Raft.RocksRaft.Log vlog) {
        switch (vlog.getVariableId()) {
            case 1: _Int = ((Zeze.Raft.RocksRaft.Log1.LogInt)vlog).Value; break;
            case 2: _Bool = ((Zeze.Raft.RocksRaft.Log1.LogBool)vlog).Value; break;
            case 3: _Float = ((Zeze.Raft.RocksRaft.Log1.LogFloat)vlog).Value; break;
            case 4: _double = ((Zeze.Raft.RocksRaft.Log1.LogDouble)vlog).Value; break;
            case 5: _String = ((Zeze.Raft.RocksRaft.Log1.LogString)vlog).Value; break;
            case 6: _Binary = ((Zeze.Raft.RocksRaft.Log1.LogBinary)vlog).Value; break;
            case 7: _SetInt.LeaderApplyNoRecursive(vlog); break;
            case 8: _SetBeankey.LeaderApplyNoRecursive(vlog); break;
            case 9: _MapInt.LeaderApplyNoRecursive(vlog); break;
            case 10: _MapBean.LeaderApplyNoRecursive(vlog); break;
            case 11: _Beankey = ((Zeze.Raft.RocksRaft.Log1.LogBeanKey<Zeze.Builtin.TestRocks.BeanKey>)vlog).Value; break;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void FollowerApply(Zeze.Raft.RocksRaft.Log log) {
        var vars = ((Zeze.Raft.RocksRaft.LogBean)log).getVariables();
        if (vars == null)
            return;
        for (var it = vars.iterator(); it.moveToNext(); ) {
            var vlog = it.value();
            switch (vlog.getVariableId()) {
                case 1: _Int = ((Zeze.Raft.RocksRaft.Log1.LogInt)vlog).Value; break;
                case 2: _Bool = ((Zeze.Raft.RocksRaft.Log1.LogBool)vlog).Value; break;
                case 3: _Float = ((Zeze.Raft.RocksRaft.Log1.LogFloat)vlog).Value; break;
                case 4: _double = ((Zeze.Raft.RocksRaft.Log1.LogDouble)vlog).Value; break;
                case 5: _String = ((Zeze.Raft.RocksRaft.Log1.LogString)vlog).Value; break;
                case 6: _Binary = ((Zeze.Raft.RocksRaft.Log1.LogBinary)vlog).Value; break;
                case 7: _SetInt.FollowerApply(vlog); break;
                case 8: _SetBeankey.FollowerApply(vlog); break;
                case 9: _MapInt.FollowerApply(vlog); break;
                case 10: _MapBean.FollowerApply(vlog); break;
                case 11: _Beankey = ((Zeze.Raft.RocksRaft.Log1.LogBeanKey<Zeze.Builtin.TestRocks.BeanKey>)vlog).Value; break;
            }
        }
    }
}
