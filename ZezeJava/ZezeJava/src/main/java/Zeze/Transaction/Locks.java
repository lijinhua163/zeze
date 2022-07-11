package Zeze.Transaction;

import java.util.concurrent.locks.ReentrantLock;
import Zeze.Util.WeakHashSet;

/**
 * <p>
 * Locks原来使用 单个容器管理锁，效率太低：
 * <p>
 * 1. 每次查询都会试图去回收; 以前java版实现一个懒惰的WeakHashSet。c# ConditionalWeakTable 使用 this==another 吧，没有调用 Equals，不能使用。
 * 2. 并发访问效率低. 通过增加segment解决。
 */
public final class Locks {
	/**
	 * The maximum number of segments to allow; used to bound constructor arguments.
	 */
	private static final int MAX_SEGMENTS = 1 << 16; // slightly conservative
	private final int segmentShift;
	private final int segmentMask;
	private final Segment[] segments;

	/* ---------------- hash算法和映射规则都是来自 ConcurrentHashMap. -------------- */

	/**
	 * Returns the segment that should be used for key with given hash.
	 */
	private Segment segmentFor(Lockey lockey) {
		/*
		 * Applies a supplemental hash function to a given hashCode, which defends
		 * against poor quality hash functions. This is critical because
		 * ConcurrentHashMap uses power-of-two length hash tables, that otherwise
		 * encounter collisions for hashCodes that do not differ in lower or upper bits.
		 */
		// Spread bits to regularize both segment and index locations,
		// using variant of single-word Wang/Jenkins hash.
		int h = lockey.hashCode();
		h += (h << 15) ^ 0xffffcd7d;
		h ^= (h >>> 10);
		h += (h << 3);
		h ^= (h >>> 6);
		h += (h << 2) + (h << 14);
		int hash = h ^ (h >>> 16);

		int index = (hash >>> segmentShift) & segmentMask;
		return segments[index];
	}

	public Locks() {
		this(1024);
	}

	public Locks(int concurrencyLevel) {
		if (concurrencyLevel <= 0) {
			throw new IllegalArgumentException();
		}

		if (concurrencyLevel > MAX_SEGMENTS) {
			concurrencyLevel = MAX_SEGMENTS;
		}

		// Find power-of-two sizes best matching arguments
		int sShift = 0;
		int sSize = 1;
		while (sSize < concurrencyLevel) {
			++sShift;
			sSize <<= 1;
		}
		segmentShift = 32 - sShift;
		segmentMask = sSize - 1;
		segments = new Segment[sSize];
		for (int i = 0; i < segments.length; ++i) {
			segments[i] = new Segment();
		}
	}

	/* ------------- 实现 --------------- */
	private static final class Segment {
		private final WeakHashSet<Lockey> locks = new WeakHashSet<>();
		private final ReentrantLock lock = new ReentrantLock();

		public boolean Contains(Lockey key) {
			lock.lock();
			try {
				// 需要lock，get不是线程安全的
				return locks.get(key) != null;
			} finally {
				lock.unlock();
			}
		}

		public Lockey Get(Lockey key) {
			lock.lock();
			try {
				Lockey exist = locks.get(key);
				if (exist != null)
					return exist;
				locks.add(key);
				return key.Alloc();
			} finally {
				lock.unlock();
			}
		}
	}

	public boolean Contains(Lockey lockey) {
		return segmentFor(lockey).Contains(lockey);
	}

	public Lockey Get(Lockey lockey) {
		return segmentFor(lockey).Get(lockey);
	}

	public Lockey Get(TableKey tkey) {
		return Get(new Lockey(tkey));
	}

	public boolean Contains(TableKey tkey) {
		return Contains(new Lockey(tkey));
	}
}
