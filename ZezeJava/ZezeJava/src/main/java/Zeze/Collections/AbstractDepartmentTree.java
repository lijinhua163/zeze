// auto-generated @formatter:off
package Zeze.Collections;

public abstract class AbstractDepartmentTree extends Zeze.IModule {
    public static final int ModuleId = 11101;
    @Override public String getFullName() { return "Zeze.Collections.DepartmentTree"; }
    @Override public String getName() { return "DepartmentTree"; }
    @Override public int getId() { return ModuleId; }
    @Override public boolean isBuiltin() { return true; }

    public static final int ErrorChangeRootNotOwner = 1;
    public static final int ErrorDepartmentDuplicate = 2;
    public static final int ErrorDepartmentNotExist = 3;
    public static final int ErrorDeleteDepartmentRemainChilds = 4;
    public static final int ErrorDepartmentSameParent = 5;
    public static final int ErrorCanNotMoveToChilds = 6;
    public static final int ErrorDepartmentParentNotExist = 7;

    protected final Zeze.Builtin.Collections.DepartmentTree.tDepartment _tDepartment = new Zeze.Builtin.Collections.DepartmentTree.tDepartment();
    protected final Zeze.Builtin.Collections.DepartmentTree.tDepartmentTree _tDepartmentTree = new Zeze.Builtin.Collections.DepartmentTree.tDepartmentTree();

    public void RegisterProtocols(Zeze.Net.Service service) {
    }

    public void UnRegisterProtocols(Zeze.Net.Service service) {
    }

    public void RegisterZezeTables(Zeze.Application zeze) {
        zeze.AddTable(zeze.getConfig().GetTableConf(_tDepartment.getName()).getDatabaseName(), _tDepartment);
        zeze.AddTable(zeze.getConfig().GetTableConf(_tDepartmentTree.getName()).getDatabaseName(), _tDepartmentTree);
    }

    public void UnRegisterZezeTables(Zeze.Application zeze) {
        zeze.RemoveTable(zeze.getConfig().GetTableConf(_tDepartment.getName()).getDatabaseName(), _tDepartment);
        zeze.RemoveTable(zeze.getConfig().GetTableConf(_tDepartmentTree.getName()).getDatabaseName(), _tDepartmentTree);
    }

    public void RegisterRocksTables(Zeze.Raft.RocksRaft.Rocks rocks) {
    }
}
