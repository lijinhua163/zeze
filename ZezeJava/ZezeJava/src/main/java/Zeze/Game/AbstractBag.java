// auto-generated @formatter:off
package Zeze.Game;

public abstract class AbstractBag extends Zeze.IModule {
    public static final int ModuleId = 11014;
    @Override public String getFullName() { return "Zeze.Game.Bag"; }
    @Override public String getName() { return "Bag"; }
    @Override public int getId() { return ModuleId; }
    @Override public boolean isBuiltin() { return true; }

    public static final int ResultCodeFromInvalid = 1;
    public static final int ResultCodeToInvalid = 2;
    public static final int ResultCodeFromNotExist = 3;
    public static final int ResultCodeTrySplitButTargetExistDifferenceItem = 4;

    protected final Zeze.Builtin.Game.Bag.tbag _tbag = new Zeze.Builtin.Game.Bag.tbag();
    protected final Zeze.Builtin.Game.Bag.tItemClasses _tItemClasses = new Zeze.Builtin.Game.Bag.tItemClasses();

    public void RegisterProtocols(Zeze.Net.Service service) {
        var _reflect = new Zeze.Util.Reflect(this.getClass());
        {
            var factoryHandle = new Zeze.Net.Service.ProtocolFactoryHandle<Zeze.Builtin.Game.Bag.Destroy>();
            factoryHandle.Factory = Zeze.Builtin.Game.Bag.Destroy::new;
            factoryHandle.Handle = this::ProcessDestroyRequest;
            factoryHandle.Level = _reflect.getTransactionLevel("ProcessDestroyRequest", Zeze.Transaction.TransactionLevel.Serializable);
            service.AddFactoryHandle(47307869964755L, factoryHandle); // 11014, -1194800685
        }
        {
            var factoryHandle = new Zeze.Net.Service.ProtocolFactoryHandle<Zeze.Builtin.Game.Bag.Move>();
            factoryHandle.Factory = Zeze.Builtin.Game.Bag.Move::new;
            factoryHandle.Handle = this::ProcessMoveRequest;
            factoryHandle.Level = _reflect.getTransactionLevel("ProcessMoveRequest", Zeze.Transaction.TransactionLevel.Serializable);
            service.AddFactoryHandle(47308274693689L, factoryHandle); // 11014, -790071751
        }
    }

    public void UnRegisterProtocols(Zeze.Net.Service service) {
        service.getFactorys().remove(47307869964755L);
        service.getFactorys().remove(47308274693689L);
    }

    public void RegisterZezeTables(Zeze.Application zeze) {
        zeze.AddTable(zeze.getConfig().GetTableConf(_tbag.getName()).getDatabaseName(), _tbag);
        zeze.AddTable(zeze.getConfig().GetTableConf(_tItemClasses.getName()).getDatabaseName(), _tItemClasses);
    }

    public void UnRegisterZezeTables(Zeze.Application zeze) {
        zeze.RemoveTable(zeze.getConfig().GetTableConf(_tbag.getName()).getDatabaseName(), _tbag);
        zeze.RemoveTable(zeze.getConfig().GetTableConf(_tItemClasses.getName()).getDatabaseName(), _tItemClasses);
    }

    public void RegisterRocksTables(Zeze.Raft.RocksRaft.Rocks rocks) {
    }

    protected abstract long ProcessDestroyRequest(Zeze.Builtin.Game.Bag.Destroy r) throws Throwable;
    protected abstract long ProcessMoveRequest(Zeze.Builtin.Game.Bag.Move r) throws Throwable;
}
