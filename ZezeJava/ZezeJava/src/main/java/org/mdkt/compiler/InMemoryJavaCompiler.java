package org.mdkt.compiler;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;

/**
 * Compile Java sources in-memory
 */
public class InMemoryJavaCompiler {
	private final JavaCompiler javac;
	private DynamicClassLoader classLoader;
	private Iterable<String> options;
	boolean ignoreWarnings = false;

	private final Map<String, SourceCode> sourceCodes = new HashMap<>();

	public static InMemoryJavaCompiler newInstance() {
		return new InMemoryJavaCompiler();
	}

	private InMemoryJavaCompiler() {
		this.javac = ToolProvider.getSystemJavaCompiler();
		this.classLoader = new DynamicClassLoader(ClassLoader.getSystemClassLoader());
	}

	public InMemoryJavaCompiler useParentClassLoader(ClassLoader parent) {
		this.classLoader = new DynamicClassLoader(parent);
		return this;
	}

	/**
	 * @return the class loader used internally by the compiler
	 */
	public ClassLoader getClassloader() {
		return classLoader;
	}

	/**
	 * Options used by the compiler, e.g. '-Xlint:unchecked'.
	 *
	 * @param options
	 * options
	 * @return
	 * this
	 */
	public InMemoryJavaCompiler useOptions(String... options) {
		this.options = Arrays.asList(options);
		return this;
	}

	/**
	 * Ignore non-critical compiler output, like unchecked/unsafe operation
	 * warnings.
	 *
	 * @return
	 * this
	 */
	public InMemoryJavaCompiler ignoreWarnings() {
		ignoreWarnings = true;
		return this;
	}

	/**
	 * Compile all sources
	 *
	 * @return Map containing instances of all compiled classes
	 * @throws Exception
	 * exception
	 */
	public Map<String, Class<?>> compileAll() throws Exception {
		if (sourceCodes.size() == 0) {
			throw new CompilationException("No source code to compile");
		}
		Collection<SourceCode> compilationUnits = sourceCodes.values();
		// CompiledCode[] code;

		// code = new CompiledCode[compilationUnits.size()];
		Iterator<SourceCode> iter = compilationUnits.iterator();
		for (int i = 0; i < compilationUnits.size(); i++) {
			/*code[i] = */new CompiledCode(iter.next().getClassName());
		}
		DiagnosticCollector<JavaFileObject> collector = new DiagnosticCollector<>();
		ExtendedStandardJavaFileManager fileManager = new ExtendedStandardJavaFileManager(javac.getStandardFileManager(null, null, null), classLoader);
		JavaCompiler.CompilationTask task = javac.getTask(null, fileManager, collector, options, null, compilationUnits);
		boolean result = task.call();
		if (!result || collector.getDiagnostics().size() > 0) {
			StringBuilder exceptionMsg = new StringBuilder();
			exceptionMsg.append("Unable to compile the source");
			boolean hasWarnings = false;
			boolean hasErrors = false;
			for (Diagnostic<? extends JavaFileObject> d : collector.getDiagnostics()) {
				switch (d.getKind()) {
				case NOTE:
				case MANDATORY_WARNING:
				case WARNING:
					hasWarnings = true;
					break;
				case OTHER:
				case ERROR:
				default:
					hasErrors = true;
					break;
				}
				exceptionMsg.append("\n").append("[kind=").append(d.getKind());
				exceptionMsg.append(", ").append("line=").append(d.getLineNumber());
				exceptionMsg.append(", ").append("message=").append(d.getMessage(Locale.US)).append("]");
			}
			if (hasWarnings && !ignoreWarnings || hasErrors) {
				throw new CompilationException(exceptionMsg.toString());
			}
		}

		Map<String, Class<?>> classes = new HashMap<>();
		for (String className : sourceCodes.keySet()) {
			classes.put(className, classLoader.loadClass(className));
		}
		return classes;
	}

	/**
	 * Compile single source
	 *
	 * @param className
	 * className
	 * @param sourceCode
	 * sourceCode
	 * @return
	 * compile result class
	 * @throws Exception
	 * exception
	 */
	public Class<?> compile(String className, String sourceCode) throws Exception {
		return addSource(className, sourceCode).compileAll().get(className);
	}

	/**
	 * Add source code to the compiler
	 *
	 * @param className
	 * class name
	 * @param sourceCode
	 * source code
	 * @return
	 * this
	 */
	public InMemoryJavaCompiler addSource(String className, String sourceCode) {
		sourceCodes.put(className, new SourceCode(className, sourceCode));
		return this;
	}
}