﻿using System;
using System.Collections.Generic;
using System.Runtime.CompilerServices;
using System.Threading;
using System.Threading.Tasks;

namespace Zeze.Util
{
	/// <summary>
	/// 对每个相同的key，最多只提交一个 Task.Run。
	/// 
	/// 说明：
	/// 严格的来说应该对每个key建立一个队列，但是key可能很多，就需要很多队列。
	/// 如果队列为空，需要回收队列，会产生很多垃圾回收对象。
	/// 具体的实现对于相同的key.hash使用相同的队列。
	/// 固定总的队列数，不回收队列。
	/// 构造的时候，可以通过参数控制总的队列数量。
	/// </summary>
	public sealed class TaskOneByOneByKey
    {
		private static readonly NLog.Logger logger = NLog.LogManager.GetCurrentClassLogger();

		private readonly TaskOneByOne[] concurrency;

		public TaskOneByOneByKey() : this(1024)
        {
        }

		public TaskOneByOneByKey(int concurrencyLevel)
        {
			if (concurrencyLevel < 0 || concurrencyLevel > 0x40000000)
				throw new Exception("Illegal concurrencyLevel: " + concurrencyLevel);

			int capacity = 1;
			while (capacity < concurrencyLevel)
				capacity <<= 1;
			this.concurrency = new TaskOneByOne[capacity];
			for (int i = 0; i < this.concurrency.Length; ++i)
				this.concurrency[i] = new TaskOneByOne();
		}

		public void Execute(object key, Action action, string actionName = null, Action cancel = null)
        {
			int h = Hash(key.GetHashCode());
			int index = h & (concurrency.Length - 1);

			concurrency[index].Execute(new JobAction()
			{
				Action = action,
				Name = actionName,
				Cancel = cancel
			});
        }

		public void Execute(object key, Func<long> func, string actionName = null, Action cancel = null)
		{
			int h = Hash(key.GetHashCode());
			int index = h & (concurrency.Length - 1);

			concurrency[index].Execute(new JobFunc()
			{
				Func = func,
				Name = actionName,
				Cancel = cancel
			});
		}

		public void Execute(object key, Func<Net.Protocol, Task<long>> pHandle, Net.Protocol p,
			Action<Net.Protocol, long> actionWhenError = null, Action cancel = null)
		{
			int h = Hash(key.GetHashCode());
			int index = h & (concurrency.Length - 1);

			concurrency[index].Execute(new JobProtocol(pHandle, p, actionWhenError, cancel));
		}

		public void Execute(object key, Zeze.Transaction.Procedure procedure,
			Net.Protocol from = null, Action<Net.Protocol, long> actionWhenError = null,
			Action cancel = null)
		{
			int h = Hash(key.GetHashCode());
			int index = h & (concurrency.Length - 1);

			concurrency[index].Execute(new JobProcedure(procedure, from, actionWhenError, cancel));
		}

		public void Shutdown(bool nowait, Action beforeWait, bool cancel)
        {
			foreach (var ts in concurrency)
            {
				ts.Shutdown(cancel);
            }
			beforeWait?.Invoke();

			if (nowait)
				return;

			foreach (var ts in concurrency)
            {
				ts.WaitComplete();
			}
        }

		/**
		 * Applies a supplemental hash function to a given hashCode, which defends
		 * against poor quality hash functions. This is critical because HashMap uses
		 * power-of-two length hash tables, that otherwise encounter collisions for
		 * hashCodes that do not differ in lower bits. Note: Null keys always map to
		 * hash 0, thus index 0.
		 * 
		 * @see java.util.HashMap
		 */
		[MethodImpl(MethodImplOptions.AggressiveInlining)]
		static int Hash(int _h)
		{
			uint h = (uint)_h;
			// This function ensures that hashCodes that differ only by
			// constant multiples at each bit position have a bounded
			// number of collisions (approximately 8 at default load factor).
			h ^= (h >> 20) ^ (h >> 12);
			return (int)(h ^ (h >> 7) ^ (h >> 4));
		}

		internal abstract class Job
		{
			public string Name { get; set; }
			public Action Cancel { get; set; }
			public abstract Task ProcessAsync();

			public async Task<long> DoIt(TaskOneByOne obo)
			{
				try
				{
					await ProcessAsync();
				}
				catch (Exception ex)
				{
					logger.Error(ex, "TaskOneByOne.DoIt");
				}
				finally
				{
					obo.RunNext();
				}
				return 0;
			}
		}

		internal class JobProtocol : Job
		{
			public Net.Protocol Protocol { get; set; }
			public Func<Net.Protocol, Task<long>> Handle { get; set; }
			public Action<Net.Protocol, long> ActionWhenError { get; set; }

			public JobProtocol(Func<Net.Protocol, Task<long>> pHandle, Net.Protocol p, Action<Net.Protocol, long> eHandle, Action cancel)
			{
				Handle = pHandle;
				Protocol = p;
				ActionWhenError = eHandle;
				Cancel = cancel;

				Name = Protocol.GetType().FullName;
			}


			public override async Task ProcessAsync()
			{
				await Mission.CallAsync(Handle, Protocol, ActionWhenError);
			}
        }

		internal class JobProcedure : Job
        {
			public Transaction.Procedure Procedure { get; set; }
			public Net.Protocol From { get; set; }
			public Action<Net.Protocol, long> ActionWhenError { get; set; }

			public JobProcedure(Transaction.Procedure proc, Net.Protocol proto, Action<Net.Protocol, long> eHandle, Action cancel)
            {
				Procedure = proc;
				From = proto;
				ActionWhenError = eHandle;
				Cancel = cancel;

				Name = Procedure.ActionName;
            }

			public override async Task ProcessAsync()
			{
				await Mission.CallAsync(Procedure, From, ActionWhenError);
			}
        }

		internal class JobFunc : Job
        {
			public Func<long> Func { get; set; }

#pragma warning disable CS1998 // Async method lacks 'await' operators and will run synchronously
            public override async Task ProcessAsync()
#pragma warning restore CS1998 // Async method lacks 'await' operators and will run synchronously
            {
				Func();
			}
        }

		internal class JobAction : Job
		{
			public Action Action { get; set; }

#pragma warning disable CS1998 // Async method lacks 'await' operators and will run synchronously
            public override async Task ProcessAsync()
#pragma warning restore CS1998 // Async method lacks 'await' operators and will run synchronously
            {
				Action();
			}
		}

		internal class TaskOneByOne
		{
			LinkedList<Job> Queue = new ();

			bool IsShutdown = false;

			internal void Shutdown(bool cancel)
            {
				LinkedList<Job> tmp = null;
				lock (this)
                {
					if (IsShutdown)
						return;

					IsShutdown = true;
					if (cancel)
                    {
						tmp = Queue;
						Queue = new (); // clear
						if (tmp.Count > 0)
							Queue.AddLast(tmp.First.Value); // put back running task back.
					}
				}
				if (tmp == null)
					return;

				bool first = true;
				foreach (var job in tmp)
                {
					if (first) // first is running task
					{
						first = false;
						continue;
                    }
                    try
                    {
						job.Cancel?.Invoke();
                    }
					catch (Exception ex)
                    {
						logger.Error(ex, $"CancelAction={job.Cancel}");
                    }
                }
            }

			internal void WaitComplete()
            {
				lock (this)
				{
					// wait running task.
					while (Queue.Count > 0)
					{
						Monitor.Wait(this);
					}
				}
			}

			public TaskOneByOne()
            {
            }

			internal void Execute(Job job)
			{
				lock (this)
				{
					if (IsShutdown)
					{
						job.Cancel?.Invoke();
						return;
					}
					Queue.AddLast(job);
					if (Queue.Count == 1)
					{
						_ = Queue.First.Value.DoIt(this);
					}
				}
			}

			internal void RunNext()
			{
				lock (this)
				{
					if (Queue.Count > 0)
					{
						Queue.RemoveFirst();
						if (IsShutdown && Queue.Count == 0)
                        {
							Monitor.PulseAll(this);
							return;
                        }
					}
					if (Queue.Count > 0)
					{
						_ = Queue.First.Value.DoIt(this);
					}
				}
			}
		}
	}
}
