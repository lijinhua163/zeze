package Zeze.Transaction.Logs;

import java.lang.invoke.MethodHandle;
import Zeze.Transaction.Bean;
import Zeze.Transaction.Log;
import Zeze.Serialize.ByteBuffer;
import Zeze.Serialize.Serializable;
import Zeze.Util.Reflect;

public abstract class LogBeanKey<T extends Serializable> extends Log {
	public T Value;
	private final MethodHandle valueFactory;

	public LogBeanKey(Class<T> valueClass) {
		super("Zeze.Transaction.Log<" + Reflect.GetStableName(valueClass) + '>');
		valueFactory = Reflect.getDefaultConstructor(valueClass);
	}

	// 事务修改过程中不需要Factory。
	public LogBeanKey(Class<T> cls, Bean belong, int varId, T value) {
		this(cls);
		setBelong(belong);
		setVariableId(varId);
		Value = value;
	}

	@Override
	public void Encode(ByteBuffer bb) {
		Value.Encode(bb);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void Decode(ByteBuffer bb) {
		try {
			Value = (T)valueFactory.invoke();
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
		Value.Decode(bb);
	}

	@Override
	public String toString() {
		return Value.toString();
	}
}
