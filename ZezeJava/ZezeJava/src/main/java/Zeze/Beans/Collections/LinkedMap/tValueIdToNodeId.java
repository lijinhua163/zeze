// auto-generated @formatter:off
package Zeze.Beans.Collections.LinkedMap;

import Zeze.Serialize.ByteBuffer;

public final class tValueIdToNodeId extends Zeze.Transaction.TableX<Zeze.Beans.Collections.LinkedMap.BLinkedMapKey, Zeze.Beans.Collections.LinkedMap.BLinkedMapNodeId> {
    public tValueIdToNodeId() {
        super("Zeze_Beans_Collections_LinkedMap_tValueIdToNodeId");
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
    public static final int VAR_NodeId = 1;

    @Override
    public Zeze.Beans.Collections.LinkedMap.BLinkedMapKey DecodeKey(ByteBuffer _os_) {
        Zeze.Beans.Collections.LinkedMap.BLinkedMapKey _v_ = new Zeze.Beans.Collections.LinkedMap.BLinkedMapKey();
        _v_.Decode(_os_);
        return _v_;
    }

    @Override
    public ByteBuffer EncodeKey(Zeze.Beans.Collections.LinkedMap.BLinkedMapKey _v_) {
        ByteBuffer _os_ = ByteBuffer.Allocate(16);
        _v_.Encode(_os_);
        return _os_;
    }

    @Override
    public Zeze.Beans.Collections.LinkedMap.BLinkedMapNodeId NewValue() {
        return new Zeze.Beans.Collections.LinkedMap.BLinkedMapNodeId();
    }

    @Override
    public Zeze.Transaction.ChangeVariableCollector CreateChangeVariableCollector(int variableId) {
        switch (variableId) {
            case 0: return new Zeze.Transaction.ChangeVariableCollectorChanged();
            case 1: return new Zeze.Transaction.ChangeVariableCollectorChanged();
            default: return null;
        }
    }
}