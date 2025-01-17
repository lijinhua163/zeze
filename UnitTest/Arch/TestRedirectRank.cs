
using System;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using Zeze.Transaction;
using Zeze.Util;

namespace Arch
{
	[TestClass]
	public class TestRedirectRank
	{
		[TestMethod]
		public async Task TestRedirect()
		{
			var app1 = Game.App.Instance;
			var app2 = new Game.App();

			app1.Start(new string[] { "-ServerId", "0" });
			app2.Start(new string[] { "-ServerId", "1", "-ProviderDirectPort", "20002" });

			Console.WriteLine("Begin Thread.sleep");
			await Task.Delay(2000); // wait connected
			var app1subs = new StringBuilder();
			Str.BuildString(app1subs, app1.Zeze.ServiceManagerAgent.SubscribeStates.Values);
			Console.WriteLine("End Thread.sleep app1 " + app1subs.ToString());
			var app2subs = new StringBuilder();
			Str.BuildString(app2subs, app2.Zeze.ServiceManagerAgent.SubscribeStates.Values);
			Console.WriteLine("End Thread.sleep app2 " + app2subs.ToString());

			try
			{
				int outParam = 0;
				int outServerId = 0;

				// RedirectToServer
				await app1.Game_Rank.TestToServer(0, 111, (i, s) => { outParam = i; outServerId = s; });
				Assert.IsTrue(outParam == 111);
				Assert.IsTrue(outServerId == 0);

				await app1.Game_Rank.TestToServer(1, 222, (i, s) => { outParam = i; outServerId = s; });
				Assert.IsTrue(outParam == 222);
				Assert.IsTrue(outServerId == 1);

				await app2.Game_Rank.TestToServer(0, 333, (i, s) => { outParam = i; outServerId = s; });
				Assert.IsTrue(outParam == 333);
				Assert.IsTrue(outServerId == 0);

				await app2.Game_Rank.TestToServer(1, 444, (i, s) => { outParam = i; outServerId = s; });
				Assert.IsTrue(outParam == 444);
				Assert.IsTrue(outServerId == 1);

				// RedirectHash
				await app1.Game_Rank.TestHash(0, 555, (i, s) => { outParam = i; outServerId = s; });
				Assert.IsTrue(outParam == 555);
				Assert.IsTrue(outServerId == 0);

				await app1.Game_Rank.TestHash(1, 666, (i, s) => { outParam = i; outServerId = s; });
				Assert.IsTrue(outParam == 666);
				Assert.IsTrue(outServerId == 1);

				await app2.Game_Rank.TestHash(0, 777, (i, s) => { outParam = i; outServerId = s; });
				Assert.IsTrue(outParam == 777);
				Assert.IsTrue(outServerId == 0);

				await app2.Game_Rank.TestHash(1, 888, (i, s) => { outParam = i; outServerId = s; });
				Assert.IsTrue(outParam == 888);
				Assert.IsTrue(outServerId == 1);

				// RedirectToServerResult
				long result = 0;
				result = await app1.Game_Rank.TestToServerResult(0, 111, (i, s) => { outParam = i; outServerId = s; });
				Assert.AreEqual(12345, result);
				Assert.IsTrue(outParam == 111);
				Assert.IsTrue(outServerId == 0);
				result = await app1.Game_Rank.TestToServerResult(1, 222, (i, s) => { outParam = i; outServerId = s; });
				Assert.AreEqual(12345, result);
				Assert.IsTrue(outParam == 222);
				Assert.IsTrue(outServerId == 1);

				result = await app2.Game_Rank.TestToServerResult(0, 333, (i, s) => { outParam = i; outServerId = s; });
				Assert.AreEqual(12345, result);
				Assert.IsTrue(outParam == 333);
				Assert.IsTrue(outServerId == 0);

				result = await app2.Game_Rank.TestToServerResult(1, 444, (i, s) => { outParam = i; outServerId = s; });
				Assert.AreEqual(12345, result);
				Assert.IsTrue(outParam == 444);
				Assert.IsTrue(outServerId == 1);

				// RedirectHashResult
				result = await app1.Game_Rank.TestHashResult(0, 555, (i, s) => { outParam = i; outServerId = s; });
				Assert.AreEqual(12345, result);
				Assert.IsTrue(outParam == 555);
				Assert.IsTrue(outServerId == 0);

				result = await app1.Game_Rank.TestHashResult(1, 666, (i, s) => { outParam = i; outServerId = s; });
				Assert.AreEqual(12345, result);
				Assert.IsTrue(outParam == 666);
				Assert.IsTrue(outServerId == 1);

				result = await app2.Game_Rank.TestHashResult(0, 777, (i, s) => { outParam = i; outServerId = s; });
				Assert.AreEqual(12345, result);
				Assert.IsTrue(outParam == 777);
				Assert.IsTrue(outServerId == 0);

				result = await app2.Game_Rank.TestHashResult(1, 888, (i, s) => { outParam = i; outServerId = s; });
				Assert.AreEqual(12345, result);
				Assert.IsTrue(outParam == 888);
				Assert.IsTrue(outServerId == 1);

				// RedirectAll
				{
					var param = 1;
					var ctx = await app1.Game_Rank.TestAllResult(param);
					Assert.AreEqual(0, ctx.HashCodes.Count);
					var sb = new StringBuilder();
					Str.BuildString(sb, ctx.HashErrors);
					sb.AppendLine();
					Str.BuildString(sb, ctx.HashResults, new ComparerInt());
					Console.WriteLine(sb.ToString());

					Assert.AreEqual(0, ctx.HashErrors.Count);
					Assert.AreEqual(100, ctx.HashResults.Count);
					for (int hash = 0; hash < 100; hash++)
                    {
						Assert.IsTrue(ctx.HashResults.TryGetValue(hash, out var value));
						Assert.IsTrue((value & 0xffffffffffff) == ((long)hash << 32 | (uint)param));
					}
				}
				{
					var param = 2;
					var ctx = await app2.Game_Rank.TestAllResult(param);
					Assert.AreEqual(0, ctx.HashCodes.Count);
					var sb = new StringBuilder();
					Str.BuildString(sb, ctx.HashErrors);
					sb.AppendLine();
					Str.BuildString(sb, ctx.HashResults, new ComparerInt());
					Console.WriteLine(sb.ToString());

					Assert.AreEqual(0, ctx.HashErrors.Count);
					Assert.AreEqual(100, ctx.HashResults.Count);
					for (int hash = 0; hash < 100; hash++)
					{
						Assert.IsTrue(ctx.HashResults.TryGetValue(hash, out var value));
						Assert.IsTrue((value & 0xffffffffffff) == ((long)hash << 32 | (uint)param));
					}
				}
			}
			finally
			{
				Console.WriteLine("Begin Stop");
				app1.Stop();
				app2.Stop();
				Console.WriteLine("End Stop");
			}
		}
	}
}
