// auto-generated
using ByteBuffer = Zeze.Serialize.ByteBuffer;
using Environment = System.Environment;

namespace Zeze.Beans.RedoQueue
{
    public interface BQueueTaskReadOnly
    {
        public long TypeId { get; }
        public void Encode(ByteBuffer _os_);
        public bool NegativeCheck();
        public Zeze.Transaction.Bean CopyBean();

        public string QueueName { get; }
        public int TaskType { get; }
        public long TaskId { get; }
        public Zeze.Net.Binary TaskParam { get; }
        public long PrevTaskId { get; }
    }

    public sealed class BQueueTask : Zeze.Transaction.Bean, BQueueTaskReadOnly
    {
        string _QueueName; // 队列名称。
        int _TaskType; // 任务类型。
        long _TaskId; // 任务编号，必须递增。
        Zeze.Net.Binary _TaskParam; // 任务参数。
        long _PrevTaskId; // 上一个任务编号，用来发现错误。

        public string QueueName
        {
            get
            {
                if (!IsManaged)
                    return _QueueName;
                var txn = Zeze.Transaction.Transaction.Current;
                if (txn == null) return _QueueName;
                txn.VerifyRecordAccessed(this, true);
                var log = (Log__QueueName)txn.GetLog(ObjectId + 1);
                return log != null ? log.Value : _QueueName;
            }
            set
            {
                if (value == null) throw new System.ArgumentNullException();
                if (!IsManaged)
                {
                    _QueueName = value;
                    return;
                }
                var txn = Zeze.Transaction.Transaction.Current;
                txn.VerifyRecordAccessed(this);
                txn.PutLog(new Log__QueueName(this, value));
            }
        }

        public int TaskType
        {
            get
            {
                if (!IsManaged)
                    return _TaskType;
                var txn = Zeze.Transaction.Transaction.Current;
                if (txn == null) return _TaskType;
                txn.VerifyRecordAccessed(this, true);
                var log = (Log__TaskType)txn.GetLog(ObjectId + 2);
                return log != null ? log.Value : _TaskType;
            }
            set
            {
                if (!IsManaged)
                {
                    _TaskType = value;
                    return;
                }
                var txn = Zeze.Transaction.Transaction.Current;
                txn.VerifyRecordAccessed(this);
                txn.PutLog(new Log__TaskType(this, value));
            }
        }

        public long TaskId
        {
            get
            {
                if (!IsManaged)
                    return _TaskId;
                var txn = Zeze.Transaction.Transaction.Current;
                if (txn == null) return _TaskId;
                txn.VerifyRecordAccessed(this, true);
                var log = (Log__TaskId)txn.GetLog(ObjectId + 3);
                return log != null ? log.Value : _TaskId;
            }
            set
            {
                if (!IsManaged)
                {
                    _TaskId = value;
                    return;
                }
                var txn = Zeze.Transaction.Transaction.Current;
                txn.VerifyRecordAccessed(this);
                txn.PutLog(new Log__TaskId(this, value));
            }
        }

        public Zeze.Net.Binary TaskParam
        {
            get
            {
                if (!IsManaged)
                    return _TaskParam;
                var txn = Zeze.Transaction.Transaction.Current;
                if (txn == null) return _TaskParam;
                txn.VerifyRecordAccessed(this, true);
                var log = (Log__TaskParam)txn.GetLog(ObjectId + 4);
                return log != null ? log.Value : _TaskParam;
            }
            set
            {
                if (value == null) throw new System.ArgumentNullException();
                if (!IsManaged)
                {
                    _TaskParam = value;
                    return;
                }
                var txn = Zeze.Transaction.Transaction.Current;
                txn.VerifyRecordAccessed(this);
                txn.PutLog(new Log__TaskParam(this, value));
            }
        }

        public long PrevTaskId
        {
            get
            {
                if (!IsManaged)
                    return _PrevTaskId;
                var txn = Zeze.Transaction.Transaction.Current;
                if (txn == null) return _PrevTaskId;
                txn.VerifyRecordAccessed(this, true);
                var log = (Log__PrevTaskId)txn.GetLog(ObjectId + 5);
                return log != null ? log.Value : _PrevTaskId;
            }
            set
            {
                if (!IsManaged)
                {
                    _PrevTaskId = value;
                    return;
                }
                var txn = Zeze.Transaction.Transaction.Current;
                txn.VerifyRecordAccessed(this);
                txn.PutLog(new Log__PrevTaskId(this, value));
            }
        }

        public BQueueTask() : this(0)
        {
        }

        public BQueueTask(int _varId_) : base(_varId_)
        {
            _QueueName = "";
            _TaskParam = Zeze.Net.Binary.Empty;
        }

        public void Assign(BQueueTask other)
        {
            QueueName = other.QueueName;
            TaskType = other.TaskType;
            TaskId = other.TaskId;
            TaskParam = other.TaskParam;
            PrevTaskId = other.PrevTaskId;
        }

        public BQueueTask CopyIfManaged()
        {
            return IsManaged ? Copy() : this;
        }

        public BQueueTask Copy()
        {
            var copy = new BQueueTask();
            copy.Assign(this);
            return copy;
        }

        public static void Swap(BQueueTask a, BQueueTask b)
        {
            BQueueTask save = a.Copy();
            a.Assign(b);
            b.Assign(save);
        }

        public override Zeze.Transaction.Bean CopyBean()
        {
            return Copy();
        }

        public const long TYPEID = -948485694888722793;
        public override long TypeId => TYPEID;

        sealed class Log__QueueName : Zeze.Transaction.Log<BQueueTask, string>
        {
            public Log__QueueName(BQueueTask self, string value) : base(self, value) {}
            public override long LogKey => this.Bean.ObjectId + 1;
            public override void Commit() { this.BeanTyped._QueueName = this.Value; }
        }

        sealed class Log__TaskType : Zeze.Transaction.Log<BQueueTask, int>
        {
            public Log__TaskType(BQueueTask self, int value) : base(self, value) {}
            public override long LogKey => this.Bean.ObjectId + 2;
            public override void Commit() { this.BeanTyped._TaskType = this.Value; }
        }

        sealed class Log__TaskId : Zeze.Transaction.Log<BQueueTask, long>
        {
            public Log__TaskId(BQueueTask self, long value) : base(self, value) {}
            public override long LogKey => this.Bean.ObjectId + 3;
            public override void Commit() { this.BeanTyped._TaskId = this.Value; }
        }

        sealed class Log__TaskParam : Zeze.Transaction.Log<BQueueTask, Zeze.Net.Binary>
        {
            public Log__TaskParam(BQueueTask self, Zeze.Net.Binary value) : base(self, value) {}
            public override long LogKey => this.Bean.ObjectId + 4;
            public override void Commit() { this.BeanTyped._TaskParam = this.Value; }
        }

        sealed class Log__PrevTaskId : Zeze.Transaction.Log<BQueueTask, long>
        {
            public Log__PrevTaskId(BQueueTask self, long value) : base(self, value) {}
            public override long LogKey => this.Bean.ObjectId + 5;
            public override void Commit() { this.BeanTyped._PrevTaskId = this.Value; }
        }

        public override string ToString()
        {
            System.Text.StringBuilder sb = new System.Text.StringBuilder();
            BuildString(sb, 0);
            sb.Append(Environment.NewLine);
            return sb.ToString();
        }

        public override void BuildString(System.Text.StringBuilder sb, int level)
        {
            sb.Append(Zeze.Util.Str.Indent(level)).Append("Zeze.Beans.RedoQueue.BQueueTask: {").Append(Environment.NewLine);
            level += 4;
            sb.Append(Zeze.Util.Str.Indent(level)).Append("QueueName").Append('=').Append(QueueName).Append(',').Append(Environment.NewLine);
            sb.Append(Zeze.Util.Str.Indent(level)).Append("TaskType").Append('=').Append(TaskType).Append(',').Append(Environment.NewLine);
            sb.Append(Zeze.Util.Str.Indent(level)).Append("TaskId").Append('=').Append(TaskId).Append(',').Append(Environment.NewLine);
            sb.Append(Zeze.Util.Str.Indent(level)).Append("TaskParam").Append('=').Append(TaskParam).Append(',').Append(Environment.NewLine);
            sb.Append(Zeze.Util.Str.Indent(level)).Append("PrevTaskId").Append('=').Append(PrevTaskId).Append(Environment.NewLine);
            level -= 4;
            sb.Append(Zeze.Util.Str.Indent(level)).Append('}');
        }

        public override void Encode(ByteBuffer _o_)
        {
            int _i_ = 0;
            {
                string _x_ = QueueName;
                if (_x_.Length != 0)
                {
                    _i_ = _o_.WriteTag(_i_, 1, ByteBuffer.BYTES);
                    _o_.WriteString(_x_);
                }
            }
            {
                int _x_ = TaskType;
                if (_x_ != 0)
                {
                    _i_ = _o_.WriteTag(_i_, 2, ByteBuffer.INTEGER);
                    _o_.WriteInt(_x_);
                }
            }
            {
                long _x_ = TaskId;
                if (_x_ != 0)
                {
                    _i_ = _o_.WriteTag(_i_, 3, ByteBuffer.INTEGER);
                    _o_.WriteLong(_x_);
                }
            }
            {
                var _x_ = TaskParam;
                if (_x_.Count != 0)
                {
                    _i_ = _o_.WriteTag(_i_, 4, ByteBuffer.BYTES);
                    _o_.WriteBinary(_x_);
                }
            }
            {
                long _x_ = PrevTaskId;
                if (_x_ != 0)
                {
                    _i_ = _o_.WriteTag(_i_, 5, ByteBuffer.INTEGER);
                    _o_.WriteLong(_x_);
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
                QueueName = _o_.ReadString(_t_);
                _i_ += _o_.ReadTagSize(_t_ = _o_.ReadByte());
            }
            if (_i_ == 2)
            {
                TaskType = _o_.ReadInt(_t_);
                _i_ += _o_.ReadTagSize(_t_ = _o_.ReadByte());
            }
            if (_i_ == 3)
            {
                TaskId = _o_.ReadLong(_t_);
                _i_ += _o_.ReadTagSize(_t_ = _o_.ReadByte());
            }
            if (_i_ == 4)
            {
                TaskParam = _o_.ReadBinary(_t_);
                _i_ += _o_.ReadTagSize(_t_ = _o_.ReadByte());
            }
            if (_i_ == 5)
            {
                PrevTaskId = _o_.ReadLong(_t_);
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
        }

        public override bool NegativeCheck()
        {
            if (TaskType < 0) return true;
            if (TaskId < 0) return true;
            if (PrevTaskId < 0) return true;
            return false;
        }
    }
}