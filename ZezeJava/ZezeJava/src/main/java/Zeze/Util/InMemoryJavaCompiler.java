package Zeze.Util;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;

/**
 * Compile Java sources in-memory (not thread safe for one instance)
 * from https://github.com/trung/InMemoryJavaCompiler
 */
public class InMemoryJavaCompiler {
	private final JavaCompiler javac;
	private DynamicClassLoader classLoader;
	private Iterable<String> options;
	private final ArrayList<SourceCode> sourceCodes = new ArrayList<>();
	private boolean ignoreWarnings;

	public InMemoryJavaCompiler() {
		javac = ToolProvider.getSystemJavaCompiler();
		classLoader = new DynamicClassLoader(ClassLoader.getSystemClassLoader());
	}

	/**
	 * @return the class loader used internally by the compiler
	 */
	public ClassLoader getClassloader() {
		return classLoader;
	}

	public InMemoryJavaCompiler useParentClassLoader(ClassLoader parent) {
		classLoader = new DynamicClassLoader(parent);
		return this;
	}

	/**
	 * Options used by the compiler, e.g. '-Xlint:unchecked'.
	 */
	public InMemoryJavaCompiler useOptions(String... options) {
		this.options = Arrays.asList(options);
		return this;
	}

	/**
	 * Ignore non-critical compiler output, like unchecked/unsafe operation warnings.
	 */
	public InMemoryJavaCompiler ignoreWarnings() {
		ignoreWarnings = true;
		return this;
	}

	public InMemoryJavaCompiler addSource(String className, String sourceCode) {
		sourceCodes.add(new SourceCode(className, sourceCode));
		return this;
	}

	/**
	 * @return warning/error message or null for compiling success
	 */
	public String compileAll() {
		if (sourceCodes.isEmpty())
			return null;
		DiagnosticCollector<JavaFileObject> collector = new DiagnosticCollector<>();
		javac.getTask(null, new ExtendedJavaFileManager(javac.getStandardFileManager(null, null, null), classLoader),
				collector, options, null, sourceCodes).call();
		if (!collector.getDiagnostics().isEmpty()) {
			StringBuilder exceptionMsg = new StringBuilder("Unable to compile the source");
			boolean hasWarnings = false, hasErrors = false;
			for (Diagnostic<?> d : collector.getDiagnostics()) {
				switch (d.getKind()) {
				case NOTE:
				case MANDATORY_WARNING:
				case WARNING:
					hasWarnings = true;
					break;
				default: // OTHER, ERROR
					hasErrors = true;
					break;
				}
				exceptionMsg.append('\n').append("[kind=").append(d.getKind());
				exceptionMsg.append(", ").append("line=").append(d.getLineNumber());
				exceptionMsg.append(", ").append("message=").append(d.getMessage(Locale.US)).append(']');
			}
			if (hasWarnings && !ignoreWarnings || hasErrors)
				return exceptionMsg.toString();
		}
		return null;
	}

	public Class<?> compile(String className, String sourceCode) throws ClassNotFoundException {
		sourceCodes.clear();
		String exMsg = addSource(className, sourceCode).compileAll();
		if (exMsg != null)
			throw new RuntimeException(exMsg);
		return classLoader.findClass(className);
	}

	private static final class SourceCode extends SimpleJavaFileObject {
		private final String contents;

		SourceCode(String className, String contents) {
			super(URI.create("string:///" + className.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
			this.contents = contents;
		}

		@Override
		public CharSequence getCharContent(boolean ignoreEncodingErrors) {
			return contents;
		}
	}

	private static final class CompiledCode extends SimpleJavaFileObject {
		private final ByteArrayOutputStream baos = new ByteArrayOutputStream();

		CompiledCode(String className) throws URISyntaxException {
			super(new URI(className), Kind.CLASS);
		}

		@Override
		public OutputStream openOutputStream() {
			return baos;
		}

		byte[] getByteCode() {
			return baos.toByteArray();
		}
	}

	private static final class DynamicClassLoader extends ClassLoader {
		private final HashMap<String, CompiledCode> customCompiledCode = new HashMap<>();

		DynamicClassLoader(ClassLoader parent) {
			super(parent);
		}

		void addCode(CompiledCode cc) {
			customCompiledCode.put(cc.getName(), cc);
		}

		@Override
		protected Class<?> findClass(String name) throws ClassNotFoundException {
			CompiledCode cc = customCompiledCode.get(name);
			if (cc == null)
				return super.findClass(name);
			byte[] byteCode = cc.getByteCode();
			return defineClass(name, byteCode, 0, byteCode.length);
		}
	}

	private static final class ExtendedJavaFileManager extends ForwardingJavaFileManager<JavaFileManager> {
		private final DynamicClassLoader cl;

		/**
		 * @param fileManager delegate to this file manager
		 */
		ExtendedJavaFileManager(JavaFileManager fileManager, DynamicClassLoader cl) {
			super(fileManager);
			this.cl = cl;
		}

		@Override
		public JavaFileObject getJavaFileForOutput(JavaFileManager.Location location, String className,
												   JavaFileObject.Kind kind, FileObject sibling) {
			try {
				CompiledCode innerClass = new CompiledCode(className);
				cl.addCode(innerClass);
				return innerClass;
			} catch (Exception e) {
				throw new RuntimeException("Error while creating in-memory output file for " + className, e);
			}
		}

		@Override
		public ClassLoader getClassLoader(JavaFileManager.Location location) {
			return cl;
		}
	}
}
