// auto-generated @formatter:off
public final class Redirect_Zeze_Game_Online extends Zeze.Game.Online {
    private final Zeze.Arch.RedirectBase _redirect_;

    @Override
    protected Zeze.Arch.RedirectFuture<Long> redirectNotify(int arg0, long arg1) {
        var _t_ = _redirect_.ChoiceServer(this, arg0);
        if (_t_ == null) { // local: loop-back
            return _redirect_.RunFuture(Zeze.Transaction.TransactionLevel.Serializable,
                () -> super.redirectNotify(arg0, arg1));
        }

        var _p_ = new Zeze.Builtin.ProviderDirect.ModuleRedirect();
        var _a_ = _p_.Argument;
        _a_.setModuleId(11013);
        _a_.setRedirectType(Zeze.Builtin.ProviderDirect.ModuleRedirect.RedirectTypeToServer);
        _a_.setHashCode(arg0);
        _a_.setMethodFullName("Zeze.Game.Online:redirectNotify");
        _a_.setServiceNamePrefix(_redirect_.ProviderApp.ServerServiceNamePrefix);
        var _b_ = Zeze.Serialize.ByteBuffer.Allocate();
        _b_.WriteLong(arg1);
        _a_.setParams(new Zeze.Net.Binary(_b_));

        var _f_ = new Zeze.Arch.RedirectFuture<Long>();
        _p_.Send(_t_, _rpc_ -> {
            _f_.SetResult(_rpc_.isTimeout() ? Zeze.Transaction.Procedure.Timeout : _rpc_.getResultCode());
            return Zeze.Transaction.Procedure.Success;
        });
        return _f_;
    }

    public Redirect_Zeze_Game_Online(Zeze.AppBase _app_) {
        super(_app_);
        _redirect_ = _app_.getZeze().Redirect;

        _app_.getZeze().Redirect.Handles.put("Zeze.Game.Online:redirectNotify", new Zeze.Arch.RedirectHandle(
            Zeze.Transaction.TransactionLevel.Serializable, (_hash_, _params_) -> {
                long arg1;
                var _b_ = _params_.Wrap();
                arg1 = _b_.ReadLong();
                return super.redirectNotify(_hash_, arg1);
            }, _result_ -> Zeze.Net.Binary.Empty));
    }
}
