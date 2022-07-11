package Zeze.Transaction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import Zeze.Application;
import Zeze.Config.DatabaseConf;
import Zeze.Serialize.ByteBuffer;
import Zeze.Util.KV;
import Zeze.Util.ShutdownHook;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 数据访问的效率主要来自TableCache的命中。根据以往的经验，命中率是很高的。
 * 所以数据库层就不要求很高的效率。马马虎虎就可以了。
 */
public abstract class Database {
	protected static final Logger logger = LogManager.getLogger(Database.class);
	private static final boolean isDebugEnabled = logger.isDebugEnabled();

	static {
		ShutdownHook.init();
	}

	private final ConcurrentHashMap<String, Zeze.Transaction.Table> tables = new ConcurrentHashMap<>();
	private final ArrayList<Storage<?, ?>> storages = new ArrayList<>();
	private final DatabaseConf Conf;
	private final String DatabaseUrl;
	private Operates DirectOperates;

	public Database(DatabaseConf conf) {
		Conf = conf;
		DatabaseUrl = conf.getDatabaseUrl();
	}

	public final Collection<Zeze.Transaction.Table> getTables() {
		return tables.values();
	}

	public final Zeze.Transaction.Table GetTable(String name) {
		return tables.get(name);
	}

	public final void AddTable(Zeze.Transaction.Table table) {
		if (null != tables.putIfAbsent(table.getName(), table))
			throw new IllegalStateException("duplicate table=" + table.getName());
	}

	public final void RemoveTable(Zeze.Transaction.Table table) {
		table.Close();
		tables.remove(table.getName());
	}

	public final DatabaseConf GetConf() {
		return Conf;
	}

	public final String getDatabaseUrl() {
		return DatabaseUrl;
	}

	public final Operates getDirectOperates() {
		return DirectOperates;
	}

	protected final void setDirectOperates(Operates value) {
		DirectOperates = value;
	}

	public final void Open(Application app) {
		for (Zeze.Transaction.Table table : tables.values()) {
			var storage = table.Open(app, this);
			if (null != storage) {
				storages.add(storage);
			}
		}
	}

	public void Close() {
		for (Zeze.Transaction.Table table : tables.values()) {
			table.Close();
		}
		tables.clear();
		storages.clear();
	}

	public final void EncodeN() {
		// try Encode. 可以多趟。
		for (int i = 1; i <= 1; ++i) {
			int countEncodeN = 0;
			for (var storage : storages) {
				countEncodeN += storage.EncodeN();
			}
			if (isDebugEnabled)
				logger.debug("Checkpoint EncodeN {}@{}", i, countEncodeN);
		}
	}

	public final void Snapshot() {
		int countEncode0 = 0;
		int countSnapshot = 0;
		for (var storage : storages) {
			countEncode0 += storage.Encode0();
		}
		for (var storage : storages) {
			countSnapshot += storage.Snapshot();
		}

		logger.info("Checkpoint Encode0 And Snapshot countEncode0={} countSnapshot={}", countEncode0, countSnapshot);
	}

	public final void Flush(Transaction trans, Database.Transaction lct) {
		int countFlush = 0;
		for (var storage : storages) {
			countFlush += storage.Flush(trans, lct);
		}
		logger.info("Checkpoint Flush count={}", countFlush);
	}

	public final void Cleanup() {
		for (var storage : storages) {
			storage.Cleanup();
		}
	}

	public abstract Table OpenTable(String name);

	public interface Table {
		boolean isNew();

		Database getDatabase();

		ByteBuffer Find(ByteBuffer key);

		void Replace(Transaction t, ByteBuffer key, ByteBuffer value);

		void Remove(Transaction t, ByteBuffer key);

		/**
		 * 每一条记录回调。回调返回true继续遍历，false中断遍历。
		 *
		 * @return 返回已经遍历的数量
		 */
		long Walk(TableWalkHandleRaw callback);

		long WalkKey(TableWalkKeyRaw callback);

		void Close();
	}

	public abstract Transaction BeginTransaction();

	public interface Transaction extends AutoCloseable {
		void Commit();

		void Rollback();
	}

	public static class DataWithVersion {
		public ByteBuffer Data;
		public long Version;
	}

	/**
	 * 由后台数据库直接支持的存储过程。
	 * 直接操作后台数据库，不经过cache。
	 */
	public interface Operates {
		/*
		  table zeze_global {string global} 一条记录
		  table zeze_instances {int localId} 每个启动的gs一条记录
		  SetInUse(localId, global) // 没有启用cache-sync时，global是""
		    if (false == zeze_instances.insert(localId)) {
		      rollback;
		      return false; // 同一个localId只能启动一个。
		    }
		    globalNow = zeze_global.getOrAdd(global); // sql 应该是没有这样的方法的
		    if (globalNow != global) {
		      // 不管是否启用cache-sync，global都必须一致
		      rollback;
		      return false;
		    }
		    if (zeze_instances.count == 1)
		      return true; // 只有一个实例，肯定成功。
		    if (global.Count == 0) {
		      // 没有启用global，但是实例超过1。
		      rollback;
		      return false;
		    }
		    commit;
		    return true;
		*/
		void SetInUse(int localId, String global);

		int ClearInUse(int localId, String global);

		/*
		  if (Exist(key)) {
		    if (CurrentVersion != version)
		      return false;
		    UpdateData(data);
		    return (++CurrentVersion, true);
		  }
		  InsertData(data);
		  return (CurrentVersion = version, true);
		*/
		KV<Long, Boolean> SaveDataWithSameVersion(ByteBuffer key, ByteBuffer data, long version);

		DataWithVersion GetDataWithVersion(ByteBuffer key);
	}
}
