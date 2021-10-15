package Game.Bag;

import Zeze.Serialize.*;
import Game.*;

public final class BDestroy extends Zeze.Transaction.Bean implements BDestroyReadOnly {
	private int _Position;

	public int getPosition() {
		if (false == this.isManaged()) {
			return _Position;
		}
		var txn = Zeze.Transaction.Transaction.Current;
		if (txn == null) {
			return _Position;
		}
		txn.VerifyRecordAccessed(this, true);
		var log = (Log__Position)txn.GetLog(this.getObjectId() + 1);
		return log != null ? log.getValue() : _Position;
	}
	public void setPosition(int value) {
		if (false == this.isManaged()) {
			_Position = value;
			return;
		}
		var txn = Zeze.Transaction.Transaction.Current;
		txn.VerifyRecordAccessed(this, false);
		txn.PutLog(new Log__Position(this, value));
	}


	public BDestroy() {
		this(0);
	}

	public BDestroy(int _varId_) {
		super(_varId_);
	}

	public void Assign(BDestroy other) {
		setPosition(other.getPosition());
	}

	public BDestroy CopyIfManaged() {
		return isManaged() ? Copy() :this;
	}

	public BDestroy Copy() {
		var copy = new BDestroy();
		copy.Assign(this);
		return copy;
	}

	public static void Swap(BDestroy a, BDestroy b) {
		BDestroy save = a.Copy();
		a.Assign(b);
		b.Assign(save);
	}

	@Override
	public Zeze.Transaction.Bean CopyBean() {
		return Copy();
	}

	public static final long TYPEID = -6074217865062200097;
	@Override
	public long getTypeId() {
		return TYPEID;
	}

	private final static class Log__Position extends Zeze.Transaction.Log<BDestroy, Integer> {
		public Log__Position(BDestroy self, int value) {
			super(self, value);
		}
		@Override
		public long getLogKey() {
			return this.Bean.ObjectId + 1;
		}
		@Override
		public void Commit() {
			this.getBeanTyped()._Position = this.getValue();
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		BuildString(sb, 0);
		sb.append(System.lineSeparator());
		return sb.toString();
	}

	@Override
	public void BuildString(StringBuilder sb, int level) {
		sb.append(tangible.StringHelper.repeatChar(' ', level * 4)).Append("Game.Bag.BDestroy: {").Append(System.lineSeparator());
		level++;
		sb.append(tangible.StringHelper.repeatChar(' ', level * 4)).Append("Position").Append("=").Append(getPosition()).Append("").Append(System.lineSeparator());
		sb.append("}");
	}

	@Override
	public void Encode(ByteBuffer _os_) {
		_os_.WriteInt(1); // Variables.Count
		_os_.WriteInt(ByteBuffer.INT | 1 << ByteBuffer.TAG_SHIFT);
		_os_.WriteInt(getPosition());
	}

	@Override
	public void Decode(ByteBuffer _os_) {
		for (int _varnum_ = _os_.ReadInt(); _varnum_ > 0; --_varnum_) { // Variables.Count
			int _tagid_ = _os_.ReadInt();
			switch (_tagid_) {
				case ByteBuffer.INT | 1 << ByteBuffer.TAG_SHIFT:
					setPosition(_os_.ReadInt());
					break;
				default:
					ByteBuffer.SkipUnknownField(_tagid_, _os_);
					break;
			}
		}
	}

	@Override
	protected void InitChildrenRootInfo(Zeze.Transaction.Record.RootInfo root) {
	}

	@Override
	public boolean NegativeCheck() {
		if (getPosition() < 0) {
			return true;
		}
		return false;
	}

}