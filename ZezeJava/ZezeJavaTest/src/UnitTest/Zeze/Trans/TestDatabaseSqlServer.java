package UnitTest.Zeze.Trans;

import Zeze.Config;
import Zeze.Config.DatabaseConf;
import Zeze.Config.DbType;
import Zeze.Serialize.ByteBuffer;
import Zeze.Transaction.Database;
import Zeze.Transaction.DatabaseSqlServer;
import junit.framework.TestCase;
import org.junit.Assert;

public class TestDatabaseSqlServer extends TestCase {

	public final void test1() throws Exception {
		System.out.println(System.getProperties().get("user.home"));
		System.err.println("sqlserver jdbc 不能连接 vs 自带的 LocalDB(不用配置的）。所以这个测试先不管了。");
		if (!TestDatabaseMySql.checkDriverClassExist("com.microsoft.sqlserver.jdbc.SQLServerDriver"))
			return;

		String url = "jdbc:sqlserver://localhost;user=MyUserName;password=*****;";
		DatabaseConf databaseConf = new DatabaseConf();
		databaseConf.setDatabaseType(DbType.SqlServer);
		databaseConf.setDatabaseUrl(url);
		databaseConf.setName("sqlserver");
		databaseConf.setDbcpConf(new Config.DbcpConf());

		DatabaseSqlServer sqlserver = new DatabaseSqlServer(databaseConf);
		Database.Table table = sqlserver.OpenTable("test1");
		{
			try (var trans = sqlserver.BeginTransaction()) {
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
		}
		Assert.assertEquals(0, table.Walk(this::PrintRecord));
		{
			try (var trans = sqlserver.BeginTransaction()) {
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
		}
		{
			ByteBuffer key = ByteBuffer.Allocate();
			key.WriteInt(1);
			ByteBuffer value = table.Find(key);
			Assert.assertNotNull(value);
			Assert.assertEquals(1, value.ReadInt());
			Assert.assertEquals(value.ReadIndex, value.WriteIndex);
		}
		{
			ByteBuffer key = ByteBuffer.Allocate();
			key.WriteInt(2);
			ByteBuffer value = table.Find(key);
			Assert.assertNotNull(value);
			Assert.assertEquals(2, value.ReadInt());
			Assert.assertEquals(value.ReadIndex, value.WriteIndex);
		}
		Assert.assertEquals(2, table.Walk(this::PrintRecord));
	}

	public final boolean PrintRecord(byte[] key, byte[] value) {
		int ikey = ByteBuffer.Wrap(key).ReadInt();
		int ivalue = ByteBuffer.Wrap(value).ReadInt();
		System.out.println(Zeze.Util.Str.format("key={} value={}", ikey, ivalue));
		return true;
	}
}
