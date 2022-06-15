package Zeze.Transaction;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;
import Zeze.Application;
import Zeze.Services.GlobalCacheManagerServer;
import Zeze.Util.Task;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// MESI？

/**
 ConcurrentLruLike
 普通Lru一般把最新访问的放在列表一端，这直接导致并发上不去。
 基本思路是按块（用ConcurrentDictionary）保存最近访问。
 定时添加新块。
 访问需要访问1 ~3次ConcurrentDictionary。

 通用类的写法需要在V外面包装一层。这里直接使用Record来达到这个目的。
 这样，这个类就不通用了。通用类需要包装，多创建一个对象，还需要包装接口。


 <typeparam name="K"></typeparam>
 <typeparam name="V"></typeparam>
*/
public class TableCache<K extends Comparable<K>, V extends Bean> {
	private static final Logger logger = LogManager.getLogger(TableCache.class);

	private final ConcurrentHashMap<K, Record1<K, V>> DataMap;
	public final ConcurrentHashMap<K, Record1<K, V>> getDataMap() {
		return DataMap;
	}

	private final ConcurrentLinkedQueue<ConcurrentHashMap<K, Record1<K, V>>> LruQueue = new ConcurrentLinkedQueue<> ();

	private volatile ConcurrentHashMap<K, Record1<K, V>> LruHot;

	private final TableX<K, V> Table;
	public final TableX<K, V> getTable() {
		return Table;
	}

	public TableCache(Application ignoredApp, TableX<K, V> table) {
		this.Table = table;
		DataMap = new ConcurrentHashMap<>(GetCacheInitialCapacity(), 0.75f, GetCacheConcurrencyLevel());
		NewLruHot();
		TimerNewHot = Task.schedule(table.getTableConf().getCacheNewLruHotPeriod(), table.getTableConf().getCacheNewLruHotPeriod(),
				() -> {
				// 访问很少的时候不创建新的热点。这个选项没什么意思。
				if (LruHot.size() > table.getTableConf().getCacheNewAccessHotThreshold()) {
					NewLruHot();
				}
		});
		TimerClean = Task.schedule(getTable().getTableConf().getCacheCleanPeriod(), this::CleanNow);
	}

	private int GetCacheConcurrencyLevel() {
		// 这样写，当配置修改，可以使用的时候马上生效。
		var processors = Runtime.getRuntime().availableProcessors();
		return Math.max(getTable().getTableConf().getCacheConcurrencyLevel(), processors);
	}

	private int GetCacheInitialCapacity() {
		// 31 from c# document
		// 这样写，当配置修改，可以使用的时候马上生效。
		return Math.max(getTable().getTableConf().getCacheInitialCapacity(), 31);
	}

	public long WalkKey(TableWalkKey<K> callback) {
		long cw = 0;
		for (var e : DataMap.entrySet()) {
			if (!callback.handle(e.getKey()))
				return cw;
			++cw;
		}
		return cw;
	}

	private int GetLruInitialCapacity() {
		int c = (int)(GetCacheInitialCapacity() * 0.2);
		return Math.min(c, getTable().getTableConf().getCacheMaxLruInitialCapacity());
	}

	private void NewLruHot() {
		var newLru = new ConcurrentHashMap<K, Record1<K, V>>(
				GetLruInitialCapacity(), 0.75f, GetCacheConcurrencyLevel());
		LruHot = newLru;
		LruQueue.add(newLru);
	}

	public final Record1<K, V> GetOrAdd(K key, Zeze.Util.Factory<Record1<K, V>> valueFactory) {
		var lruHot = LruHot;
		var result = DataMap.get(key);
		if (result == null) { // slow-path
			result = DataMap.computeIfAbsent(key, k -> {
				var r = valueFactory.create();
				lruHot.put(key, r); // replace: add or update see this.Remove
				r.setLruNode(lruHot);
				return r;
			});
		}

		// 旧纪录 && 优化热点执行调整
		// 下面在发生LruHot变动+并发GetOrAdd时，哪个后执行，就调整到哪个node，不严格调整到真正的LruHot。
		if (result.getLruNode() != lruHot) {
			var oldNode = result.getAndSetLruNodeNull();
			if (oldNode != null) {
				oldNode.remove(key);
				if (lruHot.putIfAbsent(key, result) == null)
					result.setLruNode(lruHot);
			}
		}
		return result;
	}

	/**
	 内部特殊使用，不调整 Lru。

	 @param key key
	 @return  Record1
	*/
	public final Record1<K, V> Get(K key) {
		return DataMap.get(key);
	}

	// 不再提供删除，由 Cleaner 集中清理。
	// under lockey.writeLock
	/*
	internal void Remove(K key)
	{
	    map.Remove(key, out var _);
	}
	*/

	private void TryPollLruQueue() {
		var cap = LruQueue.size() - 8640;
		if (cap <= 0)
			return;

		var polls = new ArrayList<ConcurrentHashMap<K, Record1<K, V>>>(cap);
		while (LruQueue.size() > 8640) {
			// 大概，删除超过一天的节点。
			var node =  LruQueue.poll();
			if (null == node)
				break;
			polls.add(node);
		}

		// 把被删除掉的node里面的记录迁移到当前最老(head)的node里面。
		var head =  LruQueue.peek();
		if (null == head)
			throw new RuntimeException("Impossible!");
		for (var poll : polls) {
			for (var e : poll.entrySet()) {
				// concurrent see GetOrAdd
				var r = e.getValue();
				if (r.compareAndSetLruNodeNull(poll) && head.putIfAbsent(e.getKey(), r) == null) // 并发访问导致这个记录已经被迁移走。
					r.setLruNode(head);
			}
		}
	}

	private void CleanNow() {
		// 这个任务的执行时间可能很长，
		// 不直接使用 Scheduler 的定时任务，
		// 每次执行完重新调度。

		if (getTable().getTableConf().getCacheCapacity() <= 0) {
			TimerClean = Task.schedule(getTable().getTableConf().getCacheCleanPeriod(), this::CleanNow);
			TryPollLruQueue();
			return; // 容量不限
		}

		try {
			while (DataMap.size() > getTable().getTableConf().getCacheCapacity()) { // 超出容量，循环尝试
				var node = LruQueue.peek();
				if (null == node || node == LruHot) { // 热点。不回收。
					break;
				}

				for (var e : node.entrySet()) {
					if (!TryRemoveRecord(e)) {
						// 出现回收不了，一般是批量修改数据，此时启动一次Checkpoint。
						getTable().getZeze().CheckpointRun();
					}
				}
				if (node.size() == 0) {
					LruQueue.poll();
				} else {
					logger.warn("remain record when clean oldest lruNode.");
				}

				try {
					//noinspection BusyWait
					Thread.sleep(getTable().getTableConf().getCacheCleanPeriodWhenExceedCapacity());
				} catch (InterruptedException skip) {
					// skip
				}
			}
			TryPollLruQueue();
		} finally {
			TimerClean = Task.schedule(getTable().getTableConf().getCacheCleanPeriod(), this::CleanNow);
		}
	}

	private Future<?> TimerClean;
	private Future<?> TimerNewHot;

	public void close() {
		if (null != TimerClean)
			TimerClean.cancel(true);
		TimerClean = null;
		if (null != TimerNewHot)
			TimerNewHot.cancel(true);
		TimerNewHot = null;
	}

	// under lockey.writeLock and record.fairLock
	private boolean Remove(Map.Entry<K, Record1<K, V>> p) {
		if (DataMap.remove(p.getKey(), p.getValue())) {
			// 这里有个时间窗口：先删除DataMap再去掉Lru引用，
			// 当对Key再次GetOrAdd时，LruNode里面可能已经存在旧的record。
			// see GetOrAdd
			p.getValue().setState(GlobalCacheManagerServer.StateRemoved);
			// 必须使用 Pair，有可能 LurNode 里面已经有新建的记录了。
			var oldNode = p.getValue().getLruNode();
			if (oldNode != null)
				oldNode.remove(p.getKey(), p.getValue());
			getTable().RocksCacheRemove(p.getKey());
			return true;
		}
		return true; // 没有删除成功，仍然返回true。
	}

	private boolean TryRemoveRecordUnderLock(Map.Entry<K, Record1<K, V>> p) {
		var storage = getTable().TStorage;
		if (null == storage) {
				/* 不支持内存表cache同步。
				if (p.Value.Acquire(GlobalCacheManager.StateInvalid) != GlobalCacheManager.StateInvalid)
				    return false;
				*/
			return Remove(p);
		}
		// 这个变量的修改操作在不同 CheckpointMode 下并发模式不同。
		// case CheckpointMode.Immediately
		// 永远不会为false。记录Commit的时候就Flush到数据库。
		// case CheckpointMode.Period
		// 修改的时候需要记录锁（lockey）。
		// 这里只是读取，就不加锁了。
		// case CheckpointMode.Table 修改的时候需要RelativeRecordSet锁。
		// （修改为true的时也在记录锁（lockey）下）。
		// 这里只是读取，就不加锁了。

		if (p.getValue().getDirty()) {
			return false;
		}

		if (p.getValue().isFreshAcquire())
			return false;

		if (p.getValue().getState() != GlobalCacheManagerServer.StateInvalid) {
			var r = p.getValue().Acquire(GlobalCacheManagerServer.StateInvalid, false);
			if (r.ResultCode != 0 || r.ResultState != GlobalCacheManagerServer.StateInvalid) {
				return false;
			}
		}
		return Remove(p);
	}

	private boolean TryRemoveRecord(Map.Entry<K, Record1<K, V>> p) {
		// lockey 第一优先，和事务并发。
		final TableKey tkey = new TableKey(this.getTable().getId(), p.getKey());
		final Locks locks = Table.getZeze().getLocks();
		if (locks == null) // 可能是已经执行Application.Stop导致的
			return TryRemoveRecordUnderLock(p); // 临时修正
		final Lockey lockey = locks.Get(tkey);
		if (!lockey.TryEnterWriteLock(0))
			return false;
		try {
			// record.lock 和事务并发。
			if (!p.getValue().TryEnterFairLockWhenIdle())
				return false;
			try {
				// rrs.lock
				var rrs = p.getValue().getRelativeRecordSet();
				if (!rrs.TryLockWhenIdle())
					return false;
				try {
					if (rrs.getMergeTo() != null)
						return false; // // 刚刚被合并或者删除（flushed）的记录认为是活跃的，不删除。

					if (rrs.getRecordSet() != null && rrs.getRecordSet().size() > 1)
						return false; // 只包含自己的时候才可以删除，多个记录关联起来时不删除。

					return TryRemoveRecordUnderLock(p);
				} finally {
					rrs.UnLock();
				}
			} finally {
				p.getValue().ExitFairLock();
			}
		}
		finally {
			lockey.ExitWriteLock();
		}
	}
}
