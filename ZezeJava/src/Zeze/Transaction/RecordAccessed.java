package Zeze.Transaction;

import Zeze.Serialize.ByteBuffer;

public class RecordAccessed extends Bean {
	Record OriginRecord;
	long Timestamp;
	boolean Dirty;

	public final Bean NewestValue() {
		var log = Transaction.getCurrent().GetLog(getObjectId());
		if (log instanceof PutLog) {
			PutLog putlog = (PutLog)log;
			return putlog.getValue();
		}
		return OriginRecord.getValue();
	}

	// Record 修改日志先提交到这里(Savepoint.Commit里面调用）。处理完Savepoint后再处理 Dirty 记录。
	PutLog CommittedPutLog;

	public static class PutLog extends Log1<RecordAccessed, Bean> {
		public PutLog(RecordAccessed bean, Bean putValue) {
			super(bean, putValue);
		}

		@Override
		public long getLogKey() {
			return getBean().getObjectId();
		}

		@Override
		public void Commit() {
			RecordAccessed host = (RecordAccessed)getBean();
			host.CommittedPutLog = this; // 肯定最多只有一个 PutLog。由 LogKey 保证。
		}
	}

	public RecordAccessed(Record originRecord) {
		OriginRecord = originRecord;
		Timestamp = originRecord.getTimestamp();
	}

	public final void Put(Transaction current, Bean putValue) {
		current.PutLog(new PutLog(this, putValue));
	}

	public final void Remove(Transaction current) {
		Put(current, null);
	}

	@Override
	protected void InitChildrenRootInfo(Record.RootInfo root) {
	}

	@Override
	public void Decode(ByteBuffer bb) {
	}

	@Override
	public void Encode(ByteBuffer bb) {
	}
}