// auto-generated @formatter:off
package Zeze.Builtin.Online;

import Zeze.Serialize.ByteBuffer;

@SuppressWarnings({"DuplicateBranchesInSwitch", "RedundantSuppression"})
public final class tlocal extends Zeze.Transaction.TableX<String, Zeze.Builtin.Online.BLocal> {
    public tlocal() {
        super("Zeze_Builtin_Online_tlocal");
    }

    @Override
    public boolean isMemory() {
        return true;
    }

    @Override
    public boolean isAutoKey() {
        return false;
    }

    public static final int VAR_All = 0;
    public static final int VAR_LoginVersion = 1;
    public static final int VAR_Datas = 2;

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
    public Zeze.Builtin.Online.BLocal NewValue() {
        return new Zeze.Builtin.Online.BLocal();
    }

    @Override
    public Zeze.Transaction.ChangeVariableCollector CreateChangeVariableCollector(int variableId) {
        switch (variableId) {
            case 0: return new Zeze.Transaction.ChangeVariableCollectorChanged();
            case 1: return new Zeze.Transaction.ChangeVariableCollectorChanged();
            case 2: return new Zeze.Transaction.ChangeVariableCollectorMap(() -> new Zeze.Transaction.ChangeNoteMap2<String, Zeze.Builtin.Online.BAny>(null));
            default: return null;
        }
    }
}