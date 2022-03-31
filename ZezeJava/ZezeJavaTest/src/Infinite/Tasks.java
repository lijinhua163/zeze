package Infinite;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import Zeze.Serialize.ByteBuffer;
import Zeze.Transaction.DatabaseMemory;
import Zeze.Transaction.Procedure;
import Zeze.Transaction.ProcedureStatistics;
import Zeze.Transaction.Transaction;
import Zeze.Util.Func0;
import Zeze.Util.Random;

public final class Tasks {
	private static final ConcurrentHashMap<String, ConcurrentHashMap<Long, AtomicLong>> CounterRun = new ConcurrentHashMap<>();
	private static final ConcurrentHashMap<String, ConcurrentHashMap<Long, AtomicLong>> CounterSuccess = new ConcurrentHashMap<>();

	static ConcurrentHashMap<Long, AtomicLong> getRunCounters(String name) {
		return CounterRun.computeIfAbsent(name, __ -> new ConcurrentHashMap<>());
	}

	static AtomicLong getRunCounter(String name, long key) {
		return getRunCounters(name).computeIfAbsent(key, __ -> new AtomicLong());
	}

	static ConcurrentHashMap<Long, AtomicLong> getSuccessCounters(String name) {
		return CounterSuccess.computeIfAbsent(name, __ -> new ConcurrentHashMap<>());
	}

	static AtomicLong getSuccessCounter(String name, long key) {
		return getSuccessCounters(name).computeIfAbsent(key, __ -> new AtomicLong());
	}

	// 所有以long为key的记录访问可以使用这个基类。
	// 其他类型的key需要再定义新的基类。
	static abstract class Task implements Func0<Long> {
		final Set<Long> Keys = new HashSet<>();
		demo.App App;

		int getKeyNumber() {
			return 1;
		}

		int getKeyBound() {
			return Simulate.AccessKeyBound;
		}

		boolean IsProcedure() {
			return true;
		}

		void Run() {
			Simulate.getInstance().randApp().Run(this);
		}

		void prepare() throws Throwable {
		}

		abstract long process();

		void verify() {
			// default verify
			var name = getClass().getName();
			var runs = getRunCounters(name);
			var success = getSuccessCounters(name);
			assert runs.size() == success.size();
			for (var r : runs.entrySet()) {
				var s = success.get(r.getKey());
				assert s != null;
				// ignore TooManyTry error
				var tooManyTry = ProcedureStatistics.getInstance().GetOrAdd(name).GetOrAdd(Procedure.TooManyTry).get();
				assert r.getValue().get() == s.get() + tooManyTry;
				if (tooManyTry != 0)
					Simulate.logger.fatal("tooManyTry={} {}", tooManyTry, name);
			}
		}

		@Override
		public Long call() {
			var result = process();
			if (result == 0) {
				var txn = Transaction.getCurrent();
				if (txn != null) {
					txn.RunWhileCommit(() -> {
						for (var key : Keys)
							getSuccessCounter(getClass().getName(), key).incrementAndGet();
					});
				} else {
					for (var key : Keys)
						getSuccessCounter(getClass().getName(), key).incrementAndGet();
				}
			}
			return result;
		}
	}

	static final class TaskFactory {
		final Class<?> Class;
		final Supplier<Task> Factory;
		final int Weight;

		TaskFactory(Class<?> cls, Supplier<Task> factory, int weight) {
			Class = cls;
			Factory = factory;
			Weight = weight;
		}
	}

	static final ArrayList<TaskFactory> taskFactorys = new ArrayList<>();
	static int TotalWeight;

	static {
		// 新的操作数据的测试任务在这里注册。weight是权重，see randCreateTask();
		taskFactorys.add(new TaskFactory(Table1Long2Add1.class, Table1Long2Add1::new, 100));
		taskFactorys.add(new TaskFactory(Table1List9AddOrRemove.class, Table1List9AddOrRemove::new, 100));
		taskFactorys.add(new TaskFactory(tflushInt1Trade.class, tflushInt1Trade::new, 100));
		taskFactorys.add(new TaskFactory(tflushInt1TradeConcurrentVerify.class, tflushInt1TradeConcurrentVerify::new, 100));

		taskFactorys.sort(Comparator.comparingInt(a -> a.Weight));
		for (var task : taskFactorys)
			TotalWeight += task.Weight;
	}

	static Task randCreateTask() {
		var rand = Random.getInstance().nextInt(TotalWeight);
		for (var task : taskFactorys) {
			if (rand < task.Weight)
				return task.Factory.get();
			rand -= task.Weight;
		}
		throw new RuntimeException("impossible!");
	}

	static void prepare() throws Throwable {
		for (var tf : taskFactorys)
			tf.Factory.get().prepare();
	}

	static void verify() {
		for (var tf : taskFactorys)
			tf.Factory.get().verify();
	}

	static class Table1Long2Add1 extends Task {
		@Override
		void prepare() throws Throwable {
			// 所有使用 Table1 的测试都可以依赖这个 prepare，不需要单独写了。
			var app = Simulate.getInstance().randApp().app;
			app.Zeze.NewProcedure(() -> {
				for (long key = 0; key < Simulate.AccessKeyBound; key++)
					app.demo_Module1.getTable1().remove(key);
				return 0L;
			}, Table1Long2Add1.class.getName() + ".prepare").Call();
		}

		@Override
		long process() {
			var value = App.demo_Module1.getTable1().getOrAdd(Keys.iterator().next());
			value.setLong2(value.getLong2() + 1);
			return 0L;
		}

		@Override
		void verify() {
			// verify 时，所有任务都执行完毕，不需要考虑并发。
			var name = Table1Long2Add1.class.getName();
			var app = Simulate.getInstance().randApp().app; // 任何一个app都能查到相同的结果。
			var success = getSuccessCounters(name);
			for (var key : getRunCounters(name).keySet())
				assert app.demo_Module1.getTable1().selectDirty(key).getLong2() == success.get(key).get();
			Simulate.logger.debug("{}.verify OK!", name);
		}
	}

	static class Table1List9AddOrRemove extends Task {
		@Override
		long process() {
			var value = App.demo_Module1.getTable1().getOrAdd(Keys.iterator().next());
			// 使用 bool4 变量：用来决定添加或者删除。
			if (value.isBool4()) {
				// Simulate.logger.error("list9.size={}", value.getList9().size());
				if (!value.getList9().isEmpty())
					value.getList9().remove(value.getList9().size() - 1);
				value.setBool4(!value.getList9().isEmpty());
			} else {
				value.getList9().add(new demo.Bean1());
				if (value.getList9().size() > 50)
					value.setBool4(true); // 改成删除模式。
			}
			return 0L;
		}
	}

	// 在随机两个记录内进行交易。
	static class tflushInt1Trade extends Task {
		static final int KeyBoundTrade = Simulate.AccessKeyBound / 2;
		static final int CacheCapacity = Simulate.CacheCapacity / 2;

		@Override
		int getKeyNumber() {
			return 2;
		}

		@Override
		int getKeyBound() {
			return KeyBoundTrade;
		}

		@Override
		void Run() {
			Simulate.getInstance().randApp(2).Run(this);
		}

		@Override
		long process() {
			var it = Keys.iterator();
			var v1 = App.demo_Module1.getTflush().getOrAdd(it.next());
			var v2 = App.demo_Module1.getTflush().getOrAdd(it.next());
			var money = Random.getInstance().nextInt(1000);
			if (Random.getInstance().nextBoolean()) {
				// random swap
				var tmp = v1;
				v1 = v2;
				v2 = tmp;
			}
			v1.setInt1(v1.getInt1() - money);
			v2.setInt1(v2.getInt1() + money);
			return 0L;
		}

		@Override
		void verify() {
			var app = Simulate.getInstance().randApp().app; // 任何一个app都能查到相同的结果。
			int sum = 0;
			for (int key = 0; key < KeyBoundTrade; key++) {
				var value = app.demo_Module1.getTable1().selectDirty((long)key);
				if (value != null)
					sum += value.getInt1();
			}
			assert sum == 0;
		}
	}

	static class tflushInt1TradeConcurrentVerify extends Task {
		private static volatile HashSet<ByteBuffer> keys;

		@Override
		int getKeyNumber() {
			return 0;
		}

		@Override
		boolean IsProcedure() {
			return false;
		}

		@Override
		long process() {
			var table1 = App.demo_Module1.getTflush();
			if (keys == null) {
				synchronized (tflushInt1TradeConcurrentVerify.class) {
					if (keys == null) {
						var ks = new HashSet<ByteBuffer>();
						for (int key = 0; key < tflushInt1Trade.KeyBoundTrade; key++)
							ks.add(table1.EncodeKey((long)key));
						keys = ks;
					}
				}
			}
			try (var t = table1.getDatabase().BeginTransaction()) {
				if (t instanceof DatabaseMemory.MemTrans) {
					int sum = 0;
					var all = ((DatabaseMemory.MemTrans)t).Finds(table1.getName(), keys);
					for (var valueBytes : all.values()) {
						if (valueBytes != null)
							sum += table1.DecodeValue(valueBytes).getInt1();
					}
					assert sum == 0;
				}
			} catch (Exception e) {
				Simulate.logger.error(tflushInt1TradeConcurrentVerify.class.getName(), e);
				assert false;
			}
			return 0L;
		}
	}

	private Tasks() {
	}
}