﻿using System;
using System.Collections.Generic;
using System.Diagnostics.CodeAnalysis;
using System.Text;
using System.Runtime.CompilerServices;
using Nito.AsyncEx;
using System.Threading;
using System.Threading.Tasks;

namespace Zeze.Transaction
{
	/// <summary>
	/// 线程不安全，用来保存真正的锁和分配状态。目前用于局部变量和Transaction.HoldLocks，
	/// </summary>
	public sealed class LockAsync : IDisposable
	{
		public Lockey Lockey { get; }
		private IDisposable Acquired;

		// 暂时不需要这个状态，考虑删除。相关判断改成使用 null != Acquired。
		public int AcquiredType { get; private set; } // 0 none, 1 read, 2 write

		public LockAsync(Lockey lockey)
        {
			Lockey = lockey;
        }

		public async Task<LockAsync> ReaderLockAsync()
		{
			if (AcquiredType != 0)
				throw new InvalidOperationException();

#if ENABLE_STATISTICS
			TableStatistics.Instance.GetOrAdd(Lockey.TableKey.Id).ReadLockTimes.IncrementAndGet();
#endif
			Acquired = await Lockey.RWlock.ReaderLockAsync();
			AcquiredType = 1;
			return this;
		}

		public async Task<LockAsync> WriterLockAsync()
		{
			if (AcquiredType != 0)
				throw new InvalidOperationException();
#if ENABLE_STATISTICS
			TableStatistics.Instance.GetOrAdd(Lockey.TableKey.Id).WriteLockTimes.IncrementAndGet();
#endif
			Acquired = await Lockey.RWlock.WriterLockAsync();
			AcquiredType = 2;
			return this;
		}

		public bool TryEnterReadLock()
		{
			if (AcquiredType != 0)
				throw new InvalidOperationException();

#if ENABLE_STATISTICS
			TableStatistics.Instance.GetOrAdd(Lockey.TableKey.Id).TryReadLockTimes.IncrementAndGet();
#endif
			var source = new CancellationTokenSource();
			var context = Lockey.RWlock.ReaderLockAsync(source.Token);
			if (context.AsTask().Wait(0))
            {
				Acquired = context.AsTask().Result;
				AcquiredType = 1;
				return true;
			}

			source.Cancel();
			try
			{
				// Cancel 之后需要等待结果。此时还可能得到锁。
				context.AsTask().Wait();
				Acquired = context.AsTask().Result;
				AcquiredType = 1;
				return true;
			}
			catch (Exception)
			{
			}
			return false;
		}

		public bool TryEnterWriteLock()
		{
			if (AcquiredType != 0)
				throw new InvalidOperationException();

#if ENABLE_STATISTICS
			TableStatistics.Instance.GetOrAdd(Lockey.TableKey.Id).TryWriteLockTimes.IncrementAndGet();
#endif
			var source = new CancellationTokenSource();
			var context = Lockey.RWlock.WriterLockAsync(source.Token);
			if (context.AsTask().Wait(0))
			{
				Acquired = context.AsTask().Result;
				AcquiredType = 2;
				return true;
			}

			source.Cancel();
			try
			{
				// Cancel 之后需要等待结果。此时还可能得到锁。
				context.AsTask().Wait();
				Acquired = context.AsTask().Result;
				AcquiredType = 2;
				return true;
			}
			catch (Exception)
			{
			}
			return false;
		}

		public void EnterReadLock()
        {
			if (AcquiredType != 0)
				throw new InvalidOperationException();

			Acquired = Lockey.RWlock.ReaderLock();
			AcquiredType = 1;
        }

		/*
		public void EnterWriteLock()
        {
			if (HoldType != 0)
				throw new InvalidOperationException();

			Holder = Lockey.RWlock.WriterLock();
			HoldType = 2;
        }
		*/

		/// <summary>
		/// 根据参数进入读或写锁。
		/// 进入写锁时如果已经获得读锁，会先释放，使用时注意竞争条件。
		/// EnterUpgradeableReadLock 看起来不好用，慢慢研究。
		/// </summary>
		/// <param name="isWrite"></param>
		internal async Task<LockAsync> EnterLockAsync(bool isWrite)
		{
			if (isWrite)
			{
				return await WriterLockAsync();
			}
			return await ReaderLockAsync();
		}

		public void Release()
		{
			Acquired?.Dispose();
			AcquiredType = 0;
		}

		public void Dispose()
		{
			Release();
		}

		public override string ToString()
		{
			return $"{Lockey} HoldType={AcquiredType}";
		}
	}

	public sealed class Lockey : System.IComparable<Lockey>
    {
		public TableKey TableKey { get; }
		internal AsyncReaderWriterLock RWlock;

		/// <summary>
		/// 相同值的 TableKey 要得到同一个 Lock 引用，必须使用 Locks 查询。
		/// 不要自己构造这个对象。开放出去仅仅为了测试。
		/// </summary>
		/// <param name="key"></param>
		public Lockey(TableKey key)
		{
			TableKey = key;
		}

		/// <summary>
		/// 创建真正的锁对象。
		/// </summary>
		/// <returns></returns>
		internal Lockey Alloc()
		{
			RWlock = new Nito.AsyncEx.AsyncReaderWriterLock();
			return this;
		}

		public int CompareTo(Lockey other)
        {
			if (other == null)
				return 1; // null always small

			return TableKey.CompareTo(other.TableKey);
        }

        public override int GetHashCode()
        {
            return TableKey.GetHashCode();
        }

        public override bool Equals(object obj)
        {
			if (this == obj)
				return true;

			if (obj is Lockey another)
	            return TableKey.Equals(another.TableKey);

			return false;
        }
    }

	/**
	 * <p>
	 * Locks原来使用 单个容器管理锁，效率太低：
	 * <p>
	 * 1. 每次查询都会试图去回收; 以前java版实现一个懒惰的WeakHashSet。c# ConditionalWeakTable 使用 this==another 吧，没有调用 Equals，不能使用。
	 * 2. 并发访问效率低. 通过增加segment解决。
	 */
	public sealed class Locks
	{
		/**
		 * The maximum number of segments to allow; used to bound constructor arguments.
		 */
		private const int MAX_SEGMENTS = 1 << 16; // slightly conservative
		private readonly int segmentShift;
		private readonly uint segmentMask;
		private readonly Segment[] segments;

		/* ---------------- hash算法和映射规则都是来自 ConcurrentHashMap. -------------- */
		/**
		 * Returns the segment that should be used for key with given hash.
		 * 
		 * @param lockey the Lockey
		 * @return the segment
		 */
		private Segment SegmentFor(Lockey lockey)
		{
			/**
			 * Applies a supplemental hash function to a given hashCode, which defends
			 * against poor quality hash functions. This is critical because
			 * ConcurrentHashMap uses power-of-two length hash tables, that otherwise
			 * encounter collisions for hashCodes that do not differ in lower or upper bits.
			 */
			// Spread bits to regularize both segment and index locations,
			// using variant of single-word Wang/Jenkins hash.
			uint h = (uint)lockey.GetHashCode();
			h += (h << 15) ^ 0xffffcd7d;
			h ^= (h >> 10);
			h += (h << 3);
			h ^= (h >> 6);
			h += (h << 2) + (h << 14);
			uint hash = h ^ (h >> 16);

			uint index = (hash >> segmentShift) & segmentMask;
			return segments[index];
		}

		public Locks() : this(1024)
		{
		}

		public Locks(int concurrencyLevel)
		{
			if (concurrencyLevel <= 0)
				throw new ArgumentException("concurrencyLevel <= 0");

			if (concurrencyLevel > MAX_SEGMENTS)
				concurrencyLevel = MAX_SEGMENTS;

			// Find power-of-two sizes best matching arguments
			int sshift = 0;
			int ssize = 1;
			while (ssize < concurrencyLevel)
			{
				++sshift;
				ssize <<= 1;
			}
			this.segmentShift = 32 - sshift;
			this.segmentMask = (uint)(ssize - 1);
			this.segments = new Segment[ssize];
			for (int i = 0; i < this.segments.Length; ++i)
				this.segments[i] = new Segment();
		}

		/* ------------- 实现 --------------- */
		sealed class Segment
		{
			private readonly Util.WeakHashSet<Lockey> locks = new();

			public Segment()
			{
			}

			public bool Contains(Lockey key)
			{
				// 需要sync，get不是线程安全的
				lock (this)
                {
					return locks.get(key) != null;
				}
			}

			public Lockey Get(Lockey key)
			{
				lock (this)
				{
					Lockey exist = locks.get(key);
					if (null != exist)
						return exist;

					locks.add(key);
					return key.Alloc();
				}
			}
		}

		public Lockey Get(Lockey lockey)
		{
			return this.SegmentFor(lockey).Get(lockey);
		}

		public LockAsync Get(TableKey tkey)
        {
			return new LockAsync(Get(new Lockey(tkey)));
        }
	}

}
