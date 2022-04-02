package Zeze.Arch;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import Zeze.Services.ServiceManager.Agent;

public class ProviderDistribute {
	public Zeze.Application zeze;
	public LinkdConfig LinkdConfig;
	public Zeze.Net.Service ProviderService;

	public String MakeServiceName(String serviceNamePrefix, int moduleId) {
		return serviceNamePrefix + moduleId;
	}

	public boolean ChoiceHash(Agent.SubscribeState providers, int hash, Zeze.Util.OutObject<Long> provider) {
		provider.Value = 0L;

		var list = providers.getServiceInfos().getServiceInfoListSortedByIdentity();
		if (list.size() == 0) {
			return false;
		}

		Object tempVar = list.get(Integer.remainderUnsigned(hash, list.size())).getLocalState();
		var providerModuleState = (ProviderModuleState)tempVar;
		if (null == providerModuleState) {
			return false;
		}

		provider.Value = providerModuleState.SessionId;
		return true;
	}

	public boolean ChoiceLoad(Agent.SubscribeState providers, Zeze.Util.OutObject<Long> provider) {
		provider.Value = 0L;

		var list = providers.getServiceInfos().getServiceInfoListSortedByIdentity();
		var frees = new ArrayList<ProviderSession>(list.size());
		var all = new ArrayList<ProviderSession>(list.size());
		int TotalWeight = 0;

		// 新的provider在后面，从后面开始搜索。后面的可能是新的provider。
		for (int i = list.size() - 1; i >= 0; --i) {
			var providerModuleState = (ProviderModuleState)list.get(i).getLocalState();
			if (null == providerModuleState) {
				continue;
			}
			// Object tempVar2 = App.ProviderService.GetSocket(providerModuleState.getSessionId()).getUserState();
			var ps = (ProviderSession)ProviderService.GetSocket(providerModuleState.SessionId).getUserState();
			if (null == ps) {
				continue; // 这里发现关闭的服务，仅仅忽略.
			}
			all.add(ps);
			if (ps.getOnlineNew() > LinkdConfig.getMaxOnlineNew()) {
				continue;
			}
			int weight = ps.getProposeMaxOnline() - ps.getOnline();
			if (weight <= 0) {
				continue;
			}
			frees.add(ps);
			TotalWeight += weight;
		}
		if (TotalWeight > 0) {
			int randweight = Zeze.Util.Random.getInstance().nextInt(TotalWeight);
			for (var ps : frees) {
				int weight = ps.getProposeMaxOnline() - ps.getOnline();
				if (randweight < weight) {
					provider.Value = ps.getSessionId();
					return true;
				}
				randweight -= weight;
			}
		}
		// 选择失败，一般是都满载了，随机选择一个。
		if (!all.isEmpty()) {
			provider.Value = all.get(Zeze.Util.Random.getInstance().nextInt(all.size())).getSessionId();
			return true;
		}
		// no providers
		return false;
	}

	private final AtomicInteger FeedFullOneByOneIndex = new AtomicInteger();

	public boolean ChoiceFeedFullOneByOne(Agent.SubscribeState providers, Zeze.Util.OutObject<Long> provider) {
		// 查找时增加索引，和喂饱时增加索引，需要原子化。提高并发以后慢慢想，这里应该足够快了。
		synchronized (this) {
			provider.Value = 0L;

			var list = providers.getServiceInfos().getServiceInfoListSortedByIdentity();
			// 最多遍历一次。循环里面 continue 时，需要递增索引。
			for (int i = 0; i < list.size(); ++i, FeedFullOneByOneIndex.incrementAndGet()) {
				var index = Integer.remainderUnsigned(FeedFullOneByOneIndex.get(), list.size()); // current
				var serviceinfo = list.get(index);
				var providerModuleState = (ProviderModuleState)serviceinfo.getLocalState();
				if (providerModuleState == null)
					continue;
				var providerSocket = ProviderService.GetSocket(providerModuleState.SessionId);
				if (null == providerSocket)
					continue;
				var ps = (LinkdProviderSession)providerSocket.getUserState();
				// 这里发现关闭的服务，仅仅忽略.
				if (null == ps)
					continue;
				// 这个和一个一个喂饱冲突，但是一下子给一个服务分配太多用户，可能超载。如果不想让这个生效，把MaxOnlineNew设置的很大。
				if (ps.getOnlineNew() > LinkdConfig.getMaxOnlineNew())
					continue;

				provider.Value = ps.getSessionId();
				if (ps.getOnline() >= ps.getProposeMaxOnline())
					FeedFullOneByOneIndex.incrementAndGet(); // 已经喂饱了一个，下一个。
				return true;
			}
			return false;
		}
	}

	public boolean ChoiceProvider(String serviceNamePrefix, int moduleId, int hash, Zeze.Util.OutObject<Long> provider) {
		var serviceName = MakeServiceName(serviceNamePrefix, moduleId);

		var volatileProviders = zeze.getServiceManagerAgent().getSubscribeStates().get(serviceName);
		if (null == volatileProviders) {
			provider.Value = 0L;
			return false;
		}
		return ChoiceHash(volatileProviders, hash, provider);
	}

	public boolean ChoiceProviderByServerId(String serviceNamePrefix, int moduleId, int serverId, Zeze.Util.OutObject<Long> provider) {
		var serviceName = MakeServiceName(serviceNamePrefix, moduleId);

		var volatileProviders = zeze.getServiceManagerAgent().getSubscribeStates().get(serviceName);
		if (null == volatileProviders) {
			provider.Value = 0L;
			return false;
		}
		var si = volatileProviders.getServiceInfos().findServiceInfoByServerId(serverId);
		if (null != si) {
			var state = (ProviderModuleState)si.getLocalState();
			provider.Value = state.SessionId;
			return true;
		}
		return false;
	}

}
