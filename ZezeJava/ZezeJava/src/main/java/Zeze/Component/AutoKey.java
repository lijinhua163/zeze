package Zeze.Component;

import java.util.concurrent.ConcurrentHashMap;
import Zeze.Transaction.Transaction;

public class AutoKey {
	private final static Module module = new Module();
	public static Module getModule() {
		return module;
	}
	public static class Module extends  AbstractAutoKey {
		private Module() {

		}

		// 这个组件Zeze.Application会自动初始化，不需要应用初始化。
		public void initialize(Zeze.Application zeze) {
			RegisterZezeTables(zeze);
		}

		private final ConcurrentHashMap<String, AutoKey> map = new ConcurrentHashMap<>();

		private AutoKey getOrAdd(String name) {
			return map.computeIfAbsent(name, AutoKey::new);
		}
	}

	/**
	 * 这个返回值，可以在自己模块内保存下来，效率高一些。
	 * @param name
	 * @return
	 */
	public static AutoKey getAutoKey(String name) {
		return module.getOrAdd(name);
	}

	private static final int AllocateCount = 500;

	private final String name;
	private volatile Range range;
	private long logKey;

	private AutoKey(String name) {
		this.name = name;
		// 详细参考Bean的Log的用法。这里只有一个variable。
		logKey = Zeze.Transaction.Bean.getNextObjectId();
	}

	public long nextId() {
		if (null != range) {
			var next = range.tryNextId();
			if (null != next)
				return next; // allocate in range success
		}

		var txn = Transaction.getCurrent();
		var log = (RangeLog)txn.GetLog(logKey);
		while (true) {
			if (null == log) {
				// allocate: 多线程，多事务，多服务器（缓存同步）由zeze保证。
				var key = module._tAutoKeys.getOrAdd(name);
				var start = key.getNextId();
				var end = start + AllocateCount; // AllocateCount == 0 会死循环。
				key.setNextId(end);
				// create log，本事务可见，
				log = new RangeLog();
				log.range = new Range(start, end);
				txn.PutLog(log);
			}
			var tryNext = log.range.tryNextId();
			if (null != tryNext)
				return tryNext;

			// 事务内分配了超出Range范围的id，再次allocate。
			// 覆盖RangeLog是可以的。就像事务内多次改变变量。最后面的Log里面的数据是最新的。
			// 已分配的范围保存在_AutoKeys表内，事务内可以继续分配。
			log = null;
		}
	}

	private static class Range {
		private long nextId;
		private long max;

		public synchronized Long tryNextId() {
			if (nextId >= max) {
				return null;
			}
			return ++nextId;
		}

		public Range(long start, long end) {
			nextId = start;
			max = end;
		}
	}

	private class RangeLog extends Zeze.Transaction.Log {
		private Range range;

		public RangeLog() {
			super(null); // null: 特殊日志，不关联Bean。
		}

		@Override
		public void Commit() {
			// 这里直接修改拥有者的引用，开放出去，以后其他事务就能看到新的Range了。
			// 并发：多线程实际上由 _autokeys 表的锁来达到互斥，commit的时候，是互斥锁。
			AutoKey.this.range = range;
		}

		@Override
		public long getLogKey() {
			return AutoKey.this.logKey;
		}
	}
}