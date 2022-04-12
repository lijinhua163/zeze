// auto generate
namespace Zeze.Services
{
    public abstract class AbstractGlobalCacheManagerWithRaftAgent : Zeze.IModule 
    {
    public const int ModuleId = 11001;
    public override string FullName => "Zeze.Beans.GlobalCacheManagerWithRaft";
    public override string Name => "GlobalCacheManagerWithRaft";
    public override int Id => ModuleId;


        public void RegisterProtocols(Zeze.Net.Service service)
        {
            // register protocol factory and handles
            var _reflect = new Zeze.Util.Reflect(this.GetType());
            service.AddFactoryHandle(47251758877516, new Zeze.Net.Service.ProtocolFactoryHandle()
            {
                Factory = () => new Zeze.Beans.GlobalCacheManagerWithRaft.Acquire(),
                TransactionLevel = _reflect.GetTransactionLevel("ProcessAcquireRequest", Zeze.Transaction.TransactionLevel.Serializable),
            });
            service.AddFactoryHandle(47249689802603, new Zeze.Net.Service.ProtocolFactoryHandle()
            {
                Factory = () => new Zeze.Beans.GlobalCacheManagerWithRaft.Cleanup(),
                TransactionLevel = _reflect.GetTransactionLevel("ProcessCleanupRequest", Zeze.Transaction.TransactionLevel.Serializable),
            });
            service.AddFactoryHandle(47250139303472, new Zeze.Net.Service.ProtocolFactoryHandle()
            {
                Factory = () => new Zeze.Beans.GlobalCacheManagerWithRaft.KeepAlive(),
                TransactionLevel = _reflect.GetTransactionLevel("ProcessKeepAliveRequest", Zeze.Transaction.TransactionLevel.Serializable),
            });
            service.AddFactoryHandle(47251605578232, new Zeze.Net.Service.ProtocolFactoryHandle()
            {
                Factory = () => new Zeze.Beans.GlobalCacheManagerWithRaft.Login(),
                TransactionLevel = _reflect.GetTransactionLevel("ProcessLoginRequest", Zeze.Transaction.TransactionLevel.Serializable),
            });
            service.AddFactoryHandle(47250988461421, new Zeze.Net.Service.ProtocolFactoryHandle()
            {
                Factory = () => new Zeze.Beans.GlobalCacheManagerWithRaft.NormalClose(),
                TransactionLevel = _reflect.GetTransactionLevel("ProcessNormalCloseRequest", Zeze.Transaction.TransactionLevel.Serializable),
            });
            service.AddFactoryHandle(47252602373450, new Zeze.Net.Service.ProtocolFactoryHandle()
            {
                Factory = () => new Zeze.Beans.GlobalCacheManagerWithRaft.Reduce(),
                Handle = ProcessReduceRequest,
                TransactionLevel = _reflect.GetTransactionLevel("ProcessReduceRequest", Zeze.Transaction.TransactionLevel.Serializable),
            });
            service.AddFactoryHandle(47251661990773, new Zeze.Net.Service.ProtocolFactoryHandle()
            {
                Factory = () => new Zeze.Beans.GlobalCacheManagerWithRaft.ReLogin(),
                TransactionLevel = _reflect.GetTransactionLevel("ProcessReLoginRequest", Zeze.Transaction.TransactionLevel.Serializable),
            });
        }

        public void UnRegisterProtocols(Zeze.Net.Service service)
        {
            service.Factorys.TryRemove(47251758877516, out var _);
            service.Factorys.TryRemove(47249689802603, out var _);
            service.Factorys.TryRemove(47250139303472, out var _);
            service.Factorys.TryRemove(47251605578232, out var _);
            service.Factorys.TryRemove(47250988461421, out var _);
            service.Factorys.TryRemove(47252602373450, out var _);
            service.Factorys.TryRemove(47251661990773, out var _);
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
        }


        protected abstract System.Threading.Tasks.Task<long>  ProcessReduceRequest(Zeze.Net.Protocol p);
    }
}
