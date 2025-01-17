package Zeze.Transaction.Logs;

import Zeze.Transaction.Bean;
import Zeze.Transaction.Log;
import Zeze.Serialize.ByteBuffer;

public abstract class LogByte extends Log {
	private static final int TYPE_ID = Zeze.Transaction.Bean.Hash32("Zeze.Raft.RocksRaft.Log<byte>");

	public byte Value;

	public LogByte() {
		super(TYPE_ID);
	}

	public LogByte(Bean belong, int varId, byte value) {
		this();
		setBelong(belong);
		setVariableId(varId);
		Value = value;
	}

	@Override
	public void Encode(ByteBuffer bb) {
		bb.WriteLong(Value);
	}

	@Override
	public void Decode(ByteBuffer bb) {
		Value = (byte)bb.ReadLong();
	}

	@Override
	public String toString() {
		return String.valueOf(Value);
	}
}
