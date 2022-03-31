// auto-generated @formatter:off
package Zeze.Beans.Collections.Queue;

import Zeze.Serialize.ByteBuffer;

public final class tQueues extends Zeze.Transaction.TableX<String, Zeze.Beans.Collections.Queue.BQueue> {
    public tQueues() {
        super("Zeze_Beans_Collections_Queue_tQueues");
    }

    @Override
    public boolean isMemory() {
        return false;
    }

    @Override
    public boolean isAutoKey() {
        return false;
    }

    public static final int VAR_All = 0;
    public static final int VAR_HeadNodeId = 1;
    public static final int VAR_TailNodeId = 2;
    public static final int VAR_Count = 3;
    public static final int VAR_LastNodeId = 4;

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
    public Zeze.Beans.Collections.Queue.BQueue NewValue() {
        return new Zeze.Beans.Collections.Queue.BQueue();
    }

    @Override
    public Zeze.Transaction.ChangeVariableCollector CreateChangeVariableCollector(int variableId) {
        switch (variableId) {
            case 0: return new Zeze.Transaction.ChangeVariableCollectorChanged();
            case 1: return new Zeze.Transaction.ChangeVariableCollectorChanged();
            case 2: return new Zeze.Transaction.ChangeVariableCollectorChanged();
            case 3: return new Zeze.Transaction.ChangeVariableCollectorChanged();
            case 4: return new Zeze.Transaction.ChangeVariableCollectorChanged();
            default: return null;
        }
    }
}