package Zeze.Transaction;

import Zeze.Application;
import Zeze.Services.AchillesHeelConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 【问题】 Server失联，Global回收记录锁怎么处理？
 * Server与Global之间记录锁管理机制。这里锁有三个状态，Modify,Share,Invalid。
 * 下面分析Server-Global之间所有的交互。
 * <p>
 * 0. Acquire Rpc
 * Server向Global申请记录锁。Release也是通过这个操作处理，Release的锁状态是Invalid。
 * <p>
 * 1. NormalClose Rpc
 * Server正常退出时发送，Global主动释放所有分配到该Server的记录锁。
 * <p>
 * 2. Login Rpc
 * Server初次与Global建立连接时发送。Global会释放该Server上已经分配的记录锁。
 * 一般是Server宕机又重启了。
 * <p>
 * 3. ReLogin Rpc
 * Server与Global之间的连接短暂断开又重新连上时发送。Global简单的把新连接绑定上，不会释放已经分配的锁。
 * <p>
 * 4. KeepAlive Rpc
 * Server空闲时发送给Global。
 * ActiveTime = Acquire或者KeepAlive的活动时间。
 * Global为每个Server都维护ActiveTime。在收到Acquire或者KeepAlive设为now。
 * Server为每个Global都维护ActiveTime。在收到Acquire.Response或者KeepAlive.Response时设为now。
 * Server每秒检查ActiveTime，发现 now - ActiveTime > ServerIdleTimeout 时发送KeepAlive。
 * <p>
 * 5. Global发现Server断开连接
 * 不做任何处理。短暂断开允许重连。锁释放由Global-AchillesHeel-Daemon处理。
 * <p>
 * 6. Global-AchillesHeel-Daemon
 * 每5秒扫描一遍所有Server，发现 now - Server.ActiveTime > GlobalDaemonTimeout，释放该Server所有锁。【Important!】
 * a) 5秒慢检查;如果Server很多，避免轮询消耗太多cpu。慢检查会造成实际回收时间超出超时设置，但不会造成锁状态问题。
 * b) GlobalDaemonTimeout，最终超时。Server必须在这之前释放自己持有的锁或者退出进程；
 * <p>
 * 7. Server-AchillesHeel-Daemon
 * Server每秒扫描一遍Global，发现 now - Global.ActiveTime > ServerDaemonTimeout，启动本地释放锁线程。
 * a) ServerDaemonTimeout需要大于KeepAlive的空闲间隔 + 尝试重连的时间。
 * b) 本地释放锁必须在独立线程执行，守护线程等待释放完成，如果释放线程超过ServerReleaseTimeout还未完成，就自杀！【Important！】
 * c) 守护线程一开始创建，做最简单的事情，确保需要的时候，最终的自杀能成功。【Important！】
 * <p>
 * 8. Timeout
 * a) ServerKeepAliveIdleTimeout < ServerDaemonTimeout;
 * b) ServerDaemonTimeout + ServerReleaseTimeout < GlobalDaemonTimeout; 必须满足而且不能太接近【Important！】
 * c) 其他Timeout：Acquire.Timeout, Reduce.Timeout, KeepAlive.Timeout, Server.FastErrorPeriod, Global.ForbidPeriod
 * <p>
 * 9. Timeout Config
 * a) 在Global配置三个参数：MaxNetPing=1000, ServerProcessTime=500, ServerReleaseTimeout=10*1000,
 * b) 其他Timeout配置全部从上面两个参数按一定比例计算得出。
 * c) Gs不独立配置，Login的时候从Global得到配置。避免由于配置不一致导致问题。
 * d) Global多个实例允许不一样的配置，异构网络里面可能需要。简单起见，最好统一配置。
 * e) ServerReleaseTimeout 默认10秒，这个和Server.Cache.Capacity相关，而且会与应用事务竞争，可能需要长一些。
 * <p>
 * 10. Timeout Compute
 * *) Reconnect.Timer = 1000;
 * a) ServerKeepAliveIdleTimeout = MaxNetPing;
 * b) ServerDaemonTimeout = Reconnect.Timer * 8; // 期间允许8次重连尝试
 * c) ServerReleaseTimeout = 10 * 1000; // From Global
 * d) GlobalDaemonTimeout = ServerDaemonTimeout + ServerReleaseTimeout + MaxNetPing * 2 + 1000;
 * e) Reduce.Timeout = MaxNetPing + ServerProcessTime;
 * f) Acquire.Timeout = Reduce.Timeout + MaxNetPing
 * g) KeepAlive.Timeout = MaxNetPing;
 * h) Server.FastErrorPeriod = ServerDaemonTimeout / 2; // Global请求失败一次即进入这个超时，期间所有的Acquire都本地马上失败。
 * i) Global.ForbidPeriod = ServerDaemonTimeout / 2; // Reduce失败一次即进入这个超时，期间所有的Reduce马上失败。
 * <p>
 * 11. Change Log
 * a) Server在发现Global断开连接，马上释放本地资源。改成由AchillesHeelDaemon处理。
 * b) Global.Cleanup 手动释放锁禁用。
 * <p>
 * 12. Implement
 * 查看相关实现代码: 打开 Zeze.Services.AchillesHeelConfig，在每个配置上查看引用。
 * 下面是配置的使用情况统计。No表示没有用到。
 * ==============================================
 * Server-Implement         RaftAgent NormalAgent
 * ----------------------------------------------
 * *) Reconnect.Timer            No       Yes
 * a) ServerKeepAliveIdleTimeout Yes      Yes  - In Same Place
 * b) ServerDaemonTimeout        Yes      Yes  - In Same Place
 * c) ServerReleaseTimeout       Yes      Yes  - In Same Place
 * f) Acquire.Timeout            No       Yes
 * g) KeepAlive.Timeout          No       Yes
 * h) Server.FastErrorPeriod     No       Yes
 * ==============================================
 * Global-Implement       WithRaft   Normal  Async
 * ----------------------------------------------
 * d) GlobalDaemonTimeout             Yes   Yes
 * e) Reduce.Timeout                  Yes   Yes
 * i) Global.ForbidPeriod             Yes   Yes
 * <p>
 * *. 原来的思路参见 zeze/GlobalCacheManager/Cleanup.txt。在这个基础上增加了KeepAlive。
 */

public class AchillesHeelDaemon {
	private static final Logger logger = LogManager.getLogger(AchillesHeelDaemon.class);
	private final Application Zeze;
	private final ThreadDaemon Daemon;

	public <T extends GlobalAgentBase> AchillesHeelDaemon(Application zeze, T[] agents) {
		Zeze = zeze;
		Daemon = new ThreadDaemon(agents);
	}

	public void stopAndJoin() throws InterruptedException {
		if (null != Daemon) {
			Daemon.Running = false;
			Daemon.join();
		}
	}

	public void start() {
		if (null != Daemon)
			Daemon.start();
	}

	public void setProcessDaemonActiveTime(int index, long value) {
	}

	static class ProcessDaemonMMap {

	}

	class ThreadDaemon extends Thread {
		private final GlobalAgentBase[] Agents;

		public final AchillesHeelConfig getConfig(int index) {
			return Agents[index].getConfig();
		}

		public <T extends GlobalAgentBase> ThreadDaemon(T[] agents) {
			super("AchillesHeelDaemon");
			setDaemon(true);
			Agents = agents.clone();
		}

		private volatile boolean Running = true;

		@Override
		public synchronized void run() {
			try {
				while (Running) {
					var now = System.currentTimeMillis();
					for (int i = 0; i < Agents.length; ++i) {
						var agent = Agents[i];
						var config = agent.getConfig();
						if (null == config)
							continue; // skip agent not login

						var rr = agent.checkReleaseTimeout(now, config.ServerReleaseTimeout);
						if (rr == GlobalAgentBase.CheckReleaseResult.Timeout) {
							logger.fatal("AchillesHeelDaemon global release timeout. index={}", i);
							LogManager.shutdown();
							Runtime.getRuntime().halt(123123);
						}

						var idle = now - agent.getActiveTime();
						if (idle > config.ServerKeepAliveIdleTimeout) {
							//logger.debug("KeepAlive ServerKeepAliveIdleTimeout={}", config.ServerKeepAliveIdleTimeout);
							agent.keepAlive();
						}

						if (idle > config.ServerDaemonTimeout) {
							if (rr != GlobalAgentBase.CheckReleaseResult.Releasing) {
								// 这个判断只能避免正在Releasing时不要启动新的Release。
								// 如果Global一直恢复不了，那么每ServerDaemonTimeout会再次尝试Release，
								// 这里没法快速手段判断本Server是否存在从该Global获取的记录锁。
								// 在Agent中增加获得的计数是个方案，但挺烦的。
								logger.warn("StartRelease ServerDaemonTimeout={}", config.ServerDaemonTimeout);
								agent.startRelease(Zeze, null);
							}
						}
					}
					try {
						//noinspection BusyWait
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						logger.warn("", e);
					}
				}
			} catch (Throwable ex) {
				// 这个线程不准出错。
				logger.fatal("AchillesHeelDaemon ", ex);
				LogManager.shutdown();
				Runtime.getRuntime().halt(321321);
			}
		}
	}
}
