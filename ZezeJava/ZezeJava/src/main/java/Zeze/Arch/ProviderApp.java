package Zeze.Arch;

import java.util.HashMap;
import Zeze.Builtin.Provider.BLoad;
import Zeze.Builtin.Provider.BModule;
import Zeze.Util.IntHashMap;

/**
 * 记录实现一个Provider需要的对象，
 * 设置相关对象之间的引用，
 * 初始化。
 */
public class ProviderApp {
	public final Zeze.Application Zeze;

	public final ProviderImplement ProviderImplement;
	public final ProviderService ProviderService;
	public final String ServerServiceNamePrefix;

	public final ProviderDirect ProviderDirect;
	public final ProviderDirectService ProviderDirectService;

	public final String LinkdServiceName;

	// 现在内部可以自动设置两个参数，但有点不够可靠，生产环境最好手动设置。
	public final String DirectIp;
	public final int DirectPort;

	public final ProviderDistribute Distribute;

	public final IntHashMap<BModule> StaticBinds = new IntHashMap<>();
	public final IntHashMap<BModule> DynamicModules = new IntHashMap<>();
	public final IntHashMap<BModule> Modules = new IntHashMap<>();
	public final HashMap<String, Zeze.IModule> BuiltinModules = new HashMap<>();

	public ProviderApp(Zeze.Application zeze,
					   ProviderImplement server,
					   ProviderService toLinkdService,
					   String providerModulePrefixNameOnServiceManager,
					   ProviderDirect direct,
					   ProviderDirectService toOtherProviderService,
					   String linkdNameOnServiceManager,
					   LoadConfig loadConfig
					   ) {
		this.Zeze = zeze;
		this.Zeze.Redirect = new RedirectBase(this);

		this.ProviderImplement = server;
		this.ProviderImplement.ProviderApp = this;
		this.ProviderService = toLinkdService;
		this.ProviderService.ProviderApp = this;
		this.ServerServiceNamePrefix = providerModulePrefixNameOnServiceManager;

		this.ProviderDirect = direct;
		this.ProviderDirect.ProviderApp = this;
		this.ProviderDirectService = toOtherProviderService;
		this.ProviderDirectService.ProviderApp = this;

		var kv = ProviderDirectService.GetOnePassiveAddress();
		this.DirectIp = kv.getKey();
		this.DirectPort = kv.getValue();

		this.LinkdServiceName = linkdNameOnServiceManager;

		this.ProviderImplement.RegisterProtocols(ProviderService);

		Zeze.getServiceManagerAgent().setOnSetServerLoad((serverLoad) -> {
			var ps = ProviderDirectService.ProviderByLoadName.get(serverLoad.getName());
			if (ps != null) {
				var load = new BLoad();
				load.Decode(serverLoad.Param.Wrap());
				ps.Load = load;
			}
		});

		this.Distribute = new ProviderDistribute();
		this.Distribute.LoadConfig = loadConfig;
		this.Distribute.Zeze = Zeze;
		this.Distribute.ProviderService = ProviderDirectService;

		this.Zeze.getServiceManagerAgent().setOnChanged((ss) -> {
			ProviderImplement.ApplyOnChanged(ss);
			Distribute.ApplyServers(ss);
		});
		this.Zeze.getServiceManagerAgent().setOnPrepare(ProviderImplement::ApplyOnPrepare);
		this.Zeze.getServiceManagerAgent().setOnUpdate((ss, si) -> {
			Distribute.AddServer(ss, si);
			ProviderDirectService.AddServer(ss, si);
		});
		this.Zeze.getServiceManagerAgent().setOnRemoved((ss, si) -> {
			Distribute.RemoveServer(ss, si);
			ProviderDirectService.RemoveServer(ss, si);
		});

		this.ProviderDirect.RegisterProtocols(ProviderDirectService);
	}

	public void StartLast(ProviderModuleBinds binds, HashMap<String, Zeze.IModule> modules) {
		for (var builtin : BuiltinModules.values())
			modules.put(builtin.getFullName(), builtin);
		binds.BuildStaticBinds(modules, Zeze.getConfig().getServerId(), StaticBinds);
		binds.BuildDynamicBinds(modules, Zeze.getConfig().getServerId(), DynamicModules);
		Modules.putAll(StaticBinds);
		Modules.putAll(DynamicModules);
		ProviderImplement.RegisterModulesAndSubscribeLinkd();
	}
}
