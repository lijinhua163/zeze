package Zeze.Transaction.Collections;

import java.lang.invoke.MethodHandle;
import java.util.Collection;
import Zeze.Serialize.ByteBuffer;
import Zeze.Transaction.Bean;
import Zeze.Transaction.Log;
import Zeze.Transaction.Record;
import Zeze.Transaction.Transaction;
import Zeze.Util.IntHashSet;
import Zeze.Util.Reflect;
import org.pcollections.Empty;

public class PList2<V extends Bean> extends PList<V> {
	private final MethodHandle valueFactory;
	private final int logTypeId;

	public PList2(Class<V> valueClass) {
		valueFactory = Reflect.getDefaultConstructor(valueClass);
		logTypeId = Zeze.Transaction.Bean.Hash32("Zeze.Raft.RocksRaft.LogList2<" + Reflect.GetStableName(valueClass) + '>');
	}

	private PList2(int logTypeId, MethodHandle valueFactory) {
		this.valueFactory = valueFactory;
		this.logTypeId = logTypeId;
	}

	@Override
	public boolean add(V item) {
		if (item == null) {
			throw new NullPointerException();
		}

		if (isManaged()) {
			item.InitRootInfo(RootInfo, this);
			var txn = Transaction.getCurrent();
			assert txn != null;
			txn.VerifyRecordAccessed(this);
			@SuppressWarnings("unchecked")
			var listLog = (LogList2<V>)txn.LogGetOrAdd(
					getParent().getObjectId() + getVariableId(), this::CreateLogBean);
			return listLog.Add(item);
		}
		var newList = _list.plus(item);
		if (newList == _list)
			return false;
		_list = newList;
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean remove(Object item) {
		if (isManaged()) {
			var txn = Transaction.getCurrent();
			assert txn != null;
			txn.VerifyRecordAccessed(this);
			var listLog = (LogList2<V>)txn.LogGetOrAdd(
					getParent().getObjectId() + getVariableId(), this::CreateLogBean);
			return listLog.Remove((V)item);
		}
		var newList = _list.minus(item);
		if (newList == _list)
			return false;
		_list = newList;
		return true;
	}

	@Override
	public void clear() {
		if (isManaged()) {
			var txn = Transaction.getCurrent();
			assert txn != null;
			txn.VerifyRecordAccessed(this);
			@SuppressWarnings("unchecked")
			var listLog = (LogList2<V>)txn.LogGetOrAdd(
					getParent().getObjectId() + getVariableId(), this::CreateLogBean);
			listLog.Clear();
		} else
			_list = org.pcollections.Empty.vector();
	}

	@Override
	public V set(int index, V item) {
		if (item == null) {
			throw new NullPointerException();
		}

		if (isManaged()) {
			item.InitRootInfo(RootInfo, this);
			var txn = Transaction.getCurrent();
			assert txn != null;
			txn.VerifyRecordAccessed(this);
			@SuppressWarnings("unchecked")
			var listLog = (LogList2<V>)txn.LogGetOrAdd(
					getParent().getObjectId() + getVariableId(), this::CreateLogBean);
			return listLog.Set(index, item);
		}
		var old = _list.get(index);
		_list = _list.with(index, item);
		return old;
	}

	@Override
	public void add(int index, V item) {
		if (item == null) {
			throw new NullPointerException();
		}

		if (isManaged()) {
			item.InitRootInfo(RootInfo, this);
			var txn = Transaction.getCurrent();
			assert txn != null;
			txn.VerifyRecordAccessed(this);
			@SuppressWarnings("unchecked")
			var listLog = (LogList2<V>)txn.LogGetOrAdd(
					getParent().getObjectId() + getVariableId(), this::CreateLogBean);
			listLog.Add(index, item);
		} else
			_list = _list.plus(index, item);
	}

	@Override
	public V remove(int index) {
		if (isManaged()) {
			var txn = Transaction.getCurrent();
			assert txn != null;
			txn.VerifyRecordAccessed(this);
			@SuppressWarnings("unchecked")
			var listLog = (LogList2<V>)txn.LogGetOrAdd(
					getParent().getObjectId() + getVariableId(), this::CreateLogBean);
			return listLog.Remove(index);
		}
		var old = _list.get(index);
		_list = _list.minus(index);
		return old;
	}

	@Override
	public boolean addAll(Collection<? extends V> items) {
		if (isManaged()) {
			for (var item : items) {
				item.InitRootInfo(RootInfo, this);
			}
			var txn = Transaction.getCurrent();
			assert txn != null;
			txn.VerifyRecordAccessed(this);
			@SuppressWarnings("unchecked")
			var listLog = (LogList2<V>)txn.LogGetOrAdd(
					getParent().getObjectId() + getVariableId(), this::CreateLogBean);
			return listLog.AddAll(items);
		}
		_list = _list.plusAll(items);
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean removeAll(Collection<?> c) {
		if (isManaged()) {
			var txn = Transaction.getCurrent();
			assert txn != null;
			txn.VerifyRecordAccessed(this);
			var listLog = (LogList2<V>)txn.LogGetOrAdd(
					getParent().getObjectId() + getVariableId(), this::CreateLogBean);
			return listLog.RemoveAll((Collection<? extends V>)c);
		}
		var oldV = _list;
		_list = _list.minusAll(c);
		return oldV != _list;
	}

	@Override
	public LogBean CreateLogBean() {
		var log = new LogList2<V>(logTypeId, valueFactory);
		log.setBelong(getParent());
		log.setThis(this);
		log.setVariableId(getVariableId());
		log.setValue(_list);
		return log;
	}

	@Override
	public void FollowerApply(Log _log) {
		@SuppressWarnings("unchecked")
		var log = (LogList2<V>)_log;
		var tmp = _list;
		var newest = new IntHashSet();
		for (var opLog : log.getOpLogs()) {
			switch (opLog.op) {
			case LogList1.OpLog.OP_MODIFY:
				opLog.value.InitRootInfo(RootInfo, this);
				tmp = tmp.with(opLog.index, opLog.value);
				newest.add(opLog.index);
				break;
			case LogList1.OpLog.OP_ADD:
				opLog.value.InitRootInfo(RootInfo, this);
				tmp = tmp.plus(opLog.index, opLog.value);
				newest.add(opLog.index);
				break;
			case LogList1.OpLog.OP_REMOVE:
				tmp = tmp.minus(opLog.index);
				break;
			case LogList1.OpLog.OP_CLEAR:
				tmp = Empty.vector();
			}
		}
		_list = tmp;

		// apply changed
		for (var e : log.getChanged().entrySet()) {
			if (newest.contains(e.getValue().Value))
				continue;
			_list.get(e.getValue().Value).FollowerApply(e.getKey());
		}
	}

	@Override
	protected void InitChildrenRootInfo(Record.RootInfo root) {
		for (var v : _list)
			v.InitRootInfo(root, this);
	}

	@Override
	public Bean CopyBean() {
		var copy = new PList2<V>(logTypeId, valueFactory);
		copy._list = _list;
		return copy;
	}

	@Override
	public void Encode(ByteBuffer bb) {
		var tmp = getList();
		bb.WriteUInt(tmp.size());
		for (var e : tmp)
			e.Encode(bb);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void Decode(ByteBuffer bb) {
		clear();
		for (int i = bb.ReadUInt(); i > 0; i--) {
			V value;
			try {
				value = (V)valueFactory.invoke();
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
			value.Decode(bb);
			add(value);
		}
	}
}
