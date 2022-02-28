﻿using System;
using System.Collections.Generic;
using System.Collections.Immutable;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Zeze.Raft.RocksRaft
{
	public abstract class CollMap<K, V> : Collection
	{
		protected ImmutableDictionary<K, V> map;

		public abstract V Get(K key);
		public abstract void Put(K key, V value);
		public abstract void Remove(K key);
		public abstract void Apply(LogMap1<K, V> log);

		internal V _Get(K key)
		{
			if (map.TryGetValue(key, out var tmp))
				return tmp;
			return default(V);
		}
	}
}