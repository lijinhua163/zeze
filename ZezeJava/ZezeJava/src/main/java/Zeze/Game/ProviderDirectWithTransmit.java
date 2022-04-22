package Zeze.Game;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.concurrent.ConcurrentHashMap;
import Zeze.Arch.ProviderDirect;
import Zeze.Builtin.ProviderDirect.Transmit;
import Zeze.Serialize.Serializable;
import Zeze.Transaction.Procedure;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ProviderDirectWithTransmit extends ProviderDirect {
	private static final Logger logger = LogManager.getLogger(ProviderDirectWithTransmit.class);
	private static final MethodHandles.Lookup lookup = MethodHandles.lookup();
	private static final MethodType voidType = MethodType.methodType(void.class);

	private final ConcurrentHashMap<String, MethodHandle> objectFactory = new ConcurrentHashMap<>();

	private Serializable createObject(String className) {
		try {
			return (Serializable)objectFactory.computeIfAbsent(className, cn -> {
				try {
					Class<?> cls = lookup.findClass(cn);
					if (!Serializable.class.isAssignableFrom(cls))
						throw new RuntimeException("not based on " + Serializable.class.getName());
					return lookup.findConstructor(cls, voidType);
				} catch (ReflectiveOperationException e) {
					throw new RuntimeException(e);
				}
			}).invoke();
		} catch (Throwable e) {
			logger.error("createObject failed: " + className, e);
			return null;
		}
	}

	@Override
	protected long ProcessTransmit(Transmit p) {
		Serializable parameter = null;
		if (!p.Argument.getParameterBeanName().isEmpty()) {
			parameter = createObject(p.Argument.getParameterBeanName());
			if (parameter == null)
				return ErrorCode(ErrorTransmitParameterFactoryNotFound);
			parameter.Decode(p.Argument.getParameterBeanValue().Wrap());
		}

		((ProviderImplementWithOnline)ProviderApp.ProviderImplement).Online.processTransmit(
				p.Argument.getSender(), p.Argument.getActionName(), p.Argument.getRoles().keySet(), parameter);
		return Procedure.Success;
	}
}
