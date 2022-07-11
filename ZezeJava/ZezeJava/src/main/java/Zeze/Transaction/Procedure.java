package Zeze.Transaction;

import Zeze.Application;
import Zeze.IModule;
import Zeze.Util.FuncLong;
import Zeze.Util.TaskCanceledException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Procedure {
	public static final long Success = 0;
	public static final long Exception = -1;
	public static final long TooManyTry = -2;
	public static final long NotImplement = -3;
	public static final long Unknown = -4;
	public static final long ErrorSavepoint = -5;
	public static final long LogicError = -6;
	public static final long RedoAndRelease = -7;
	public static final long AbortException = -8;
	public static final long ProviderNotExist = -9;
	public static final long Timeout = -10;
	public static final long CancelException = -11;
	public static final long DuplicateRequest = -12;
	public static final long ErrorRequestId = -13;
	public static final long ErrorSendFail = -14;
	public static final long RaftRetry = -15;
	public static final long RaftApplied = -16;
	public static final long RaftExpired = -17;
	public static final long Closed = -18;
	// >0 用户自定义。

	public interface ILogAction {
		void run(Throwable ex, long result, Procedure p, String message);
	}

	private static final Logger logger = LogManager.getLogger(Procedure.class);
	public static ILogAction LogAction = Procedure::DefaultLogAction;

	public static void DefaultLogAction(Throwable ex, long result, Procedure p, String message) {
		org.apache.logging.log4j.Level level;
		if (ex != null)
			level = org.apache.logging.log4j.Level.ERROR;
		else if (result != 0)
			level = p.Zeze.getConfig().getProcessReturnErrorLogLevel();
		else if (logger.isTraceEnabled())
			level = org.apache.logging.log4j.Level.TRACE;
		else
			return;

		String module = result > 0 ? "@" + IModule.GetModuleId(result) + ":" + IModule.GetErrorCode(result) : "";
		logger.log(level, "Procedure={} Return={}{}{} UserState={}", p, result, module, message, p.UserState, ex);
	}

	private final Application Zeze;
	private final TransactionLevel Level;
	private FuncLong Action;
	private String ActionName;
	private Object UserState;
//	public Runnable RunWhileCommit;

	// 用于继承方式实现 Procedure。
	public Procedure(Application app) {
		Zeze = app;
		Level = null;
	}

	public Procedure(Application app, FuncLong action, String actionName, TransactionLevel level, Object userState) {
		Zeze = app;
		Level = level;
		Action = action;
		ActionName = actionName;
		if (userState != null)
			UserState = userState;
		else { // 没指定，就从当前存储过程继承。嵌套时发生。
			Transaction currentT = Transaction.getCurrent();
			if (currentT != null) {
				Procedure proc = currentT.getTopProcedure();
				if (proc != null)
					UserState = proc.UserState;
			}
		}
	}

	public final Application getZeze() {
		return Zeze;
	}

	public final TransactionLevel getTransactionLevel() {
		return Level;
	}

	public final FuncLong getAction() {
		return Action;
	}

	public final void setAction(FuncLong value) {
		Action = value;
	}

	public final String getActionName() {
		return ActionName;
	}

	public final void setActionName(String value) {
		ActionName = value;
	}

	public final Object getUserState() {
		return UserState;
	}

	public final void setUserState(Object value) {
		UserState = value;
	}

	/**
	 * 创建 Savepoint 并执行。
	 * 嵌套 Procedure 实现，
	 *
	 * @return 0 success; other means error.
	 */
	public final long Call() throws Throwable {
		Transaction currentT = Transaction.getCurrent();
		if (currentT == null) {
			try {
				// 有点奇怪，Perform 里面又会回调这个方法。这是为了把主要流程都写到 Transaction 中。
				return Transaction.Create(Zeze.getLocks()).Perform(this);
			} finally {
				Transaction.Destroy();
			}
		}

		currentT.Begin();
		currentT.getProcedureStack().add(this);
		try {
//			var runWhileCommit = RunWhileCommit;
//			if (runWhileCommit != null) {
//				RunWhileCommit = null;
//				currentT.runWhileCommit(runWhileCommit);
//			}
			long result = Process();
			currentT.VerifyRunning(); // 防止应用抓住了异常，通过return方式返回。

			if (result == Success) {
				currentT.Commit();
				ProcedureStatistics.getInstance().GetOrAdd(ActionName).GetOrAdd(result).increment();
				return Success;
			}
			currentT.Rollback();
			var tmpLogAction = LogAction;
			if (tmpLogAction != null)
				tmpLogAction.run(null, result, this, "");
			ProcedureStatistics.getInstance().GetOrAdd(ActionName).GetOrAdd(result).increment();
			return result;
		} catch (GoBackZeze gobackzeze) {
			// 单独抓住这个异常，是为了能原样抛出，并且使用不同的级别记录日志。
			// 对状态正确性没有影响。
			currentT.Rollback();
			logger.debug("", gobackzeze);
			throw gobackzeze;
		} catch (Throwable e) {
			currentT.Rollback();
			var tmpLogAction = LogAction;
			if (tmpLogAction != null)
				tmpLogAction.run(e, Exception, this, "");
			ProcedureStatistics.getInstance().GetOrAdd(ActionName).GetOrAdd(Exception).increment();
			// 验证状态：Running状态将吃掉所有异常。
			currentT.VerifyRunning();
			// 对于 unit test 的异常特殊处理，与unit test框架能搭配工作
			if (e instanceof AssertionError)
				throw e;
			// 回滚当前存储过程，不中断事务，外层存储过程判断结果自己决定是否继续。
			return e instanceof TaskCanceledException ? CancelException : Exception;
		} finally {
			currentT.getProcedureStack().remove(currentT.getProcedureStack().size() - 1);
		}
	}

	protected long Process() throws Throwable {
		return Action != null ? Action.call() : NotImplement;
	}

	@Override
	public String toString() {
		// getClass().getName() 仅在用继承的方式实现 Procedure 才有意义。
		return Action != null ? ActionName : getClass().getName();
	}
}
