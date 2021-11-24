package Zeze.Services.Handshake;

import Zeze.Net.*;
import Zeze.Transaction.*;

public final class CHandshakeDone extends Protocol1<EmptyBean> {
	public final static int ProtocolId_ = Bean.Hash32(CHandshakeDone.class.getName());

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