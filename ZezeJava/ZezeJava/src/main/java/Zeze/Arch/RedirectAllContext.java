package Zeze.Arch;

import java.util.function.Function;
import Zeze.Builtin.ProviderDirect.ModuleRedirectAllResult;
import Zeze.Net.Binary;
import Zeze.Net.Service;
import Zeze.Transaction.Procedure;
import Zeze.Util.IntHashMap;

public final class RedirectAllContext<R extends RedirectResult> extends Service.ManualContext {
	private final int concurrentLevel;
	private final IntHashMap<R> hashResults = new IntHashMap<>(); // <hash, result>
	private final Function<Binary, R> resultDecoder;
	private final RedirectAllFutureImpl<R> future;

	public RedirectAllContext(int concurrentLevel, Function<Binary, R> resultDecoder) {
		this.concurrentLevel = concurrentLevel;
		this.resultDecoder = resultDecoder;
		future = resultDecoder != null ? new RedirectAllFutureImpl<>() : null;
	}

	public int getConcurrentLevel() {
		return concurrentLevel;
	}

	// 只用于AllDone时获取所有结果, 此时不会再修改hashResults所以没有并发问题
	public IntHashMap<R> getAllResults() {
		return hashResults;
	}

	public RedirectAllFutureImpl<R> getFuture() {
		return future;
	}

	public boolean isCompleted() {
		return hashResults.size() >= concurrentLevel || isTimeout();
	}

	@Override
	public synchronized void OnRemoved() throws Throwable {
		if (isCompleted() && future != null)
			future.allDone(this);
	}

	// 这里处理真正redirect发生时，从远程返回的结果。
	public synchronized void ProcessResult(Zeze.Application zeze, ModuleRedirectAllResult res) throws Throwable {
		if (isCompleted())
			return; // 如果已经超时,那就只能忽略后续的结果了
		for (var e : res.Argument.getHashs().entrySet()) {
			int hash = e.getKey();
			var result = e.getValue();
			var resultCode = result.getReturnCode();
			if (resultDecoder != null) {
				R resultBean = resultDecoder.apply(resultCode == Procedure.Success ? result.getParams() : null);
				resultBean.setHash(hash);
				resultBean.setResultCode(resultCode);
				if (hashResults.putIfAbsent(hash, resultBean) == null) // 不可能回复相同hash的多个结果,忽略掉后面的好了
					future.result(this, resultBean);
			} else
				hashResults.put(hash, null);
		}
		if (hashResults.size() >= concurrentLevel)
			getService().TryRemoveManualContext(getSessionId());
	}
}
