package Game;

import Zeze.Net.*;
import Zeze.Transaction.TransactionLevel;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.concurrent.ConcurrentHashMap;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import Zeze.Util.TaskCompletionSource;

// auto-generated


public final class Server extends ServerBase {
	private static final Logger logger = LogManager.getLogger(Server.class);

	/**
	 不使用 RemoteEndPoint 是怕有些系统返回 ipv6 有些 ipv4，造成不一致。
	 这里要求 linkName 在所有 provider 中都一样。
	 使用 Connector 配置得到名字，只要保证配置一样。
	*/
	public String GetLinkName(AsyncSocket sender) {
		return sender.getConnector().getName();
	}

	public String GetLinkName(Zeze.Services.ServiceManager.ServiceInfo serviceInfo) {
		return serviceInfo.getPassiveIp() + ":" + serviceInfo.getPassivePort();
	}

	@Override
	public void Start() throws Throwable {
		// copy Config.Connector to Links
		getConfig().ForEachConnector((c) -> getLinks().putIfAbsent(c.getName(), c));
		super.Start();
	}

	public void ApplyLinksChanged(Zeze.Services.ServiceManager.ServiceInfos serviceInfos) {
		HashSet<String> current = new HashSet<>();
		for (var link : serviceInfos.getServiceInfoListSortedByIdentity()) {
			var linkName = GetLinkName(link);
			current.add(getLinks().computeIfAbsent(linkName, (key) -> {
				var outc = new Zeze.Util.OutObject<Connector>();
					if (getConfig().TryGetOrAddConnector(link.getPassiveIp(), link.getPassivePort(), true, outc)) {
						try {
							outc.Value.Start();
						} catch (Throwable e) {
							logger.error("", e);
							return null;
						}
					}
					return outc.Value;
			}).getName());
		}
		// 删除多余的连接器。
		for (var linkName : getLinks().keySet()) {
			if (current.contains(linkName)) {
				continue;
			}
			var removed = getLinks().remove(linkName);
			if (null != removed) {
				getConfig().RemoveConnector(removed);
				removed.Stop();
			}
		}
		LinkConnectors = Links.values().toArray(new Connector[Links.size()]);
	}

	public static class LinkSession {
		private final String Name;
		public final String getName() {
			return Name;
		}
		private final long SessionId;
		public final long getSessionId() {
			return SessionId;
		}

		// 在和linkd连接建立完成以后，由linkd发送通告协议时保存。
		private int LinkId;
		public final int getLinkId() {
			return LinkId;
		}
		private void setLinkId(int value) {
			LinkId = value;
		}
		private long ProviderSessionId;
		public final long getProviderSessionId() {
			return ProviderSessionId;
		}
		private void setProviderSessionId(long value) {
			ProviderSessionId = value;
		}

		public LinkSession(String name, long sid) {
			Name = name;
			SessionId = sid;
		}

		public final void Setup(int linkId, long providerSessionId) {
			setLinkId(linkId);
			setProviderSessionId(providerSessionId);
		}
	}

	private final ConcurrentHashMap<String, Connector> Links = new ConcurrentHashMap<>();
	private volatile Connector[] LinkConnectors = Links.values().toArray(new Connector[Links.size()]);
	private final AtomicInteger LinkRandomIndex = new AtomicInteger();
	public ConcurrentHashMap<String, Connector> getLinks() {
		return Links;
	}

	public AsyncSocket RandomLink() {
		var volatileTmp = LinkConnectors;
		if (volatileTmp.length == 0)
			return null;

		var index = LinkRandomIndex.getAndIncrement();
		var connector = volatileTmp[Integer.remainderUnsigned(index, volatileTmp.length)];
		// 如果只选择已经连上的Link，当所有的连接都没准备好时，仍然需要GetReadySocket，
		// 所以简单处理成总是等待连接完成。
		return connector.GetReadySocket();
	}

	// 用来同步等待Provider的静态绑定完成。
	public TaskCompletionSource<Boolean> ProviderStaticBindCompleted = new TaskCompletionSource<>();
	public TaskCompletionSource<Boolean> ProviderDynamicSubscribeCompleted = new TaskCompletionSource<>();

	@Override
	public void OnHandshakeDone(AsyncSocket sender) throws Throwable {
		super.OnHandshakeDone(sender);
		var linkName = GetLinkName(sender);
		sender.setUserState(new LinkSession(linkName, sender.getSessionId()));

		var announce = new Zezex.Provider.AnnounceProviderInfo();
		announce.Argument.setServiceNamePrefix(App.ServerServiceNamePrefix);
		announce.Argument.setServiceIndentity(String.valueOf(getZeze().getConfig().getServerId()));
		announce.Send(sender);

		// static binds
		var rpc = new Zezex.Provider.Bind();
		rpc.Argument.getModules().putAll(Game.App.getInstance().getStaticBinds());
		rpc.Send(sender, (protocol) -> {
				ProviderStaticBindCompleted.SetResult(true);
				return 0;
		});
		var sub = new Zezex.Provider.Subscribe();
		sub.Argument.getModules().putAll(Game.App.getInstance().getDynamicModules());
		sub.Send(sender, (protocol) -> {
			ProviderDynamicSubscribeCompleted.SetResult(true);
			return 0;
		});
	}

	@Override
	public <P extends Protocol<?>> void DispatchProtocol(P p, ProtocolFactoryHandle<P> factoryHandle) throws Throwable {
		// 防止Client不进入加密，直接发送用户协议。
		if (!IsHandshakeProtocol(p.getTypeId())) {
			p.getSender().VerifySecurity();
		}

		if (p.getTypeId() == Zezex.Provider.ModuleRedirect.TypeId_) {
			if (null != factoryHandle.Handle) {
				var modureRecirect = (Zezex.Provider.ModuleRedirect)p;
				if (null != getZeze()) {
					if (factoryHandle.Level != TransactionLevel.None) {
						getZeze().getTaskOneByOneByKey().Execute(
								modureRecirect.Argument.getHashCode(),
								() -> Zeze.Util.Task.Call(getZeze().NewProcedure(() -> factoryHandle.Handle.handle(p),
												p.getClass().getName(), factoryHandle.Level, p.getUserState()),
										p, Protocol::SendResultCode));
					} else {
						getZeze().getTaskOneByOneByKey().Execute(modureRecirect.Argument.getHashCode(),
								() -> Zeze.Util.Task.Call(() -> factoryHandle.Handle.handle(p), p,
										Protocol::SendResultCode));
					}
				}
			} else
				logger.warn("Protocol Handle Not Found: {}", p);
			return;
		}

		super.DispatchProtocol(p, factoryHandle);
	}

	public void ReportLoad(int online, int proposeMaxOnline, int onlineNew) {
		var report = new Zezex.Provider.ReportLoad();

		report.Argument.setOnline(online);
		report.Argument.setProposeMaxOnline(proposeMaxOnline);
		report.Argument.setOnlineNew(onlineNew);

		for (var link : getLinks().values()) {
			if (link.isHandshakeDone()) {
				link.getSocket().Send(report);
			}
		}
	}

	public Server(Zeze.Application zeze) throws Throwable {
		super(zeze);
	}
}