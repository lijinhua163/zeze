package Zeze.Transaction.Collections;

import Zeze.Transaction.Changes;
import Zeze.Transaction.Log;
import org.pcollections.PVector;

public abstract class LogList<V> extends LogBean {
	private PVector<V> Value;

	public LogList(int typeId) {
		super(typeId);
	}

	public LogList(String typeName) {
		super(typeName);
	}

	final PVector<V> getValue() {
		return Value;
	}

	final void setValue(PVector<V> value) {
		Value = value;
	}

	@Override
	public void Collect(Changes changes, Zeze.Transaction.Bean recent, Log vlog) {
		throw new UnsupportedOperationException("Collect Not Implement.");
	}

	@SuppressWarnings("unchecked")
	@Override
	public void Commit() {
		((PList<V>)getThis())._list = Value;
	}
}
