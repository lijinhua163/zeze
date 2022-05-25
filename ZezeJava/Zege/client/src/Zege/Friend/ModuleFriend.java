package Zege.Friend;

import Zeze.Transaction.EmptyBean;
import Zeze.Util.TaskCompletionSource;

public class ModuleFriend extends AbstractModule {
    public void Start(Zege.App app) throws Throwable {
    }

    public void Stop(Zege.App app) throws Throwable {
    }

    public TaskCompletionSource<EmptyBean> add(String account) {
        var req = new AddFriend();
        req.Argument.setAccount(account);
        return req.SendForWait(App.Connector.GetReadySocket());
    }

    public BFriendNode getFriendNode(long nodeId) {
        var req = new GetFriendNode();
        req.Argument.setNodeId(nodeId);
        req.SendForWait(App.Connector.GetReadySocket()).await();
        return req.Result;
    }

    public BMemberNode getMemberNode(long nodeId) {
        var req = new GetGroupMemberNode();
        req.Argument.setNodeId(nodeId);
        req.SendForWait(App.Connector.GetReadySocket()).await();
        return req.Result;
    }

    public BDepartmentMemberNode getDepartmentMemberNode(long nodeId) {
        var req = new GetDepartmentMemberNode();
        req.Argument.setNodeId(nodeId);
        req.SendForWait(App.Connector.GetReadySocket()).await();
        return req.Result;
    }

    // ZEZE_FILE_CHUNK {{{ GEN MODULE @formatter:off
    public ModuleFriend(Zege.App app) {
        super(app);
    }
    // ZEZE_FILE_CHUNK }}} GEN MODULE @formatter:on
}
