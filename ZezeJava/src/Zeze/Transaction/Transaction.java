package Zeze.Transaction;

import Zeze.Transaction.Collections.*;
import Zeze.Serialize.*;
import Zeze.Services.*;
import Zeze.*;
import java.util.*;

public final class Transaction {
	private static final NLog.Logger logger = NLog.LogManager.GetCurrentClassLogger();

	private static System.Threading.ThreadLocal<Transaction> threadLocal = new System.Threading.ThreadLocal<Transaction>();

	public static Transaction getCurrent() {
		return threadLocal.Value;
	}

	// 嵌套存储过程栈。
	private ArrayList<Procedure> ProcedureStack = new ArrayList<Procedure> ();
	public ArrayList<Procedure> getProcedureStack() {
		return ProcedureStack;
	}

	public Procedure getTopProcedure() {
		return getProcedureStack().isEmpty() ? null : getProcedureStack().get(getProcedureStack().size() - 1);
	}

	public static Transaction Create() {
		if (null == threadLocal.Value) {
			threadLocal.Value = new Transaction();
		}
		return threadLocal.Value;
	}

	public static void Destroy() {
		threadLocal.Value = null;
	}

	public void Begin() {
		Savepoint sp = !savepoints.isEmpty() ? savepoints.get(savepoints.size() - 1).Duplicate() : new Savepoint();
		savepoints.add(sp);
	}

	public void Commit() {
		if (savepoints.size() > 1) {
			// 嵌套事务，把日志合并到上一层。
			int lastIndex = savepoints.size() - 1;
			Savepoint last = savepoints.get(lastIndex);
			savepoints.remove(lastIndex);
			savepoints.get(savepoints.size() - 1).Merge(last);
		}
		/*
		else
		{
		    // 最外层存储过程提交在 Perform 中处理
		}
		*/
	}

	public void Rollback() {
		int lastIndex = savepoints.size() - 1;
		Savepoint last = savepoints.get(lastIndex);
		savepoints.remove(lastIndex);
		last.Rollback();
	}

	public Log GetLog(long key) {
		// 允许没有 savepoint 时返回 null. 就是说允许在保存点不存在时进行读取操作。
		return !savepoints.isEmpty() ? savepoints.get(savepoints.size() - 1).GetLog(key) : null;
	}

	public void PutLog(Log log) {
		if (isCompleted()) {
			throw new RuntimeException("Transaction Is Completed.");
		}
		savepoints.get(savepoints.size() - 1).PutLog(log);
	}

	public ChangeNote GetOrAddChangeNote(long key, tangible.Func0Param<ChangeNote> factory) {
		// 必须存在 Savepoint. 可能是为了修改。
		return savepoints.get(savepoints.size() - 1).GetOrAddChangeNote(key, factory);
	}

	/*
	public void PutChangeNote(long key, ChangeNote note)
	{
	    savepoints[~1].PutChangeNote(key, note);
	}
	*/

	private final ArrayList<tangible.Action0Param> CommitActions = new ArrayList<tangible.Action0Param>();
	private final ArrayList<tangible.Action0Param> RollbackActions = new ArrayList<tangible.Action0Param>();

	public void RunWhileCommit(tangible.Action0Param action) {
		CommitActions.add(action);
	}

	public void RunWhileRollback(tangible.Action0Param action) {
		RollbackActions.add(action);
	}

	/** 
	 Procedure 第一层入口，总的处理流程，包括重做和所有错误处理。
	 
	 @param procedure
	*/
	public int Perform(Procedure procedure) {
		try {
			for (int tryCount = 0; tryCount < 256; ++tryCount) { // 最多尝试次数
				try {
					// 默认在锁内重复尝试，除非CheckResult.RedoAndReleaseLock，否则由于CheckResult.Redo保持锁会导致死锁。

					procedure.getZeze().getCheckpoint().EnterFlushReadLock();
					for (; tryCount < 256; ++tryCount) { // 最多尝试次数
						CheckResult checkResult = CheckResult.Redo; // 用来决定是否释放锁，除非 _lock_and_check_ 明确返回需要释放锁，否则都不释放。
						try {
							int result = procedure.Call();
							if ((result == Procedure.Success && savepoints.size() != 1) || (result != Procedure.Success && !savepoints.isEmpty())) {
								// 这个错误不应该重做
								logger.Fatal("Transaction.Perform:{0}. savepoints.Count != 1.", procedure);
								_final_rollback_(procedure);
								return Procedure.ErrorSavepoint;
							}
							checkResult = _lock_and_check_();
							if (checkResult == CheckResult.Success) {
								if (result == Procedure.Success) {
									_final_commit_(procedure);
//C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
//#if ENABLE_STATISTICS
									// 正常一次成功的不统计，用来观察redo多不多。
									// 失败在 Procedure.cs 中的统计。
									if (tryCount > 0) {
										ProcedureStatistics.getInstance().GetOrAdd("Zeze.Transaction.TryCount").GetOrAdd(tryCount).IncrementAndGet();
									}
//#endif
									return Procedure.Success;
								}
								_final_rollback_(procedure);
								return result;
							}
							// retry
						}
						catch (RedoAndReleaseLockException redorelease) {
							checkResult = CheckResult.RedoAndReleaseLock;
							logger.Debug(redorelease, "RedoAndReleaseLockException");
						}
						catch (AbortException abort) {
							logger.Debug(abort, "Transaction.Perform: Abort");
							_final_rollback_(procedure);
							return Procedure.AbortException;
						}
						catch (RuntimeException e) {
							// Procedure.Call 里面已经处理了异常。只有 unit test 或者内部错误会到达这里。
							// 在 unit test 下，异常日志会被记录两次。
							logger.Error(e, "Transaction.Perform:{0} exception. run count:{1}", procedure, tryCount);
							if (!savepoints.isEmpty()) {
								// 这个错误不应该重做
								logger.Fatal(e, "Transaction.Perform:{0}. exception. savepoints.Count != 0.", procedure);
								_final_rollback_(procedure);
								return Procedure.ErrorSavepoint;
							}
//C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
//#if DEBUG
							// 对于 unit test 的异常特殊处理，与unit test框架能搭配工作
							if (e.getClass().getSimpleName().equals("AssertFailedException")) {
								_final_rollback_(procedure);
								throw e;
							}
//#endif
							checkResult = _lock_and_check_();
							if (checkResult == CheckResult.Success) {
								_final_rollback_(procedure);
								return Procedure.Excption;
							}
							// retry
						}
						finally {
							if (checkResult == CheckResult.RedoAndReleaseLock) {
								for (var holdLock : holdLocks) {
									holdLock.ExitLock();
								}
								holdLocks.clear();
							}
							// retry 可能保持已有的锁，清除记录和保存点。
							getAccessedRecords().clear();
							savepoints.clear();
						}
						if (checkResult == CheckResult.RedoAndReleaseLock) {
							//logger.Debug("CheckResult.RedoAndReleaseLock break {0}", procedure);
							break;
						}
					}
				}
				finally {
					procedure.getZeze().getCheckpoint().ExitFlushReadLock();
				}
				//logger.Debug("Checkpoint.WaitRun {0}", procedure);
				procedure.getZeze().getCheckpoint().WaitRun();
			}
			logger.Error("Transaction.Perform:{0}. too many try.", procedure);
			_final_rollback_(procedure);
			return Procedure.TooManyTry;
		}
		finally {
			for (var holdLock : holdLocks) {
				holdLock.ExitLock();
			}
			holdLocks.clear();
		}
	}

	private void _notify_listener_(ChangeCollector cc) {
		try {
			Savepoint sp = savepoints.get(savepoints.size() - 1);
			for (Log log : sp.getLogs().values()) {
				if (log.Bean == null) {
					continue; // 特殊日志没有Bean。
				}

				// 写成回调是为了优化，仅在需要的时候才创建path。
//C# TO JAVA CONVERTER TODO TASK: The following lambda contained an unresolved 'out' keyword - these are not converted by C# to Java Converter:
				cc.CollectChanged(log.Bean.TableKey, (out ArrayList<Util.KV<Bean, Integer>> path, out ChangeNote note) -> {
							path = new ArrayList<Util.KV<Bean, Integer>>();
							note = null;
							path.Add(Util.KV.Create(log.Bean, log.VariableId));
							log.Bean.BuildChangeListenerPath(path);
				});
			}
			for (ChangeNote cn : sp.getChangeNotes().values()) {
				if (cn.getBean() == null) {
					continue;
				}

				// 写成回调是为了优化，仅在需要的时候才创建path。
//C# TO JAVA CONVERTER TODO TASK: The following lambda contained an unresolved 'out' keyword - these are not converted by C# to Java Converter:
				cc.CollectChanged(cn.getBean().TableKey, (out ArrayList<Util.KV<Bean, Integer>> path, out ChangeNote note) -> {
							path = new ArrayList<Util.KV<Bean, Integer>>();
							note = cn;
							path.Add(Util.KV.Create(cn.getBean().Parent, cn.getBean().VariableId));
							cn.getBean().Parent.BuildChangeListenerPath(path);
				});
			}

			savepoints.clear();
			//accessedRecords.Clear(); // 事务内访问过的记录保留，这样在Listener中可以读取。

			cc.Notify();
		}
		catch (RuntimeException ex) {
			logger.Error(ex, "ChangeListener Collect And Notify");
		}
	}

	private void _trigger_commit_actions_(Procedure procedure) {
		for (tangible.Action0Param action : CommitActions) {
			try {
				action.invoke();
			}
			catch (RuntimeException e) {
				logger.Error(e, "Commit Procedure {0} Action {1}", procedure, action.Method.Name);
			}
		}
		CommitActions.clear();
	}

	private void _final_commit_(Procedure procedure) {
		// 下面不允许失败了，因为最终提交失败，数据可能不一致，而且没法恢复。
		// 可以在最终提交里可以实现每事务checkpoint。
		ChangeCollector cc = new ChangeCollector();

		RelativeRecordSet.TryUpdateAndCheckpoint(this, procedure, () -> {
				try {
					savepoints.get(savepoints.size() - 1).Commit();
					for (var e : getAccessedRecords().entrySet()) {
						if (e.getValue().Dirty) {
							e.getValue().OriginRecord.Commit(e.getValue());
							cc.BuildCollect(e.getKey(), e.getValue()); // 首先对脏记录创建Table,Record相关Collector。
						}
					}
				}
				catch (RuntimeException e) {
					logger.Error(e, "Transaction._final_commit_ {0}", procedure);
					System.exit(54321);
				}
		});

		// 禁止在listener回调中访问表格的操作。除了回调参数中给定的记录可以访问。
		// 不再支持在回调中再次执行事务。
		setCompleted(true); // 在Notify之前设置的。
		_notify_listener_(cc);
		_trigger_commit_actions_(procedure);
	}

	private void _final_rollback_(Procedure procedure) {
		setCompleted(true);
		for (tangible.Action0Param action : RollbackActions) {
			try {
				action.invoke();
			}
			catch (RuntimeException e) {
				logger.Error(e, "Rollback Procedure {0} Action {1}", procedure, action.Method.Name);
			}
		}
		RollbackActions.clear();
	}

	private final ArrayList<Lockey> holdLocks = new ArrayList<Lockey>(); // 读写锁的话需要一个包装类，用来记录当前维持的是哪个锁。

	public static class RecordAccessed extends Bean {
		private Record OriginRecord;
		public final Record getOriginRecord() {
			return OriginRecord;
		}
		private long Timestamp;
		public final long getTimestamp() {
			return Timestamp;
		}
		private boolean Dirty;
		public final boolean getDirty() {
			return Dirty;
		}
		public final void setDirty(boolean value) {
			Dirty = value;
		}

		public final Bean NewestValue() {
			PutLog log = (PutLog)getCurrent().GetLog(getObjectId());
			if (null != log) {
				return log.getValue();
			}
			return getOriginRecord().getValue();
		}

		// Record 修改日志先提交到这里(Savepoint.Commit里面调用）。处理完Savepoint后再处理 Dirty 记录。
		private PutLog CommittedPutLog;
		public final PutLog getCommittedPutLog() {
			return CommittedPutLog;
		}
		private void setCommittedPutLog(PutLog value) {
			CommittedPutLog = value;
		}

		public static class PutLog extends Log<RecordAccessed, Bean> {
			public PutLog(RecordAccessed bean, Bean putValue) {
				super(bean, putValue);
			}

			@Override
			public long getLogKey() {
				return getBean().ObjectId;
			}

			@Override
			public void Commit() {
				RecordAccessed host = (RecordAccessed)getBean();
				host.setCommittedPutLog(this); // 肯定最多只有一个 PutLog。由 LogKey 保证。
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

	private TreeMap<TableKey, RecordAccessed> AccessedRecords = new TreeMap<TableKey, RecordAccessed> ();
	public TreeMap<TableKey, RecordAccessed> getAccessedRecords() {
		return AccessedRecords;
	}
	private final ArrayList<Savepoint> savepoints = new ArrayList<Savepoint>();

	private boolean IsCompleted = false;
	public boolean isCompleted() {
		return IsCompleted;
	}
	private void setCompleted(boolean value) {
		IsCompleted = value;
	}

	/** 
	 只能添加一次。
	 
	 @param key
	 @param r
	*/
	public void AddRecordAccessed(Record.RootInfo root, RecordAccessed r) {
		if (isCompleted()) {
			throw new RuntimeException("Transaction Is Completed");
		}

		r.InitRootInfo(root, null);
		getAccessedRecords().put(root.getTableKey(), r);
	}

	public RecordAccessed GetRecordAccessed(TableKey key) {
		// 允许读取事务内访问过的记录。
		//if (IsCompleted)
		//    throw new Exception("Transaction Is Completed");

		if (getAccessedRecords().containsKey(key) && (var record = getAccessedRecords().get(key)) == var record) {
			return record;
		}
		return null;
	}


	public void VerifyRecordAccessed(Bean bean) {
		VerifyRecordAccessed(bean, false);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public void VerifyRecordAccessed(Bean bean, bool IsRead = false)
	public void VerifyRecordAccessed(Bean bean, boolean IsRead) {
		//if (IsRead)// && App.Config.AllowReadWhenRecoredNotAccessed)
		//    return;
		if (bean.RootInfo.Record.State == GlobalCacheManager.StateRemoved) {
			throw new RuntimeException(String.format("VerifyRecordAccessed: Record Has Bean Removed From Cache. %1$s", bean.TableKey));
		}
		var ra = GetRecordAccessed(bean.TableKey);
		if (ra == null) {
			throw new RuntimeException(String.format("VerifyRecordAccessed: Record Not Control Under Current Transastion. %1$s", bean.TableKey));
		}
		if (bean.RootInfo.Record != ra.getOriginRecord()) {
			throw new RuntimeException(String.format("VerifyRecordAccessed: Record Reloaded.%1$s", bean.TableKey));
		}
	}

	private enum CheckResult {
		Success,
		Redo,
		RedoAndReleaseLock;

		public static final int SIZE = java.lang.Integer.SIZE;

		public int getValue() {
			return this.ordinal();
		}

		public static CheckResult forValue(int value) {
			return values()[value];
		}
	}
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [MethodImpl(MethodImplOptions.AggressiveInlining)] private CheckResult _check_(bool writeLock, RecordAccessed e)
	private CheckResult _check_(boolean writeLock, RecordAccessed e) {
		if (writeLock) {
			switch (e.getOriginRecord().getState()) {
				case GlobalCacheManager.StateRemoved:
					// fall down
				case GlobalCacheManager.StateInvalid:
					return CheckResult.RedoAndReleaseLock; // 写锁发现Invalid，肯定有Reduce请求。

				case GlobalCacheManager.StateModify:
					return e.getTimestamp() != e.getOriginRecord().getTimestamp() ? CheckResult.Redo : CheckResult.Success;

				case GlobalCacheManager.StateShare:
					// 这里可能死锁：另一个先获得提升的请求要求本机Recude，但是本机Checkpoint无法进行下去，被当前事务挡住了。
					// 通过 GlobalCacheManager 检查死锁，返回失败;需要重做并释放锁。
					if (e.getOriginRecord().Acquire(GlobalCacheManager.StateModify) != GlobalCacheManager.StateModify) {
						logger.Warn("Acquire Faild. Maybe DeadLock Found {0}", e.getOriginRecord());
						e.getOriginRecord().setState(GlobalCacheManager.StateInvalid);
						return CheckResult.RedoAndReleaseLock;
					}
					e.getOriginRecord().setState(GlobalCacheManager.StateModify);
					return e.getTimestamp() != e.getOriginRecord().getTimestamp() ? CheckResult.Redo : CheckResult.Success;
			}
			return e.getTimestamp() != e.getOriginRecord().getTimestamp() ? CheckResult.Redo : CheckResult.Success; // imposible
		}
		else {
			if (e.getOriginRecord().getState() == GlobalCacheManager.StateInvalid || e.getOriginRecord().getState() == GlobalCacheManager.StateRemoved) {
				return CheckResult.RedoAndReleaseLock; // 发现Invalid，肯定有Reduce请求或者被Cache清理，此时保险起见释放锁。
			}
			return e.getTimestamp() != e.getOriginRecord().getTimestamp() ? CheckResult.Redo : CheckResult.Success;
		}
	}

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [MethodImpl(MethodImplOptions.AggressiveInlining)] private CheckResult _lock_and_check_(KeyValuePair<TableKey, RecordAccessed> e)
	private CheckResult _lock_and_check_(Map.Entry<TableKey, RecordAccessed> e) {
		Lockey lockey = Locks.getInstance().Get(e.getKey());
		boolean writeLock = e.getValue().Dirty;
		lockey.EnterLock(writeLock);
		holdLocks.add(lockey);
		return _check_(writeLock, e.getValue());
	}

	private CheckResult _lock_and_check_() {
		if (!savepoints.isEmpty()) {
			// 全部 Rollback 时 Count 为 0；最后提交时 Count 必须为 1；
			// 其他情况属于Begin,Commit,Rollback不匹配。外面检查。
			for (var log : savepoints.get(savepoints.size() - 1).getLogs().values()) {
				// 特殊日志。不是 bean 的修改日志，当然也不会修改 Record。
				// 现在不会有这种情况，保留给未来扩展需要。
				if (log.Bean == null) {
					continue;
				}

				TableKey tkey = log.Bean.TableKey;
				if (getAccessedRecords().containsKey(tkey) && (var record = getAccessedRecords().get(tkey)) == var record) {
					record.Dirty = true;
				}
				else {
					// 只有测试代码会把非 Managed 的 Bean 的日志加进来。
					logger.Fatal("impossible! record not found.");
				}
			}
		}

		boolean conflict = false; // 冲突了，也继续加锁，为重做做准备！！！
		if (holdLocks.isEmpty()) {
			for (var e : getAccessedRecords().entrySet()) {
				switch (_lock_and_check_(e)) {
					case Success:
						break;
					case Redo:
						conflict = true;
						break; // continue lock
					case RedoAndReleaseLock:
						return CheckResult.RedoAndReleaseLock;
				}
			}
			return conflict ? CheckResult.Redo : CheckResult.Success;
		}

		int index = 0;
		int n = holdLocks.size();
		for (var e : getAccessedRecords().entrySet()) {
			// 如果 holdLocks 全部被对比完毕，直接锁定它
			if (index >= n) {
				switch (_lock_and_check_(e)) {
					case Success:
						break;
					case Redo:
						conflict = true;
						break; // continue lock
					case RedoAndReleaseLock:
						return CheckResult.RedoAndReleaseLock;
				}
				continue;
			}

			Lockey curLock = holdLocks.get(index);
			int c = curLock.getTableKey().compareTo(e.getKey());

			// holdlocks a  b  ...
			// needlocks a  b  ...
			if (c == 0) {
				// 这里可能发生读写锁提升
				if (e.getValue().Dirty && false == curLock.isWriteLockHeld()) {
					// 必须先全部释放，再升级当前记录锁，再锁后面的记录。
					// 直接 unlockRead，lockWrite会死锁。
					n = _unlock_start_(index, n);
					switch (_lock_and_check_(e)) {
						case Success:
							break;
						case Redo:
							conflict = true;
							break; // continue lock
						case RedoAndReleaseLock:
							return CheckResult.RedoAndReleaseLock;
					}
					// 从当前index之后都是新加锁，并且index和n都不会再发生变化。
					continue;
				}
				// else 已经持有读锁，不可能被修改也不可能降级(reduce)，所以不做检测了。                    
				// 已经锁定了，跳过当前锁，比较下一个。
				++index;
				continue;
			}
			// holdlocks a  b  ...
			// needlocks a  c  ...
			if (c < 0) {
				// 释放掉 比当前锁序小的锁，因为当前事务中不再需要这些锁
				int unlockEndIndex = index;
				for (; unlockEndIndex < n && holdLocks.get(unlockEndIndex).getTableKey().compareTo(e.getKey()) < 0; ++unlockEndIndex) {
					var toUnlockLocker = holdLocks.get(unlockEndIndex);
					toUnlockLocker.ExitLock();
				}
				holdLocks.subList(index, unlockEndIndex).clear();
				n = holdLocks.size();
				continue;
			}

			// holdlocks a  c  ...
			// needlocks a  b  ...
			// 为了不违背锁序，释放从当前锁开始的所有锁
			n = _unlock_start_(index, n);
		}
		return conflict ? CheckResult.Redo : CheckResult.Success;
	}

	private int _unlock_start_(int index, int nLast) {
		for (int i = index; i < nLast; ++i) {
			var toUnlockLocker = holdLocks.get(i);
			toUnlockLocker.ExitLock();
		}
		holdLocks.subList(index, nLast).clear();
		return holdLocks.size();
	}
}