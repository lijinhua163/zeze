// auto-generated @formatter:off
package Zeze.Game;

public abstract class AbstractRank extends Zeze.IModule {
    public static final int ModuleId = 11015;
    @Override public String getFullName() { return "Zeze.Game.Rank"; }
    @Override public String getName() { return "Rank"; }
    @Override public int getId() { return ModuleId; }
    @Override public boolean isBuiltin() { return true; }

    protected final Zeze.Builtin.Game.Rank.trank _trank = new Zeze.Builtin.Game.Rank.trank();

    public void RegisterProtocols(Zeze.Net.Service service) {
    }

    public void UnRegisterProtocols(Zeze.Net.Service service) {
    }

    public void RegisterZezeTables(Zeze.Application zeze) {
        zeze.AddTable(zeze.getConfig().GetTableConf(_trank.getName()).getDatabaseName(), _trank);
    }

    public void UnRegisterZezeTables(Zeze.Application zeze) {
        zeze.RemoveTable(zeze.getConfig().GetTableConf(_trank.getName()).getDatabaseName(), _trank);
    }

    public void RegisterRocksTables(Zeze.Raft.RocksRaft.Rocks rocks) {
    }
}
