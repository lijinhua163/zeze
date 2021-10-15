package UnitTest.Zeze.Trans;

import Zeze.Transaction.*;
import Zeze.Serialize.*;
import Zeze.Net.*;
import UnitTest.*;
import java.util.*;

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [TestClass] public class TestChangeListener
public class TestChangeListener {
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [TestInitialize] public void TestInit()
	public final void TestInit() {
		demo.App.getInstance().Start();
	}

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [TestCleanup] public void TestCleanup()
	public final void TestCleanup() {
		demo.App.getInstance().Stop();
	}

	private void Prepare() {
		assert Procedure.Success == demo.App.getInstance().getZeze().NewProcedure(() -> {
					demo.App.getInstance().getDemoModule1().getTable1().Remove(1);
					return Procedure.Success;
		}, "TestChangeListener.Remove", null).Call();

		assert Procedure.Success == demo.App.getInstance().getZeze().NewProcedure(() -> {
					demo.Module1.Value value = demo.App.getInstance().getDemoModule1().getTable1().GetOrAdd(1);
					value.Int1 = 123;
					value.Long2 = 123;
					value.String3 = "123";
					value.Bool4 = true;
					value.Short5 = 123;
					value.Float6 = 123.0f;
					value.Double7 = 123.0;
					value.Bytes8 = Binary.Empty;
					demo.Bean1 tempVar = new demo.Bean1();
					tempVar.setV1(1);
					value.getList9().Add(tempVar);
					demo.Bean1 tempVar2 = new demo.Bean1();
					tempVar2.setV1(2);
					value.getList9().Add(tempVar2);
					value.getSet10().Add(123);
					value.getSet10().Add(124);
					value.getMap11().Add(1, new demo.Module2.Value());
					value.getMap11().Add(2, new demo.Module2.Value());
					value.getBean12().Int1 = 123;
					value.Byte13 = 12;
					value.Dynamic14_demo_Module1_Simple = new demo.Module1.Simple();
					value.getDynamic14DemoModule1Simple().setInt1(123);
					value.getMap15().Add(1, 1);
					value.getMap15().Add(2, 2);
					return Procedure.Success;
		}, "TestChangeListener.Prepare", null).Call();
	}

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [TestMethod] public void TestAllType()
	public final void TestAllType() {
		Prepare();
		AddListener();

		Init();
		assert Procedure.Success == demo.App.getInstance().getZeze().NewProcedure(() -> {
					demo.Module1.Value value = demo.App.getInstance().getDemoModule1().getTable1().GetOrAdd(1);
					value.Int1 = 124;
					value.Long2 = 124;
					value.String3 = "124";
					value.Bool4 = false;
					value.Short5 = 124;
					value.Float6 = 124.0f;
					value.Double7 = 124.0;
					value.Bytes8 = new Binary(new byte[4]);
					demo.Bean1 tempVar = new demo.Bean1();
					tempVar.setV1(2);
					value.getList9().Add(tempVar);
					demo.Bean1 tempVar2 = new demo.Bean1();
					tempVar2.setV1(3);
					value.getList9().Add(tempVar2);
					value.getSet10().Add(125);
					value.getSet10().Add(126);
					value.getMap11().Add(3, new demo.Module2.Value());
					value.getMap11().Add(4, new demo.Module2.Value());
					value.getBean12().Int1 = 124;
					value.Byte13 = 13;
					value.Dynamic14_demo_Module1_Simple = new demo.Module1.Simple();
					value.getDynamic14DemoModule1Simple().setInt1(124);
					value.getMap15().Add(3, 3);
					value.getMap15().Add(4, 4);
					return Procedure.Success;
		}, "TestChangeListener.Modify", null).Call();
		Verify();

		Init();
		assert Procedure.Success == demo.App.getInstance().getZeze().NewProcedure(() -> {
					demo.Module1.Value value = demo.App.getInstance().getDemoModule1().getTable1().GetOrAdd(1);
					value.getSet10().Add(127);
					value.getSet10().Remove(124);
					value.getMap11().Add(5, new demo.Module2.Value());
					value.getMap11().Add(6, new demo.Module2.Value());
					value.getMap11().Remove(1);
					value.getMap11().Remove(2);
					value.getMap15().Add(5, 5);
					value.getMap15().Add(6, 6);
					value.getMap15().Remove(1);
					value.getMap15().Remove(2);
					return Procedure.Success;
		}, "TestChangeListener.ModifyCollections", null).Call();
		Verify();

		Init();
		assert Procedure.Success == demo.App.getInstance().getZeze().NewProcedure(() -> {
					demo.Module1.Value value = demo.App.getInstance().getDemoModule1().getTable1().GetOrAdd(1);
					ArrayList<Integer> except = new ArrayList<Integer>(Arrays.asList(1, 2));
					value.getSet10().ExceptWith(except);
					return Procedure.Success;
		}, "TestChangeListener.ModifySetExcept", null).Call();
		Verify();

		Init();
		assert Procedure.Success == demo.App.getInstance().getZeze().NewProcedure(() -> {
					demo.Module1.Value value = demo.App.getInstance().getDemoModule1().getTable1().GetOrAdd(1);
					ArrayList<Integer> intersect = new ArrayList<Integer>(Arrays.asList(123, 126));
					value.getSet10().IntersectWith(intersect);
					return Procedure.Success;
		}, "TestChangeListener.ModifySetIntersect", null).Call();
		Verify();

		Init();
		assert Procedure.Success == demo.App.getInstance().getZeze().NewProcedure(() -> {
					demo.Module1.Value value = demo.App.getInstance().getDemoModule1().getTable1().GetOrAdd(1);
					ArrayList<Integer> SymmetricExcept = new ArrayList<Integer>(Arrays.asList(123, 140));
					value.getSet10().SymmetricExceptWith(SymmetricExcept);
					return Procedure.Success;
		}, "TestChangeListener.ModifySetSymmetricExcept", null).Call();
		Verify();

		Init();
		assert Procedure.Success == demo.App.getInstance().getZeze().NewProcedure(() -> {
					demo.Module1.Value value = demo.App.getInstance().getDemoModule1().getTable1().GetOrAdd(1);
					ArrayList<Integer> Union = new ArrayList<Integer>(Arrays.asList(123, 140));
					value.getSet10().UnionWith(Union);
					return Procedure.Success;
		}, "TestChangeListener.ModifySetUnion", null).Call();
		Verify();

		Init();
		assert Procedure.Success == demo.App.getInstance().getZeze().NewProcedure(() -> {
					demo.App.getInstance().getDemoModule1().getTable1().Put(1, new demo.Module1.Value());
					return Procedure.Success;
		}, "TestChangeListener.PutRecord", null).Call();
		Verify();

		Init();
		assert Procedure.Success == demo.App.getInstance().getZeze().NewProcedure(() -> {
					demo.App.getInstance().getDemoModule1().getTable1().Remove(1);
					return Procedure.Success;
		}, "TestChangeListener.RemoveRecord", null).Call();
		Verify();
	}

	private demo.Module1.Value localValue;

	private void Init() {
		assert Procedure.Success == demo.App.getInstance().getZeze().NewProcedure(() -> {
					demo.Module1.Value value = demo.App.getInstance().getDemoModule1().getTable1().Get(1);
					localValue = value == null ? null : value.Copy();
					return Procedure.Success;
		}, "TestChangeListener.CopyLocal", null).Call();


		_CLInt1.Init(localValue);
		_ClLong2.Init(localValue);
		_CLString3.Init(localValue);
		_CLBool4.Init(localValue);
		_CLShort5.Init(localValue);
		_CLFloat6.Init(localValue);
		_CLDouble7.Init(localValue);
		_CLBytes8.Init(localValue);
		_CLList9.Init(localValue);
		_CLSet10.Init(localValue);
		_CLMap11.Init(localValue);
		_CLBean12.Init(localValue);
		_CLByte13.Init(localValue);
		_ClDynamic14.Init(localValue);
		_CLMap15.Init(localValue);
	}

	private void Verify() {
		assert Procedure.Success == demo.App.getInstance().getZeze().NewProcedure(() -> {
					demo.Module1.Value value = demo.App.getInstance().getDemoModule1().getTable1().Get(1);
					localValue = value == null ? null : value.Copy();
					return Procedure.Success;
		}, "TestChangeListener.CopyLocal", null).Call();

		_CLInt1.Verify(localValue);
		_ClLong2.Verify(localValue);
		_CLString3.Verify(localValue);
		_CLBool4.Verify(localValue);
		_CLShort5.Verify(localValue);
		_CLFloat6.Verify(localValue);
		_CLDouble7.Verify(localValue);
		_CLBytes8.Verify(localValue);
		_CLList9.Verify(localValue);
		_CLSet10.Verify(localValue);
		_CLMap11.Verify(localValue);
		_CLBean12.Verify(localValue);
		_CLByte13.Verify(localValue);
		_ClDynamic14.Verify(localValue);
		_CLMap15.Verify(localValue);
	}

	private void AddListener() {
		demo.App.getInstance().getDemoModule1().getTable1().ChangeListenerMap.AddListener(demo.Module1.Table1.VAR_int1, _CLInt1);
		demo.App.getInstance().getDemoModule1().getTable1().ChangeListenerMap.AddListener(demo.Module1.Table1.VAR_long2, _ClLong2);
		demo.App.getInstance().getDemoModule1().getTable1().ChangeListenerMap.AddListener(demo.Module1.Table1.VAR_string3, _CLString3);
		demo.App.getInstance().getDemoModule1().getTable1().ChangeListenerMap.AddListener(demo.Module1.Table1.VAR_bool4, _CLBool4);
		demo.App.getInstance().getDemoModule1().getTable1().ChangeListenerMap.AddListener(demo.Module1.Table1.VAR_short5, _CLShort5);
		demo.App.getInstance().getDemoModule1().getTable1().ChangeListenerMap.AddListener(demo.Module1.Table1.VAR_float6, _CLFloat6);
		demo.App.getInstance().getDemoModule1().getTable1().ChangeListenerMap.AddListener(demo.Module1.Table1.VAR_double7, _CLDouble7);
		demo.App.getInstance().getDemoModule1().getTable1().ChangeListenerMap.AddListener(demo.Module1.Table1.VAR_bytes8, _CLBytes8);
		demo.App.getInstance().getDemoModule1().getTable1().ChangeListenerMap.AddListener(demo.Module1.Table1.VAR_list9, _CLList9);
		demo.App.getInstance().getDemoModule1().getTable1().ChangeListenerMap.AddListener(demo.Module1.Table1.VAR_set10, _CLSet10);
		demo.App.getInstance().getDemoModule1().getTable1().ChangeListenerMap.AddListener(demo.Module1.Table1.VAR_map11, _CLMap11);
		demo.App.getInstance().getDemoModule1().getTable1().ChangeListenerMap.AddListener(demo.Module1.Table1.VAR_bean12, _CLBean12);
		demo.App.getInstance().getDemoModule1().getTable1().ChangeListenerMap.AddListener(demo.Module1.Table1.VAR_byte13, _CLByte13);
		demo.App.getInstance().getDemoModule1().getTable1().ChangeListenerMap.AddListener(demo.Module1.Table1.VAR_dynamic14, _ClDynamic14);
		demo.App.getInstance().getDemoModule1().getTable1().ChangeListenerMap.AddListener(demo.Module1.Table1.VAR_map15, _CLMap15);
	}

	private final CLInt1 _CLInt1 = new CLInt1();
	private final ClLong2 _ClLong2 = new ClLong2();
	private final CLString3 _CLString3 = new CLString3();
	private final CLBool4 _CLBool4 = new CLBool4();
	private final CLShort5 _CLShort5 = new CLShort5();
	private final CLFloat6 _CLFloat6 = new CLFloat6();
	private final CLDouble7 _CLDouble7 = new CLDouble7();
	private final CLBytes8 _CLBytes8 = new CLBytes8();
	private final CLList9 _CLList9 = new CLList9();
	private final CLSet10 _CLSet10 = new CLSet10();
	private final CLMap11 _CLMap11 = new CLMap11();
	private final CLBean12 _CLBean12 = new CLBean12();
	private final CLByte13 _CLByte13 = new CLByte13();
	private final ClDynamic14 _ClDynamic14 = new ClDynamic14();
	private final CLMap15 _CLMap15 = new CLMap15();

	private static class CLMap15 implements ChangeListener {
		private HashMap<Long, Long> newValue;

		public final void Init(demo.Module1.Value current) {
			if (null != current) {
				newValue = new HashMap<Long, Long>();
				for (var e : current.getMap15()) {
					newValue.put(e.Key, e.Value);
				}
			}
			else {
				newValue = null;
			}
		}

		public final void Verify(demo.Module1.Value current) {
			if (null == current) {
				assert null == newValue;
				return;
			}
			HashMap<Long, Long> newValueCopy = new HashMap<Long, Long>();
			for (var e : ((demo.Module1.Value)current).getMap15()) {
				newValueCopy.put(e.Key, e.Value);
			}
			assert newValue.size() == newValueCopy.size();
			for (var e : newValue.entrySet()) {
				TValue exist;
				assert newValueCopy.containsKey(e.getKey()) && (exist = newValueCopy.get(e.getKey())) == exist;
				assert e.getValue() == exist;
			}
		}

		public final void OnChanged(Object key, Bean value) {
			newValue = new HashMap<Long, Long>();
			for (var e : ((demo.Module1.Value)value).getMap15()) {
				newValue.put(e.Key, e.Value);
			}
		}

		public final void OnChanged(Object key, Bean value, ChangeNote note) {
			ChangeNoteMap1<Long, Long> notemap1 = (ChangeNoteMap1<Long, Long>)note;

			for (var a : notemap1.Replaced) {
				newValue.put(a.Key, a.Value);
			}
			for (var r : notemap1.Removed) {
				newValue.remove(r);
			}
		}

		public final void OnRemoved(Object key) {
			newValue = null;
		}
	}

	private static class ClDynamic14 implements ChangeListener {
		private Bean newValue;

		public final void Init(demo.Module1.Value current) {
			newValue = current == null ? null : current.getDynamic14().CopyBean();
		}

		public final void Verify(demo.Module1.Value current) {
			if (null == current) {
				assert null == newValue;
				return;
			}
			assert newValue.TypeId == current.getDynamic14().TypeId;
			if (newValue.TypeId == demo.Module1.Simple.TYPEID) {
				demo.Module1.Simple newSimple = newValue instanceof demo.Module1.Simple ? (demo.Module1.Simple)newValue : null;
				Zeze.Transaction.Bean tempVar = current.getDynamic14().Bean;
				demo.Module1.Simple currentSimple = tempVar instanceof demo.Module1.Simple ? (demo.Module1.Simple)tempVar : null;
				assert null != newSimple;
				assert null != currentSimple;
				assert newSimple.getInt1() == currentSimple.getInt1();
			}
		}

		public final void OnChanged(Object key, Bean value) {
			newValue = ((demo.Module1.Value)value).getDynamic14();
		}

		public final void OnChanged(Object key, Bean value, ChangeNote note) {
			newValue = ((demo.Module1.Value)value).getDynamic14();
		}

		public final void OnRemoved(Object key) {
			newValue = null;
		}
	}

	private static class CLByte13 implements ChangeListener {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: private byte newValue;
		private byte newValue;

		public final void Init(demo.Module1.Value current) {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: newValue = (null != current) ? current.Byte13 : (byte)255;
			newValue = (null != current) ? current.getByte13() : (byte)255;
		}

		public final void Verify(demo.Module1.Value current) {
			if (null == current) {
				assert 255 == newValue;
				return;
			}
			assert newValue == current.getByte13();
		}

		public final void OnChanged(Object key, Bean value) {
			newValue = ((demo.Module1.Value)value).getByte13();
		}

		public final void OnChanged(Object key, Bean value, ChangeNote note) {
			newValue = ((demo.Module1.Value)value).getByte13();
		}

		public final void OnRemoved(Object key) {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: newValue = 255;
			newValue = (byte)255;
		}
	}

	private static class CLBean12 implements ChangeListener {
		private demo.Module1.Simple newValue;

		public final void Init(demo.Module1.Value current) {
			if (null != current) {
				newValue = current.getBean12().Copy();
			}
			else {
				newValue = null;
			}
		}
		public final void Verify(demo.Module1.Value current) {
			if (null == current) {
				assert null == newValue;
				return;
			}
			assert newValue.getInt1() == current.getBean12().getInt1();
		}

		public final void OnChanged(Object key, Bean value) {
			newValue = ((demo.Module1.Value)value).getBean12().Copy();
		}

		public final void OnChanged(Object key, Bean value, ChangeNote note) {
			newValue = ((demo.Module1.Value)value).getBean12().Copy();
		}

		public final void OnRemoved(Object key) {
			newValue = null;
		}
	}

	private static class CLMap11 implements ChangeListener {
		private HashMap<Long, demo.Module2.Value> newValue;

		public final void Init(demo.Module1.Value current) {
			if (null != current) {
				newValue = new HashMap<Long, demo.Module2.Value>();
				for (var e : current.getMap11()) {
					newValue.put(e.Key, e.Value.Copy());
				}
			}
			else {
				newValue = null;
			}
		}

		public final void Verify(demo.Module1.Value current) {
			if (null == current) {
				assert null == newValue;
				return;
			}
			HashMap<Long, demo.Module2.Value> newValueCopy = new HashMap<Long, demo.Module2.Value>();
			for (var e : ((demo.Module1.Value)current).getMap11()) {
				newValueCopy.put(e.Key, e.Value.Copy());
			}
			assert newValue.size() == newValueCopy.size();
			for (var e : newValue.entrySet()) {
				TValue exist;
				assert newValueCopy.containsKey(e.getKey()) && (exist = newValueCopy.get(e.getKey())) == exist;
				assert e.getValue().S == exist.S;
			}
		}

		public final void OnChanged(Object key, Bean value) {
			newValue = new HashMap<Long, demo.Module2.Value>();
			for (var e : ((demo.Module1.Value)value).getMap11()) {
				newValue.put(e.Key, e.Value.Copy());
			}
		}

		public final void OnChanged(Object key, Bean value, ChangeNote note) {
			ChangeNoteMap2<Long, demo.Module2.Value> notemap2 = (ChangeNoteMap2<Long, demo.Module2.Value>)note;
			notemap2.MergeChangedToReplaced(((demo.Module1.Value)value).getMap11());

			for (var a : notemap2.Replaced) {
				newValue.put(a.Key, a.Value);
			}
			for (var r : notemap2.Removed) {
				newValue.remove(r);
			}
		}

		public final void OnRemoved(Object key) {
			newValue = null;
		}
	}

	private static class CLSet10 implements ChangeListener {
		private HashSet<Integer> newValue;

		public final void Init(demo.Module1.Value current) {
			if (null != current) {
				newValue = new HashSet<Integer>();
				for (var i : current.getSet10()) {
					newValue.add(i);
				}
			}
			else {
				newValue = null;
			}
		}

		public final void Verify(demo.Module1.Value current) {
			if (null == current) {
				assert null == newValue;
				return;
			}
			HashSet<Integer> newValueCopy = new HashSet<Integer>();
			for (var i : ((demo.Module1.Value)current).getSet10()) {
				newValueCopy.add(i);
			}
			assert newValue.size() == newValueCopy.size();
			for (var i : newValue) {
				assert newValueCopy.contains(i);
			}
		}

		public final void OnChanged(Object key, Bean value) {
			newValue = new HashSet<Integer>();
			for (var i : ((demo.Module1.Value)value).getSet10()) {
				newValue.add(i);
			}
		}

		public final void OnChanged(Object key, Bean value, ChangeNote note) {
			ChangeNoteSet<Integer> noteset = (ChangeNoteSet<Integer>)note;
			for (var a : noteset.Added) {
				newValue.add(a);
			}
			for (var r : noteset.Removed) {
				newValue.remove((Integer)r);
			}
		}

		public final void OnRemoved(Object key) {
			newValue = null;
		}
	}

	private static class CLList9 implements ChangeListener {
		private ArrayList<demo.Bean1> newValue;

		public final void Init(demo.Module1.Value current) {
			if (null != current) {
				newValue = new ArrayList<demo.Bean1>();
				for (var e : current.getList9()) {
					newValue.add(e.Copy());
				}
			}
			else {
				newValue = null;
			}
		}

		public final void Verify(demo.Module1.Value current) {
			if (null == current) {
				assert null == newValue;
				return;
			}
			assert newValue.size() == current.getList9().Count;
			for (int i = 0; i < newValue.size(); ++i) {
				assert newValue.get(i).getV1() == current.getList9().get(i).V1;
			}
		}

		public final void OnChanged(Object key, Bean value) {
			newValue = new ArrayList<demo.Bean1>();
			for (var e : ((demo.Module1.Value)value).getList9()) {
				newValue.add(e.Copy());
			}
		}

		public final void OnChanged(Object key, Bean value, ChangeNote note) {
			newValue = new ArrayList<demo.Bean1>();
			for (var e : ((demo.Module1.Value)value).getList9()) {
				newValue.add(e.Copy());
			}
		}

		public final void OnRemoved(Object key) {
			newValue = null;
		}
	}

	private static class CLBytes8 implements ChangeListener {
		private Binary newValue;

		public final void Init(demo.Module1.Value current) {
			newValue = current == null ? null : current.getBytes8();
		}

		public final void Verify(demo.Module1.Value current) {
			if (null == current) {
				assert null == newValue;
				return;
			}
			assert newValue == current.getBytes8();
		}

		public final void OnChanged(Object key, Bean value) {
			newValue = ((demo.Module1.Value)value).getBytes8();
		}

		public final void OnChanged(Object key, Bean value, ChangeNote note) {
			newValue = ((demo.Module1.Value)value).getBytes8();
		}

		public final void OnRemoved(Object key) {
			newValue = null;
		}
	}

	private static class CLDouble7 implements ChangeListener {
		private double newValue;

		public final void Init(demo.Module1.Value current) {
			newValue = (null != current) ? current.getDouble7() : 0;
		}

		public final void Verify(demo.Module1.Value current) {
			if (null == current) {
				assert 0 == newValue;
				return;
			}
			assert newValue == current.getDouble7();
		}

		public final void OnChanged(Object key, Bean value) {
			newValue = ((demo.Module1.Value)value).getDouble7();
		}

		public final void OnChanged(Object key, Bean value, ChangeNote note) {
			newValue = ((demo.Module1.Value)value).getDouble7();
		}

		public final void OnRemoved(Object key) {
			newValue = 0;
		}
	}

	private static class CLFloat6 implements ChangeListener {
		private float newValue;

		public final void Init(demo.Module1.Value current) {
			newValue = (null != current) ? current.getFloat6() : 0;
		}

		public final void Verify(demo.Module1.Value current) {
			if (null == current) {
				assert 0 == newValue;
				return;
			}
			assert newValue == current.getFloat6();
		}

		public final void OnChanged(Object key, Bean value) {
			newValue = ((demo.Module1.Value)value).getFloat6();
		}

		public final void OnChanged(Object key, Bean value, ChangeNote note) {
			newValue = ((demo.Module1.Value)value).getFloat6();
		}

		public final void OnRemoved(Object key) {
			newValue = 0;
		}
	}

	private static class CLShort5 implements ChangeListener {
		private short newValue;

		public final void Init(demo.Module1.Value current) {
			newValue = (null != current) ? current.getShort5() : (short)-1;
		}

		public final void Verify(demo.Module1.Value current) {
			if (null == current) {
				assert - 1 == newValue;
				return;
			}
			assert newValue == current.getShort5();
		}

		public final void OnChanged(Object key, Bean value) {
			newValue = ((demo.Module1.Value)value).getShort5();
		}

		public final void OnChanged(Object key, Bean value, ChangeNote note) {
			newValue = ((demo.Module1.Value)value).getShort5();
		}

		public final void OnRemoved(Object key) {
			newValue = -1;
		}
	}

	private static class CLBool4 implements ChangeListener {
		private boolean newValue;

		public final void Init(demo.Module1.Value current) {
			newValue = (null != current) && current.getBool4();
		}

		public final void Verify(demo.Module1.Value current) {
			if (null == current) {
				assert false == newValue;
				return;
			}
			assert newValue == current.getBool4();
		}

		public final void OnChanged(Object key, Bean value) {
			newValue = ((demo.Module1.Value)value).getBool4();
		}

		public final void OnChanged(Object key, Bean value, ChangeNote note) {
			newValue = ((demo.Module1.Value)value).getBool4();
		}

		public final void OnRemoved(Object key) {
			newValue = false;
		}

	}

	private static class CLString3 implements ChangeListener {
		private String newValue;

		public final void Init(demo.Module1.Value current) {
			newValue = current == null ? null : current.getString3();
		}

		public final void Verify(demo.Module1.Value current) {
			if (null == current) {
				assert newValue.equals(null);
				return;
			}
			assert newValue == current.getString3();
		}

		public final void OnChanged(Object key, Bean value) {
			newValue = ((demo.Module1.Value)value).getString3();
		}

		public final void OnChanged(Object key, Bean value, ChangeNote note) {
			newValue = ((demo.Module1.Value)value).getString3();
		}

		public final void OnRemoved(Object key) {
			newValue = null;
		}
	}

	private static class ClLong2 implements ChangeListener {
		private long newValue;

		public final void Init(demo.Module1.Value current) {
			newValue = (null != current) ? current.getLong2() : -1;
		}

		public final void Verify(demo.Module1.Value current) {
			if (null == current) {
				assert - 1 == newValue;
				return;
			}
			assert newValue == current.getLong2();
		}

		public final void OnChanged(Object key, Bean value) {
			newValue = ((demo.Module1.Value)value).getLong2();
		}

		public final void OnChanged(Object key, Bean value, ChangeNote note) {
			newValue = ((demo.Module1.Value)value).getLong2();
		}

		public final void OnRemoved(Object key) {
			newValue = -1;
		}
	}


	private static class CLInt1 implements ChangeListener {
		private int newValue;

		public final void Init(demo.Module1.Value current) {
			newValue = (null != current) ? current.getInt1() : -1;
		}

		public final void Verify(demo.Module1.Value current) {
			if (null == current) {
				assert - 1 == newValue;
				return;
			}
			assert newValue == current.getInt1();
		}

		public final void OnChanged(Object key, Bean value) {
			newValue = ((demo.Module1.Value)value).getInt1();
		}

		public final void OnChanged(Object key, Bean value, ChangeNote note) {
			newValue = ((demo.Module1.Value)value).getInt1();
		}

		public final void OnRemoved(Object key) {
			newValue = -1;
		}
	}
}