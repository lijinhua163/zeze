package Game.Rank;

import Zeze.Serialize.*;
import Game.*;

public final class BRankValue extends Zeze.Transaction.Bean implements BRankValueReadOnly {
	private long _RoleId;
	private long _Value; // 含义由 BConcurrentKey.RankType 决定
	private Zeze.Net.Binary _ValueEx; // 排名更多自定义数据。
	private boolean _AwardTaken; // 奖励已经获取。当开始领奖时，榜单不能再更新。这个变量可用于有时效的排行榜。

	public long getRoleId() {
		if (false == this.isManaged()) {
			return _RoleId;
		}
		var txn = Zeze.Transaction.Transaction.Current;
		if (txn == null) {
			return _RoleId;
		}
		txn.VerifyRecordAccessed(this, true);
		var log = (Log__RoleId)txn.GetLog(this.getObjectId() + 1);
		return log != null ? log.getValue() : _RoleId;
	}
	public void setRoleId(long value) {
		if (false == this.isManaged()) {
			_RoleId = value;
			return;
		}
		var txn = Zeze.Transaction.Transaction.Current;
		txn.VerifyRecordAccessed(this, false);
		txn.PutLog(new Log__RoleId(this, value));
	}

	public long getValue() {
		if (false == this.isManaged()) {
			return _Value;
		}
		var txn = Zeze.Transaction.Transaction.Current;
		if (txn == null) {
			return _Value;
		}
		txn.VerifyRecordAccessed(this, true);
		var log = (Log__Value)txn.GetLog(this.getObjectId() + 2);
		return log != null ? log.getValue() : _Value;
	}
	public void setValue(long value) {
		if (false == this.isManaged()) {
			_Value = value;
			return;
		}
		var txn = Zeze.Transaction.Transaction.Current;
		txn.VerifyRecordAccessed(this, false);
		txn.PutLog(new Log__Value(this, value));
	}

	public Zeze.Net.Binary getValueEx() {
		if (false == this.isManaged()) {
			return _ValueEx;
		}
		var txn = Zeze.Transaction.Transaction.Current;
		if (txn == null) {
			return _ValueEx;
		}
		txn.VerifyRecordAccessed(this, true);
		var log = (Log__ValueEx)txn.GetLog(this.getObjectId() + 3);
		return log != null ? log.getValue() : _ValueEx;
	}
	public void setValueEx(Zeze.Net.Binary value) {
		if (null == value) {
			throw new NullPointerException();
		}
		if (false == this.isManaged()) {
			_ValueEx = value;
			return;
		}
		var txn = Zeze.Transaction.Transaction.Current;
		txn.VerifyRecordAccessed(this, false);
		txn.PutLog(new Log__ValueEx(this, value));
	}

	public boolean getAwardTaken() {
		if (false == this.isManaged()) {
			return _AwardTaken;
		}
		var txn = Zeze.Transaction.Transaction.Current;
		if (txn == null) {
			return _AwardTaken;
		}
		txn.VerifyRecordAccessed(this, true);
		var log = (Log__AwardTaken)txn.GetLog(this.getObjectId() + 4);
		return log != null ? log.getValue() : _AwardTaken;
	}
	public void setAwardTaken(boolean value) {
		if (false == this.isManaged()) {
			_AwardTaken = value;
			return;
		}
		var txn = Zeze.Transaction.Transaction.Current;
		txn.VerifyRecordAccessed(this, false);
		txn.PutLog(new Log__AwardTaken(this, value));
	}


	public BRankValue() {
		this(0);
	}

	public BRankValue(int _varId_) {
		super(_varId_);
		_ValueEx = Zeze.Net.Binary.Empty;
	}

	public void Assign(BRankValue other) {
		setRoleId(other.getRoleId());
		setValue(other.getValue());
		setValueEx(other.getValueEx());
		setAwardTaken(other.getAwardTaken());
	}

	public BRankValue CopyIfManaged() {
		return isManaged() ? Copy() :this;
	}

	public BRankValue Copy() {
		var copy = new BRankValue();
		copy.Assign(this);
		return copy;
	}

	public static void Swap(BRankValue a, BRankValue b) {
		BRankValue save = a.Copy();
		a.Assign(b);
		b.Assign(save);
	}

	@Override
	public Zeze.Transaction.Bean CopyBean() {
		return Copy();
	}

	public static final long TYPEID = -1315645842391636530;
	@Override
	public long getTypeId() {
		return TYPEID;
	}

	private final static class Log__RoleId extends Zeze.Transaction.Log<BRankValue, Long> {
		public Log__RoleId(BRankValue self, long value) {
			super(self, value);
		}
		@Override
		public long getLogKey() {
			return this.Bean.ObjectId + 1;
		}
		@Override
		public void Commit() {
			this.getBeanTyped()._RoleId = this.getValue();
		}
	}

	private final static class Log__Value extends Zeze.Transaction.Log<BRankValue, Long> {
		public Log__Value(BRankValue self, long value) {
			super(self, value);
		}
		@Override
		public long getLogKey() {
			return this.Bean.ObjectId + 2;
		}
		@Override
		public void Commit() {
			this.getBeanTyped()._Value = this.getValue();
		}
	}

	private final static class Log__ValueEx extends Zeze.Transaction.Log<BRankValue, Zeze.Net.Binary> {
		public Log__ValueEx(BRankValue self, Zeze.Net.Binary value) {
			super(self, value);
		}
		@Override
		public long getLogKey() {
			return this.Bean.ObjectId + 3;
		}
		@Override
		public void Commit() {
			this.getBeanTyped()._ValueEx = this.getValue();
		}
	}

	private final static class Log__AwardTaken extends Zeze.Transaction.Log<BRankValue, Boolean> {
		public Log__AwardTaken(BRankValue self, boolean value) {
			super(self, value);
		}
		@Override
		public long getLogKey() {
			return this.Bean.ObjectId + 4;
		}
		@Override
		public void Commit() {
			this.getBeanTyped()._AwardTaken = this.getValue();
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
		sb.append(tangible.StringHelper.repeatChar(' ', level * 4)).Append("Game.Rank.BRankValue: {").Append(System.lineSeparator());
		level++;
		sb.append(tangible.StringHelper.repeatChar(' ', level * 4)).Append("RoleId").Append("=").Append(getRoleId()).Append(",").Append(System.lineSeparator());
		sb.append(tangible.StringHelper.repeatChar(' ', level * 4)).Append("Value").Append("=").Append(getValue()).Append(",").Append(System.lineSeparator());
		sb.append(tangible.StringHelper.repeatChar(' ', level * 4)).Append("ValueEx").Append("=").Append(getValueEx()).Append(",").Append(System.lineSeparator());
		sb.append(tangible.StringHelper.repeatChar(' ', level * 4)).Append("AwardTaken").Append("=").Append(getAwardTaken()).Append("").Append(System.lineSeparator());
		sb.append("}");
	}

	@Override
	public void Encode(ByteBuffer _os_) {
		_os_.WriteInt(4); // Variables.Count
		_os_.WriteInt(ByteBuffer.LONG | 1 << ByteBuffer.TAG_SHIFT);
		_os_.WriteLong(getRoleId());
		_os_.WriteInt(ByteBuffer.LONG | 2 << ByteBuffer.TAG_SHIFT);
		_os_.WriteLong(getValue());
		_os_.WriteInt(ByteBuffer.BYTES | 3 << ByteBuffer.TAG_SHIFT);
		_os_.WriteBinary(getValueEx());
		_os_.WriteInt(ByteBuffer.BOOL | 4 << ByteBuffer.TAG_SHIFT);
		_os_.WriteBool(getAwardTaken());
	}

	@Override
	public void Decode(ByteBuffer _os_) {
		for (int _varnum_ = _os_.ReadInt(); _varnum_ > 0; --_varnum_) { // Variables.Count
			int _tagid_ = _os_.ReadInt();
			switch (_tagid_) {
				case ByteBuffer.LONG | 1 << ByteBuffer.TAG_SHIFT:
					setRoleId(_os_.ReadLong());
					break;
				case ByteBuffer.LONG | 2 << ByteBuffer.TAG_SHIFT:
					setValue(_os_.ReadLong());
					break;
				case ByteBuffer.BYTES | 3 << ByteBuffer.TAG_SHIFT:
					setValueEx(_os_.ReadBinary());
					break;
				case ByteBuffer.BOOL | 4 << ByteBuffer.TAG_SHIFT:
					setAwardTaken(_os_.ReadBool());
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
		if (getRoleId() < 0) {
			return true;
		}
		if (getValue() < 0) {
			return true;
		}
		return false;
	}

}