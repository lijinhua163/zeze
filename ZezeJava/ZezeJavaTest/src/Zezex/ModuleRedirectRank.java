package Zezex;

import Game.App;
import Zeze.Transaction.Procedure;
import Zeze.Util.IntHashSet;
import Zeze.Util.TaskCompletionSource;
import junit.framework.TestCase;

@SuppressWarnings("NewClassNamingConvention")
public class ModuleRedirectRank extends TestCase {
	private App app1;
	private App app2;

	@Override
	protected void setUp() {
		app1 = App.Instance;
		app2 = new App();

		try {
			app1.Start(new String[]{"-ServerId", "0"});
			app2.Start(new String[]{"-ServerId", "1", "-ProviderDirectPort", "20002"});

			System.out.println("Begin Thread.sleep");
			Thread.sleep(2000); // wait connected
			System.out.println("End Thread.sleep app1 " + app1.Zeze.getServiceManagerAgent().getSubscribeStates().values());
			System.out.println("End Thread.sleep app2 " + app2.Zeze.getServiceManagerAgent().getSubscribeStates().values());
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void tearDown() {
		System.out.println("Begin Stop");
		try {
			app1.Stop();
			app2.Stop();
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
		System.out.println("End Stop");
	}

	public void testRedirect() throws Throwable {
		// RedirectToServer
		app1.Game_Rank.TestToServer(0, 111).then(result -> {
			assertEquals(111, result.out);
			assertEquals(0, result.serverId);
		}).await();

		app1.Game_Rank.TestToServer(1, 222).then(result -> {
			assertEquals(222, result.out);
			assertEquals(1, result.serverId);
		}).await();

		app2.Game_Rank.TestToServer(0, 333).then(result -> {
			assertEquals(333, result.out);
			assertEquals(0, result.serverId);
		}).await();

		app2.Game_Rank.TestToServer(1, 444).then(result -> {
			assertEquals(444, result.out);
			assertEquals(1, result.serverId);
		}).await();

		// RedirectHash
		var hash11 = Zeze.Serialize.ByteBuffer.calc_hashnr(127364);
		System.out.println("11--->" + hash11);
		app1.Game_Rank.TestHash(hash11, 555).then(result -> {
			assertEquals(hash11, result.hash);
			assertEquals(555, result.out);
			System.out.println("11--->" + result.serverId);
			assertEquals(1, result.serverId);
		}).await();

		var hash12 = Zeze.Serialize.ByteBuffer.calc_hashnr(100);
		System.out.println("12--->" + hash12);
		app1.Game_Rank.TestHash(hash12, 666).then(result -> {
			assertEquals(hash12, result.hash);
			assertEquals(666, result.out);
			System.out.println("12--->" + result.serverId);
			assertEquals(0, result.serverId);
		}).await();

		var hash21 = Zeze.Serialize.ByteBuffer.calc_hashnr(127364);
		System.out.println("21--->" + hash21);
		app2.Game_Rank.TestHash(hash21, 777).then(result -> {
			assertEquals(hash21, result.hash);
			assertEquals(777, result.out);
			System.out.println("21--->" + result.serverId);
			assertEquals(1, result.serverId);
		}).await();

		var hash22 = Zeze.Serialize.ByteBuffer.calc_hashnr(100);
		System.out.println("22--->" + hash22);
		app2.Game_Rank.TestHash(hash22, 888).then(result -> {
			assertEquals(hash22, result.hash);
			assertEquals(888, result.out);
			System.out.println("22--->" + result.serverId);
			assertEquals(0, result.serverId);
		}).await();

		// RedirectAll
		final int CONCURRENT_LEVEL = 6;
		var future1 = new TaskCompletionSource<Boolean>();
		var hashes = new IntHashSet();
		app1.Game_Rank.TestToAll(CONCURRENT_LEVEL, 12345).onResult(r -> {
			var h = r.getHash();
			var rc = r.getResultCode();
			System.out.println("TestToAll onResult: hash=" + h + ", resultCode=" + rc + ", out=" + r.out);
			assertTrue(h >= 0 && h < CONCURRENT_LEVEL);
			assertTrue(hashes.add(h));
			if (rc == Procedure.Success)
				assertEquals(12345, r.out);
			else if (rc == Procedure.Exception)
				assertEquals(0, r.out);
		}).onAllDone(ctx -> {
			assertFalse(ctx.isTimeout());
			try {
				var allResults = ctx.getAllResults();
				System.out.println("TestToAll onAllDone: allResults=" + allResults);
				assertEquals(CONCURRENT_LEVEL, allResults.size());
				assertEquals(Procedure.Success, allResults.get(0).getResultCode()); // local
				assertEquals(Procedure.Success, allResults.get(1).getResultCode()); // remote
				assertEquals(Procedure.Exception, allResults.get(2).getResultCode()); // local exception
				assertEquals(Procedure.Exception, allResults.get(3).getResultCode()); // remote exception
				assertEquals(Procedure.Success, allResults.get(4).getResultCode()); // local async
				assertEquals(Procedure.Success, allResults.get(4).getResultCode()); // remote async
			} finally {
				future1.SetResult(true);
			}
		});
		assertTrue(future1.get());
		assertEquals(CONCURRENT_LEVEL, hashes.size());

		app2.Game_Rank.TestToAll(0, 12345).await().onAllDone(ctx -> {
			if (ctx.isCompleted()) {
				System.out.println("TestToAll(0) onAllDone: allResults=" + ctx.getAllResults());
				assertEquals(0, ctx.getAllResults().size());
			}
		});
	}
}
