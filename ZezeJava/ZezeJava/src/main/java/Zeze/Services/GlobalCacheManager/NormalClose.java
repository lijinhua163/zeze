package Zeze.Services.GlobalCacheManager;

public class NormalClose extends Zeze.Net.Rpc<Zeze.Transaction.EmptyBean, Zeze.Transaction.EmptyBean> {
	public static final int ProtocolId_ = Zeze.Transaction.Bean.Hash32(NormalClose.class.getName()); // -532299976
	public static final long TypeId_ = ProtocolId_ & 0xffff_ffffL; // 3762667320

	@Override
	public int getModuleId() {
		return 0;
	}

	@Override
	public int getProtocolId() {
		return ProtocolId_;
	}

	public NormalClose() {
		Argument = new Zeze.Transaction.EmptyBean();
		Result = new Zeze.Transaction.EmptyBean();
	}
}
