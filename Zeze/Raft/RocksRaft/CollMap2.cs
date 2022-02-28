﻿using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Zeze.Serialize;

namespace Zeze.Raft.RocksRaft
{
	public class CollMap2<K, V> : CollMap<K, V>
		where K : Serializable, new()
		where V : Bean, new()
	{
		public override V Get(K key)
		{
			if (false == Transaction.Current.LogTryGet(Parent.ObjectId + VariableId, out var log))
				return _Get(key);
			var maplog = (LogMap2<K, V>)log;
			return maplog.Get(key, this);
		}

		public override void Put(K key, V value)
		{
			var maplog = (LogMap2<K, V>)Transaction.Current.LogGetOrAdd(Parent.ObjectId + VariableId, CreateLogBean);
			maplog.Put(key, value);
		}

		public override void Remove(K key)
		{
			var maplog = (LogMap2<K, V>)Transaction.Current.LogGetOrAdd(Parent.ObjectId + VariableId, CreateLogBean);
			maplog.Remove(key);
		}

		public override void Apply(LogMap _log)
		{
			var log = (LogMap2<K, V>)_log;
			var tmp = map;
			tmp = tmp.RemoveRange(log.Removed);
			tmp = tmp.AddRange(log.Putted);
			map = tmp;
		}

		public override LogBean CreateLogBean()
		{
			return new LogMap2<K, V>() { VariableId = VariableId };
		}

		public override void Decode(ByteBuffer bb)
		{
			for (int i = bb.ReadInt(); i >= 0; --i)
			{
				var key = SerializeHelper<K>.Decode(bb);
				var value = SerializeHelper<V>.Decode(bb);
				Put(key, value);
			}
		}

		public override void Encode(ByteBuffer bb)
		{
			var tmp = map;
			bb.WriteInt(tmp.Count);
			foreach (var e in tmp)
			{
				SerializeHelper<K>.Encode(bb, e.Key);
				SerializeHelper<V>.Encode(bb, e.Value);
			}
		}

		protected override void InitChildrenRootInfo(Record.RootInfo tableKey)
		{
			foreach (var v in map.Values)
			{
				v.InitRootInfo(RootInfo, Parent);
			}
		}
	}
}