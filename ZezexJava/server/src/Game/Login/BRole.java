package Game.Login;

import Zeze.Serialize.*;
import Game.*;

public final class BRole extends Zeze.Transaction.Bean implements BRoleReadOnly {
	private long _Id;
	private String _Name;

	public long getId() {
		if (false == this.isManaged()) {
			return _Id;
		}
		var txn = Zeze.Transaction.Transaction.Current;
		if (txn == null) {
			return _Id;
		}
		txn.VerifyRecordAccessed(this, true);
		var log = (Log__Id)txn.GetLog(this.getObjectId() + 1);
		return log != null ? log.getValue() : _Id;
	}
	public void setId(long value) {
		if (false == this.isManaged()) {
			_Id = value;
			return;
		}
		var txn = Zeze.Transaction.Transaction.Current;
		txn.VerifyRecordAccessed(this, false);
		txn.PutLog(new Log__Id(this, value));
	}

	public String getName() {
		if (false == this.isManaged()) {
			return _Name;
		}
		var txn = Zeze.Transaction.Transaction.Current;
		if (txn == null) {
			return _Name;
		}
		txn.VerifyRecordAccessed(this, true);
		var log = (Log__Name)txn.GetLog(this.getObjectId() + 2);
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


	public BRole() {
		this(0);
	}

	public BRole(int _varId_) {
		super(_varId_);
		_Name = "";
	}

	public void Assign(BRole other) {
		setId(other.getId());
		setName(other.getName());
	}

	public BRole CopyIfManaged() {
		return isManaged() ? Copy() :this;
	}

	public BRole Copy() {
		var copy = new BRole();
		copy.Assign(this);
		return copy;
	}

	public static void Swap(BRole a, BRole b) {
		BRole save = a.Copy();
		a.Assign(b);
		b.Assign(save);
	}

	@Override
	public Zeze.Transaction.Bean CopyBean() {
		return Copy();
	}

	public static final long TYPEID = -8384964419164522880;
	@Override
	public long getTypeId() {
		return TYPEID;
	}

	private final static class Log__Id extends Zeze.Transaction.Log<BRole, Long> {
		public Log__Id(BRole self, long value) {
			super(self, value);
		}
		@Override
		public long getLogKey() {
			return this.Bean.ObjectId + 1;
		}
		@Override
		public void Commit() {
			this.getBeanTyped()._Id = this.getValue();
		}
	}

	private final static class Log__Name extends Zeze.Transaction.Log<BRole, String> {
		public Log__Name(BRole self, String value) {
			super(self, value);
		}
		@Override
		public long getLogKey() {
			return this.Bean.ObjectId + 2;
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
		sb.append(tangible.StringHelper.repeatChar(' ', level * 4)).Append("Game.Login.BRole: {").Append(System.lineSeparator());
		level++;
		sb.append(tangible.StringHelper.repeatChar(' ', level * 4)).Append("Id").Append("=").Append(getId()).Append(",").Append(System.lineSeparator());
		sb.append(tangible.StringHelper.repeatChar(' ', level * 4)).Append("Name").Append("=").Append(getName()).Append("").Append(System.lineSeparator());
		sb.append("}");
	}

	@Override
	public void Encode(ByteBuffer _os_) {
		_os_.WriteInt(2); // Variables.Count
		_os_.WriteInt(ByteBuffer.LONG | 1 << ByteBuffer.TAG_SHIFT);
		_os_.WriteLong(getId());
		_os_.WriteInt(ByteBuffer.STRING | 2 << ByteBuffer.TAG_SHIFT);
		_os_.WriteString(getName());
	}

	@Override
	public void Decode(ByteBuffer _os_) {
		for (int _varnum_ = _os_.ReadInt(); _varnum_ > 0; --_varnum_) { // Variables.Count
			int _tagid_ = _os_.ReadInt();
			switch (_tagid_) {
				case ByteBuffer.LONG | 1 << ByteBuffer.TAG_SHIFT:
					setId(_os_.ReadLong());
					break;
				case ByteBuffer.STRING | 2 << ByteBuffer.TAG_SHIFT:
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
		if (getId() < 0) {
			return true;
		}
		return false;
	}

}