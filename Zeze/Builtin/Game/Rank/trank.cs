// auto-generated
using Zeze.Serialize;

namespace Zeze.Builtin.Game.Rank
{
    public sealed class trank : Zeze.Transaction.Table<Zeze.Builtin.Game.Rank.BConcurrentKey, Zeze.Builtin.Game.Rank.BRankList>
    {
        public trank() : base("Zeze_Builtin_Game_Rank_trank")
        {
        }

        public override int Id => -2043108039;
        public override bool IsMemory => false;
        public override bool IsAutoKey => false;

        public const int VAR_RankList = 1;

        public override Zeze.Builtin.Game.Rank.BConcurrentKey DecodeKey(ByteBuffer _os_)
        {
            var _v_ = new Zeze.Builtin.Game.Rank.BConcurrentKey();
            _v_.Decode(_os_);
            return _v_;
        }

        public override ByteBuffer EncodeKey(Zeze.Builtin.Game.Rank.BConcurrentKey _v_)
        {
            ByteBuffer _os_ = ByteBuffer.Allocate();
            _v_.Encode(_os_);
            return _os_;
        }
    }
}
