// auto generate
namespace Zeze.Services
{
    public abstract class AbstractTestRocks : Zeze.IModule 
    {
        public const int ModuleId = 11002;
        public override string FullName => "Zeze.Services.TestRocks";
        public override string Name => "TestRocks";
        public override int Id => ModuleId;
        public override bool IsBuiltin => true;


        public void RegisterProtocols(Zeze.Net.Service service)
        {
            // register protocol factory and handles
            var _reflect = new Zeze.Util.Reflect(this.GetType());
        }

        public void UnRegisterProtocols(Zeze.Net.Service service)
        {
        }

        public void RegisterZezeTables(Zeze.Application zeze)
        {
            // register table
        }

        public void UnRegisterZezeTables(Zeze.Application zeze)
        {
        }

        public void RegisterRocksTables(Zeze.Raft.RocksRaft.Rocks rocks)
        {
            rocks.RegisterTableTemplate<int, Zeze.Builtin.TestRocks.Value>("tRocks");
            Zeze.Raft.RocksRaft.Rocks.RegisterLog<Zeze.Raft.RocksRaft.Log<int>>();
            Zeze.Raft.RocksRaft.Rocks.RegisterLog<Zeze.Raft.RocksRaft.Log<bool>>();
            Zeze.Raft.RocksRaft.Rocks.RegisterLog<Zeze.Raft.RocksRaft.Log<float>>();
            Zeze.Raft.RocksRaft.Rocks.RegisterLog<Zeze.Raft.RocksRaft.Log<double>>();
            Zeze.Raft.RocksRaft.Rocks.RegisterLog<Zeze.Raft.RocksRaft.Log<string>>();
            Zeze.Raft.RocksRaft.Rocks.RegisterLog<Zeze.Raft.RocksRaft.Log<Zeze.Net.Binary>>();
            Zeze.Raft.RocksRaft.Rocks.RegisterLog<Zeze.Raft.RocksRaft.LogSet1<int>>();
            Zeze.Raft.RocksRaft.Rocks.RegisterLog<Zeze.Raft.RocksRaft.LogSet1<Zeze.Builtin.TestRocks.BeanKey>>();
            Zeze.Raft.RocksRaft.Rocks.RegisterLog<Zeze.Raft.RocksRaft.Log<Zeze.Builtin.TestRocks.BeanKey>>();
            Zeze.Raft.RocksRaft.Rocks.RegisterLog<Zeze.Raft.RocksRaft.LogMap1<int, int>>();
            Zeze.Raft.RocksRaft.Rocks.RegisterLog<Zeze.Raft.RocksRaft.LogMap2<int, Zeze.Builtin.TestRocks.Value>>();
        }

    }
}
