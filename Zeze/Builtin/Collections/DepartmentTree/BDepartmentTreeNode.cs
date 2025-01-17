// auto-generated
using ByteBuffer = Zeze.Serialize.ByteBuffer;
using Environment = System.Environment;

namespace Zeze.Builtin.Collections.DepartmentTree
{
    public interface BDepartmentTreeNodeReadOnly
    {
        public long TypeId { get; }
        public void Encode(ByteBuffer _os_);
        public bool NegativeCheck();
        public Zeze.Transaction.Bean CopyBean();

        public long ParentDepartment { get; }
        public System.Collections.Generic.IReadOnlyDictionary<string,long> Childs { get; }
        public string Name { get; }
        public System.Collections.Generic.IReadOnlyDictionary<string,Zeze.Transaction.DynamicBean> Managers { get; }
    }

    public sealed class BDepartmentTreeNode : Zeze.Transaction.Bean, BDepartmentTreeNodeReadOnly
    {
        long _ParentDepartment; // 0表示第一级部门
        readonly Zeze.Transaction.Collections.CollMap1<string, long> _Childs; // name 2 id。采用整体保存，因为需要排序和重名判断。需要加数量上限。
        Zeze.Transaction.Collections.CollMapReadOnly<string,long,long> _ChildsReadOnly;
        string _Name;
        readonly Zeze.Transaction.Collections.CollMap1<string, Zeze.Transaction.DynamicBean> _Managers;
        Zeze.Transaction.Collections.CollMapReadOnly<string,Zeze.Transaction.DynamicBean,Zeze.Transaction.DynamicBean> _ManagersReadOnly;
        public static long GetSpecialTypeIdFromBean_Managers(Zeze.Transaction.Bean bean)
        {
            switch (bean.TypeId)
            {
                case Zeze.Transaction.EmptyBean.TYPEID: return Zeze.Transaction.EmptyBean.TYPEID;
            }
            throw new System.Exception("Unknown Bean! dynamic@Zeze.Builtin.Collections.DepartmentTree.BDepartmentTreeNode:Managers");
        }

        public static Zeze.Transaction.Bean CreateBeanFromSpecialTypeId_Managers(long typeId)
        {
            return null;
        }


        public long ParentDepartment
        {
            get
            {
                if (!IsManaged)
                    return _ParentDepartment;
                var txn = Zeze.Transaction.Transaction.Current;
                if (txn == null) return _ParentDepartment;
                txn.VerifyRecordAccessed(this, true);
                var log = (Log__ParentDepartment)txn.GetLog(ObjectId + 1);
                return log != null ? log.Value : _ParentDepartment;
            }
            set
            {
                if (!IsManaged)
                {
                    _ParentDepartment = value;
                    return;
                }
                var txn = Zeze.Transaction.Transaction.Current;
                txn.VerifyRecordAccessed(this);
                txn.PutLog(new Log__ParentDepartment() { Belong = this, VariableId = 1, Value = value });
            }
        }

        public Zeze.Transaction.Collections.CollMap1<string, long> Childs => _Childs;
        System.Collections.Generic.IReadOnlyDictionary<string,long> Zeze.Builtin.Collections.DepartmentTree.BDepartmentTreeNodeReadOnly.Childs => _ChildsReadOnly;

        public string Name
        {
            get
            {
                if (!IsManaged)
                    return _Name;
                var txn = Zeze.Transaction.Transaction.Current;
                if (txn == null) return _Name;
                txn.VerifyRecordAccessed(this, true);
                var log = (Log__Name)txn.GetLog(ObjectId + 3);
                return log != null ? log.Value : _Name;
            }
            set
            {
                if (value == null) throw new System.ArgumentNullException(nameof(value));
                if (!IsManaged)
                {
                    _Name = value;
                    return;
                }
                var txn = Zeze.Transaction.Transaction.Current;
                txn.VerifyRecordAccessed(this);
                txn.PutLog(new Log__Name() { Belong = this, VariableId = 3, Value = value });
            }
        }

        public Zeze.Transaction.Collections.CollMap1<string, Zeze.Transaction.DynamicBean> Managers => _Managers;
        System.Collections.Generic.IReadOnlyDictionary<string,Zeze.Transaction.DynamicBean> Zeze.Builtin.Collections.DepartmentTree.BDepartmentTreeNodeReadOnly.Managers => _ManagersReadOnly;

        public BDepartmentTreeNode() : this(0)
        {
        }

        public BDepartmentTreeNode(int _varId_) : base(_varId_)
        {
            _Childs = new Zeze.Transaction.Collections.CollMap1<string, long>() { VariableId = 2 };
            _ChildsReadOnly = new Zeze.Transaction.Collections.CollMapReadOnly<string,long,long>(_Childs);
            _Name = "";
            _Managers = new Zeze.Transaction.Collections.CollMap1<string, Zeze.Transaction.DynamicBean>() { VariableId = 4 };
            _ManagersReadOnly = new Zeze.Transaction.Collections.CollMapReadOnly<string,Zeze.Transaction.DynamicBean,Zeze.Transaction.DynamicBean>(_Managers);
        }

        public void Assign(BDepartmentTreeNode other)
        {
            ParentDepartment = other.ParentDepartment;
            Childs.Clear();
            foreach (var e in other.Childs)
                Childs.Add(e.Key, e.Value);
            Name = other.Name;
            Managers.Clear();
            foreach (var e in other.Managers)
                Managers.Add(e.Key, e.Value);
        }

        public BDepartmentTreeNode CopyIfManaged()
        {
            return IsManaged ? Copy() : this;
        }

        public BDepartmentTreeNode Copy()
        {
            var copy = new BDepartmentTreeNode();
            copy.Assign(this);
            return copy;
        }

        public static void Swap(BDepartmentTreeNode a, BDepartmentTreeNode b)
        {
            BDepartmentTreeNode save = a.Copy();
            a.Assign(b);
            b.Assign(save);
        }

        public override Zeze.Transaction.Bean CopyBean()
        {
            return Copy();
        }

        public const long TYPEID = 2712461973987809351;
        public override long TypeId => TYPEID;

        sealed class Log__ParentDepartment : Zeze.Transaction.Log<long>
        {
            public override void Commit() { ((BDepartmentTreeNode)Belong)._ParentDepartment = this.Value; }
        }


        sealed class Log__Name : Zeze.Transaction.Log<string>
        {
            public override void Commit() { ((BDepartmentTreeNode)Belong)._Name = this.Value; }
        }


        public override string ToString()
        {
            var sb = new System.Text.StringBuilder();
            BuildString(sb, 0);
            sb.Append(Environment.NewLine);
            return sb.ToString();
        }

        public override void BuildString(System.Text.StringBuilder sb, int level)
        {
            sb.Append(Zeze.Util.Str.Indent(level)).Append("Zeze.Builtin.Collections.DepartmentTree.BDepartmentTreeNode: {").Append(Environment.NewLine);
            level += 4;
            sb.Append(Zeze.Util.Str.Indent(level)).Append("ParentDepartment").Append('=').Append(ParentDepartment).Append(',').Append(Environment.NewLine);
            sb.Append(Zeze.Util.Str.Indent(level)).Append("Childs").Append("=[").Append(Environment.NewLine);
            level += 4;
            foreach (var _kv_ in Childs)
            {
                sb.Append(Zeze.Util.Str.Indent(level)).Append('(').Append(Environment.NewLine);
                var Key = _kv_.Key;
                sb.Append(Zeze.Util.Str.Indent(level)).Append("Key").Append('=').Append(Key).Append(',').Append(Environment.NewLine);
                var Value = _kv_.Value;
                sb.Append(Zeze.Util.Str.Indent(level)).Append("Value").Append('=').Append(Value).Append(',').Append(Environment.NewLine);
                sb.Append(Zeze.Util.Str.Indent(level)).Append(')').Append(Environment.NewLine);
            }
            level -= 4;
            sb.Append(Zeze.Util.Str.Indent(level)).Append(']').Append(',').Append(Environment.NewLine);
            sb.Append(Zeze.Util.Str.Indent(level)).Append("Name").Append('=').Append(Name).Append(',').Append(Environment.NewLine);
            sb.Append(Zeze.Util.Str.Indent(level)).Append("Managers").Append("=[").Append(Environment.NewLine);
            level += 4;
            foreach (var _kv_ in Managers)
            {
                sb.Append(Zeze.Util.Str.Indent(level)).Append('(').Append(Environment.NewLine);
                var Key = _kv_.Key;
                sb.Append(Zeze.Util.Str.Indent(level)).Append("Key").Append('=').Append(Key).Append(',').Append(Environment.NewLine);
                var Value = _kv_.Value;
                sb.Append(Zeze.Util.Str.Indent(level)).Append("Value").Append('=').Append(Environment.NewLine);
                Value.Bean.BuildString(sb, level + 4);
                sb.Append(',').Append(Environment.NewLine);
                sb.Append(Zeze.Util.Str.Indent(level)).Append(')').Append(Environment.NewLine);
            }
            level -= 4;
            sb.Append(Zeze.Util.Str.Indent(level)).Append(']').Append(Environment.NewLine);
            level -= 4;
            sb.Append(Zeze.Util.Str.Indent(level)).Append('}');
        }

        public override void Encode(ByteBuffer _o_)
        {
            int _i_ = 0;
            {
                long _x_ = ParentDepartment;
                if (_x_ != 0)
                {
                    _i_ = _o_.WriteTag(_i_, 1, ByteBuffer.INTEGER);
                    _o_.WriteLong(_x_);
                }
            }
            {
                var _x_ = Childs;
                int _n_ = _x_.Count;
                if (_n_ != 0)
                {
                    _i_ = _o_.WriteTag(_i_, 2, ByteBuffer.MAP);
                    _o_.WriteMapType(_n_, ByteBuffer.BYTES, ByteBuffer.INTEGER);
                    foreach (var _e_ in _x_)
                    {
                        _o_.WriteString(_e_.Key);
                        _o_.WriteLong(_e_.Value);
                    }
                }
            }
            {
                string _x_ = Name;
                if (_x_.Length != 0)
                {
                    _i_ = _o_.WriteTag(_i_, 3, ByteBuffer.BYTES);
                    _o_.WriteString(_x_);
                }
            }
            {
                var _x_ = Managers;
                int _n_ = _x_.Count;
                if (_n_ != 0)
                {
                    _i_ = _o_.WriteTag(_i_, 4, ByteBuffer.MAP);
                    _o_.WriteMapType(_n_, ByteBuffer.BYTES, ByteBuffer.DYNAMIC);
                    foreach (var _e_ in _x_)
                    {
                        _o_.WriteString(_e_.Key);
                        _x_.Encode(_o_);
                    }
                }
            }
            _o_.WriteByte(0);
        }

        public override void Decode(ByteBuffer _o_)
        {
            int _t_ = _o_.ReadByte();
            int _i_ = _o_.ReadTagSize(_t_);
            if (_i_ == 1)
            {
                ParentDepartment = _o_.ReadLong(_t_);
                _i_ += _o_.ReadTagSize(_t_ = _o_.ReadByte());
            }
            if (_i_ == 2)
            {
                var _x_ = Childs;
                _x_.Clear();
                if ((_t_ & ByteBuffer.TAG_MASK) == ByteBuffer.MAP)
                {
                    int _s_ = (_t_ = _o_.ReadByte()) >> ByteBuffer.TAG_SHIFT;
                    for (int _n_ = _o_.ReadUInt(); _n_ > 0; _n_--)
                    {
                        var _k_ = _o_.ReadString(_s_);
                        var _v_ = _o_.ReadLong(_t_);
                        _x_.Add(_k_, _v_);
                    }
                }
                else
                    _o_.SkipUnknownField(_t_);
                _i_ += _o_.ReadTagSize(_t_ = _o_.ReadByte());
            }
            if (_i_ == 3)
            {
                Name = _o_.ReadString(_t_);
                _i_ += _o_.ReadTagSize(_t_ = _o_.ReadByte());
            }
            if (_i_ == 4)
            {
                var _x_ = Managers;
                _x_.Clear();
                if ((_t_ & ByteBuffer.TAG_MASK) == ByteBuffer.MAP)
                {
                    int _s_ = (_t_ = _o_.ReadByte()) >> ByteBuffer.TAG_SHIFT;
                    for (int _n_ = _o_.ReadUInt(); _n_ > 0; _n_--)
                    {
                        var _k_ = _o_.ReadString(_s_);
                        var _v_ = new Zeze.Transaction.DynamicBean(0, Zeze.Collections.DepartmentTree.GetSpecialTypeIdFromBean, Zeze.Collections.DepartmentTree.CreateBeanFromSpecialTypeId);
                        _v_.Decode(_o_);
                        _x_.Add(_k_, _v_);
                    }
                }
                else
                    _o_.SkipUnknownField(_t_);
                _i_ += _o_.ReadTagSize(_t_ = _o_.ReadByte());
            }
            while (_t_ != 0)
            {
                _o_.SkipUnknownField(_t_);
                _o_.ReadTagSize(_t_ = _o_.ReadByte());
            }
        }

        protected override void InitChildrenRootInfo(Zeze.Transaction.Record.RootInfo root)
        {
            _Childs.InitRootInfo(root, this);
            _Managers.InitRootInfo(root, this);
        }

        public override bool NegativeCheck()
        {
            if (ParentDepartment < 0) return true;
            foreach (var _v_ in Childs.Values)
            {
                if (_v_ < 0) return true;
            }
            return false;
        }

        public override void FollowerApply(Zeze.Transaction.Log log)
        {
            var blog = (Zeze.Transaction.Collections.LogBean)log;
            foreach (var vlog in blog.Variables.Values)
            {
                switch (vlog.VariableId)
                {
                    case 1: _ParentDepartment = ((Zeze.Transaction.Log<long>)vlog).Value; break;
                    case 2: _Childs.FollowerApply(vlog); break;
                    case 3: _Name = ((Zeze.Transaction.Log<string>)vlog).Value; break;
                    case 4: _Managers.FollowerApply(vlog); break;
                }
            }
        }

    }
}
