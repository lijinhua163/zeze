package UnitTest.Zeze.Trans;

import Zeze.Config;
import Zeze.Config.DatabaseConf;
import Zeze.Config.DbType;
import Zeze.Serialize.ByteBuffer;
import Zeze.Transaction.Database;
import Zeze.Transaction.DatabaseMySql;
import Zeze.Transaction.DatabaseRocksDb;
import junit.framework.TestCase;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class TestDatabaseRocksDB extends TestCase{



	public final void test1() throws UnknownHostException {
		DatabaseRocksDb db = getDatabaseRocksDb();
		try {
			Database.Table table = db.OpenTable("test_1");
			{
				var trans = db.BeginTransaction();
				{
					ByteBuffer key = ByteBuffer.Allocate();
					key.WriteInt(1);
					table.Remove(trans, key);
				}
				{
					ByteBuffer key = ByteBuffer.Allocate();
					key.WriteInt(2);
					table.Remove(trans, key);
				}
				trans.Commit();
			}
			assert 0 == table.Walk(this::PrintRecord);
			{
				var trans = db.BeginTransaction();
				{
					ByteBuffer key = ByteBuffer.Allocate();
					key.WriteInt(1);
					ByteBuffer value = ByteBuffer.Allocate();
					value.WriteInt(1);
					table.Replace(trans, key, value);
				}
				{
					ByteBuffer key = ByteBuffer.Allocate();
					key.WriteInt(2);
					ByteBuffer value = ByteBuffer.Allocate();
					value.WriteInt(2);
					table.Replace(trans, key, value);
				}
				trans.Commit();
			}
			{
				ByteBuffer key = ByteBuffer.Allocate();
				key.WriteInt(1);
				ByteBuffer value = table.Find(key);
				assert value != null;
				assert 1 == value.ReadInt();
				assert value.ReadIndex == value.WriteIndex;
			}
			{
				ByteBuffer key = ByteBuffer.Allocate();
				key.WriteInt(2);
				ByteBuffer value = table.Find(key);
				assert value != null;
				assert 2 == value.ReadInt();
				assert value.ReadIndex == value.WriteIndex;
			}
			assert 2 == table.Walk(this::PrintRecord);
		} finally {
			db.Close();
		}
	}

	/**
	 * 执行test1插入数据后 ,再次启动db查看数据是否依然存在
	 */
	public final void test2(){
		DatabaseRocksDb db = getDatabaseRocksDb();
		try {
			Database.Table table = db.OpenTable("test_1");
			assert 2 == table.Walk(this::PrintRecord);
		} finally {
			db.Close();
		}
	}

	private DatabaseRocksDb getDatabaseRocksDb() {
		String dbHome = "dbhome";
		DatabaseConf databaseConf = new DatabaseConf();
		databaseConf.setDatabaseType(DbType.MySql);
		databaseConf.setDatabaseUrl(dbHome);
		databaseConf.setName("RocksDB");
		databaseConf.setDbcpConf(new Config.DbcpConf());

		DatabaseRocksDb db = new DatabaseRocksDb(databaseConf);
		return db;
	}

	public final boolean PrintRecord(byte[] key, byte[] value) {
		int ikey = ByteBuffer.Wrap(key).ReadInt();
		int ivalue = ByteBuffer.Wrap(value).ReadInt();
		System.out.println(Zeze.Util.Str.format("key={} value={}", ikey, ivalue));
		return true;
	}
}