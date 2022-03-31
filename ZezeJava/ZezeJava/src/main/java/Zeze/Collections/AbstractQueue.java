// auto-generated @formatter:off
package Zeze.Collections;

public abstract class AbstractQueue {
    protected final Zeze.Beans.Collections.Queue.tQueueNodes _tQueueNodes = new Zeze.Beans.Collections.Queue.tQueueNodes();
    protected final Zeze.Beans.Collections.Queue.tQueues _tQueues = new Zeze.Beans.Collections.Queue.tQueues();

    public void RegisterProtocols(Zeze.Net.Service service) {
    }

    public void UnRegisterProtocols(Zeze.Net.Service service) {
    }

    public void RegisterZezeTables(Zeze.Application zeze) {
        zeze.AddTable(zeze.getConfig().GetTableConf(_tQueueNodes.getName()).getDatabaseName(), _tQueueNodes);
        zeze.AddTable(zeze.getConfig().GetTableConf(_tQueues.getName()).getDatabaseName(), _tQueues);
    }

    public void UnRegisterZezeTables(Zeze.Application zeze) {
        zeze.RemoveTable(zeze.getConfig().GetTableConf(_tQueueNodes.getName()).getDatabaseName(), _tQueueNodes);
        zeze.RemoveTable(zeze.getConfig().GetTableConf(_tQueues.getName()).getDatabaseName(), _tQueues);
    }

    public void RegisterRocksTables(Zeze.Raft.RocksRaft.Rocks rocks) {
    }
}