package Zezex;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import Game.App;
import Zeze.Net.AsyncSocket;
import Zeze.Net.Binary;
import Zeze.Transaction.Transaction;
import Zeze.Util.Func4;
import Zeze.Util.Str;
import Zezex.Provider.BActionParam;
import org.mdkt.compiler.InMemoryJavaCompiler;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;

/** 
 把模块的方法调用发送到其他服务器实例上执行。
 被重定向的方法用注解标明。
 被重定向的方法需要是virtual的。
 实现方案：
 Game.App创建Module的时候调用回调。
 在回调中判断是否存在需要拦截的方法。
 如果需要就动态生成子类实现代码并编译并返回新的实例。

 注意：
 使用 virtual override 的方式可以选择拦截部分方法。
 可以提供和原来模块一致的接口。
*/
public class ModuleRedirect {
	// 本应用：hash分组的一些配置。
	public static final int ChoiceType = Zezex.Provider.BModule.ChoiceTypeHashAccount;
	public static int GetChoiceHashCode() {
		String account = ((Game.Login.Session) Transaction.getCurrent().getTopProcedure().getUserState()).getAccount();
		return Zeze.Serialize.ByteBuffer.calc_hashnr(account);
	}

	public static AsyncSocket RandomLink() {
		return Game.App.Instance.Server.RandomLink();
	}

	public static ModuleRedirect Instance = new ModuleRedirect();

	public enum OverrideType {
		Redirect,
		RedirectWithHash,
		RedirectAll,
		RedirectToServer,
	}

	public static class MethodOverride {
		public java.lang.reflect.Method Method;
		public OverrideType OverrideType = ModuleRedirect.OverrideType.Redirect;
		public Annotation Attribute;

		public MethodOverride(java.lang.reflect.Method method, OverrideType type, Annotation attribute) {
			Method = method;
			OverrideType = type;
			Attribute = attribute;
		}

		public java.lang.reflect.Parameter ParameterHashOrServer;
		public ArrayList<java.lang.reflect.Parameter> ParametersNormal = new ArrayList<> ();
		public java.lang.reflect.Parameter ParameterLastWithMode;
		public java.lang.reflect.Parameter[] ParametersAll;
		public java.lang.reflect.Parameter ParameterRedirectResultHandle;
		public java.lang.reflect.Parameter ParameterRedirectAllResultHandle;
		public java.lang.reflect.Parameter ParameterRedirectAllDoneHandle;

		public String getThrows() {
			var throwsexp = Method.getGenericExceptionTypes();
			if (throwsexp.length == 0)
				return "";
			var sb = new StringBuilder();
			sb.append(" throws ");
			for (int i = 0; i < throwsexp.length; ++i) {
				if (i > 0)
					sb.append(", ");
				sb.append(throwsexp[i].getTypeName());
			}
			return sb.toString();
		}

		public final void PrepareParameters() {
			ParametersAll = Method.getParameters();
			ParametersNormal.addAll(Arrays.asList(ParametersAll));

			if (OverrideType == OverrideType.RedirectToServer || OverrideType == OverrideType.RedirectWithHash) {
				ParameterHashOrServer = ParametersAll[0];
				if (ParameterHashOrServer.getType() != int.class) {
					throw new RuntimeException("ModuleRedirectWithHash: type of first parameter must be 'int'");
				}
				//System.out.println(ParameterFirstWithHash.getName() + "<-----");
				//if (false == ParameterFirstWithHash.getName().equals("hash")) {
				//	throw new RuntimeException("ModuleRedirectWithHash: name of first parameter must be 'hash'");
				//}
				ParametersNormal.remove(0);
			}

			if (!ParametersNormal.isEmpty()
					&& ParametersNormal.get(ParametersNormal.size() - 1).getType() == Zeze.TransactionModes.class) {
				ParameterLastWithMode = ParametersNormal.get(ParametersNormal.size() - 1);
				ParametersNormal.remove(ParametersNormal.size() - 1);
			}

			for (var p : ParametersAll) {
				if (p.getType() == Zezex.RedirectAllDoneHandle.class)
					ParameterRedirectAllDoneHandle = p;
				else if (p.getType() == Zezex.RedirectAllResultHandle.class)
					ParameterRedirectAllResultHandle = p;
				else if (p.getType() == Zezex.RedirectResultHandle.class)
					ParameterRedirectResultHandle = p;
			}
		}

		public final String GetNarmalCallString() throws Throwable {
			return GetNarmalCallString(null);
		}

		public final String GetNarmalCallString(Zeze.Util.Func1<java.lang.reflect.Parameter, Boolean> skip) throws Throwable {
			StringBuilder sb = new StringBuilder();
			boolean first = true;
			for (int i = 0; i < ParametersNormal.size(); ++i) {
				var p = ParametersNormal.get(i);
				if (null != skip && skip.call(p)) {
					continue;
				}
				if (first) {
					first = false;
				}
				else {
					sb.append(", ");
				}
				sb.append(p.getName());
			}
			return sb.toString();
		}

		public final String GetModeCallString() {
			if (ParameterLastWithMode == null) {
				return "";
			}
			if (ParametersAll.length == 1) { // 除了mode，没有其他参数。
				return ParameterLastWithMode.getName();
			}
			return Str.format(", {}", ParameterLastWithMode.getName());
		}

		public final String GetHashOrServerCallString() {
			if (ParameterHashOrServer == null) {
				return "";
			}
			if (ParametersAll.length == 1) { // 除了hash，没有其他参数。
				return ParameterHashOrServer.getName();
			}
			return Str.format("{}, ", ParameterHashOrServer.getName());
		}

		public final String GetBaseCallString() throws Throwable {
			return Str.format("{}{}{}", GetHashOrServerCallString(), GetNarmalCallString(), GetModeCallString());
		}

		public final String getRedirectType() {
			switch (OverrideType) {
				case Redirect: // fall down
				case RedirectWithHash:
					return "Zezex.Provider.ModuleRedirect.RedirectTypeWithHash";

				case RedirectToServer:
					return "Zezex.Provider.ModuleRedirect.RedirectTypeToServer";
				default:
					throw new RuntimeException("unkown OverrideType");
			}
		}

		public final String GetChoiceHashOrServerCodeSource() {
			switch (OverrideType) {
				case RedirectToServer:
				case RedirectWithHash:
					return ParameterHashOrServer.getName(); // parameter name

				case Redirect:
					var attr = (Zezex.Redirect)Attribute;
					if (attr.ChoiceHashCodeSource().isEmpty())
						return "Zezex.ModuleRedirect.GetChoiceHashCode()";
					return attr.ChoiceHashCodeSource();

				default:
					throw new RuntimeException("error state");
			}
		}

		public final String GetConcurrentLevelSource() {
			if (OverrideType != OverrideType.RedirectAll) {
				throw new RuntimeException("is not RedirectAll");
			}
			var attr = (Zezex.RedirectAll)Attribute;
			return attr.GetConcurrentLevelSource();
		}
	}

	private void tryCollectMethod(ArrayList<MethodOverride> result, OverrideType type, Method method) {
		switch (type) {
			case Redirect:
				var annotation1 = method.getAnnotation(Redirect.class);
				if (null != annotation1)
					result.add(new MethodOverride(method, OverrideType.Redirect, annotation1));
				break;
			case RedirectAll:
				var annotation2 = method.getAnnotation(RedirectAll.class);
				if (null != annotation2)
					result.add(new MethodOverride(method, OverrideType.RedirectAll, annotation2));
				break;
			case RedirectWithHash:
				var annotation3 = method.getAnnotation(RedirectWithHash.class);
				if (null != annotation3)
					result.add(new MethodOverride(method, OverrideType.RedirectWithHash, annotation3));
				break;
			case RedirectToServer:
				var annotation4 = method.getAnnotation(RedirectToServer.class);
				if (null != annotation4)
					result.add(new MethodOverride(method, OverrideType.RedirectToServer, annotation4));
				break;
		}
	}
	public final Zeze.IModule ReplaceModuleInstance(Zeze.IModule module) {
		var overrides = new ArrayList<MethodOverride>();
		var methods = module.getClass().getDeclaredMethods();
		for (var method : methods) {
			tryCollectMethod(overrides, OverrideType.Redirect, method);
			tryCollectMethod(overrides, OverrideType.RedirectWithHash, method);
			tryCollectMethod(overrides, OverrideType.RedirectAll, method);
			tryCollectMethod(overrides, OverrideType.RedirectToServer, method);
		}
		if (overrides.isEmpty()) {
			return module; // 没有需要重定向的方法。
		}

		String genClassName = Str.format("_ModuleRedirect_{}_", module.getFullName().replace('.', '_'));
		try {
			String code = GenModuleCode(module, genClassName, overrides);
			//*
			try {
				var tmp = new FileWriter(genClassName + ".java", java.nio.charset.StandardCharsets.UTF_8);
				tmp.write(code);
				tmp.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return module;
			/*/
			module.UnRegister();
			Class<?> moduleClass = compiler.compile(genClassName, code);
			var newModuleInstance = (Zeze.IModule) moduleClass.getDeclaredConstructor(new Class[0]).newInstance();
			newModuleInstance.Initialize(Game.App.Instance);
			return newModuleInstance;
			// */
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	private org.mdkt.compiler.InMemoryJavaCompiler compiler;

	public static class Return {
		public long ReturnCode;
		public Binary EncodedParameters;
		public Return(long rc, Binary params) {
			ReturnCode = rc;
			EncodedParameters = params;
		}
	}

	/**
	 0) long [in] sessionid
	 1) int [in] hash
	 2) Zeze.Net.Binary [in] encoded parameters
	 3) List<Zezex.Provider.BActionParam> [result] result for callback. avoid copy.
	 4) Return [return]
		 Func不能使用ref，而Zeze.Net.Binary是只读的。就这样吧。
	*/
	public ConcurrentHashMap<String,
				Func4<Long, Integer, Binary, List<BActionParam>, Return>> Handles = new ConcurrentHashMap <>();

	enum ReturnType {
		Void,
		TaskCompletionSource
	}

	static class ReturnTypeAndName {
		public ReturnType ReturnType;
		public String ReturnTypeName;
		public ReturnTypeAndName(ReturnType t, String n) {
			ReturnType = t;
			ReturnTypeName = n;
		}
	}

	private ReturnTypeAndName GetReturnType(Class<?> type)  {
		if (type == void.class)
			return new ReturnTypeAndName(ReturnType.Void, "void");
		if (type == Zeze.Util.TaskCompletionSource.class) {
			// java 怎么获得模板参数列表，检查一下模板参数类型必须Long.
			return new ReturnTypeAndName(ReturnType.TaskCompletionSource, "Zeze.Util.TaskCompletionSource<Long>");
		}
		throw new RuntimeException("ReturnType Must Be void Or TaskCompletionSource<Long>");
	}

	private String GetMethodNameWithHash(String name) {
		if (!name.startsWith("Run"))
			throw new RuntimeException("Method Name Need StartsWith 'Run'.");
		return name.substring(3);
	}

	private void Verify(MethodOverride method) {
		switch (method.OverrideType) {
			case RedirectAll:
				if (method.Method.getReturnType() != void.class)
					throw new RuntimeException("RedirectAll ReturnType Must Be void");
				if (method.ParameterRedirectAllDoneHandle != null && method.ParameterRedirectAllResultHandle == null)
					throw new RuntimeException("RedirectAll Has RedirectAllDoneHandle But Miss RedirectAllResultHandle.");
				break;
		}
	}

	private String GenModuleCode(Zeze.IModule module, String genClassName, List<MethodOverride> overrides) throws Throwable {
		var sb = new Zeze.Util.StringBuilderCs();
		sb.AppendLine("");
		sb.AppendLine("import java.util.List;");
		sb.AppendLine("import Zeze.Net.Binary;");
		sb.AppendLine("import Zezex.Provider.BActionParam;");
		sb.AppendLine("");
		sb.AppendLine(Str.format("public class {} extends {}.Module{}", genClassName, module.getFullName(), module.getName()));
		sb.AppendLine("{");

		// TaskCompletionSource<int> void
		var sbHandles = new Zeze.Util.StringBuilderCs();
		var sbContexts = new Zeze.Util.StringBuilderCs();
		for  (var methodOverride : overrides)  {
			methodOverride.PrepareParameters();
			var parametersDefine = ToDefineString(methodOverride.ParametersAll);
			var methodNameWithHash = GetMethodNameWithHash(methodOverride.Method.getName());
			var rtn = GetReturnType(methodOverride.Method.getReturnType());
			Verify(methodOverride);

			sb.AppendLine("    @Override");
			sb.AppendLine(Str.format("    public {} {} ({}){}",
					rtn.ReturnTypeName, methodOverride.Method.getName(), parametersDefine, methodOverride.getThrows()));
			sb.AppendLine("    {");
			sb.AppendLine(Str.format("        if (Zezex.ModuleRedirect.Instance.IsLocalServer(\"{}\"))", module.getFullName()));
			sb.AppendLine("        {");
			switch (rtn.ReturnType)
			{
				case Void:
					sb.AppendLine(Str.format("            super.{}({});",
							methodOverride.Method.getName(), methodOverride.GetBaseCallString()));
					sb.AppendLine(Str.format("            return;"));
					break;
				case TaskCompletionSource:
					sb.AppendLine(Str.format("            return super.{}({});",
							methodOverride.Method.getName(), methodOverride.GetBaseCallString()));
					break;
			}
			sb.AppendLine("        }");
			sb.AppendLine("");

			if (methodOverride.OverrideType == OverrideType.RedirectAll) {
				GenRedirectAllContext(sbContexts, module, methodOverride);
				GenRedirectAll(sb, sbHandles, module, methodOverride);
				continue;
			}
			var rpcVarName = "tmp" + TmpVarNameId.incrementAndGet();
			sb.AppendLine(Str.format("        var {} = new Zezex.Provider.ModuleRedirect();", rpcVarName));
			sb.AppendLine(Str.format("        {}.Argument.setModuleId({});", rpcVarName, module.getId()));
			sb.AppendLine(Str.format("        {}.Argument.setRedirectType({});", rpcVarName, methodOverride.getRedirectType()));
			sb.AppendLine(Str.format("        {}.Argument.setHashCode({});", rpcVarName, methodOverride.GetChoiceHashOrServerCodeSource()));
			sb.AppendLine(Str.format("        {}.Argument.setMethodFullName(\"{}:{}\");", rpcVarName, module.getFullName(), methodOverride.Method.getName()));
			sb.AppendLine(Str.format("        {}.Argument.setServiceNamePrefix(Game.App.ServerServiceNamePrefix);", rpcVarName));
			if (methodOverride.ParametersNormal.size() > 0) {
				// normal 包括了 out 参数，这个不需要 encode，所以下面可能仍然是空的，先这样了。
				sb.AppendLine(Str.format("        {"));
				sb.AppendLine(Str.format("            var _bb_ = Zeze.Serialize.ByteBuffer.Allocate();"));
				GenEncode(sb, "            ", "_bb_", methodOverride.ParametersNormal);
				sb.AppendLine(Str.format("            {}.Argument.setParams(new Binary(_bb_));", rpcVarName));
				sb.AppendLine(Str.format("        }"));
			}
			sb.AppendLine(Str.format(""));
			String futureVarName = "tmp" + TmpVarNameId.incrementAndGet();
			sb.AppendLine(Str.format("        var {} = new Zeze.Util.TaskCompletionSource<Long>();", futureVarName));
			sb.AppendLine(Str.format(""));
			sb.AppendLine(Str.format("        {}.Send(RandomLink(), (thisRpc) ->", rpcVarName));
			sb.AppendLine(Str.format("        {"));
			sb.AppendLine(Str.format("            if ({}.isTimeout())", rpcVarName));
			sb.AppendLine(Str.format("            {"));
			sb.AppendLine(Str.format("                {}.SetException(new RuntimeException(\"{}:{} Rpc Timeout.\"));", futureVarName, module.getFullName(), methodOverride.Method.getName()));
			sb.AppendLine(Str.format("            }"));
			sb.AppendLine(Str.format("            else if (Zezex.Provider.ModuleRedirect.ResultCodeSuccess != {}.getResultCode())", rpcVarName));
			sb.AppendLine(Str.format("            {"));
			sb.AppendLine(Str.format("                {}.SetException(new RuntimeException(\"{}:{} Rpc Error=\" + {}.getResultCode()));", futureVarName, module.getFullName(), methodOverride.Method.getName(), rpcVarName));
			sb.AppendLine(Str.format("            }"));
			sb.AppendLine(Str.format("            else"));
			sb.AppendLine(Str.format("            {"));
			if (null != methodOverride.ParameterRedirectResultHandle) {
				// decode and run if has result
				String redirectResultVarName = "tmp" + TmpVarNameId.incrementAndGet();
				String redirectResultBBVarName = "tmp" + TmpVarNameId.incrementAndGet();
				sb.AppendLine(Str.format("                var {} = Zeze.Serialize.ByteBuffer.Wrap({}.Result.getActions().get(0));", redirectResultBBVarName, rpcVarName));
				var resultClass = module.getClassByMethodName(methodOverride.Method.getName());
				GenLocalVariable(sb, "                ", resultClass, redirectResultVarName);
				GenDecode(sb, "                ", redirectResultBBVarName, resultClass, redirectResultVarName);
				sb.AppendLine(Str.format("                {}.handle({});", methodOverride.ParameterRedirectResultHandle.getName(), redirectResultVarName));
			}
			sb.AppendLine(Str.format("                {}.SetResult({}.Result.getReturnCode());", futureVarName, rpcVarName));
			sb.AppendLine(Str.format("            }"));
			sb.AppendLine(Str.format("            return Zeze.Transaction.Procedure.Success;"));
			sb.AppendLine(Str.format("        });"));
			sb.AppendLine(Str.format(""));
			if (rtn.ReturnType == ReturnType.TaskCompletionSource)
			{
				sb.AppendLine(Str.format("        return {};", futureVarName));
			}
			sb.AppendLine(Str.format("    }"));
			sb.AppendLine(Str.format(""));

			// Handles
			sbHandles.AppendLine(Str.format("        Zezex.ModuleRedirect.Instance.Handles.put(\"{}:{}\", (Long _sessionid_, Integer _hash_, Binary _params_, List<BActionParam> _actions_) ->", module.getFullName(), methodOverride.Method.getName()));
			sbHandles.AppendLine(Str.format("        {"));
			var rbbVarName = "tmp" + TmpVarNameId.incrementAndGet();
			sbHandles.AppendLine(Str.format("            var {} = Zeze.Serialize.ByteBuffer.Wrap(_params_);", rbbVarName));
			for (int i = 0; i < methodOverride.ParametersNormal.size(); ++i)
			{
				var p = methodOverride.ParametersNormal.get(i);
				if (IsKnownDelegate(p.getType()))
					continue; // define later.
				GenLocalVariable(sbHandles, "            ", p.getType(), p.getName());
			}
			GenDecode(sbHandles, "            ", rbbVarName, methodOverride.ParametersNormal);

			if (null != methodOverride.ParameterRedirectResultHandle) {
				var actionVarName = "tmp" + TmpVarNameId.incrementAndGet();
				var resultVarName = "tmp" + TmpVarNameId.incrementAndGet();
				var reqBBVarName = "tmp" + TmpVarNameId.incrementAndGet();
				sbHandles.AppendLine(Str.format("{}Zezex.RedirectResultHandle {} = ({}) -> {", "    ", actionVarName, resultVarName));
				sbHandles.AppendLine(Str.format("        var {} = Zeze.Serialize.ByteBuffer.Allocate();", reqBBVarName));
				var resultClass = module.getClassByMethodName(methodOverride.Method.getName());
				GenEncode(sbHandles, "        ", reqBBVarName, resultClass, resultVarName);
				var paramVarName = "tmp" + TmpVarNameId.incrementAndGet();
				sbHandles.AppendLine("        var " + paramVarName + " = new BActionParam();");
				sbHandles.AppendLine("        " + paramVarName + ".setName(\"" + methodOverride.ParameterRedirectResultHandle.getName() + "\");");
				sbHandles.AppendLine(Str.format("        " + paramVarName + ".setParams(new Binary({}));", reqBBVarName));
				sbHandles.AppendLine("        _actions_.add(" + paramVarName + ");");
				sbHandles.AppendLine(Str.format("		}"));
				// action.GenActionEncode(sbHandles, "            ");
			}
			String normalcall = methodOverride.GetNarmalCallString();
			String sep = normalcall.isEmpty() ? "" : ", ";
			var returnCodeVarName = "tmp" + TmpVarNameId.incrementAndGet();
			var returnParamsVarName = "tmp" + TmpVarNameId.incrementAndGet();
			sbHandles.AppendLine(Str.format("            var {} = super.{}(_hash_{}{});", returnCodeVarName, methodNameWithHash, sep, normalcall));
			sbHandles.AppendLine(Str.format("            var {} = Binary.Empty;", returnParamsVarName));
			sbHandles.AppendLine(Str.format("            return new Zezex.ModuleRedirect.Return({}, {});", returnCodeVarName, returnParamsVarName));
			sbHandles.AppendLine(Str.format("        });"));
			sbHandles.AppendLine(Str.format(""));
		}
		sb.AppendLine(Str.format("    public {}() ", genClassName));
		sb.AppendLine(Str.format("    {"));
		sb.AppendLine("        super(Game.App.Instance);");
		sb.Append(sbHandles.toString());
		sb.AppendLine(Str.format("    }"));
		sb.AppendLine(Str.format(""));
		sb.Append(sbContexts.toString());
		sb.AppendLine(Str.format("}"));
		return sb.toString();
	}

	private boolean IsKnownDelegate(Class<?> type) {
		if (type.getAnnotation(FunctionalInterface.class) != null) {
			if (type == Zezex.RedirectAllDoneHandle.class || type == Zezex.RedirectAllResultHandle.class
					|| type == Zezex.RedirectResultHandle.class)
				return true;
			throw new RuntimeException("Unknown Delegate!");
		}
		return false;
	}

	void GenRedirectAll(Zeze.Util.StringBuilderCs sb, Zeze.Util.StringBuilderCs sbHandles,
						Zeze.IModule module, MethodOverride m) throws Throwable {
		var reqVarName = "tmp" + TmpVarNameId.incrementAndGet();
		sb.AppendLine(Str.format("        var {} = new Zezex.Provider.ModuleRedirectAllRequest();", reqVarName));
		sb.AppendLine(Str.format("        {}.Argument.setModuleId({});", reqVarName, module.getId()));
		sb.AppendLine(Str.format("        {}.Argument.setHashCodeConcurrentLevel({});", reqVarName, m.GetConcurrentLevelSource()));
		sb.AppendLine(Str.format("        // {}.Argument.setHashCodes(); = // setup in linkd;", reqVarName));
		sb.AppendLine(Str.format("        {}.Argument.setMethodFullName(\"{}:{}\");", reqVarName, module.getFullName(), m.Method.getName()));
		sb.AppendLine(Str.format("        {}.Argument.setServiceNamePrefix(Game.App.ServerServiceNamePrefix);", reqVarName));

		String initOnHashEnd = (null == m.ParameterRedirectAllResultHandle) ? "" : ", " + m.ParameterRedirectAllResultHandle.getName();
		String contextVarName = "tmp" + TmpVarNameId.incrementAndGet();
		sb.AppendLine(Str.format("        var {} = new Context{}({}.Argument.getHashCodeConcurrentLevel(), {}.Argument.getMethodFullName(){});",
				contextVarName, m.Method.getName(), reqVarName, reqVarName, initOnHashEnd));
		sb.AppendLine(Str.format("        {}.Argument.setSessionId(App.Server.AddManualContextWithTimeout({}));",
				reqVarName, contextVarName));
		if (m.ParametersNormal.size() > 0)
		{
			// normal 包括了 out 参数，这个不需要 encode，所以下面可能仍然是空的，先这样了。
			sb.AppendLine(Str.format("        {"));
			String bbVarName = "tmp" + TmpVarNameId.incrementAndGet();
			sb.AppendLine(Str.format("            var {} = Zeze.Serialize.ByteBuffer.Allocate();", bbVarName));
			GenEncode(sb, "            ", bbVarName, m.ParametersNormal);
			sb.AppendLine(Str.format("            {}.Argument.setParams(new Binary({}));", reqVarName, bbVarName));
			sb.AppendLine(Str.format("        }"));
		}
		sb.AppendLine(Str.format(""));
		sb.AppendLine(Str.format("        {}.Send(RandomLink());", reqVarName));
		sb.AppendLine(Str.format("    }"));
		sb.AppendLine(Str.format(""));

		// handles
		sbHandles.AppendLine(Str.format("        Zezex.ModuleRedirect.Instance.Handles.put(\"{}:{}\", (Long _sessionid_, Integer _hash_, Binary _params_, List<BActionParam> _actions_) ->",
				module.getFullName(), m.Method.getName()));
		sbHandles.AppendLine(Str.format("        {"));
		var handleBBName = "tmp" + TmpVarNameId.incrementAndGet();
		sbHandles.AppendLine(Str.format("            var {} = Zeze.Serialize.ByteBuffer.Wrap(_params_);", handleBBName));
		for (int i = 0; i < m.ParametersNormal.size(); ++i)
		{
			var p = m.ParametersNormal.get(i);
			if (IsKnownDelegate(p.getType()))
				continue; // define later.
			GenLocalVariable(sbHandles, "            ", p.getType(), p.getName());
		}
		GenDecode(sbHandles, "            ", handleBBName, m.ParametersNormal);

		if (null != m.ParameterRedirectAllResultHandle) {
			var session = "tmp" + TmpVarNameId.incrementAndGet();
			var hash = "tmp" + TmpVarNameId.incrementAndGet();
			var rc = "tmp" + TmpVarNameId.incrementAndGet();
			var result = "tmp" + TmpVarNameId.incrementAndGet();

			sbHandles.AppendLine(Str.format("{}        Zezex.RedirectAllResultHandle {} = ({}, {}, {}, {}) -> {", "    ",
					m.ParameterRedirectAllResultHandle.getName(), session, hash, rc, result));
			sbHandles.AppendLine("                var _bb1_ = Zeze.Serialize.ByteBuffer.Allocate();");
			GenEncode(sbHandles, "                ", "_bb1_", long.class, session);
			GenEncode(sbHandles, "                ", "_bb1_", int.class, hash);
			GenEncode(sbHandles, "                ", "_bb1_", long.class, rc);
			var resultClass = module.getClassByMethodName(m.Method.getName());
			GenEncode(sbHandles, "                ", "_bb1_", resultClass, result);
			var paramVarName = "tmp" + TmpVarNameId.incrementAndGet();
			sbHandles.AppendLine("                var " + paramVarName + " = new BActionParam();");
			sbHandles.AppendLine("                " + paramVarName + ".setName(\"" + m.ParameterRedirectAllResultHandle.getName() + "\");");
			sbHandles.AppendLine("                " + paramVarName + ".setParams(new Binary(_bb_));");
			sbHandles.AppendLine("                _actions_.add(" + paramVarName + ");");
			sbHandles.AppendLine(Str.format("            };"));
			// action.GenActionEncode(sbHandles, "            ");
		}
		String normalcall = m.GetNarmalCallString((pInfo) -> pInfo.getType() == Zezex.RedirectAllDoneHandle.class);
		String sep = normalcall.isEmpty() ? "" : ", ";
		var returnCodeVarName = "tmp" + TmpVarNameId.incrementAndGet();
		sbHandles.AppendLine(Str.format("            var {} = super.{}(_sessionid_, _hash_{}{});",
				returnCodeVarName, GetMethodNameWithHash(m.Method.getName()), sep, normalcall));
		sbHandles.AppendLine(Str.format("            return new Zezex.ModuleRedirect.Return({}, Binary.Empty);", returnCodeVarName));
		sbHandles.AppendLine(Str.format("        });"));
		sbHandles.AppendLine(Str.format(""));
	}

	void GenRedirectAllContext(Zeze.Util.StringBuilderCs sb, Zeze.IModule module, MethodOverride m) throws Throwable {
		sb.AppendLine(Str.format("    public static class Context{} extends Zezex.Provider.ModuleProvider.ModuleRedirectAllContext", m.Method.getName()));
		sb.AppendLine(Str.format("    {"));
		if (null != m.ParameterRedirectAllResultHandle)
			sb.AppendLine(Str.format("        private Zezex.RedirectAllResultHandle redirectAllResultHandle;"));
		sb.AppendLine(Str.format(""));
		if (null != m.ParameterRedirectAllResultHandle)
			sb.AppendLine(Str.format("        public Context{}(int _c_, String _n_, Zezex.RedirectAllResultHandle _r_) {", m.Method.getName()));
		else
			sb.AppendLine(Str.format("        public Context{}(int _c_, String _n_) {", m.Method.getName()));

		sb.AppendLine(Str.format("        	super(_c_, _n_);"));
		if (null != m.ParameterRedirectAllResultHandle)
			sb.AppendLine("            this.redirectAllResultHandle = _r_;");
		sb.AppendLine("        }");
		sb.AppendLine("");
		sb.AppendLine("        @Override");
		sb.AppendLine("        public long ProcessHashResult(int _hash_, long _returnCode_, Binary _params, List<BActionParam> _actions_) throws Throwable");
		sb.AppendLine("        {");
		if (null != m.ParameterRedirectAllResultHandle) {
			var allHashResultBBName = "tmp" + TmpVarNameId.incrementAndGet();
			sb.AppendLine(Str.format("            var {} = Zeze.Serialize.ByteBuffer.Wrap(_actions_.get(0).getParams());", allHashResultBBName));
			var resultClass = module.getClassByMethodName(m.Method.getName());
			GenLocalVariable(sb, "            ", resultClass, "_result_bean_");
			GenDecode(sb, "            ", allHashResultBBName, resultClass, "_result_bean_");
			sb.AppendLine(Str.format("            redirectAllResultHandle.handle(super.getSessionId(), _hash_, _returnCode_, _result_bean_);"));
		}
		sb.AppendLine("            return Zeze.Transaction.Procedure.Success;");
		sb.AppendLine("        }");
		sb.AppendLine("    }");
		sb.AppendLine("");
	}

	// loopback 优化
	public boolean IsLocalServer(String moduleName) {
		// 要实现真正的 loopback，
		// 需要实现server-server之间直连并且可以得到当前的可用服务。
		// 通过linkd转发时，当前server没有足够信息做这个优化。
		return false;
	}

	static class KnownSerializer {
		public Zeze.Util.Action4<Zeze.Util.StringBuilderCs, String, String, String> Encoder;
		public Zeze.Util.Action4<Zeze.Util.StringBuilderCs, String, String, String> Decoder;
		public Zeze.Util.Action3<Zeze.Util.StringBuilderCs, String, String> Define;
		public Zeze.Util.Func0<String> TypeName;

		public KnownSerializer(Zeze.Util.Action4<Zeze.Util.StringBuilderCs, String, String, String> enc,
							   Zeze.Util.Action4<Zeze.Util.StringBuilderCs, String, String, String> dec,
							   Zeze.Util.Action3<Zeze.Util.StringBuilderCs, String, String> def,
							   Zeze.Util.Func0<String> typeName) {
			Encoder = enc;
			Decoder = dec;
			Define = def;
			TypeName = typeName;
		}
	}

	private HashMap<java.lang.Class, KnownSerializer> Serializer = new HashMap<>();
	private AtomicLong TmpVarNameId = new AtomicLong();

	private ModuleRedirect() {
		compiler = InMemoryJavaCompiler.newInstance();
		compiler.ignoreWarnings();

		Serializer.put(Zeze.Net.Binary.class, new KnownSerializer(
				(sb, prefix, varName, bbName) -> sb.AppendLine(Str.format("{}{}.WriteBinary({});", prefix, bbName, varName)),
				(sb, prefix, varName, bbName) -> sb.AppendLine(Str.format("{}{} = {}.ReadBinary();", prefix, varName, bbName)),
				(sb, prefix, varName) -> sb.AppendLine(Str.format("{}Binary {} = null;", prefix, varName)),
				() -> "Binary")
		);
		Serializer.put(Boolean.class, new KnownSerializer(
				(sb, prefix, varName, bbName) -> sb.AppendLine(Str.format("{}{}.WriteBool({});", prefix, bbName, varName)),
				(sb, prefix, varName, bbName) -> sb.AppendLine(Str.format("{}{} = {}.ReadBool();", prefix, varName, bbName)),
				(sb, prefix, varName) -> sb.AppendLine(Str.format("{}bool {} = false;", prefix, varName)),
				() -> "boolean")
		);
		Serializer.put(boolean.class, new KnownSerializer(
				(sb, prefix, varName, bbName) -> sb.AppendLine(Str.format("{}{}.WriteBool({});", prefix, bbName, varName)),
				(sb, prefix, varName, bbName) -> sb.AppendLine(Str.format("{}{} = {}.ReadBool();", prefix, varName, bbName)),
				(sb, prefix, varName) -> sb.AppendLine(Str.format("{}bool {} = false;", prefix, varName)),
				() -> "boolean")
		);
		Serializer.put(Byte.class, new KnownSerializer(
				(sb, prefix, varName, bbName) -> sb.AppendLine(Str.format("{}{}.WriteByte({});", prefix, bbName, varName)),
				(sb, prefix, varName, bbName) -> sb.AppendLine(Str.format("{}{} = {}.ReadByte();", prefix, varName, bbName)),
				(sb, prefix, varName) -> sb.AppendLine(Str.format("{}byte {1} = 0;", prefix, varName)),
				() -> "byte")
		);
		Serializer.put(byte.class, new KnownSerializer(
				(sb, prefix, varName, bbName) -> sb.AppendLine(Str.format("{}{}.WriteByte({});", prefix, bbName, varName)),
				(sb, prefix, varName, bbName) -> sb.AppendLine(Str.format("{}{} = {}.ReadByte();", prefix, varName, bbName)),
				(sb, prefix, varName) -> sb.AppendLine(Str.format("{}byte {} = 0;", prefix, varName)),
				() -> "byte")
		);
		Serializer.put(Zeze.Serialize.ByteBuffer.class, new KnownSerializer(
				(sb, prefix, varName, bbName) -> sb.AppendLine(Str.format("{}{}.WriteByteBuffer({});", prefix, bbName, varName)),
				(sb, prefix, varName, bbName) -> sb.AppendLine(Str.format("{}{} = Zeze.Serialize.ByteBuffer.Wrap({}.ReadBytes());", prefix, varName, bbName)),
				(sb, prefix, varName) -> sb.AppendLine(Str.format("{}Zeze.Serialize.ByteBuffer {} = null;", prefix, varName)),
				() -> "Zeze.Serialize.ByteBuffer")
		);
		Serializer.put(byte[].class, new KnownSerializer(
				(sb, prefix, varName, bbName) -> sb.AppendLine(Str.format("{}{}.WriteBytes({});", prefix, bbName, varName)),
				(sb, prefix, varName, bbName) -> sb.AppendLine(Str.format("{}{} = {}.ReadBytes();", prefix, varName, bbName)),
				(sb, prefix, varName) -> sb.AppendLine(Str.format("{}byte[] {} = null;", prefix, varName)),
				() -> "byte[]")
		);
		Serializer.put(Double.class, new KnownSerializer(
				(sb, prefix, varName, bbName) -> sb.AppendLine(Str.format("{}{}.WriteDouble({});", prefix, bbName, varName)),
				(sb, prefix, varName, bbName) -> sb.AppendLine(Str.format("{}{} = {}.ReadDouble();", prefix, varName, bbName)),
				(sb, prefix, varName) -> sb.AppendLine(Str.format("{}double {} = 0.0;", prefix, varName)),
				() -> "double")
		);
		Serializer.put(double.class, new KnownSerializer(
				(sb, prefix, varName, bbName) -> sb.AppendLine(Str.format("{}{}.WriteDouble({});", prefix, bbName, varName)),
				(sb, prefix, varName, bbName) -> sb.AppendLine(Str.format("{}{} = {}.ReadDouble();", prefix, varName, bbName)),
				(sb, prefix, varName) -> sb.AppendLine(Str.format("{}double {} = 0.0;", prefix, varName)),
				() -> "double")
		);
		Serializer.put(Float.class, new KnownSerializer(
				(sb, prefix, varName, bbName) -> sb.AppendLine(Str.format("{}{}.WriteFloat({});", prefix, bbName, varName)),
				(sb, prefix, varName, bbName) -> sb.AppendLine(Str.format("{}{} = {}.ReadFloat();", prefix, varName, bbName)),
				(sb, prefix, varName) -> sb.AppendLine(Str.format("{}float {} = 0.0;", prefix, varName)),
				() -> "float")
		);
		Serializer.put(float.class, new KnownSerializer(
				(sb, prefix, varName, bbName) -> sb.AppendLine(Str.format("{}{}.WriteFloat({});", prefix, bbName, varName)),
				(sb, prefix, varName, bbName) -> sb.AppendLine(Str.format("{}{} = {}.ReadFloat();", prefix, varName, bbName)),
				(sb, prefix, varName) -> sb.AppendLine(Str.format("{}float {} = 0.0;", prefix, varName)),
				() -> "float")
		);
		Serializer.put(Integer.class, new KnownSerializer(
				(sb, prefix, varName, bbName) -> sb.AppendLine(Str.format("{}{}.WriteInt({});", prefix, bbName, varName)),
				(sb, prefix, varName, bbName) -> sb.AppendLine(Str.format("{}{} = {}.ReadInt();", prefix, varName, bbName)),
				(sb, prefix, varName) -> sb.AppendLine(Str.format("{}int {} = 0;", prefix, varName)),
				() -> "int")
		);
		Serializer.put(int.class, new KnownSerializer(
				(sb, prefix, varName, bbName) -> sb.AppendLine(Str.format("{}{}.WriteInt({});", prefix, bbName, varName)),
				(sb, prefix, varName, bbName) -> sb.AppendLine(Str.format("{}{} = {}.ReadInt();", prefix, varName, bbName)),
				(sb, prefix, varName) -> sb.AppendLine(Str.format("{}int {} = 0;", prefix, varName)),
				() -> "int")
		);
		Serializer.put(Long.class, new KnownSerializer(
				(sb, prefix, varName, bbName) -> sb.AppendLine(Str.format("{}{}.WriteLong({});", prefix, bbName, varName)),
				(sb, prefix, varName, bbName) -> sb.AppendLine(Str.format("{}{} = {}.ReadLong();", prefix, varName, bbName)),
				(sb, prefix, varName) -> sb.AppendLine(Str.format("{}long {} = 0;", prefix, varName)),
				() -> "long")
		);
		Serializer.put(long.class, new KnownSerializer(
				(sb, prefix, varName, bbName) -> sb.AppendLine(Str.format("{}{}.WriteLong({});", prefix, bbName, varName)),
				(sb, prefix, varName, bbName) -> sb.AppendLine(Str.format("{}{} = {}.ReadLong();", prefix, varName, bbName)),
				(sb, prefix, varName) -> sb.AppendLine(Str.format("{}long {} = 0;", prefix, varName)),
				() -> "long")
		);
		Serializer.put(Short.class, new KnownSerializer(
				(sb, prefix, varName, bbName) -> sb.AppendLine(Str.format("{}{}.WriteShort({});", prefix, bbName, varName)),
				(sb, prefix, varName, bbName) -> sb.AppendLine(Str.format("{}{} = {}.ReadShort();", prefix, varName, bbName)),
				(sb, prefix, varName) -> sb.AppendLine(Str.format("{}short {} = 0;", prefix, varName)),
				() -> "short")
		);
		Serializer.put(short.class, new KnownSerializer(
				(sb, prefix, varName, bbName) -> sb.AppendLine(Str.format("{}{}.WriteShort({});", prefix, bbName, varName)),
				(sb, prefix, varName, bbName) -> sb.AppendLine(Str.format("{}{} = {}.ReadShort();", prefix, varName, bbName)),
				(sb, prefix, varName) -> sb.AppendLine(Str.format("{}short {} = 0;", prefix, varName)),
				() -> "short")
		);
		Serializer.put(String.class, new KnownSerializer(
				(sb, prefix, varName, bbName) -> sb.AppendLine(Str.format("{}{}.WriteString({});", prefix, bbName, varName)),
				(sb, prefix, varName, bbName) -> sb.AppendLine(Str.format("{}{} = {}.ReadString();", prefix, varName, bbName)),
				(sb, prefix, varName) -> sb.AppendLine(Str.format("{}string {} = null;", prefix, varName)),
				() -> "String")
		);
	}

	public String GetTypeName(Class<?> type) throws Throwable {
		var kn = Serializer.get(type);
		if (null != kn)
			return kn.TypeName.call();

		if (Zeze.Serialize.Serializable.class.isAssignableFrom(type))
			return type.getTypeName();

		return type.getTypeName();
	}

	public void GenLocalVariable(Zeze.Util.StringBuilderCs sb, String prefix, Class<?> type, String varName) throws Throwable {
		var kn = Serializer.get(type);
		if (null != kn) {
			kn.Define.run(sb, prefix, varName);
			return;
		}

		if (Zeze.Serialize.Serializable.class.isAssignableFrom(type)) {
			sb.AppendLine(Str.format("{}{} {} = new {}();",
					prefix, type.getTypeName(), varName, type.getTypeName()));
			return;
		}

		sb.AppendLine(Str.format("{}{} {} = null;", prefix, type.getTypeName(), varName));
	}

	public void GenEncode(Zeze.Util.StringBuilderCs sb, String prefix, String bbName, Class<?> type, String varName) throws Throwable {
		var kn = Serializer.get(type);
		if (null != kn) {
			kn.Encoder.run(sb, prefix, varName, bbName);
			return;
		}

		if (Zeze.Serialize.Serializable.class.isAssignableFrom(type)) {
			sb.AppendLine(Str.format("{}{}.Encode({});", prefix, varName, bbName));
			return;
		}

		sb.AppendLine(Str.format("{}try {", prefix));
		sb.AppendLine(Str.format("{}    try (var output = new java.io.ByteArrayOutputStream(); var objOutput = new java.io.ObjectOutputStream(output))", prefix));
		sb.AppendLine(Str.format("{}    {", prefix));
		sb.AppendLine(Str.format("{}        objOutput.writeObject({});", prefix, varName));
		sb.AppendLine(Str.format("{}        {}.WriteBytes(output.toByteArray());", prefix, bbName));
		sb.AppendLine(Str.format("{}	   }", prefix));
		sb.AppendLine(Str.format("{}} catch (Throwable e) {", prefix));
		sb.AppendLine(Str.format("{}    throw new RuntimeException(e);", prefix));
		sb.AppendLine(Str.format("{}}", prefix));
	}

	public void GenDecode(Zeze.Util.StringBuilderCs sb, String prefix, String bbName, Class<?> type, String varName) throws Throwable {
		var kn = Serializer.get(type);
		if (null != kn) {
			kn.Decoder.run(sb, prefix, varName, bbName);
			return;
		}

		if (Zeze.Serialize.Serializable.class.isAssignableFrom(type)) {
			sb.AppendLine(Str.format("{}{}.Decode({});", prefix, varName, bbName));
			return;
		}
		String tmp1 = "tmp" + TmpVarNameId.incrementAndGet();
		String tmp2 = "tmp" + TmpVarNameId.incrementAndGet();
		//String tmp3 = "tmp" + TmpVarNameId.incrementAndGet();
		sb.AppendLine(Str.format("{}try {", prefix));
		sb.AppendLine(Str.format("{}    var {} = {}.ReadByteBuffer();", prefix, tmp1, bbName));
		sb.AppendLine(Str.format("{}    try (var {} = new java.io.ByteArrayInputStream({}.Bytes, {}.ReadIndex, {}.Size()); var objinput = new java.io.ObjectInputStream({})) {",
				prefix, tmp2, tmp1, tmp1, tmp1, tmp2));
		sb.AppendLine(Str.format("{}        {} = ({})objinput.readObject();", prefix, varName, GetTypeName(type)));
		sb.AppendLine(Str.format("{}    }", prefix));
		sb.AppendLine(Str.format("{}} catch (Throwable e) {", prefix));
		sb.AppendLine(Str.format("{}    throw new RuntimeException(e);", prefix));
		sb.AppendLine(Str.format("{}}", prefix));
	}

	private boolean IsOut(Class<?> type) {
		return false;
		// return type == Zeze.Util.OutObject.class;
	}

	private boolean IsRef(Class<?> type) {
		return false;
		//return type == Zeze.Util.RefObject.class;
	}

	public void GenEncode(Zeze.Util.StringBuilderCs sb, String prefix, String bbName, List<java.lang.reflect.Parameter> parameters) throws Throwable {
		for (int i = 0; i < parameters.size(); ++i)  {
			var p = parameters.get(i);
			if (IsOut(p.getType()))
				continue;
			if (IsKnownDelegate(p.getType()))
				continue;
			GenEncode(sb, prefix, bbName, p.getType(), p.getName());
		}
	}

	public void GenDecode(Zeze.Util.StringBuilderCs sb, String prefix, String bbName, List<java.lang.reflect.Parameter> parameters) throws Throwable {
		for (int i = 0; i < parameters.size(); ++i) {
			var p = parameters.get(i);
			if (IsOut(p.getType()))
				continue;
			if (IsKnownDelegate(p.getType()))
				continue;
			GenDecode(sb, prefix, bbName, p.getType(), p.getName());
		}
	}

	public String ToDefineString(java.lang.reflect.Parameter[] parameters) throws Throwable {
		var sb = new Zeze.Util.StringBuilderCs();
		boolean first = true;
		for (var p : parameters) {
			if (first)
				first = false;
			else
				sb.Append(", ");
			sb.Append(GetTypeName(p.getType()));
			sb.Append(" ");
			sb.Append(p.getName());
		}
		return sb.toString();
	}
}
