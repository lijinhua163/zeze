package Zeze.Util;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class TaskCompletionSource<T> extends FutureTask<T> {
	public TaskCompletionSource() {
		super(null);
	}
	
	public boolean TrySetException(Throwable ex) {
		super.setException(ex);
		return true;
	}
	
	public void SetResult(T t) {
		super.set(t);
	}
	
	public void Wait() {
		try {
			super.get();
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}
	}
}