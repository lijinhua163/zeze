﻿using System;
using System.Collections.Generic;
using System.Text;
using System.Threading.Tasks;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using Zeze.Serialize;
using Zeze.Transaction;

namespace UnitTest.Zeze.Trans
{
    [TestClass]
    public class TestCheckpoint
    {
        [TestInitialize]
        public void TestInit()
        {
            demo.App.Instance.Start();
        }

        [TestCleanup]
        public void TestCleanup()
        {
            demo.App.Instance.Stop();
        }

		[TestMethod]
		public async Task TestModeTable()
		{
			/*
			Assert.AreTrue(demo.App.Instance.Zeze.NewProcedure(() =>
			{
				demo.App.Instance.demo_Module1.getTable1().remove(1L);
				demo.App.Instance.demo_Module1.getTable1().remove(2L);
				demo.App.Instance.demo_Module1.getTable1().remove(3L);
				demo.App.Instance.demo_Module1.getTable1().remove(4L);
				return 0L;
			}, "remove").Call() == Procedure.Success);
			Console.WriteLine("1");
			Console.WriteLine(Zeze.Transaction.RelativeRecordSet.RelativeRecordSetMapToString());
			demo.App.Instance.Zeze.CheckpointRun();
			Console.WriteLine("2");
			Console.WriteLine(Zeze.Transaction.RelativeRecordSet.RelativeRecordSetMapToString());
			*/
			Assert.IsTrue(demo.App.Instance.Zeze.NewProcedure(async () =>
			{
				await demo.App.Instance.demo_Module1.Table1.GetAsync(1L);
				(await demo.App.Instance.demo_Module1.Table1.GetOrAddAsync(2L)).Int1 = 222;
				return 0L;
			}, "12").CallSynchronously() == Procedure.Success);
			//Console.WriteLine("3");
			//Console.WriteLine(Zeze.Transaction.RelativeRecordSet.RelativeRecordSetMapToString());
			Assert.IsTrue(demo.App.Instance.Zeze.NewProcedure(async () =>
			{
				await demo.App.Instance.demo_Module1.Table1.GetAsync(3L);
				(await demo.App.Instance.demo_Module1.Table1.GetOrAddAsync(4L)).Int1 = 444;
				return 0L;
			}, "34").CallSynchronously() == Procedure.Success);
			//Console.WriteLine("4");
			//Console.WriteLine(Zeze.Transaction.RelativeRecordSet.RelativeRecordSetMapToString());
			Assert.IsTrue(demo.App.Instance.Zeze.NewProcedure(async () =>
			{
				await demo.App.Instance.demo_Module1.Table1.GetAsync(2L);
				(await demo.App.Instance.demo_Module1.Table1.GetOrAddAsync(3L)).Int1 = 333;
				return 0L;
			}, "23").CallSynchronously() == Procedure.Success);
			//Console.WriteLine("5");
			//Console.WriteLine(Zeze.Transaction.RelativeRecordSet.RelativeRecordSetMapToString());
			await demo.App.Instance.Zeze.CheckpointNow();
			//Console.WriteLine("6");
			//Console.WriteLine(Zeze.Transaction.RelativeRecordSet.RelativeRecordSetMapToString());

			var table = demo.App.Instance.demo_Module1.Table1;
			var dbtable = table.GetStorageForTestOnly("IKnownWhatIAmDoing").TableAsync;
			Assert.IsTrue(null != dbtable.ITable.Find(table.EncodeKey(2L)));
			Assert.IsTrue(null != dbtable.ITable.Find(table.EncodeKey(4L)));
			Assert.IsTrue(null != dbtable.ITable.Find(table.EncodeKey(3L)));
		}

		[TestMethod]
        public async Task TestCp()
        {
            Assert.IsTrue(demo.App.Instance.Zeze.NewProcedure(ProcClear, "ProcClear").CallSynchronously() == Procedure.Success);
            Assert.IsTrue(demo.App.Instance.Zeze.NewProcedure(ProcChange, "ProcChange").CallSynchronously() == Procedure.Success);
            await demo.App.Instance.Zeze.CheckpointNow();
			var table = demo.App.Instance.demo_Module1.Table1;
			var dbtable = table.GetStorageForTestOnly("IKnownWhatIAmDoing").TableAsync;
            ByteBuffer value = dbtable.ITable.Find(table.EncodeKey(56));
            Assert.IsNotNull(value);
            Assert.AreEqual(value, bytesInTrans);
        }

		async Task<long> ProcClear()
        {
            await demo.App.Instance.demo_Module1.Table1.RemoveAsync(56);
            return Procedure.Success;
        }

        ByteBuffer bytesInTrans;
		async Task<long> ProcChange()
        {
            demo.Module1.Value v = await demo.App.Instance.demo_Module1.Table1.GetOrAddAsync(56);
            v.Int1 = 1;
            bytesInTrans = ByteBuffer.Allocate();
            v.Encode(bytesInTrans);
            return Procedure.Success;
        }
    }
}
