package Zeze.Transaction.Logs;

import Zeze.Transaction.Bean;
import Zeze.Transaction.Log;
import Zeze.Serialize.ByteBuffer;

public abstract class LogDouble extends Log {
	private static final int TYPE_ID = Zeze.Transaction.Bean.Hash32("Zeze.Raft.RocksRaft.Log<double>");

	public double Value;

	public LogDouble() {
		super(TYPE_ID);
	}

	public LogDouble(Bean belong, int varId, double value) {
		this();
		setBelong(belong);
		setVariableId(varId);
		Value = value;
	}

	@Override
	public void Encode(ByteBuffer bb) {
		bb.WriteDouble(Value);
	}

	@Override
	public void Decode(ByteBuffer bb) {
		Value = bb.ReadDouble();
	}

	@Override
	public String toString() {
		return String.valueOf(Value);
	}
}
