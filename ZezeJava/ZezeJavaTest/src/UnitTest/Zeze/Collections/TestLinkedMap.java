package UnitTest.Zeze.Collections;

import UnitTest.Zeze.MyBean;
import Zeze.Transaction.Procedure;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestLinkedMap {

	@Before
	public final void testInit() throws Throwable {
		demo.App.getInstance().Start();
	}

	@After
	public final void testCleanup() throws Throwable {
		demo.App.getInstance().Stop();
	}

	@Test
	public final void test1_LinkedMapPut() throws Throwable {
		assert Procedure.Success == demo.App.getInstance().Zeze.NewProcedure(() -> {
			var map = demo.App.LinkedMapModule.open("test1", MyBean.class);
			for (int i = 100; i < 110; i++) {
				var bean = new MyBean();
				bean.setI(i);
				map.put(i, bean);
			}
			return Procedure.Success;
		}, "test1_LinkedMapPut").Call();
	}

	@Test
	public final void test2_LinkedMapGet() throws Throwable {
		assert Procedure.Success == demo.App.getInstance().Zeze.NewProcedure(() -> {
			var map = demo.App.LinkedMapModule.open("test1", MyBean.class);
			for (int i = 100; i < 110; i++) {
				var bean = map.get(i);
				assert bean.getI() == i;
			}
			return Procedure.Success;
		}, "test2_LinkedMapGet").Call();
	}

	@Test
	public final void test3_LinkedMapWalk() throws Throwable {
		var map = demo.App.LinkedMapModule.open("test1", MyBean.class);
		map.walk(((key, value) -> {
			// TODO
//			try {
//				demo.App.getInstance().Zeze.NewProcedure(() -> {
//					value.setI(1000000);
//					return Procedure.Success;
//				}, "test2_QueuePop").Call();
//			} catch (Throwable e) {
//
//			}
			return true;
		}));
	}

	@Test
	public final void test4_LinkedMapRemove() throws Throwable {
		assert Procedure.Success == demo.App.getInstance().Zeze.NewProcedure(() -> {
			var map = demo.App.LinkedMapModule.open("test1", MyBean.class);
			for (int i = 100; i < 110; i++) {
				var bean = map.remove(i);
				assert bean.getI() == i;
			}
			return Procedure.Success;
		}, "test2_LinkedMapRemove").Call();
	}
}
