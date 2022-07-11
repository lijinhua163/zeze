package Benchmark;

import demo.App;
import junit.framework.TestCase;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.concurrent.Future;

public class CBasicSimpleAddConcurrent extends TestCase {
    public final static int AddCount = 1_000_000;
    public final static int ConcurrentLevel = 1_000;

    public void testBenchmark() throws Throwable {
        App.Instance.Start();
        try {
            for (int i = 0; i < ConcurrentLevel; ++i) {
                final long k = i;
                App.Instance.Zeze.NewProcedure(() -> Remove(k), "remove").Call();
            }
            ArrayList<Future<Long>> tasks = new ArrayList<>(AddCount);
            for (int i = 0; i < AddCount; ++i) {
                final int c = i % ConcurrentLevel;
                tasks.add(Zeze.Util.Task.run(App.Instance.Zeze.NewProcedure(() -> Add(c), "Add"), null, null));
                //tasks.add(Zeze.Util.Task.Create(App.Instance.Zeze.NewProcedure(this::Add, "Add"), null, null));
            }
            System.out.println("benchmark start...");
            var b = new Zeze.Util.Benchmark();
            //b.Report(this.getClass().getName(), AddCount);
            for (var task : tasks) {
                task.get();
            }
            b.Report(this.getClass().getName(), AddCount);
            App.Instance.Zeze.NewProcedure(this::Check, "check").Call();
            for (long i = 0; i < ConcurrentLevel; ++i) {
                final long k = i;
                App.Instance.Zeze.NewProcedure(() -> Remove(k), "remove").Call();
            }
        }
        finally {
            App.Instance.Stop();
        }
    }

    private long Check() {
        long sum = 0;
        for (long i = 0; i < ConcurrentLevel; ++i) {
            var r = App.Instance.demo_Module1.getTable1().getOrAdd(i);
            sum += r.getLong2();
        }
        Assert.assertEquals(AddCount, sum);
        //System.out.println(r.getLong2());
        return 0;
    }

    private long Add() {
        var r = App.Instance.demo_Module1.getTable1().getOrAdd(1L);
        r.setLong2(r.getLong2() + 1);
        return 0;
    }

    private long Add(long key) {
        var r = App.Instance.demo_Module1.getTable1().getOrAdd(key);
        r.setLong2(r.getLong2() + 1);
        //System.out.println("Add=" + key);
        return 0;
    }

    private long Remove(long key) {
        App.Instance.demo_Module1.getTable1().remove(key);
        return 0;
    }
}
