package Zeze.Services.Handshake;

import Zeze.Net.Protocol;
import Zeze.Transaction.Bean;
import Zeze.Transaction.EmptyBean;

public final class CHandshakeDone extends Protocol<EmptyBean> {
	public static final int ProtocolId_ = Bean.Hash32(CHandshakeDone.class.getName()); // 1896283174
	public static final long TypeId_ = ProtocolId_ & 0xffff_ffffL; // 1896283174

	@Override
	public int getModuleId() {
		return 0;
	}

	@Override
	public int getProtocolId() {
		return ProtocolId_;
	}

	public CHandshakeDone() {
		Argument = new EmptyBean();
	}
}
