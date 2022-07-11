package Zeze.Services.ServiceManager;

import Zeze.Serialize.ByteBuffer;
import Zeze.Transaction.Bean;
import Zeze.Transaction.Record;

public final class AllocateIdArgument extends Bean {
	private String Name;
	private int Count;

	public String getName() {
		return Name;
	}

	public void setName(String value) {
		Name = value;
	}

	public int getCount() {
		return Count;
	}

	public void setCount(int value) {
		Count = value;
	}

	@Override
	public void Decode(ByteBuffer bb) {
		setName(bb.ReadString());
		setCount(bb.ReadInt());
	}

	@Override
	public void Encode(ByteBuffer bb) {
		bb.WriteString(getName());
		bb.WriteInt(getCount());
	}

	@Override
	protected void InitChildrenRootInfo(Record.RootInfo root) {
		throw new UnsupportedOperationException();
	}

	private static int _PRE_ALLOC_SIZE_ = 16;

	@Override
	public int getPreAllocSize() {
		return _PRE_ALLOC_SIZE_;
	}

	@Override
	public void setPreAllocSize(int size) {
		_PRE_ALLOC_SIZE_ = size;
	}

	@Override
	public String toString() {
		return "AllocateIdArgument{" + "Name='" + Name + '\'' + ", Count=" + Count + '}';
	}
}
