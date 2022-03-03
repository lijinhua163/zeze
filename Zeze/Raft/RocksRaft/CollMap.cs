﻿using System;
using System.Collections.Generic;
using System.Collections.Immutable;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Zeze.Serialize;

namespace Zeze.Raft.RocksRaft
{
	public abstract class CollMap<K, V> : Collection
	{
		internal ImmutableDictionary<K, V> map = ImmutableDictionary<K, V>.Empty;

		public V Get(K key)
        {
			if (Map.TryGetValue(key, out V v))
				return v;
			return default(V);
		}

		public abstract void Put(K key, V value);
		public abstract void Remove(K key);
		public abstract void Clear();

		protected ImmutableDictionary<K, V> Map
        {
			get
            {
				if (IsManaged)
				{
					if (false == Transaction.Current.TryGetLog(Parent.ObjectId + VariableId, out var log))
						return map;
					var maplog = (LogMap1<K, V>)log;
					return maplog.Value;
				}
				else
				{
					return map;
				}
			}
		}

        public override string ToString()
        {
			var sb = new StringBuilder();
			ByteBuffer.BuildString(sb, Map);
            return sb.ToString();
        }

		public override void Decode(ByteBuffer bb)
		{
			// decode to origin map. 所以! 不能在事务中使用Decode。
			map = ImmutableDictionary<K, V>.Empty;
			for (int i = bb.ReadInt(); i > 0; --i)
			{
				var key = SerializeHelper<K>.Decode(bb);
				var value = SerializeHelper<V>.Decode(bb);
				map = map.Add(key, value);
			}
		}

		public override void Encode(ByteBuffer bb)
		{
			// encode from newest Map. 是否在事务中都可以。
			var tmp = Map;
			bb.WriteInt(tmp.Count);
			foreach (var e in tmp)
			{
				SerializeHelper<K>.Encode(bb, e.Key);
				SerializeHelper<V>.Encode(bb, e.Value);
			}
		}
	}
}
