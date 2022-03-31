package Zeze.Transaction;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;
import Zeze.Application;
import Zeze.Beans.GlobalCacheManagerWithRaft.GlobalTableKey;
import Zeze.Net.AsyncSocket;
import Zeze.Net.Connector;
import Zeze.Net.Service;
import Zeze.Services.GlobalCacheManager.Acquire;
import Zeze.Services.GlobalCacheManager.Login;
import Zeze.Services.GlobalCacheManager.NormalClose;
import Zeze.Services.GlobalCacheManager.ReLogin;
import Zeze.Services.GlobalCacheManager.Reduce;
import Zeze.Services.GlobalCacheManagerServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class GlobalAgent implements IGlobalAgent {
	private static final Logger logger = LogManager.getLogger(GlobalAgent.class);

	public static final class Agent {
		private static final long FastErrorPeriod = 10 * 1000; // 10 seconds

		private final Connector connector;
		private final AtomicLong LoginTimes = new AtomicLong();
		private final int GlobalCacheManagerHashIndex;
		private boolean ActiveClose;
		private volatile long LastErrorTime;

		public Agent(GlobalClient client, String host, int port, int _GlobalCacheManagerHashIndex) {
			connector = new Zeze.Net.Connector(host, port, true);
			connector.UserState = this;
			GlobalCacheManagerHashIndex = _GlobalCacheManagerHashIndex;
			client.getConfig().AddConnector(connector);
		}

		public AtomicLong getLoginTimes() {
			return LoginTimes;
		}

		public int getGlobalCacheManagerHashIndex() {
			return GlobalCacheManagerHashIndex;
		}

		private void ThrowException(String msg, Throwable cause) {
			var txn = Transaction.getCurrent();
			if (txn != null)
				txn.ThrowAbort(msg, cause);
			throw new RuntimeException(msg, cause);
		}

		public AsyncSocket Connect() {
			try {
				var so = connector.TryGetReadySocket();
				if (so != null)
					return so;

				synchronized (this) {
					if (System.currentTimeMillis() - LastErrorTime < FastErrorPeriod)
						ThrowException("GlobalAgent In FastErrorPeriod", null); // abort
					// else continue
				}

				return connector.WaitReady();
			} catch (Throwable abort) {
				var now = System.currentTimeMillis();
				synchronized (this) {
					if (now - LastErrorTime > FastErrorPeriod)
						LastErrorTime = now;
				}
				ThrowException("GlobalAgent Login Failed", abort);
			}
			return null; // never got here.
		}

		public void Close() {
			try {
				synchronized (this) {
					// 简单保护一下重复主动调用 Close
					if (ActiveClose)
						return;
					ActiveClose = true;
				}
				var ready = connector.TryGetReadySocket();
				if (ready != null)
					new NormalClose().SendForWait(ready).Wait();
			} finally {
				connector.Stop(); // 正常关闭，先设置这个，以后 OnSocketClose 的时候判断做不同的处理。
			}
		}

		public void OnSocketClose(GlobalClient client, Throwable ignoredEx) {
			synchronized (this) {
				if (ActiveClose)
					return; // Connector 的状态在它自己的回调里面处理。
			}

			if (connector.isHandshakeDone()) {
				for (var database : client.getZeze().getDatabases().values()) {
					for (var table : database.getTables())
						table.ReduceInvalidAllLocalOnly(getGlobalCacheManagerHashIndex());
				}
				client.getZeze().CheckpointRun();
			}
		}
	}

	private final Application Zeze;
	private GlobalClient Client;
	public Agent[] Agents;

	public GlobalAgent(Application app) {
		Zeze = app;
	}

	public Application getZeze() {
		return Zeze;
	}

	public GlobalClient getClient() {
		return Client;
	}

	@Override
	public int GetGlobalCacheManagerHashIndex(GlobalTableKey gkey) {
		return gkey.hashCode() % Agents.length;
	}

	@Override
	public void close() {
		try {
			Stop();
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public AcquireResult Acquire(GlobalTableKey gkey, int state) {
		if (Client != null) {
			var agent = Agents[GetGlobalCacheManagerHashIndex(gkey)]; // hash
			var socket = agent.Connect();

			// 请求处理错误抛出异常（比如网络或者GlobalCacheManager已经不存在了），打断外面的事务。
			// 一个请求异常不关闭连接，尝试继续工作。
			var rpc = new Acquire(gkey, state);
			try {
				rpc.SendForWait(socket, 12000).get();
			} catch (InterruptedException | ExecutionException e) {
				var trans = Transaction.getCurrent();
				if (trans == null)
					throw new GoBackZeze("Acquire", e);
				trans.ThrowAbort("Acquire", e);
				// never got here
			}
			/*
			if (rpc.ResultCode != 0) // 这个用来跟踪调试，正常流程使用Result.State检查结果。
			{
			    logger.Warn("Acquire ResultCode={0} {1}", rpc.ResultCode, rpc.Result);
			}
			*/
			if (rpc.getResultCode() == GlobalCacheManagerServer.AcquireModifyFailed
					|| rpc.getResultCode() == GlobalCacheManagerServer.AcquireShareFailed) {
				var trans = Transaction.getCurrent();
				if (trans == null)
					throw new GoBackZeze("GlobalAgent.Acquire Failed");
				trans.ThrowAbort("GlobalAgent.Acquire Failed", null);
				// never got here
			}
			return new AcquireResult(rpc.getResultCode(), rpc.Result.State, rpc.Result.GlobalSerialId);
		}
		logger.debug("Acquire local ++++++");
		return new AcquireResult(0, state, 0);
	}

	public int ProcessReduceRequest(Reduce rpc) {
		switch (rpc.Argument.State) {
		case GlobalCacheManagerServer.StateInvalid:
			var table1 = Zeze.GetTable(rpc.Argument.GlobalTableKey.getTableName());
			if (null == table1) {
				logger.warn("ReduceInvalid Table Not Found={},ServerId={}",
						rpc.Argument.GlobalTableKey.getTableName(), Zeze.getConfig().getServerId());
				// 本地没有找到表格看作成功。
				rpc.Result.GlobalTableKey = rpc.Argument.GlobalTableKey;
				rpc.Result.State = GlobalCacheManagerServer.StateInvalid;
				rpc.SendResultCode(0);
				return 0;
			}
			return table1.ReduceInvalid(rpc);

		case GlobalCacheManagerServer.StateShare:
			var table2 = Zeze.GetTable(rpc.Argument.GlobalTableKey.getTableName());
			if (table2 == null) {
				logger.warn("ReduceShare Table Not Found={},ServerId={}",
						rpc.Argument.GlobalTableKey.getTableName(), Zeze.getConfig().getServerId());
				// 本地没有找到表格看作成功。
				rpc.Result.GlobalTableKey = rpc.Argument.GlobalTableKey;
				rpc.Result.State = GlobalCacheManagerServer.StateInvalid;
				rpc.SendResultCode(0);
				return 0;
			}
			return table2.ReduceShare(rpc);

		default:
			rpc.Result = rpc.Argument;
			rpc.SendResultCode(GlobalCacheManagerServer.ReduceErrorState);
			return 0;
		}
	}

	public synchronized void Start(String[] hostNameOrAddress, int port) throws Throwable {
		if (Client != null)
			return;

		Client = new GlobalClient(this, Zeze);
		Client.AddFactoryHandle(new Reduce().getTypeId(), new Service.ProtocolFactoryHandle<>(
				() -> new Reduce(), this::ProcessReduceRequest, TransactionLevel.None));

		Client.AddFactoryHandle(new Acquire().getTypeId(), new Service.ProtocolFactoryHandle<>(
				() -> new Acquire(), null, TransactionLevel.None));
		Client.AddFactoryHandle(new Login().getTypeId(), new Service.ProtocolFactoryHandle<>(
				() -> new Login(), null, TransactionLevel.None));
		Client.AddFactoryHandle(new ReLogin().getTypeId(), new Service.ProtocolFactoryHandle<>(
				() -> new ReLogin(), null, TransactionLevel.None));
		Client.AddFactoryHandle(new NormalClose().getTypeId(), new Service.ProtocolFactoryHandle<>(
				() -> new NormalClose(), null, TransactionLevel.None));

		Agents = new Agent[hostNameOrAddress.length];
		for (int i = 0; i < hostNameOrAddress.length; i++) {
			var hp = hostNameOrAddress[i].split(":", -1);
			Agents[i] = new Agent(Client, hp[0], hp.length > 1 ? Integer.parseInt(hp[1]) : port, i);
		}

		Client.Start();

		for (var agent : Agents) {
			try {
				agent.Connect();
			} catch (Throwable ex) {
				// 允许部分GlobalCacheManager连接错误时，继续启动程序，虽然后续相关事务都会失败。
				logger.error("GlobalAgent.Connect", ex);
			}
		}
	}

	public synchronized void Stop() throws Throwable {
		if (Client == null)
			return;
		for (var agent : Agents)
			agent.Close();
		Client.Stop();
		Client = null;
	}
}