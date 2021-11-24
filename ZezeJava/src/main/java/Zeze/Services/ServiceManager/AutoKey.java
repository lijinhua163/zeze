package Zeze.Services.ServiceManager;

public final class AutoKey {
	private String Name;
	public String getName() {
		return Name;
	}
	private long Current;
	public long getCurrent() {
		return Current;
	}
	private void setCurrent(long value) {
		Current = value;
	}
	private int Count;
	public int getCount() {
		return Count;
	}
	private void setCount(int value) {
		Count = value;
	}
	private Agent Agent;
	public Agent getAgent() {
		return Agent;
	}

	public AutoKey(String name, Agent agent) {
		Name = name;
		Agent = agent;
	}

	public long Next() {
		synchronized (this) {
			if (getCount() <= 0) {
				Allocate();
			}

			if (getCount() <= 0) {
				throw new RuntimeException("AllocateId failed for " + getName());
			}

			var tmp = getCurrent();
			setCount(getCount() - 1);
			setCurrent(getCurrent() + 1);
			return tmp;
		}
	}

	private void Allocate() {
		var r = new AllocateId();
		r.Argument.setName(getName());
		r.Argument.setCount(1024);
		r.SendAndWaitCheckResultCode(getAgent().getClient().getSocket());
		setCurrent(r.Result.getStartId());
		setCount(r.Result.getCount());
	}
}
