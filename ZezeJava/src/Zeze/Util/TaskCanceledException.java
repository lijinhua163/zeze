package Zeze.Util;

public class TaskCanceledException extends Error {
	static final long serialVersionUID = -1047347523279541091L;

	public TaskCanceledException() {
		
	}
	
	public TaskCanceledException(String msg) {
		super(msg);		
	}
}
