package demo;

import Benchmark.ABasicSimpleAddOneThread;
import Benchmark.CBasicSimpleAddConcurrent;
import Zeze.Config;

public class App extends Zeze.AppBase {
	public static void main(String[] args) throws Throwable {
		System.err.println(System.getProperties().get("user.dir"));
		Instance.Start();
		int i = 0;
		while (args.length == 0) {
			Thread.sleep(1000);
			var result = Instance.Zeze.NewProcedure(() ->
			{
				Instance.demo_Module1.getTable1().get(1L);
				return 0L;
			}, "Global Access").Call();
			++i;
			System.err.println("" + i + "-" + result);
		}
		Instance.Stop();
	}

	public static App Instance = new App();

	public static App getInstance() {
		return Instance;
	}

	public void Start() throws Throwable {
		Start(Config.Load("./zeze.xml"));
	}

	private void adjustTableConf(Config.TableConf conf) {
		if (null != conf) {
			if (conf.getCacheCapacity() < ABasicSimpleAddOneThread.AddCount)
				conf.setCacheCapacity(ABasicSimpleAddOneThread.AddCount);
			if (conf.getCacheConcurrencyLevel() < CBasicSimpleAddConcurrent.ConcurrentLevel)
				conf.setCacheConcurrencyLevel(CBasicSimpleAddConcurrent.ConcurrentLevel);
		}
	}

	public void Start(Config config) throws Throwable {
		System.setProperty("log4j.configurationFile", "log4j2.xml");
		// 测试本地事务性能需要容量大一点
		adjustTableConf(config.getDefaultTableConf());
		adjustTableConf(config.getTableConfMap().get("demo_Module1_Table1"));

		Create(config);
		Zeze.Start(); // 启动数据库
		StartModules(); // 启动模块，装载配置什么的。
		StartService(); // 启动网络
	}

	public void Stop() throws Throwable {
		StopService(); // 关闭网络
		StopModules(); // 关闭模块，卸载配置什么的。
		Zeze.Stop(); // 关闭数据库
		Destroy();
	}

	// ZEZE_FILE_CHUNK {{{ GEN APP @formatter:off
    public Zeze.Application Zeze;
    public final java.util.HashMap<String, Zeze.IModule> Modules = new java.util.HashMap<>();

    public demo.Server Server;

    public demo.Module1.ModuleModule1 demo_Module1;
    public demo.Module1.Module11.ModuleModule11 demo_Module1_Module11;

    public void Create() throws Throwable {
        Create(null);
    }

    public synchronized void Create(Zeze.Config config) throws Throwable {
        if (Zeze != null)
            return;

        Zeze = new Zeze.Application("demo", config);

        Server = new demo.Server(Zeze);

        demo_Module1 = new demo.Module1.ModuleModule1(this);
        demo_Module1.Initialize(this);
        demo_Module1 = (demo.Module1.ModuleModule1)ReplaceModuleInstance(demo_Module1);
        if (Modules.put(demo_Module1.getFullName(), demo_Module1) != null)
            throw new RuntimeException("duplicate module name: demo_Module1");

        demo_Module1_Module11 = new demo.Module1.Module11.ModuleModule11(this);
        demo_Module1_Module11.Initialize(this);
        demo_Module1_Module11 = (demo.Module1.Module11.ModuleModule11)ReplaceModuleInstance(demo_Module1_Module11);
        if (Modules.put(demo_Module1_Module11.getFullName(), demo_Module1_Module11) != null)
            throw new RuntimeException("duplicate module name: demo_Module1_Module11");

        Zeze.setSchemas(new demo.Schemas());
    }

    public synchronized void Destroy() {
        demo_Module1_Module11 = null;
        demo_Module1 = null;
        Modules.clear();
        Server = null;
        Zeze = null;
    }

    public synchronized void StartModules() throws Throwable {
        demo_Module1.Start(this);
        demo_Module1_Module11.Start(this);
    }

    public synchronized void StopModules() throws Throwable {
        if (demo_Module1_Module11 != null)
            demo_Module1_Module11.Stop(this);
        if (demo_Module1 != null)
            demo_Module1.Stop(this);
    }

    public synchronized void StartService() throws Throwable {
        Server.Start();
    }

    public synchronized void StopService() throws Throwable {
        if (Server != null)
            Server.Stop();
    }
	// ZEZE_FILE_CHUNK }}} GEN APP @formatter:on
}