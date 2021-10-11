package Zeze.Transaction;

import Zeze.Serialize.*;
import java.util.concurrent.ConcurrentHashMap;

public final class Storage1<K, V extends Bean> extends Storage {
	private Table Table;
	public Table getTable() {
		return Table;
	}

	public Storage1(Table1<K, V> table, Database database, String tableName) {
		Table = table;
		setDatabaseTable(database.OpenTable(tableName));
	}

	private ConcurrentHashMap<K, Record1<K, V>> changed = new ConcurrentHashMap<>();
	private ConcurrentHashMap<K, Record1<K, V>> encoded = new ConcurrentHashMap<>();
	private ConcurrentHashMap<K, Record1<K, V>> snapshot = new ConcurrentHashMap<>();

	public void OnRecordChanged(Record1<K, V> r) {
		changed.put(r.getKey(), r);
	}

	/*
	 * Not Need Now. See Record.Dirty
	internal bool IsRecordChanged(K key)
	{
	    if (changed.TryGetValue(key, out var _))
	        return true;
	    if (encoded.TryGetValue(key, out var _))
	        return true;
	    return false;
	}
	*/

	/** 
	 仅在 Checkpoint 中调用，同时只有一个线程执行。
	 没有得到任何锁。
	 
	 @return 
	*/
	@Override
	public int EncodeN() {
		int c = 0;
		for (var e : changed.entrySet()) {
			if (e.getValue().TryEncodeN(changed, encoded)) {
				++c;
			}
		}
		return c;
	}

	/** 
	 仅在 Checkpoint 中调用，在 flushWriteLock 下执行。
	 
	 @return 
	*/
	@Override
	public int Encode0() {
		for (var e : changed.entrySet()) {
			e.getValue().Encode0();
			encoded.put(e.getKey(), e.getValue());
		}
		int cc = changed.size();
		changed.clear();
		return cc;
	}

	/** 
	 仅在 Checkpoint 中调用，在 flushWriteLock 下执行。
	 
	 @return 
	*/
	@Override
	public int Snapshot() {
		var tmp = snapshot;
		snapshot = encoded;
		encoded = tmp;
		int cc = snapshot.size();
		for (var e : snapshot.entrySet()) {
			e.getValue().setSavedTimestampForCheckpointPeriod(e.getValue().getTimestamp());
		}
		return cc;
	}

	/** 
	 仅在 Checkpoint 中调用。
	 没有拥有任何锁。
	 
	 @return 
	*/
	@Override
	public int Flush(Database.Transaction t) {
		int count = 0;
		for (var e : snapshot.entrySet()) {
			if (e.getValue().Flush(t)) {
				++count;
			}
		}
		return count;
	}

	/** 
	 仅在 Checkpoint 中调用。
	 没有拥有任何锁。
	*/
	@Override
	public void Cleanup() {
		for (var e : snapshot.entrySet()) {
			e.getValue().Cleanup();
		}
		snapshot.clear();
	}

	public V Find(K key, Table1<K, V> table) {
		ByteBuffer value = getDatabaseTable().Find(table.EncodeKey(key));
		return null != value ? table.DecodeValue(value) : null;
	}

	@Override
	public void Close() {
		getDatabaseTable().Close();
	}
}