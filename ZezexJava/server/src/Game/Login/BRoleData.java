package Game.Login;

import Zeze.Serialize.*;
import Game.*;

public final class BRoleData extends Zeze.Transaction.Bean implements BRoleDataReadOnly {
	private String _Name;

	public String getName() {
		if (false == this.isManaged()) {
			return _Name;
		}
		var txn = Zeze.Transaction.Transaction.Current;
		if (txn == null) {
			return _Name;
		}
		txn.VerifyRecordAccessed(this, true);
		var log = (Log__Name)txn.GetLog(this.getObjectId() + 1);
		return log != null ? log.getValue() : _Name;
	}
	public void setName(String value) {
		if (value.equals(null)) {
			throw new NullPointerException();
		}
		if (false == this.isManaged()) {
			_Name = value;
			return;
		}
		var txn = Zeze.Transaction.Transaction.Current;
		txn.VerifyRecordAccessed(this, false);
		txn.PutLog(new Log__Name(this, value));
	}


	public BRoleData() {
		this(0);
	}

	public BRoleData(int _varId_) {
		super(_varId_);
		_Name = "";
	}

	public void Assign(BRoleData other) {
		setName(other.getName());
	}

	public BRoleData CopyIfManaged() {
		return isManaged() ? Copy() :this;
	}

	public BRoleData Copy() {
		var copy = new BRoleData();
		copy.Assign(this);
		return copy;
	}

	public static void Swap(BRoleData a, BRoleData b) {
		BRoleData save = a.Copy();
		a.Assign(b);
		b.Assign(save);
	}

	@Override
	public Zeze.Transaction.Bean CopyBean() {
		return Copy();
	}

	public static final long TYPEID = -8623489599681441610;
	@Override
	public long getTypeId() {
		return TYPEID;
	}

	private final static class Log__Name extends Zeze.Transaction.Log<BRoleData, String> {
		public Log__Name(BRoleData self, String value) {
			super(self, value);
		}
		@Override
		public long getLogKey() {
			return this.Bean.ObjectId + 1;
		}
		@Override
		public void Commit() {
			this.getBeanTyped()._Name = this.getValue();
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
		sb.append(tangible.StringHelper.repeatChar(' ', level * 4)).Append("Game.Login.BRoleData: {").Append(System.lineSeparator());
		level++;
		sb.append(tangible.StringHelper.repeatChar(' ', level * 4)).Append("Name").Append("=").Append(getName()).Append("").Append(System.lineSeparator());
		sb.append("}");
	}

	@Override
	public void Encode(ByteBuffer _os_) {
		_os_.WriteInt(1); // Variables.Count
		_os_.WriteInt(ByteBuffer.STRING | 1 << ByteBuffer.TAG_SHIFT);
		_os_.WriteString(getName());
	}

	@Override
	public void Decode(ByteBuffer _os_) {
		for (int _varnum_ = _os_.ReadInt(); _varnum_ > 0; --_varnum_) { // Variables.Count
			int _tagid_ = _os_.ReadInt();
			switch (_tagid_) {
				case ByteBuffer.STRING | 1 << ByteBuffer.TAG_SHIFT:
					setName(_os_.ReadString());
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
		return false;
	}

}