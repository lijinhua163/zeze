package Infinite;

import java.util.ArrayList;
import java.util.concurrent.Future;
import Zeze.Config;
import Zeze.Util.Random;
import Zeze.Util.Task;

public class App {
	final demo.App app = new demo.App();
	private final ArrayList<Future<?>> RunningTasks = new ArrayList<>(Simulate.BatchTaskCount);
	private final Config config;

	public App(int serverId) {
		config = Config.Load("zeze.xml");
		config.setServerId(serverId);
		config.setFastRedoWhenConflict(false);
		config.setCheckpointPeriod(1000);

		var tdef = config.getDefaultTableConf();
		// 提高并发
		tdef.setCacheCleanPeriod(1000);
		// 超出容量时，快速尝试。
		tdef.setCacheCleanPeriodWhenExceedCapacity(100);
		// 减少容量，实际使用记录数要超过一些。让TableCache.Cleanup能并发起来。
		tdef.setCacheCapacity(Simulate.CacheCapacity);
		tdef.setCacheFactor(1.0f);

		var tflush = config.getTableConfMap().get("demo_Module1_tflush");
		// 提高并发
		tflush.setCacheCleanPeriod(1000);
		// 超出容量时，快速尝试。
		tflush.setCacheCleanPeriodWhenExceedCapacity(100);
		// 减少容量，实际使用记录数要超过一些。让TableCache.Cleanup能并发起来。
		tflush.setCacheCapacity(Tasks.tflushInt1Trade.CacheCapacity);
		tflush.setCacheFactor(1.0f);
	}

	public int getServerId() {
		return config.getServerId();
	}

	public void Start() throws Throwable {
		app.Start(config);
	}

	public void Stop() throws Throwable {
		for (var task : RunningTasks) {
			task.cancel(false);
		}
		app.Stop();
	}

	public void Run(Tasks.Task task) {
		task.App = app;
		String name = task.getClass().getName();
		int keyBound = task.getKeyBound();
		for (int i = 0, n = task.getKeyNumber(); i < n; i++) {
			long key;
			do
				key = Random.getInstance().nextInt(keyBound);
			while (!task.Keys.add(key));
			Tasks.getKeyCounter(name, key).increment();
		}
		Tasks.getRunCounter(name).increment();
		RunningTasks.add(task.IsProcedure() ? Task.run(app.Zeze.NewProcedure(task, name)) : Task.run(task::call, name));
	}

	public void WaitAllRunningTasksAndClear() {
		Task.waitAll(RunningTasks);
		RunningTasks.clear();
	}
}
