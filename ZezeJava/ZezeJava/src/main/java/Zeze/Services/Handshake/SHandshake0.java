package Zeze.Services.Handshake;

import Zeze.Net.Protocol;
import Zeze.Transaction.Bean;

public class SHandshake0 extends Protocol<SHandshake0Argument> {
	public static final int ProtocolId_ = Bean.Hash32(SHandshake0.class.getName()); // -2018202792
	public static final long TypeId_ = ProtocolId_ & 0xffff_ffffL; // 2276764504

	@Override
	public int getModuleId() {
		return 0;
	}

	@Override
	public int getProtocolId() {
		return ProtocolId_;
	}

	public SHandshake0() {
		Argument = new SHandshake0Argument();
	}

	public SHandshake0(boolean encrypt) {
		Argument = new SHandshake0Argument();
		Argument.EnableEncrypt = encrypt;
	}
}
