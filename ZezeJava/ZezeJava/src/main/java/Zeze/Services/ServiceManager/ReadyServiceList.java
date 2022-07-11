package Zeze.Services.ServiceManager;

import Zeze.Net.Protocol;
import Zeze.Transaction.Bean;

public final class ReadyServiceList extends Protocol<ServiceListVersion> {
	public static final int ProtocolId_ = Bean.Hash32(ReadyServiceList.class.getName()); // -276543939
	public static final long TypeId_ = ProtocolId_ & 0xffff_ffffL; // 4018423357

	public ReadyServiceList() {
		Argument = new ServiceListVersion();
	}

	@Override
	public int getModuleId() {
		return 0;
	}

	@Override
	public int getProtocolId() {
		return ProtocolId_;
	}
}
