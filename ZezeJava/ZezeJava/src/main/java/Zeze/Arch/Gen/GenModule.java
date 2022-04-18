package Zeze.Arch.Gen;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import Zeze.Arch.RedirectAll;
import Zeze.Arch.RedirectAllFuture;
import Zeze.Arch.RedirectFuture;
import Zeze.Arch.RedirectHash;
import Zeze.Arch.RedirectToServer;
import Zeze.Util.StringBuilderCs;
import org.mdkt.compiler.InMemoryJavaCompiler;

/**
 * 把模块的方法调用发送到其他服务器实例上执行。
 * 被重定向的方法用注解标明。
 * 被重定向的方法需要是virtual的。
 * 实现方案：
 * Game.App创建Module的时候调用回调。
 * 在回调中判断是否存在需要拦截的方法。
 * 如果需要就动态生成子类实现代码并编译并返回新的实例。
 * <p>
 * 注意：
 * 使用 virtual override 的方式可以选择拦截部分方法。
 * 可以提供和原来模块一致的接口。
 */
public class GenModule {
	public static final GenModule Instance = new GenModule();

	/**
	 * 源代码跟目录。
	 * 指定的时候，生成到文件，总是覆盖。
	 * 没有指定的时候，先查看目标类是否存在，存在则直接class.forName装载，否则生成到内存并动态编译。
	 */
	public String GenFileSrcRoot;
	private final InMemoryJavaCompiler compiler;

	private GenModule() {
		compiler = InMemoryJavaCompiler.newInstance();
		compiler.ignoreWarnings();
	}

	private String getNamespace(String fullClassName) {
		var names = fullClassName.split("\\.");
		var ns = new StringBuilder();
		for (int i = 0; i < names.length; ++i) {
			if (i > 0)
				ns.append(".");
			ns.append(names[i]);
		}
		return ns.toString();
	}

	private File getNamespaceFilePath(String fullClassName) {
		var ns = fullClassName.split("\\.");
		return Paths.get(GenFileSrcRoot, ns).toFile();
	}

	public <T extends Zeze.AppBase> Zeze.IModule ReplaceModuleInstance(T userApp, Zeze.IModule module) {
		var overrides = new ArrayList<MethodOverride>();
		for (var method : module.getClass().getDeclaredMethods()) {
			var a1 = method.getAnnotation(RedirectToServer.class);
			if (a1 != null)
				overrides.add(new MethodOverride(method, a1));
			var a2 = method.getAnnotation(RedirectHash.class);
			if (a2 != null)
				overrides.add(new MethodOverride(method, a2));
			var a3 = method.getAnnotation(RedirectAll.class);
			if (a3 != null)
				overrides.add(new MethodOverride(method, a3));
		}
		if (overrides.isEmpty())
			return module; // 没有需要重定向的方法。

		// 按方法名排序，避免每次生成结果发生变化。
		overrides.sort(Comparator.comparing(o -> o.method.getName()));

		String genClassName = module.getFullName().replace('.', '_') + "_Redirect";
		String namespace = getNamespace(module.getFullName());
		try {
			if (GenFileSrcRoot == null) {
				// 不需要生成到文件的时候，尝试装载已经存在的生成模块子类。
				try {
					Class<?> moduleClass = Class.forName(namespace + "." + genClassName);
					module.UnRegister();
					var newModule = (Zeze.IModule)moduleClass.getConstructor(userApp.getClass()).newInstance(userApp);
					newModule.Initialize(userApp);
					return newModule;
				} catch (Throwable ex) {
					// skip try load error
					// continue gen if error
				}
			}

			String code = GenModuleCode(GenFileSrcRoot != null ? namespace : null, // 生成文件的时候，生成package.
					module, genClassName, overrides, userApp.getClass().getName());

			if (GenFileSrcRoot != null) {
				byte[] oldBytes = null;
				byte[] newBytes = code.getBytes(StandardCharsets.UTF_8);
				var file = new File(getNamespaceFilePath(module.getFullName()), genClassName + ".java");
				if (file.exists()) {
					oldBytes = Files.readAllBytes(file.toPath());
					if (Arrays.equals(oldBytes, newBytes))
						System.out.println("  Existed File: " + file.getAbsolutePath());
					else {
						System.out.println("Overwrite File: " + file.getAbsolutePath());
						oldBytes = null;
					}
				} else
					System.out.println("      New File: " + file.getAbsolutePath());
				if (oldBytes == null) {
					try (var fos = new FileOutputStream(file)) {
						fos.write(newBytes);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				// 生成带File需要再次编译，所以这里返回原来的module。
				return module;
			}
			Class<?> moduleClass = compiler.compile(genClassName, code);
			module.UnRegister();
			var newModule = (Zeze.IModule)moduleClass.getConstructor(userApp.getClass()).newInstance(userApp);
			newModule.Initialize(userApp);
			return newModule;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	private String GenModuleCode(String pkg, Zeze.IModule module, String genClassName, List<MethodOverride> overrides,
								 String userAppName) throws Throwable {
		var sb = new Zeze.Util.StringBuilderCs();
		sb.AppendLine("// auto-generated @" + "formatter:off");
		if (pkg != null && !pkg.isEmpty()) {
			sb.AppendLine("package {};", pkg);
			sb.AppendLine();
		}
		sb.AppendLine("public final class {} extends {}.Module{} {", genClassName, module.getFullName(), module.getName());

		// TaskCompletionSource<int> void
		var sbHandles = new Zeze.Util.StringBuilderCs();
		for (var m : overrides) {
			var parametersDefine = m.GetDefineString();
			var methodNameHash = m.method.getName();
			String returnName;
			var type = m.method.getReturnType();
			if (type == void.class)
				returnName = "void";
			else if (type == RedirectFuture.class)
				returnName = "Zeze.Arch.RedirectFuture<" + m.resultTypeName + '>';
			else if (type == RedirectAllFuture.class)
				returnName = "Zeze.Arch.RedirectAllFuture<" + m.resultTypeName + '>';
			else
				throw new RuntimeException("ReturnType Must Be void Or RedirectFuture<...> Or RedirectAllFuture<...>");

			sb.AppendLine("    @Override");
			sb.AppendLine("    public {} {}({}) {", returnName, m.method.getName(), parametersDefine); // m.getThrows() // 继承方法允许不标throws

			ChoiceTargetRunLoopback(sb, m, returnName);

			if (m.annotation instanceof RedirectAll) {
				GenRedirectAll(sb, sbHandles, module, m);
				continue;
			}

			sb.AppendLine("        var _p_ = new Zeze.Beans.ProviderDirect.ModuleRedirect();");
			sb.AppendLine("        var _a_ = _p_.Argument;");
			sb.AppendLine("        _a_.setModuleId({});", module.getId());
			sb.AppendLine("        _a_.setRedirectType({});", m.getRedirectType());
			sb.AppendLine("        _a_.setHashCode({});", m.hashOrServerIdParameter.getName());
			sb.AppendLine("        _a_.setMethodFullName(\"{}:{}\");", module.getFullName(), m.method.getName());
			sb.AppendLine("        _a_.setServiceNamePrefix(App.ProviderApp.ServerServiceNamePrefix);");
			if (m.inputParameters.size() > 0) {
				// normal 包括了 out 参数，这个不需要 encode，所以下面可能仍然是空的，先这样了。
				sb.AppendLine("        var _b_ = Zeze.Serialize.ByteBuffer.Allocate();");
				Gen.Instance.GenEncode(sb, "        ", "_b_", m.inputParameters);
				sb.AppendLine("        _a_.setParams(new Zeze.Net.Binary(_b_));");
			}
			sb.AppendLine();
			if (returnName.equals("void"))
				sb.AppendLine("        _p_.Send(_t_, null);");
			else {
				sb.AppendLine("        var _f_ = new Zeze.Arch.RedirectFuture<{}>();", m.resultTypeName);
				sb.AppendLine("        _p_.Send(_t_, _rpc_ -> {");
				if (m.resultType == Long.class)
					sb.AppendLine("            _f_.SetResult(_rpc_.isTimeout() ? Zeze.Transaction.Procedure.Timeout : _rpc_.getResultCode());");
				else {
					sb.AppendLine("            var _r_ = new {}();", m.resultTypeName);
					if (m.returnTypeHasResultCode)
						sb.AppendLine("            _r_.resultCode = _rpc_.isTimeout() ? Zeze.Transaction.Procedure.Timeout : _rpc_.getResultCode();");
					if (!m.resultTypeNames.isEmpty()) {
						sb.AppendLine("            var _param_ = _rpc_.Result.getParams();");
						sb.AppendLine("            if (_param_.size() > 0) {");
						sb.AppendLine("                var _bb_ = _param_.Wrap();");
						for (var typeName : m.resultTypeNames)
							Gen.Instance.GenDecode(sb, "                ", "_bb_", typeName.getKey(), "_r_." + typeName.getValue());
					}
					sb.AppendLine("            }");
					sb.AppendLine("            _f_.SetResult(_r_);");
				}
				sb.AppendLine("            return Zeze.Transaction.Procedure.Success;");
				sb.AppendLine("        });");
				sb.AppendLine("        return _f_;");
			}
			sb.AppendLine("    }");
			sb.AppendLine();

			// Handles
			sbHandles.AppendLine();
			sbHandles.AppendLine("        App.Zeze.Redirect.Handles.put(\"{}:{}\", new Zeze.Arch.RedirectHandle(", module.getFullName(), m.method.getName());
			sbHandles.AppendLine("            Zeze.Transaction.TransactionLevel.{}, (_hash_, _params_) -> {", m.TransactionLevel);
			boolean genLocal = false;
			for (int i = 0; i < m.inputParameters.size(); ++i) {
				var p = m.inputParameters.get(i);
				if (Gen.IsKnownDelegate(p.getType()))
					continue; // define later.
				Gen.Instance.GenLocalVariable(sbHandles, "                ", p.getType(), p.getName());
				genLocal = true;
			}
			if (genLocal)
				sbHandles.AppendLine("                var _b_ = _params_.Wrap();");
			Gen.Instance.GenDecode(sbHandles, "                ", "_b_", m.inputParameters);
			var normalCall = m.GetNormalCallString();
			var sep = normalCall.isEmpty() ? "" : ", ";
			if (returnName.equals("void")) {
				sbHandles.AppendLine("                super.{}(_hash_{}{});", methodNameHash, sep, normalCall);
				sbHandles.AppendLine("                return null;");
			} else {
				if (normalCall.isEmpty())
					sbHandles.AppendLine("                //noinspection CodeBlock2Expr");
				sbHandles.AppendLine("                return super.{}(_hash_{}{});", methodNameHash, sep, normalCall);
			}
			sbHandles.Append("            }, _result_ -> ");
			if (!m.resultTypeNames.isEmpty()) {
				sbHandles.AppendLine("{");
				sbHandles.AppendLine("                var _r_ = ({})_result_;", m.resultTypeName);
				sbHandles.AppendLine("                var _b_ = Zeze.Serialize.ByteBuffer.Allocate();");
				for (var typeName : m.resultTypeNames)
					Gen.Instance.GenEncode(sbHandles, "                ", "_b_", typeName.getKey(), "_r_." + typeName.getValue());
				sbHandles.AppendLine("                return new Zeze.Net.Binary(_b_);");
				sbHandles.Append("            }");
			} else
				sbHandles.Append("Zeze.Net.Binary.Empty");
			sbHandles.AppendLine("));");
		}
		sb.AppendLine("    public {}({} _app_) {", genClassName, userAppName);
		sb.AppendLine("        super(_app_);");
		sb.Append(sbHandles.toString());
		sb.AppendLine("    }");
		sb.AppendLine("}");
		return sb.toString();
	}

	// 根据转发类型选择目标服务器，如果目标服务器是自己，直接调用基类方法完成工作。
	private void ChoiceTargetRunLoopback(StringBuilderCs sb, MethodOverride methodOverride, String returnName) {
		if (methodOverride.annotation instanceof RedirectHash)
			sb.AppendLine("        var _t_ = App.Zeze.Redirect.ChoiceHash(this, {});", methodOverride.hashOrServerIdParameter.getName());
		else if (methodOverride.annotation instanceof RedirectToServer)
			sb.AppendLine("        var _t_ = App.Zeze.Redirect.ChoiceServer(this, {});", methodOverride.hashOrServerIdParameter.getName());
		else if (methodOverride.annotation instanceof RedirectAll)
			return; // RedirectAll 不在这里选择目标服务器。后面发送的时候直接查找所有可用服务器并进行广播。

		sb.AppendLine("        if (_t_ == null) { // local: loop-back");
		if (returnName.equals("void")) {
			sb.AppendLine("            App.Zeze.Redirect.RunVoid(Zeze.Transaction.TransactionLevel.{},", methodOverride.TransactionLevel);
			sb.AppendLine("                () -> super.{}({}));", methodOverride.method.getName(), methodOverride.GetBaseCallString());
			sb.AppendLine("            return;");
		} else {
			sb.AppendLine("            return App.Zeze.Redirect.RunFuture(Zeze.Transaction.TransactionLevel.{},", methodOverride.TransactionLevel);
			sb.AppendLine("                () -> super.{}({}));", methodOverride.method.getName(), methodOverride.GetBaseCallString());
		}
		sb.AppendLine("        }");
		sb.AppendLine();
	}

	private void GenRedirectAll(Zeze.Util.StringBuilderCs sb, Zeze.Util.StringBuilderCs sbHandles,
								Zeze.IModule module, MethodOverride m) throws Throwable {
		sb.Append("        var _c_ = new Zeze.Arch.ModuleRedirectAllContext<>({}, ", m.hashOrServerIdParameter.getName());
		if (m.resultTypeName != null) {
			sb.AppendLine("_params_ -> {");
			sb.AppendLine("            var _r_ = new {}();", m.resultTypeName);
			sb.AppendLine("            if (_params_ != null) {");
			sb.AppendLine("                var _b_ = _params_.Wrap();");
			for (var typeName : m.resultTypeNames)
				Gen.Instance.GenDecode(sb, "                ", "_b_", typeName.getKey(), "_r_." + typeName.getValue());
			sb.AppendLine("            }");
			sb.AppendLine("            return _r_;");
			sb.AppendLine("        });");
		} else
			sb.AppendLine("null);");
		sb.AppendLine("        var _p_ = new Zeze.Beans.ProviderDirect.ModuleRedirectAllRequest();");
		sb.AppendLine("        var _a_ = _p_.Argument;");
		sb.AppendLine("        _a_.setModuleId({});", module.getId());
		sb.AppendLine("        _a_.setHashCodeConcurrentLevel({});", m.hashOrServerIdParameter.getName());
		sb.AppendLine("        _a_.setMethodFullName(\"{}:{}\");", module.getFullName(), m.method.getName());
		sb.AppendLine("        _a_.setServiceNamePrefix(App.ProviderApp.ServerServiceNamePrefix);");
		sb.AppendLine("        _a_.setSessionId(App.ServerDirect.AddManualContextWithTimeout(_c_));");
		if (m.inputParameters.size() > 0) {
			sb.AppendLine("        var _b_ = Zeze.Serialize.ByteBuffer.Allocate();");
			Gen.Instance.GenEncode(sb, "        ", "_b_", m.inputParameters);
			sb.AppendLine("        _a_.setParams(new Zeze.Net.Binary(_b_));");
		}
		if (m.resultType != null)
			sb.AppendLine("        return App.Zeze.Redirect.RedirectAll(this, _p_, _c_);");
		else
			sb.AppendLine("        App.Zeze.Redirect.RedirectAll(this, _p_, _c_);");
		sb.AppendLine("    }");
		sb.AppendLine();

		// handles
		sbHandles.AppendLine();
		sbHandles.AppendLine("        App.Zeze.Redirect.Handles.put(\"{}:{}\", new Zeze.Arch.RedirectHandle(", module.getFullName(), m.method.getName());
		sbHandles.AppendLine("            Zeze.Transaction.TransactionLevel.{}, (_hash_, _params_) -> {", m.TransactionLevel);
		if (!m.inputParameters.isEmpty()) {
			sbHandles.AppendLine("                var _b_ = _params_.Wrap();");
			for (int i = 0; i < m.inputParameters.size(); ++i) {
				var p = m.inputParameters.get(i);
				if (Gen.IsKnownDelegate(p.getType()))
					continue; // define later.
				Gen.Instance.GenLocalVariable(sbHandles, "                ", p.getType(), p.getName());
			}
			Gen.Instance.GenDecode(sbHandles, "                ", "_b_", m.inputParameters);
		}

		var normalCall = m.GetNormalCallString();
		if (m.resultType != null)
			sbHandles.AppendLine("                return super.{}(_hash_{}{});", m.method.getName(), normalCall.isEmpty() ? "" : ", ", normalCall);
		else {
			sbHandles.AppendLine("                super.{}(_hash_{}{});", m.method.getName(), normalCall.isEmpty() ? "" : ", ", normalCall);
			sbHandles.AppendLine("                return null;");
		}
		sbHandles.Append("            }, _result_ -> ");
		if (m.resultTypeName != null && !m.resultTypeNames.isEmpty()) {
			sbHandles.AppendLine("{");
			sbHandles.AppendLine("                var _r_ = ({})_result_;", m.resultTypeName);
			sbHandles.AppendLine("                var _b_ = Zeze.Serialize.ByteBuffer.Allocate();");
			for (var typeName : m.resultTypeNames)
				Gen.Instance.GenEncode(sbHandles, "                ", "_b_", typeName.getKey(), "_r_." + typeName.getValue());
			sbHandles.AppendLine("                return new Zeze.Net.Binary(_b_);");
			sbHandles.Append("            }");
		} else
			sbHandles.Append("Zeze.Net.Binary.Empty");
		sbHandles.AppendLine("));");
	}
}
