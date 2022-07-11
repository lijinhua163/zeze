package Zeze.Arch;

import Zeze.Builtin.Provider.Send;
import Zeze.Net.AsyncSocket;
import Zeze.Net.Binary;
import Zeze.Net.Protocol;
import Zeze.Net.Rpc;
import Zeze.Serialize.ByteBuffer;
import Zeze.Transaction.Transaction;

/**
 * 用户登录会话。
 * 记录账号，roleId，LinkName，SessionId等信息。
 */
public class ProviderUserSession {
	private final ProviderService service;
	private final String Account;
	private final String Context;
	private final long LinkSid;
	private final String LinkName;
	private AsyncSocket Link;

	public ProviderUserSession(ProviderService service, String account, String context, AsyncSocket link, long linkSid) {
		this.service = service;
		Account = account;
		Context = context;
		LinkSid = linkSid;
		LinkName = service.GetLinkName(link);
		Link = link;
	}

	public void kick(int code, String desc) {
		ProviderImplement.SendKick(Link, LinkSid, code, desc);
	}

	public final ProviderService getService() {
		return service;
	}

	public final String getAccount() {
		return Account;
	}
	public final String getContext() { return Context; }
	public final boolean isLogin() { return null == Context || Context.isEmpty(); }
	public final Long getRoleId() {
		return Context.isEmpty() ? null : Long.parseLong(Context);
	}

	public final long getLinkSid() {
		return LinkSid;
	}

	public final String getLinkName() {
		return LinkName;
	}

	public final AsyncSocket getLink() {
		return Link;
	}

	public final void setLink(AsyncSocket value) {
		Link = value;
	}

	public final void sendResponse(Binary fullEncodedProtocol) {
		sendResponse(ByteBuffer.Wrap(fullEncodedProtocol).ReadInt4(), fullEncodedProtocol);
	}

	public final void sendResponse(long typeId, Binary fullEncodedProtocol) {
		var send = new Send();
		send.Argument.getLinkSids().add(getLinkSid());
		send.Argument.setProtocolType(typeId);
		send.Argument.setProtocolWholeData(fullEncodedProtocol);

		if (null != getLink() && !getLink().isClosed()) {
			getLink().Send(send.Encode()); // 调用Encode后的方法避免重复协议日志
			return;
		}
		// 可能发生了重连，尝试再次查找发送。网络断开以后，已经不可靠了，先这样写着吧。
		var link = service.getLinks().get(getLinkName());
		if (null != link) {
			if (link.isHandshakeDone()) {
				setLink(link.getSocket());
				link.getSocket().Send(send.Encode()); // 调用Encode后的方法避免重复协议日志
			}
		}
	}

	public final void sendResponse(Protocol<?> p) {
		p.setRequest(false);
		if (AsyncSocket.ENABLE_PROTOCOL_LOG) {
			if (p.isRequest()) {
				if (p instanceof Rpc)
					AsyncSocket.logger.log(AsyncSocket.LEVEL_PROTOCOL_LOG, "RESP[{}] {}({}): {}", LinkSid,
							p.getClass().getSimpleName(), ((Rpc<?, ?>)p).getSessionId(), p.Argument);
				else
					AsyncSocket.logger.log(AsyncSocket.LEVEL_PROTOCOL_LOG, "RESP[{}] {}: {}", LinkSid,
							p.getClass().getSimpleName(), p.Argument);
			} else
				AsyncSocket.logger.log(AsyncSocket.LEVEL_PROTOCOL_LOG, "RESP[{}] {}({})> {}", LinkSid,
						p.getClass().getSimpleName(), ((Rpc<?, ?>)p).getSessionId(), p.getResultBean());
		}
		sendResponse(p.getTypeId(), new Binary(p.Encode()));
	}

	@SuppressWarnings("ConstantConditions")
	public final void sendResponseWhileCommit(int typeId, Binary fullEncodedProtocol) {
		Transaction.getCurrent().runWhileCommit(() -> sendResponse(typeId, fullEncodedProtocol));
	}

	@SuppressWarnings("ConstantConditions")
	public final void sendResponseWhileCommit(Binary fullEncodedProtocol) {
		Transaction.getCurrent().runWhileCommit(() -> sendResponse(fullEncodedProtocol));
	}

	@SuppressWarnings("ConstantConditions")
	public final void sendResponseWhileCommit(Protocol<?> p) {
		Transaction.getCurrent().runWhileCommit(() -> sendResponse(p));
	}

	// 这个方法用来优化广播协议。不能用于Rpc，先隐藏。
	@SuppressWarnings({"ConstantConditions", "unused"})
	private void sendResponseWhileRollback(int typeId, Binary fullEncodedProtocol) {
		Transaction.getCurrent().runWhileCommit(() -> sendResponse(typeId, fullEncodedProtocol));
	}

	@SuppressWarnings({"ConstantConditions", "unused"})
	private void sendResponseWhileRollback(Binary fullEncodedProtocol) {
		Transaction.getCurrent().runWhileCommit(() -> sendResponse(fullEncodedProtocol));
	}

	@SuppressWarnings("ConstantConditions")
	public final void sendResponseWhileRollback(Protocol<?> p) {
		Transaction.getCurrent().runWhileCommit(() -> sendResponse(p));
	}

	public static ProviderUserSession get(Protocol<?> context) {
		if (null == context.getUserState()) {
			throw new RuntimeException("not auth");
		}
		return (ProviderUserSession)context.getUserState();
	}
}
