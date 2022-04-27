// auto-generated @formatter:off
package Zeze.Builtin.Online;

import Zeze.Serialize.ByteBuffer;

@SuppressWarnings({"DuplicateBranchesInSwitch", "RedundantSuppression"})
public final class tonline extends Zeze.Transaction.TableX<String, Zeze.Builtin.Online.BOnline> {
    public tonline() {
        super("Zeze_Builtin_Online_tonline");
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
    public static final int VAR_LinkName = 1;
    public static final int VAR_LinkSid = 2;
    public static final int VAR_State = 3;
    public static final int VAR_ReliableNotifyMark = 4;
    public static final int VAR_ReliableNotifyQueue = 5;
    public static final int VAR_ReliableNotifyConfirmCount = 6;
    public static final int VAR_ReliableNotifyTotalCount = 7;
    public static final int VAR_ProviderId = 8;
    public static final int VAR_ProviderSessionId = 9;
    public static final int VAR_LoginVersion = 10;

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
    public Zeze.Builtin.Online.BOnline NewValue() {
        return new Zeze.Builtin.Online.BOnline();
    }

    @Override
    public Zeze.Transaction.ChangeVariableCollector CreateChangeVariableCollector(int variableId) {
        switch (variableId) {
            case 0: return new Zeze.Transaction.ChangeVariableCollectorChanged();
            case 1: return new Zeze.Transaction.ChangeVariableCollectorChanged();
            case 2: return new Zeze.Transaction.ChangeVariableCollectorChanged();
            case 3: return new Zeze.Transaction.ChangeVariableCollectorChanged();
            case 4: return new Zeze.Transaction.ChangeVariableCollectorSet();
            case 5: return new Zeze.Transaction.ChangeVariableCollectorChanged();
            case 6: return new Zeze.Transaction.ChangeVariableCollectorChanged();
            case 7: return new Zeze.Transaction.ChangeVariableCollectorChanged();
            case 8: return new Zeze.Transaction.ChangeVariableCollectorChanged();
            case 9: return new Zeze.Transaction.ChangeVariableCollectorChanged();
            case 10: return new Zeze.Transaction.ChangeVariableCollectorChanged();
            default: return null;
        }
    }
}