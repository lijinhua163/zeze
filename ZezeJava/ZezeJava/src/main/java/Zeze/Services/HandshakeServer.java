package Zeze.Services;

import Zeze.Application;
import Zeze.Net.AsyncSocket;
import Zeze.Services.Handshake.SHandshake0;

public class HandshakeServer extends HandshakeBase {
	public HandshakeServer(String name, Zeze.Config config) throws Throwable {
		super(name, config);
		AddHandshakeServerFactoryHandle();
	}

	public HandshakeServer(String name, Application app) throws Throwable {
		super(name, app);
		AddHandshakeServerFactoryHandle();
	}

	@Override
	public void OnSocketAccept(AsyncSocket so) throws Throwable {
		// 重载这个方法，推迟OnHandshakeDone调用
		SocketMap.putIfAbsent(so.getSessionId(), so);
		var hand0 = new SHandshake0();
		hand0.Argument.EnableEncrypt = getConfig().getHandshakeOptions().getEnableEncrypt();
		hand0.Send(so);
	}
}
