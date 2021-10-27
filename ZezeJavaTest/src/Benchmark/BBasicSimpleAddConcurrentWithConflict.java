package Benchmark;

import demo.App;
import junit.framework.TestCase;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class BBasicSimpleAddConcurrentWithConflict  extends TestCase {
    public static long AddCount = 1000000L;

    public void testBenchmark() throws ExecutionException, InterruptedException {
        App.Instance.Start();
        try {
            App.Instance.Zeze.NewProcedure(this::Remove, "remove").Call();
            System.out.println("benchmark start...");
            var b = new Zeze.Util.Benchmark();
            ArrayList<Zeze.Util.Task> tasks = new ArrayList<>();
            for (long i = 0; i < AddCount; ++i) {
                tasks.add(Zeze.Util.Task.Run(App.Instance.Zeze.NewProcedure(this::Add, "Add"), null));
            }
            for (var task : tasks) {
                task.get();
            }
            b.Report(AddCount);
            App.Instance.Zeze.NewProcedure(this::Check, "check").Call();
            App.Instance.Zeze.NewProcedure(this::Remove, "remove").Call();
        }
        finally {
            App.Instance.Stop();
        }
    }

    private int Check() {
        var r = App.Instance.demo_Module1.getTable1().getOrAdd(1L);
        assert r.getLong2() == AddCount;
        //System.out.println(r.getLong2());
        return 0;
    }

    private int Add() {
        var r = App.Instance.demo_Module1.getTable1().getOrAdd(1L);
        r.setLong2(r.getLong2() + 1);
        return 0;
    }

    private int Remove() {
        App.Instance.demo_Module1.getTable1().remove(1L);
        return 0;
    }
}
