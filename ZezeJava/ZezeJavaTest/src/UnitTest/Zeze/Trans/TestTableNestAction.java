package UnitTest.Zeze.Trans;

import Zeze.Transaction.Procedure;
import Zeze.Transaction.Transaction;
import Zeze.Util.OutInt;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestTableNestAction {
	@Before
	public final void testInit() throws Throwable {
		demo.App.getInstance().Start();
	}

	@After
	public final void testCleanup() throws Throwable {
		demo.App.getInstance().Stop();
	}

	@Test
	public final void testNestProcedure() throws Throwable {
		var value1 = new OutInt();
		var value2 = new OutInt();
		demo.App.getInstance().Zeze.NewProcedure(() -> {
			Transaction.getCurrent().runWhileCommit(() -> {
				value1.Value++; //1
				System.out.println("value1:" + value1.Value);
			});

			demo.App.getInstance().Zeze.NewProcedure(() -> {
				Transaction.getCurrent().runWhileCommit(() -> value1.Value++);

				Transaction.getCurrent().runWhileRollback(() -> {
					Assert.assertEquals(value1.Value, value2.Value + 1);
					value2.Value++; // 1
					System.out.println(value1.Value);
					System.out.println(value2.Value);
				});
				return Procedure.Exception;
			}, "nest procedure1").Call();

			demo.App.getInstance().Zeze.NewProcedure(() -> {
				Transaction.getCurrent().runWhileCommit(() -> {
					Assert.assertEquals(value1.Value, value2.Value);
					value1.Value++; // 2
				});

				demo.App.getInstance().Zeze.NewProcedure(() -> {
					Transaction.getCurrent().runWhileCommit(() -> value1.Value++);

					Transaction.getCurrent().runWhileRollback(() -> {
						Assert.assertEquals(value1.Value, value2.Value + 1);
						value2.Value++; // 2
						System.out.println(value1.Value);
						System.out.println(value2.Value);
					});
					return Procedure.Exception;
				}, "nest procedure1").Call();

				return Procedure.Success;
			}, "nest procedure2").Call();

			Transaction.getCurrent().runWhileCommit(() -> {
				Assert.assertEquals(value1.Value, value2.Value);
				value1.Value++; // 3
			});
			return Procedure.Success;
		}, "out").Call();

		Assert.assertEquals(value1.Value, 3);
		Assert.assertEquals(value2.Value, 2);
	}

	@Test
	public final void testNestProcedure2() throws Throwable {
		var zeze = demo.App.getInstance().Zeze;
		var sb = new StringBuilder();

		zeze.NewProcedure(() -> {
			Transaction.whileCommit(() -> sb.append('0')); // do
			Transaction.whileRollback(() -> sb.append('1'));

			zeze.NewProcedure(() -> {
				Transaction.whileCommit(() -> sb.append('2')); // do
				Transaction.whileRollback(() -> sb.append('3'));

				zeze.NewProcedure(() -> {
					Transaction.whileCommit(() -> sb.append('4')); // do
					Transaction.whileRollback(() -> sb.append('5'));
					return Procedure.Success;
				}, "nest procedure11").Call();

				zeze.NewProcedure(() -> {
					Transaction.whileCommit(() -> sb.append('6'));
					Transaction.whileRollback(() -> sb.append('7')); // do
					return Procedure.Exception;
				}, "nest procedure12").Call();

				Transaction.whileCommit(() -> sb.append('8')); // do
				Transaction.whileRollback(() -> sb.append('9'));
				return Procedure.Success;
			}, "nest procedure1").Call();

			zeze.NewProcedure(() -> {
				Transaction.whileCommit(() -> sb.append('A'));
				Transaction.whileRollback(() -> sb.append('B')); // do

				zeze.NewProcedure(() -> {
					Transaction.whileCommit(() -> sb.append('C'));
					Transaction.whileRollback(() -> sb.append('D')); // do

					zeze.NewProcedure(() -> {
						Transaction.whileCommit(() -> sb.append('E'));
						Transaction.whileRollback(() -> sb.append('F')); // do
						return Procedure.Success;
					}, "nest procedure211").Call();

					Transaction.whileCommit(() -> sb.append('G'));
					Transaction.whileRollback(() -> sb.append('H')); // do
					return Procedure.Exception;
				}, "nest procedure21").Call();

				zeze.NewProcedure(() -> {
					Transaction.whileCommit(() -> sb.append('I'));
					Transaction.whileRollback(() -> sb.append('J')); // do

					zeze.NewProcedure(() -> {
						Transaction.whileCommit(() -> sb.append('K'));
						Transaction.whileRollback(() -> sb.append('L')); // do
						return Procedure.Exception;
					}, "nest procedure221").Call();

					Transaction.whileCommit(() -> sb.append('M'));
					Transaction.whileRollback(() -> sb.append('N')); // do
					return Procedure.Success;
				}, "nest procedure22").Call();

				Transaction.whileCommit(() -> sb.append('O'));
				Transaction.whileRollback(() -> sb.append('P')); // do
				return Procedure.Exception;
			}, "nest procedure2").Call();

			Transaction.whileCommit(() -> sb.append('Q')); // do
			Transaction.whileRollback(() -> sb.append('R'));
			return Procedure.Success;
		}, "nest procedure").Call();

		zeze.NewProcedure(() -> {
			Transaction.whileCommit(() -> sb.append('S'));
			Transaction.whileRollback(() -> sb.append('T')); // do

			zeze.NewProcedure(() -> {
				Transaction.whileCommit(() -> sb.append('U'));
				Transaction.whileRollback(() -> sb.append('V')); // do
				return Procedure.Exception;
			}, "nest procedure1").Call();

			zeze.NewProcedure(() -> {
				Transaction.whileCommit(() -> sb.append('W'));
				Transaction.whileRollback(() -> sb.append('X')); // do
				return Procedure.Success;
			}, "nest procedure2").Call();

			Transaction.whileCommit(() -> sb.append('Y'));
			Transaction.whileRollback(() -> sb.append('Z')); // do
			return Procedure.Exception;
		}, "nest procedure").Call();

		//noinspection SpellCheckingInspection
		Assert.assertEquals("02478BDFHJLNPQTVXZ", sb.toString());
	}
}
