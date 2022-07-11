package Zeze.Services.GlobalCacheManager;

import Zeze.Transaction.EmptyBean;

public final class KeepAlive extends Zeze.Net.Rpc<EmptyBean, EmptyBean> {
	public static final int ProtocolId_ = Zeze.Transaction.Bean.Hash32(KeepAlive.class.getName()); // 560224048
	public static final long TypeId_ = ProtocolId_ & 0xffff_ffffL; // 560224048

	@Override
	public int getModuleId() {
		return 0;
	}

	@Override
	public int getProtocolId() {
		return ProtocolId_;
	}

	public KeepAlive() {
		Argument = new EmptyBean();
		Result = new EmptyBean();
	}
}
