package Zezex.Linkd;

import Zezex.*;

//ZEZE_FILE_CHUNK {{{ IMPORT GEN
//ZEZE_FILE_CHUNK }}} IMPORT GEN

public final class ModuleLinkd extends AbstractModule {
	public void Start(App app) {
	}

	public void Stop(App app) {
	}

	@Override
	public int ProcessAuthRequest(Zeze.Net.Protocol _rpc) {
		var rpc = (Auth)_rpc;
		/*
		BAccount account = _taccount.Get(protocol.Argument.Account);
		if (null == account || false == account.Token.Equals(protocol.Argument.Token))
		{
		    result.Send(protocol.Sender);
		    return Zeze.Transaction.Procedure.LogicError;
		}

		Game.App.Instance.LinkdService.GetSocket(account.SocketSessionId)?.Dispose(); // kick, 最好发个协议再踢。如果允许多个连接，去掉这行。
		account.SocketSessionId = protocol.Sender.SessionId;
		*/
		var linkSession = (LinkSession)rpc.Sender.getUserState();
		linkSession.setAccount(rpc.Argument.getAccount());
		rpc.SendResultCode(Auth.Success);

		return Zeze.Transaction.Procedure.Success;
	}

	@Override
	public int ProcessKeepAlive(Zeze.Net.Protocol _p) {
		var protocol = (KeepAlive)_p;
		var linkSession = (LinkSession)protocol.Sender.getUserState();
		if (null == linkSession) {
			// handshake 完成之前不可能回收得到 keepalive，先这样处理吧。
			protocol.Sender.Close(null);
			return Zeze.Transaction.Procedure.LogicError;
		}
		linkSession.KeepAlive();
		protocol.Sender.Send(protocol); // send back;
		return Zeze.Transaction.Procedure.Success;
	}

	// ZEZE_FILE_CHUNK {{{ GEN MODULE
    public static final int ModuleId = 10000;


    public Zezex.App App;

    public ModuleLinkd(Zezex.App app) {
        App = app;
        // register protocol factory and handles
        {
            var factoryHandle = new Zeze.Net.Service.ProtocolFactoryHandle();
            factoryHandle.Factory = () -> new Zezex.Linkd.Auth();
            factoryHandle.Handle = (_p) -> ProcessAuthRequest(_p);
            App.LinkdService.AddFactoryHandle(655394483, factoryHandle);
        }
        {
            var factoryHandle = new Zeze.Net.Service.ProtocolFactoryHandle();
            factoryHandle.Factory = () -> new Zezex.Linkd.KeepAlive();
            factoryHandle.Handle = (_p) -> ProcessKeepAlive(_p);
            App.LinkdService.AddFactoryHandle(655406763, factoryHandle);
       }
        // register table
    }

    public void UnRegister() {
        App.LinkdService.getFactorys().remove(655394483);
        App.LinkdService.getFactorys().remove(655406763);
    }
	// ZEZE_FILE_CHUNK }}} GEN MODULE

}