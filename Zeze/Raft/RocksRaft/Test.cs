﻿using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Zeze.Serialize;
using Zeze.Util;

namespace Zeze.Raft.RocksRaft
{
	public class Test
	{
		public sealed class Bean1 : Bean
		{
			private int _i;
			private long _l;
			private CollMap1<int, int> _map1;
			private Bean2 _bean2;
			private CollMap2<int, Bean1> _map2;

			public int _Int32MapKey_ { get; set; }

			public int I
			{
				get
				{
					if (IsManaged)
					{
						if (Transaction.Current == null) return _i;
						if (false == Transaction.Current.TryGetLog(ObjectId + 1, out var log)) return _i;
						return ((Log<int>)log).Value;
					}
					else
					{
						return _i;
					}
				}

				set
				{
					if (IsManaged)
					{
						Transaction.Current.PutLog(new Log<int>() { Belong = this, VariableId = 1, Value = value, });
					}
					else
					{
						_i = value;
					}
				}
			}

            public override Bean CopyBean()
            {
                throw new NotImplementedException();
            }
            public long L
			{
				get
				{
					if (IsManaged)
					{
						if (Transaction.Current == null) return _l;
						if (false == Transaction.Current.TryGetLog(ObjectId + 2, out var log)) return _l;
						return ((Log<long>)log).Value;
					}
					else
					{
						return _l;
					}
				}

				set
				{
					if (IsManaged)
					{
						Transaction.Current.PutLog(new Log<long>() { Belong = this, VariableId = 2, Value = value, });
					}
					else
					{
						_l = value;
					}
				}
			}

			public CollMap1<int, int> Map1 => _map1;
			public CollMap2<int, Bean1> Map2 => _map2;

			public Bean2 Bean2 => _bean2;

			public override void FollowerApply(Log log)
			{
				var blog = (LogBean)log;
				foreach (var vlog in blog.Variables.Values)
				{
					switch (vlog.VariableId)
					{
						case 1: _i = ((Log<int>)vlog).Value; break;
						case 2: _l = ((Log<long>)vlog).Value; break;
						case 3: _map1.FollowerApply(vlog); break;
						case 4: _bean2.FollowerApply(vlog); break;
						case 5: _map2.FollowerApply(vlog); break;
					}
				}
			}

			public override void LeaderApplyNoRecursive(Log vlog)
			{
				switch (vlog.VariableId)
				{
					case 1: _i = ((Log<int>)vlog).Value; break;
					case 2: _l = ((Log<long>)vlog).Value; break;
					case 3: _map1.LeaderApplyNoRecursive(vlog); break;
					case 5: _map2.LeaderApplyNoRecursive(vlog); break;
				}
			}

			public Bean1()
			{
				_map1 = new CollMap1<int, int>() { VariableId = 3 };
				_bean2 = new Bean2() { VariableId = 4 };
				_map2 = new CollMap2<int, Bean1>() { VariableId = 5 };
			}

			public override void Decode(ByteBuffer bb)
			{
				_Int32MapKey_ = bb.ReadInt();

				I = bb.ReadInt();
				L = bb.ReadLong();
				Map1.Decode(bb);
				Bean2.Decode(bb);
				Map2.Decode(bb);
			}

			public override void Encode(ByteBuffer bb)
			{
				bb.WriteInt(_Int32MapKey_);

				bb.WriteInt(I);
				bb.WriteLong(L);
				Map1.Encode(bb);
				Bean2.Encode(bb);
				Map2.Encode(bb);
			}

			protected override void InitChildrenRootInfo(Record.RootInfo root)
			{
				_map1.InitRootInfo(root, this);
				_bean2.InitRootInfo(root, this);
				_map2.InitRootInfo(root, this);
			}

			public override string ToString()
			{
				return $"Bean1({_Int32MapKey_} I={I} L={L} Map1={Map1} Bean2={Bean2} Map2={Map2})";
			}
		}

		public sealed class Bean2 : Bean
		{
			private int _i;

            public override Bean CopyBean()
            {
                throw new NotImplementedException();
            }

            public int I
			{
				get
				{
					if (IsManaged)
					{
						if (Transaction.Current == null) return _i;
						if (false == Transaction.Current.TryGetLog(ObjectId + 1, out var log)) return _i;
						return ((Log<int>)log).Value;
					}
					else
					{
						return _i;
					}
				}

				set
				{
					if (IsManaged)
					{
						Transaction.Current.PutLog(new Log<int>() { Belong = this, VariableId = 1, Value = value, });
					}
					else
					{
						_i = value;
					}
				}
			}

			public override void Decode(ByteBuffer bb)
			{
				I = bb.ReadInt();
			}

			public override void Encode(ByteBuffer bb)
			{
				bb.WriteInt(I);
			}

			protected override void InitChildrenRootInfo(Record.RootInfo root)
			{
			}

			public override string ToString()
			{
				return $"Bean2(I={I})";
			}

			public override void FollowerApply(Log log)
			{
				var blog = (LogBean)log;
				foreach (var vlog in blog.Variables.Values)
				{
					switch (vlog.VariableId)
					{
						case 1: _i = ((Log<int>)vlog).Value; break;
					}
				}
			}

			public override void LeaderApplyNoRecursive(Log vlog)
			{
				switch (vlog.VariableId)
				{
					case 1: _i = ((Log<int>)vlog).Value; break;
				}
			}
		}

		private void Remove1(Rocks rocks)
		{
			rocks.NewProcedure(() =>
			{
				var table = rocks.OpenTable<int, Bean1>("tRocksRaft");
				table.Remove(1);

				Transaction.Current.RunWhileCommit(() =>
				{
					var c = Transaction.Current.Changes;

					SimpleAssert.IsTrue(c.Beans.Count == 1);

					SimpleAssert.IsTrue(c.Records.Count == 1);
					SimpleAssert.IsTrue(c.Records.TryGetValue(new TableKey(table.Name, 1), out var r));
					SimpleAssert.IsNull(r.PutValue);
					SimpleAssert.AreEqual(Changes.Record.Remove, r.State);
					SimpleAssert.IsTrue(r.LogBeans.Count == 1);
					SimpleAssert.IsTrue(r.LogBean.Count == 0);
				});
				return 0;
			}).Call();
		}

		private void Update(Table<int, Bean1> table, int num)
		{
			var value = table.GetOrAdd(1);

			// 本层Bean变量修改日志
			value.I = 1 + num;

			// 下一层Bean变量修改日志
			value.Bean2.I = 2 + num;

			// 本层Bean容器变量修改日志
			value.Map1.Put(3 + num, 3 + num);

			// 本层Bean容器变量修改日志2
			var bean1 = new Bean1();
			value.Map2.Put(4 + num, bean1);

			// 容器内Bean修改日志。
			bean1.I = 5 + num;
		}

		private void VerifyChanges(string except)
		{
			Transaction.Current.RunWhileCommit(() =>
			{
				var Changes = Transaction.Current.Changes;
				var sb = new StringBuilder();
				ByteBuffer.BuildString(sb, Changes.Records);
				if (string.IsNullOrEmpty(except))
				{
					Console.WriteLine(sb.ToString());
				}
				else
				{
					except = except.Replace("\r\n", "\n");
					SimpleAssert.AreEqual(except, sb.ToString());
				}
			});
		}

		private void VerifyData(Rocks rocks, string except)
		{
			rocks.NewProcedure(() =>
			{
				var table = rocks.OpenTable<int, Bean1>("tRocksRaft");
				var value = table.GetOrAdd(1);
				var current = value.ToString();
				if (string.IsNullOrEmpty(except))
				{
					Console.WriteLine(current);
				}
				else
				{
					SimpleAssert.AreEqual(except, current);
				}
				return 0;
			}).Call();
		}

		private void PutAndEdit(Rocks rocks)
		{
			rocks.NewProcedure(() =>
			{
				var table = rocks.OpenTable<int, Bean1>("tRocksRaft");
				Update(table, 0);
				VerifyChanges(@"{(tRocksRaft,1):State=1 PutValue=Bean1(0 I=1 L=0 Map1={3:3} Bean2=Bean2(I=2) Map2={4:Bean1(4 I=5 L=0 Map1={} Bean2=Bean2(I=0) Map2={})})
Log=[]
AllLog=[{0:Value=Bean1(0 I=1 L=0 Map1={3:3} Bean2=Bean2(I=2) Map2={4:Bean1(4 I=5 L=0 Map1={} Bean2=Bean2(I=0) Map2={})})},{1:Value=1,3: Putted:{3:3} Removed:[],4:{1:Value=2},5: Putted:{4:Bean1(4 I=5 L=0 Map1={} Bean2=Bean2(I=0) Map2={})} Removed:[] Changed:[{1:Value=5}]}]}");
				return 0;
			}).Call();
		}

		private void Edit(Rocks rocks)
		{
			rocks.NewProcedure(() =>
			{
				var table = rocks.OpenTable<int, Bean1>("tRocksRaft");
				Update(table, 10);
				VerifyChanges(@"{(tRocksRaft,1):State=2 PutValue=
Log=[{1:Value=11,3: Putted:{13:13} Removed:[],4:{1:Value=12},5: Putted:{14:Bean1(14 I=15 L=0 Map1={} Bean2=Bean2(I=0) Map2={})} Removed:[] Changed:[{1:Value=15}]}]
AllLog=[{1:Value=11,3: Putted:{13:13} Removed:[],4:{1:Value=12},5: Putted:{14:Bean1(14 I=15 L=0 Map1={} Bean2=Bean2(I=0) Map2={})} Removed:[] Changed:[{1:Value=15}]}]}");
				return 0;
			}).Call();
		}

		private void EditAndPut(Rocks rocks)
		{
			rocks.NewProcedure(() =>
			{
				var table = rocks.OpenTable<int, Bean1>("tRocksRaft");
				Update(table, 20);
				// 重新put，将会让上面的修改树作废。但所有的日志树都可以从All中看到。
				var bean1put = new Bean1();
				table.Put(1, bean1put);
				VerifyChanges(@"{(tRocksRaft,1):State=1 PutValue=Bean1(0 I=0 L=0 Map1={} Bean2=Bean2(I=0) Map2={})
Log=[]
AllLog=[{0:Value=Bean1(0 I=0 L=0 Map1={} Bean2=Bean2(I=0) Map2={})},{1:Value=21,3: Putted:{23:23} Removed:[],4:{1:Value=22},5: Putted:{24:Bean1(24 I=25 L=0 Map1={} Bean2=Bean2(I=0) Map2={})} Removed:[] Changed:[{1:Value=25}]}]}");
				return 0;
			}).Call();
		}

		private void EditInContainer(Rocks rocks)
		{
			rocks.NewProcedure(() =>
			{
				var table = rocks.OpenTable<int, Bean1>("tRocksRaft");
				var value = table.GetOrAdd(1);
				var edit = value.Map2.Get(14);
				edit.Bean2.I = 2222;
				VerifyChanges(@"{(tRocksRaft,1):State=2 PutValue=
Log=[{5: Putted:{} Removed:[] Changed:[{4:{1:Value=2222}}]}]
AllLog=[{5: Putted:{} Removed:[] Changed:[{4:{1:Value=2222}}]}]}");
				return 0;
			}).Call();
		}

		private void NestProcedure(Rocks rocks)
		{
			rocks.NewProcedure(() =>
			{
				var table = rocks.OpenTable<int, Bean1>("tRocksRaft");
				var value = table.Get(1);
				value.Bean2.I = 3333;

				rocks.NewProcedure(() =>
				{
					var table = rocks.OpenTable<int, Bean1>("tRocksRaft");
					var value = table.Get(1);
					value.Bean2.I = 4444;
					SimpleAssert.AreEqual(4444, value.Bean2.I);
					return -1;
				}).Call();

				VerifyChanges(@"{(tRocksRaft,1):State=2 PutValue=
Log=[{4:{1:Value=3333}}]
AllLog=[{4:{1:Value=3333}}]}");
				return 0;
			}).Call();
		}

		private void NestProcedureContainer(Rocks rocks)
		{
			rocks.NewProcedure(() =>
			{
				rocks.NewProcedure(() =>
				{
					var table = rocks.OpenTable<int, Bean1>("tRocksRaft");
					var value = table.Get(1);
					value.Map2.Put(4444, new Bean1());
					value.Map1.Put(4444, 4444);
					value.Map1.Remove(3);
					value.Map2.Remove(4);
					return 0;
				}).Call();

				VerifyChanges(@"{(tRocksRaft,1):State=2 PutValue=
Log=[{3: Putted:{4444:4444} Removed:[3],5: Putted:{4444:Bean1(4444 I=0 L=0 Map1={} Bean2=Bean2(I=0) Map2={})} Removed:[4] Changed:[]}]
AllLog=[{3: Putted:{4444:4444} Removed:[3],5: Putted:{4444:Bean1(4444 I=0 L=0 Map1={} Bean2=Bean2(I=0) Map2={})} Removed:[4] Changed:[]}]}");
				return 0;
			}).Call();
		}

		private Rocks GetLeader(List<Rocks> rocks, Rocks skip)
		{
			while (true)
			{
				foreach (var rock in rocks)
				{
					if (rock == skip)
						continue;
					if (rock.IsLeader)
						return rock;
				}
				System.Threading.Thread.Sleep(1000);
			}
		}

		public void Test_1()
		{
			FileSystem.DeleteDirectory("127.0.0.1_6000");
			FileSystem.DeleteDirectory("127.0.0.1_6001");
			FileSystem.DeleteDirectory("127.0.0.1_6002");

			using var rocks1 = new Rocks("127.0.0.1:6000");
			using var rocks2 = new Rocks("127.0.0.1:6001");
			using var rocks3 = new Rocks("127.0.0.1:6002");

			var rockslist = new List<Rocks> { rocks1, rocks2, rocks3 };
			foreach (var rr in rockslist)
			{
				rr.RegisterLog<LogMap1<int, int>>();
				rr.RegisterLog<LogMap2<int, Bean1>>();
				rr.OpenTable<int, Bean1>("tRocksRaft");
			}

			// start
			rocks1.Raft.Server.Start();
			rocks2.Raft.Server.Start();
			rocks3.Raft.Server.Start();

			// leader
			var leader = GetLeader(rockslist, null);
			RunLeader(leader);
			leader.Raft.Server.Stop();

			// 只简单验证一下最新的数据。
			var newleader = GetLeader(rockslist, leader);
			VerifyData(newleader, "Bean1(0 I=0 L=0 Map1={} Bean2=Bean2(I=0) Map2={})");
		}

		private void RunLeader(Rocks rocks)
		{
			Remove1(rocks);

			PutAndEdit(rocks);
			VerifyData(rocks, "Bean1(0 I=1 L=0 Map1={3:3} Bean2=Bean2(I=2) Map2={4:Bean1(4 I=5 L=0 Map1={} Bean2=Bean2(I=0) Map2={})})");

			Edit(rocks);
			VerifyData(rocks, "Bean1(0 I=11 L=0 Map1={3:3,13:13} Bean2=Bean2(I=12) Map2={4:Bean1(4 I=5 L=0 Map1={} Bean2=Bean2(I=0) Map2={}),14:Bean1(14 I=15 L=0 Map1={} Bean2=Bean2(I=0) Map2={})})");

			EditInContainer(rocks);
			VerifyData(rocks, "Bean1(0 I=11 L=0 Map1={3:3,13:13} Bean2=Bean2(I=12) Map2={4:Bean1(4 I=5 L=0 Map1={} Bean2=Bean2(I=0) Map2={}),14:Bean1(14 I=15 L=0 Map1={} Bean2=Bean2(I=2222) Map2={})})");

			NestProcedure(rocks);
			VerifyData(rocks, "Bean1(0 I=11 L=0 Map1={3:3,13:13} Bean2=Bean2(I=3333) Map2={4:Bean1(4 I=5 L=0 Map1={} Bean2=Bean2(I=0) Map2={}),14:Bean1(14 I=15 L=0 Map1={} Bean2=Bean2(I=2222) Map2={})})");

			NestProcedureContainer(rocks);
			VerifyData(rocks, "Bean1(0 I=11 L=0 Map1={13:13,4444:4444} Bean2=Bean2(I=3333) Map2={14:Bean1(14 I=15 L=0 Map1={} Bean2=Bean2(I=2222) Map2={}),4444:Bean1(4444 I=0 L=0 Map1={} Bean2=Bean2(I=0) Map2={})})");

			EditAndPut(rocks);
			VerifyData(rocks, "Bean1(0 I=0 L=0 Map1={} Bean2=Bean2(I=0) Map2={})");

			// 再次运行本测试，才会执行到 LoadSnapshot。
			rocks.Raft.LogSequence.Snapshot(true);
		}
	}
}