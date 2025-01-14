// auto-generated @formatter:off
package Zeze.Builtin.Collections.LinkedMap;

import Zeze.Serialize.ByteBuffer;

@SuppressWarnings({"DuplicateBranchesInSwitch", "RedundantSuppression"})
public final class tLinkedMapNodes extends Zeze.Transaction.TableX<Zeze.Builtin.Collections.LinkedMap.BLinkedMapNodeKey, Zeze.Builtin.Collections.LinkedMap.BLinkedMapNode> {
    public tLinkedMapNodes() {
        super("Zeze_Builtin_Collections_LinkedMap_tLinkedMapNodes");
    }

    @Override
    public int getId() {
        return -1295098614;
    }

    @Override
    public boolean isMemory() {
        return false;
    }

    @Override
    public boolean isAutoKey() {
        return false;
    }

    public static final int VAR_PrevNodeId = 1;
    public static final int VAR_NextNodeId = 2;
    public static final int VAR_Values = 3;

    @Override
    public Zeze.Builtin.Collections.LinkedMap.BLinkedMapNodeKey DecodeKey(ByteBuffer _os_) {
        Zeze.Builtin.Collections.LinkedMap.BLinkedMapNodeKey _v_ = new Zeze.Builtin.Collections.LinkedMap.BLinkedMapNodeKey();
        _v_.Decode(_os_);
        return _v_;
    }

    @Override
    public ByteBuffer EncodeKey(Zeze.Builtin.Collections.LinkedMap.BLinkedMapNodeKey _v_) {
        ByteBuffer _os_ = ByteBuffer.Allocate(16);
        _v_.Encode(_os_);
        return _os_;
    }

    @Override
    public Zeze.Builtin.Collections.LinkedMap.BLinkedMapNode NewValue() {
        return new Zeze.Builtin.Collections.LinkedMap.BLinkedMapNode();
    }
}
