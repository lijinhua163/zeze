package UnitTest.Zeze.Game;

import java.util.HashMap;
import Zeze.AppBase;
import Zeze.Application;
import Zeze.Arch.Gen.GenModule;
import Zeze.Arch.LoadConfig;
import Zeze.Arch.ProviderApp;
import Zeze.Arch.ProviderDirect;
import Zeze.Arch.ProviderDirectService;
import Zeze.Arch.ProviderImplement;
import Zeze.Arch.ProviderModuleBinds;
import Zeze.Arch.ProviderService;
import Zeze.Builtin.Game.Rank.BConcurrentKey;
import Zeze.Builtin.Game.Rank.BRankValue;
import Zeze.Builtin.Provider.LinkBroken;
import Zeze.Builtin.Provider.SendConfirm;
import Zeze.Builtin.ProviderDirect.Transmit;
import Zeze.Config;
import Zeze.Game.Rank;
import Zeze.IModule;
import Zeze.Net.Acceptor;
import Zeze.Net.Binary;
import Zeze.Net.Connector;
import Zeze.Net.ServiceConf;
import Zeze.Util.ConcurrentHashSet;
import junit.framework.TestCase;

public class TestRank extends TestCase {
	public static final class App extends AppBase { // 简单的无需读配置文件的App
		final Application zeze;
		final ProviderApp providerApp;
		Rank rank;

		App(int serverId) throws Throwable {
			var config = new Config();
			var serviceConf = new ServiceConf();
			serviceConf.AddConnector(new Connector("127.0.0.1", 5001)); // 连接本地ServiceManager
			config.getServiceConfMap().put("Zeze.Services.ServiceManager.Agent", serviceConf);
			serviceConf = new ServiceConf();
			serviceConf.AddAcceptor(new Acceptor(20000 + serverId, null));
			config.getServiceConfMap().put("ProviderDirectService", serviceConf); // 提供Provider之间直连服务
			config.setGlobalCacheManagerHostNameOrAddress("127.0.0.1"); // 连接本地GlobalServer
			config.setGlobalCacheManagerPort(5555);
			config.getDatabaseConfMap().put("", new Config.DatabaseConf()); // 默认内存数据库配置
			config.setDefaultTableConf(new Config.TableConf()); // 默认的Table配置
			config.setServerId(serverId); // 设置服务器ID
			zeze = new Application("TestRank", config);

			providerApp = new ProviderApp(zeze, new ProviderImplement() {
				@Override
				protected long ProcessLinkBroken(LinkBroken p) {
					return 0;
				}

				@Override
				protected long ProcessSendConfirm(SendConfirm p) {
					return 0;
				}
			}, new ProviderService("ProviderService", zeze), "TestRank#", new ProviderDirect() {
				@Override
				protected long ProcessTransmit(Transmit p) {
					return 0;
				}
			}, new ProviderDirectService("ProviderDirectService", zeze), "Game.Linkd", new LoadConfig());
		}

		@Override
		public Application getZeze() {
			return zeze;
		}

		void start() throws Throwable {
			rank = new Rank(this);
			rank = (Rank)zeze.Redirect.ReplaceModuleInstance(this, rank);
			rank.RegisterZezeTables(zeze);
			rank.Initialize(this);
			if (GenModule.Instance.GenFileSrcRoot != null) {
				System.out.println("---------------");
				throw new RuntimeException("New Source File Has Generate. Re-Compile Need.");
			}
			var modules = new HashMap<String, IModule>();
			modules.put(rank.getFullName(), rank);
			providerApp.initialize(ProviderModuleBinds.Load(""), modules);
			zeze.Start();
			providerApp.ProviderService.Start();
			providerApp.ProviderDirectService.Start();
			providerApp.StartLast();
		}

		void stop() throws Throwable {
			providerApp.ProviderDirectService.Stop();
			providerApp.ProviderService.Stop();
			zeze.Stop();
			if (rank != null) {
				rank.UnRegisterZezeTables(zeze);
				rank = null;
			}
		}
	}

	static {
		System.setProperty("log4j.configurationFile", "log4j2.xml");
	}

	private static final int CONC_LEVEL = 100;
	private static final int APP_COUNT = 3;

	private final App[] apps = new App[APP_COUNT];

	@Override
	protected void setUp() {
		System.out.println("------ setUp begin");
		try {
			for (int i = 0; i < APP_COUNT; i++)
				apps[i] = new App(1 + i);
			for (int i = 0; i < APP_COUNT; i++)
				apps[i].start();

			System.out.println("Begin Thread.sleep");
			Thread.sleep(2000); // wait connected
			for (int i = 0; i < APP_COUNT; i++) {
				System.out.format("End Thread.sleep app%d %s%n",
						1 + i, apps[i].getZeze().getServiceManagerAgent().getSubscribeStates().values());
			}
		} catch (Throwable e) {
			throw new RuntimeException(e);
		} finally {
			System.out.println("------ setUp end");
		}
	}

	@Override
	protected void tearDown() {
		System.out.println("------ tearDown begin");
		try {
			for (int i = 0; i < APP_COUNT; i++)
				apps[i].stop();
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
		System.out.println("------ tearDown end");
	}

	public void testRank() throws Throwable {
		try {
			System.out.println("------ testRank begin");
			App app = apps[0]; // 可以随便取一个, 不过都是对称的, 应该不用都测

			app.rank.funcConcurrentLevel = rankType -> CONC_LEVEL;
			int concLevel = app.rank.getConcurrentLevel(1);
			var rankKey = app.rank.newRankKey(1, BConcurrentKey.TimeTypeTotal);

			for (int hash = 0; hash < concLevel; hash++) {
				int h = hash;
				long roleId = 1000 + h;
				app.zeze.NewProcedure(() -> {
					app.rank.updateRank(h, rankKey, roleId, roleId * 10, Binary.Empty).await().then(r -> {
						assertNotNull(r);
						assertEquals(0L, r.longValue());
					});
					return 0;
				}, "updateRank").Call();
			}

			app.zeze.NewProcedure(() -> {
				// 直接从数据库读取并合并
				var result = app.rank.getRankDirect(rankKey);
//				System.out.format("--- getRankDirect: concurrent=%d, rankList=[%d]:%s%n",
//						concLevel, result.getRankList().size(), result);
				assertEquals(concLevel, result.getRankList().size());
				for (BRankValue rank : result.getRankList()) {
					assertTrue(rank.getRoleId() >= 1000 && rank.getRoleId() < 1000 + concLevel);
					assertEquals(rank.getRoleId() * 10, rank.getValue());
				}
				return 0;
			}, "getRankDirect").Call();

			var hashSet1 = new ConcurrentHashSet<Integer>();
			var hashSet2 = new ConcurrentHashSet<Integer>();
			app.zeze.NewProcedure(() -> {
				app.rank.getRankAll(rankKey).onResult(r -> {
					assertNotNull(r);
//					System.out.format("--- getRankAll onResult: hash=%d, resultCode=%d, rankList=%s%n",
//							r.getHash(), r.getResultCode(), r.rankList);
					assertTrue(r.getHash() >= 0 && r.getHash() < concLevel);
					assertTrue(hashSet1.add(r.getHash()));
					assertEquals(0, r.getResultCode());
					assertEquals(1, r.rankList.getRankList().size());
					var rank = r.rankList.getRankList().get(0);
					assertTrue(rank.getRoleId() >= 1000 && rank.getRoleId() < 1000 + concLevel);
					assertEquals(rank.getRoleId() * 10, rank.getValue());
				}).onAllDone(ctx -> {
					assertNotNull(ctx);
					var results = ctx.getAllResults();
//					System.out.format("--- getRankAll onAllDone: timeout=%b, results.size=%d, concurrent=%d%n",
//							ctx.isTimeout(), results.size(), ctx.getConcurrentLevel());
					assertFalse(ctx.isTimeout());
					assertEquals(concLevel, results.size());
					assertEquals(concLevel, ctx.getConcurrentLevel());
					results.foreachValue(r -> {
//						System.out.format("        hash=%d, resultCode=%d, rankList=[%d]:%s%n",
//								r.getHash(), r.getResultCode(), r.rankList.getRankList().size(), r.rankList);
						assertTrue(r.getHash() >= 0 && r.getHash() < concLevel);
						assertTrue(hashSet2.add(r.getHash()));
						assertEquals(0, r.getResultCode());
						assertEquals(1, r.rankList.getRankList().size());
						var rank = r.rankList.getRankList().get(0);
						assertTrue(rank.getRoleId() >= 1000 && rank.getRoleId() < 1000 + concLevel);
						assertEquals(rank.getRoleId() * 10, rank.getValue());
					});
				}).await();
				return 0;
			}, "getRankAll").Call();
			assertEquals(concLevel, hashSet1.size());
			assertEquals(concLevel, hashSet2.size());
		} catch (Throwable e) {
			e.printStackTrace();
			throw e;
		} finally {
			System.out.println("------ testRank end");
		}
	}

	// 用于生成Redirect代码
	public static void main(String[] args) throws Throwable {
		TestRank testRank = new TestRank();
		try {
			testRank.setUp();
			testRank.testRank();
		} finally {
			testRank.tearDown();
		}
	}
}