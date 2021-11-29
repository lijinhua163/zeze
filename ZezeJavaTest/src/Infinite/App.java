package Infinite;

import UnitTest.Zeze.Trans.TestGlobal;
import Zeze.Config;
import Zeze.Util.Task;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;

public class App {
    static final Logger logger = LogManager.getLogger(TestGlobal.class);

    demo.App app;
    Config config;

    public final static int CacheCapacity = 1000;
    public final static int AccessKeyBound = (int)(CacheCapacity * 1.20f);

    public App(int serverId) {
        config = Config.Load("zeze.xml");
        config.setServerId(serverId);
        var tdef = config.getDefaultTableConf();
        tdef.setCacheCleanPeriod(1000); // 提高并发
        tdef.setCacheCleanPeriodWhenExceedCapacity(0); // 超出容量时，快速尝试。
        tdef.setCacheCapacity(CacheCapacity); // 减少容量，实际使用记录数要超过一些。让TableCache.Cleanup能并发起来。
        app = new demo.App();
    }

    public void Start() throws Throwable {
        app.Start(config);
    }

    public void Stop() throws Throwable {
        app.Stop();
    }

    public ArrayList<Task> RunningTasks = new ArrayList<>(Simulate.BatchTaskCount);

    public void Run(Tasks.Task task) {
        task.App = app;
        task.Key = Zeze.Util.Random.getInstance().nextInt(AccessKeyBound);
        RunningTasks.add(Zeze.Util.Task.Run(app.Zeze.NewProcedure(
                ()-> { task.run(); return 0L; },
                task.getClass().getName() + "-" + task.Key)
        ));
    }

    public void WaitAllRunningTasks() {
        Task.WaitAll(RunningTasks);
    }

    public void ClearRunningTasks() {
        RunningTasks.clear();
    }
}
