package Game.Login;

import Zeze.Serialize.*;
import Game.*;

public final class BRoles extends Zeze.Transaction.Bean implements BRolesReadOnly {
	private Zeze.Transaction.Collections.PList2<Game.Login.BRole> _RoleList;
	private long _LastLoginRoleId;

	public Zeze.Transaction.Collections.PList2<Game.Login.BRole> getRoleList() {
		return _RoleList;
	}
	private System.Collections.Generic.IReadOnlyList<Game.Login.BRoleReadOnly> Game.Login.BRolesReadOnly.RoleList -> _RoleList;

	public long getLastLoginRoleId() {
		if (false == this.isManaged()) {
			return _LastLoginRoleId;
		}
		var txn = Zeze.Transaction.Transaction.Current;
		if (txn == null) {
			return _LastLoginRoleId;
		}
		txn.VerifyRecordAccessed(this, true);
		var log = (Log__LastLoginRoleId)txn.GetLog(this.getObjectId() + 2);
		return log != null ? log.getValue() : _LastLoginRoleId;
	}
	public void setLastLoginRoleId(long value) {
		if (false == this.isManaged()) {
			_LastLoginRoleId = value;
			return;
		}
		var txn = Zeze.Transaction.Transaction.Current;
		txn.VerifyRecordAccessed(this, false);
		txn.PutLog(new Log__LastLoginRoleId(this, value));
	}


	public BRoles() {
		this(0);
	}

	public BRoles(int _varId_) {
		super(_varId_);
		_RoleList = new Zeze.Transaction.Collections.PList2<Game.Login.BRole>(getObjectId() + 1, _v -> new Log__RoleList(this, _v));
	}

	public void Assign(BRoles other) {
		getRoleList().clear();
		for (var e : other.getRoleList()) {
			getRoleList().add(e.Copy());
		}
		setLastLoginRoleId(other.getLastLoginRoleId());
	}

	public BRoles CopyIfManaged() {
		return isManaged() ? Copy() :this;
	}

	public BRoles Copy() {
		var copy = new BRoles();
		copy.Assign(this);
		return copy;
	}

	public static void Swap(BRoles a, BRoles b) {
		BRoles save = a.Copy();
		a.Assign(b);
		b.Assign(save);
	}

	@Override
	public Zeze.Transaction.Bean CopyBean() {
		return Copy();
	}

	public static final long TYPEID = -7562249081436134307;
	@Override
	public long getTypeId() {
		return TYPEID;
	}

	private final static class Log__RoleList extends Zeze.Transaction.Collections.PList2<Game.Login.BRole>.LogV {
		public Log__RoleList(BRoles host, System.Collections.Immutable.ImmutableList<Game.Login.BRole> value) {
			super(host, value);
		}
		@Override
		public long getLogKey() {
			return Bean.ObjectId + 1;
		}
		public BRoles getBeanTyped() {
			return (BRoles)Bean;
		}
		@Override
		public void Commit() {
			Commit(getBeanTyped()._RoleList);
		}
	}

	private final static class Log__LastLoginRoleId extends Zeze.Transaction.Log<BRoles, Long> {
		public Log__LastLoginRoleId(BRoles self, long value) {
			super(self, value);
		}
		@Override
		public long getLogKey() {
			return this.Bean.ObjectId + 2;
		}
		@Override
		public void Commit() {
			this.getBeanTyped()._LastLoginRoleId = this.getValue();
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
		sb.append(tangible.StringHelper.repeatChar(' ', level * 4)).Append("Game.Login.BRoles: {").Append(System.lineSeparator());
		level++;
		sb.append(tangible.StringHelper.repeatChar(' ', level * 4)).Append("RoleList").Append("=[").Append(System.lineSeparator());
		level++;
		for (var Item : getRoleList()) {
			sb.append(tangible.StringHelper.repeatChar(' ', level * 4)).Append("Item").Append("=").Append(System.lineSeparator());
			Item.BuildString(sb, level + 1);
			sb.append(",").Append(System.lineSeparator());
		}
		level--;
		sb.append(tangible.StringHelper.repeatChar(' ', level * 4)).Append("],").Append(System.lineSeparator());
		sb.append(tangible.StringHelper.repeatChar(' ', level * 4)).Append("LastLoginRoleId").Append("=").Append(getLastLoginRoleId()).Append("").Append(System.lineSeparator());
		sb.append("}");
	}

	@Override
	public void Encode(ByteBuffer _os_) {
		_os_.WriteInt(2); // Variables.Count
		_os_.WriteInt(ByteBuffer.LIST | 1 << ByteBuffer.TAG_SHIFT); {
			int _state_;
			tangible.OutObject<Integer> tempOut__state_ = new tangible.OutObject<Integer>();
			_os_.BeginWriteSegment(tempOut__state_);
		_state_ = tempOut__state_.outArgValue;
			_os_.WriteInt(ByteBuffer.BEAN);
			_os_.WriteInt(getRoleList().size());
			for (var _v_ : getRoleList()) {
				_v_.Encode(_os_);
			}
			_os_.EndWriteSegment(_state_);
		}
		_os_.WriteInt(ByteBuffer.LONG | 2 << ByteBuffer.TAG_SHIFT);
		_os_.WriteLong(getLastLoginRoleId());
	}

	@Override
	public void Decode(ByteBuffer _os_) {
		for (int _varnum_ = _os_.ReadInt(); _varnum_ > 0; --_varnum_) { // Variables.Count
			int _tagid_ = _os_.ReadInt();
			switch (_tagid_) {
				case ByteBuffer.LIST | 1 << ByteBuffer.TAG_SHIFT: {
						int _state_;
						tangible.OutObject<Integer> tempOut__state_ = new tangible.OutObject<Integer>();
						_os_.BeginReadSegment(tempOut__state_);
					_state_ = tempOut__state_.outArgValue;
						_os_.ReadInt(); // skip collection.value typetag
						getRoleList().clear();
						for (int _size_ = _os_.ReadInt(); _size_ > 0; --_size_) {
							Game.Login.BRole _v_ = new Game.Login.BRole();
							_v_.Decode(_os_);
							getRoleList().add(_v_);
						}
						_os_.EndReadSegment(_state_);
				}
					break;
				case ByteBuffer.LONG | 2 << ByteBuffer.TAG_SHIFT:
					setLastLoginRoleId(_os_.ReadLong());
					break;
				default:
					ByteBuffer.SkipUnknownField(_tagid_, _os_);
					break;
			}
		}
	}

	@Override
	protected void InitChildrenRootInfo(Zeze.Transaction.Record.RootInfo root) {
		_RoleList.InitRootInfo(root, this);
	}

	@Override
	public boolean NegativeCheck() {
		for (var _v_ : getRoleList()) {
			if (_v_.NegativeCheck()) {
				return true;
			}
		}
		if (getLastLoginRoleId() < 0) {
			return true;
		}
		return false;
	}

}