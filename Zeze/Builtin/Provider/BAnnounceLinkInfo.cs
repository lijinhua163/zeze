// auto-generated
using ByteBuffer = Zeze.Serialize.ByteBuffer;
using Environment = System.Environment;

namespace Zeze.Builtin.Provider
{
    public interface BAnnounceLinkInfoReadOnly
    {
        public long TypeId { get; }
        public void Encode(ByteBuffer _os_);
        public bool NegativeCheck();
        public Zeze.Transaction.Bean CopyBean();

    }

    public sealed class BAnnounceLinkInfo : Zeze.Transaction.Bean, BAnnounceLinkInfoReadOnly
    {

        public BAnnounceLinkInfo() : this(0)
        {
        }

        public BAnnounceLinkInfo(int _varId_) : base(_varId_)
        {
        }

        public void Assign(BAnnounceLinkInfo other)
        {
        }

        public BAnnounceLinkInfo CopyIfManaged()
        {
            return IsManaged ? Copy() : this;
        }

        public BAnnounceLinkInfo Copy()
        {
            var copy = new BAnnounceLinkInfo();
            copy.Assign(this);
            return copy;
        }

        public static void Swap(BAnnounceLinkInfo a, BAnnounceLinkInfo b)
        {
            BAnnounceLinkInfo save = a.Copy();
            a.Assign(b);
            b.Assign(save);
        }

        public override Zeze.Transaction.Bean CopyBean()
        {
            return Copy();
        }

        public const long TYPEID = 6291432069805514560;
        public override long TypeId => TYPEID;

        public override string ToString()
        {
            var sb = new System.Text.StringBuilder();
            BuildString(sb, 0);
            sb.Append(Environment.NewLine);
            return sb.ToString();
        }

        public override void BuildString(System.Text.StringBuilder sb, int level)
        {
            sb.Append(Zeze.Util.Str.Indent(level)).Append("Zeze.Builtin.Provider.BAnnounceLinkInfo: {").Append(Environment.NewLine);
            level += 4;
            level -= 4;
            sb.Append(Zeze.Util.Str.Indent(level)).Append('}');
        }

        public override void Encode(ByteBuffer _o_)
        {
            _o_.WriteByte(0);
        }

        public override void Decode(ByteBuffer _o_)
        {
            int _t_ = _o_.ReadByte();
            _o_.ReadTagSize(_t_);
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
            return false;
        }

        public override void FollowerApply(Zeze.Transaction.Log log)
        {
        }

    }
}
