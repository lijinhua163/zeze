package Zeze.Net;

import Zeze.*;

/** 
 连接器：建立并保持一个连接，可以设置自动重连及相关参数。
 可以继承并重载相关事件函数。重载实现里面需要调用 base.OnXXX。
 继承是为了给链接扩充状态，比如：应用的连接需要login，可以维护额外的状态。
 继承类启用方式：
 1. 在配置中通过 class="FullClassName" 的。
 2. 动态创建并加入Service
*/
public class Connector {
	private Service Service;
	public final Service getService() {
		return Service;
	}
	private void setService(Service value) {
		Service = value;
	}

	private String HostNameOrAddress;
	public final String getHostNameOrAddress() {
		return HostNameOrAddress;
	}
	private int Port = 0;
	public final int getPort() {
		return Port;
	}
	private boolean IsAutoReconnect = true;
	public final boolean isAutoReconnect() {
		return IsAutoReconnect;
	}
	public final void setAutoReconnect(boolean value) {
		IsAutoReconnect = value;
	}
	private int MaxReconnectDelay;
	public final int getMaxReconnectDelay() {
		return MaxReconnectDelay;
	}
	public final void setMaxReconnectDelay(int value) {
		MaxReconnectDelay = value;
	}
	private boolean IsConnected = false;
	public final boolean isConnected() {
		return IsConnected;
	}
	private void setConnected(boolean value) {
		IsConnected = value;
	}
	private int ConnectDelay;
	public final boolean isHandshakeDone() {
		return getHandshakeDoneEvent().WaitOne(0);
	}
	private ManualResetEvent HandshakeDoneEvent = new ManualResetEvent(false);
	public final ManualResetEvent getHandshakeDoneEvent() {
		return HandshakeDoneEvent;
	}
	public final String getName() {
		return String.format("%1$s:%2$s", getHostNameOrAddress(), getPort());
	}

	private AsyncSocket Socket;
	public final AsyncSocket getSocket() {
		return Socket;
	}
	private void setSocket(AsyncSocket value) {
		Socket = value;
	}
	private Util.SchedulerTask ReconnectTask;
	public final Util.SchedulerTask getReconnectTask() {
		return ReconnectTask;
	}
	private void setReconnectTask(Util.SchedulerTask value) {
		ReconnectTask = value;
	}


	public Connector(String host, int port) {
		this(host, port, true);
	}

	public Connector(String host) {
		this(host, 0, true);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public Connector(string host, int port = 0, bool autoReconnect = true)
	public Connector(String host, int port, boolean autoReconnect) {
		HostNameOrAddress = host;
		Port = port;
		setAutoReconnect(autoReconnect);
	}

	public static Connector Create(XmlElement e) {
		var className = e.GetAttribute("Class");
		return tangible.StringHelper.isNullOrEmpty(className) ? new Connector(e) : (Connector)System.Activator.CreateInstance(java.lang.Class.forName(className), e);
	}

	public Connector(XmlElement self) {
		String attr = self.GetAttribute("Port");
		if (attr.length() > 0) {
			Port = Integer.parseInt(attr);
		}
		HostNameOrAddress = self.GetAttribute("HostNameOrAddress");
		attr = self.GetAttribute("IsAutoReconnect");
		if (attr.length() > 0) {
			setAutoReconnect(Boolean.parseBoolean(attr));
		}
		attr = self.GetAttribute("MaxReconnectDelay");
		if (attr.length() > 0) {
			setMaxReconnectDelay(Integer.parseInt(attr) * 1000);
		}
		if (getMaxReconnectDelay() < 8000) {
			setMaxReconnectDelay(8000);
		}
	}

	public final void SetService(Service service) {
		synchronized (this) {
			if (getService() != null) {
				throw new RuntimeException(String.format("Connector of '%1$s' Service != null", getName()));
			}
			setService(service);
		}
	}

	// 允许子类重新定义Ready.

	public void WaitReady() {
		WaitReady(5000);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public virtual void WaitReady(int timeout = 5000)
	public void WaitReady(int timeout) {
		if (getHandshakeDoneEvent().WaitOne(timeout)) {
			return;
		}
		throw new RuntimeException(String.format("Connnector.WaitReady fail. %1$s", getName()));
	}

	public void OnSocketClose(AsyncSocket closed) {
		synchronized (this) {
			if (getSocket() != closed) {
				return;
			}
			Stop();
			TryReconnect();
		}
	}

	public void OnSocketConnected(AsyncSocket so) {
		synchronized (this) {
			ConnectDelay = 0;
			setConnected(true);
		}
	}

	public void OnSocketHandshakeDone(AsyncSocket so) {
		getHandshakeDoneEvent().Set();
	}

	public void TryReconnect() {
		synchronized (this) {
			if (false == isAutoReconnect() || null != getSocket() || null != getReconnectTask()) {
				return;
			}

			if (ConnectDelay <= 0) {
				ConnectDelay = 1000;
			}
			else {
				ConnectDelay *= 2;
				if (ConnectDelay > getMaxReconnectDelay()) {
					ConnectDelay = getMaxReconnectDelay();
				}
			}
			setReconnectTask(Util.Scheduler.getInstance().Schedule((ThisTask) -> Start(), ConnectDelay, -1));
			;
		}
	}

	public void Start() {
		synchronized (this) {
			if (getReconnectTask() != null) {
				getReconnectTask().Cancel();
			}
			setReconnectTask(null);

			if (null != getSocket()) {
				return;
			}

			setConnected(false);
			getHandshakeDoneEvent().Reset();
			setSocket(getService().NewClientSocket(getHostNameOrAddress(), getPort()));
			getSocket().setConnector(this);
		}
	}

	public void Stop() {
		synchronized (this) {
			if (null == getSocket()) {
				return;
			}
			getHandshakeDoneEvent().Reset();
			var tmp = getSocket();
			setSocket(null);
			tmp.close();
			setConnected(false);
		}
	}
}