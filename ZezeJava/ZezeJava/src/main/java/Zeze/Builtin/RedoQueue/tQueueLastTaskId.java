// auto-generated @formatter:off
package Zeze.Builtin.RedoQueue;

import Zeze.Serialize.ByteBuffer;

@SuppressWarnings({"DuplicateBranchesInSwitch", "RedundantSuppression"})
public final class tQueueLastTaskId extends Zeze.Transaction.TableX<String, Zeze.Builtin.RedoQueue.BTaskId> {
    public tQueueLastTaskId() {
        super("Zeze_Builtin_RedoQueue_tQueueLastTaskId");
    }

    @Override
    public int getId() {
        return -1495051256;
    }

    @Override
    public boolean isMemory() {
        return false;
    }

    @Override
    public boolean isAutoKey() {
        return false;
    }

    public static final int VAR_TaskId = 1;

    @Override
    public String DecodeKey(ByteBuffer _os_) {
        String _v_;
        _v_ = _os_.ReadString();
        return _v_;
    }

    @Override
    public ByteBuffer EncodeKey(String _v_) {
        ByteBuffer _os_ = ByteBuffer.Allocate(16);
        _os_.WriteString(_v_);
        return _os_;
    }

    @Override
    public Zeze.Builtin.RedoQueue.BTaskId NewValue() {
        return new Zeze.Builtin.RedoQueue.BTaskId();
    }
}
