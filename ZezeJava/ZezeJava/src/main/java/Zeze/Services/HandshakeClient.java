package Zeze.Services;

import Zeze.Application;
import Zeze.Net.AsyncSocket;
import Zeze.Net.Connector;
import Zeze.Net.Protocol;

public class HandshakeClient extends HandshakeBase {
	public HandshakeClient(String name, Zeze.Config config) throws Throwable {
		super(name, config);
		AddHandshakeClientFactoryHandle();
	}

	public HandshakeClient(String name, Application app) throws Throwable {
		super(name, app);
		AddHandshakeClientFactoryHandle();
	}

	public final void Connect(String hostNameOrAddress, int port) {
		Connect(hostNameOrAddress, port, true);
	}

	public final void Connect(String hostNameOrAddress, int port, boolean autoReconnect) {
		var c = new Zeze.Util.OutObject<Connector>();
		getConfig().TryGetOrAddConnector(hostNameOrAddress, port, autoReconnect, c);
		c.Value.Start();
	}

	@Override
	public void OnSocketConnected(AsyncSocket so) {
		// 重载这个方法，推迟OnHandshakeDone调用
		SocketMap.putIfAbsent(so.getSessionId(), so);
		StartHandshake(so);
	}

	@Override
	public <P extends Protocol<?>> void DispatchProtocol(P p, ProtocolFactoryHandle<P> factoryHandle) throws Throwable {
		// 防止Client不进入加密，直接发送用户协议。
		if (!IsHandshakeProtocol(p.getTypeId())) {
			p.getSender().VerifySecurity();
		}

		super.DispatchProtocol(p, factoryHandle);
	}
}