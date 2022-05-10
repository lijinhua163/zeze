package Zeze.Transaction.Logs;

import Zeze.Net.Binary;
import Zeze.Transaction.Bean;
import Zeze.Transaction.Log;
import Zeze.Serialize.ByteBuffer;

public abstract class LogBinary extends Log {
	private static final int TYPE_ID = Zeze.Transaction.Bean.Hash32("Zeze.Raft.RocksRaft.Log<binary>");

	public Binary Value;

	public LogBinary() {
		super(TYPE_ID);
	}

	public LogBinary(Bean belong, int varId, Binary value) {
		this();
		setBelong(belong);
		setVariableId(varId);
		Value = value;
	}

	@Override
	public void Encode(ByteBuffer bb) {
		bb.WriteBinary(Value);
	}

	@Override
	public void Decode(ByteBuffer bb) {
		Value = bb.ReadBinary();
	}

	@Override
	public String toString() {
		return Value.toString();
	}
}