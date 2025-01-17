﻿using System;
using System.Collections;
using System.Collections.Generic;
using System.Collections.Immutable;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Zeze.Serialize;

namespace Zeze.Raft.RocksRaft
{
	public abstract class CollMap<K, V> : Collection, IEnumerable<KeyValuePair<K, V>>, IEnumerable
	{
		internal ImmutableDictionary<K, V> _map = ImmutableDictionary<K, V>.Empty;

		public V Get(K key)
        {
			if (Map.TryGetValue(key, out V v))
				return v;
			return default;
		}

		public abstract void Add(K key, V value);
		public abstract void Put(K key, V value);
		public abstract void Remove(K key);
		public abstract void Clear();

		protected ImmutableDictionary<K, V> Map
        {
			get
            {
				if (IsManaged)
				{
					if (Transaction.Current == null) return _map;
					if (false == Transaction.Current.TryGetLog(Parent.ObjectId + VariableId, out var log))
						return _map;
					var maplog = (LogMap<K, V>)log;
					return maplog.Value;
				}
				else
				{
					return _map;
				}
			}
		}

		public int Count => Map.Count;

		IEnumerator IEnumerable.GetEnumerator()
		{
			return Map.GetEnumerator();
		}

		IEnumerator<KeyValuePair<K, V>> IEnumerable<KeyValuePair<K, V>>.GetEnumerator()
		{
			return Map.GetEnumerator();
		}

		public ImmutableDictionary<K, V>.Enumerator GetEnumerator()
		{
			return Map.GetEnumerator();
		}

		public override string ToString()
        {
			var sb = new StringBuilder();
			ByteBuffer.BuildString(sb, Map);
            return sb.ToString();
        }

		public override void Decode(ByteBuffer bb)
		{
			Clear();
			for (int i = bb.ReadUInt(); i > 0; --i)
			{
				var key = SerializeHelper<K>.Decode(bb);
				var value = SerializeHelper<V>.Decode(bb);
				Put(key, value);
			}
		}

		public override void Encode(ByteBuffer bb)
		{
			var tmp = Map;
			bb.WriteUInt(tmp.Count);
			foreach (var e in tmp)
			{
				SerializeHelper<K>.Encode(bb, e.Key);
				SerializeHelper<V>.Encode(bb, e.Value);
			}
		}
	}
}
