package Zezex.Provider;

import Zeze.Serialize.*;
import Zezex.*;

// auto-generated



public interface BDispatchReadOnly {
	public long getTypeId();
	public void Encode(ByteBuffer _os_);
	public boolean NegativeCheck();
	public Zeze.Transaction.Bean CopyBean();

	public long getLinkSid();
	public String getAccount();
	public int getProtocolType();
	public Zeze.Net.Binary getProtocolData();
	public System.Collections.Generic.IReadOnlyList<Long> getStates();
	public Zeze.Net.Binary getStatex();
}