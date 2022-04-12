// auto-generated @formatter:off
package Zeze.Beans.AutoKey;

import Zeze.Serialize.ByteBuffer;

public final class tAutoKeys extends Zeze.Transaction.TableX<String, Zeze.Beans.AutoKey.BAutoKey> {
    public tAutoKeys() {
        super("Zeze_Beans_AutoKey_tAutoKeys");
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
    public static final int VAR_NextId = 1;

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
    public Zeze.Beans.AutoKey.BAutoKey NewValue() {
        return new Zeze.Beans.AutoKey.BAutoKey();
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
