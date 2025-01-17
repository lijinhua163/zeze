using System;
using System.Collections.Generic;
using System.Text;
using System.Xml;

namespace Zeze.Gen.Types
{
	public class TypeList : TypeCollection
	{
		public int FixSize { get; set; } = -1;

		public override void Accept(Visitor visitor)
		{
			visitor.Visit(this);
		}

		public override Type Compile(ModuleSpace space, string key, string value, Variable var)
		{
			return new TypeList(space, key, value, var);
		}

		private TypeList(global::Zeze.Gen.ModuleSpace space, string key, string value, Variable var)
		{
			Variable = var;
			if (key != null && key.Length > 0)
				throw new Exception(Name + " type does not need a key. " + key);

			ValueType = Type.Compile(space, value, null, null, var);
			//if (ValueType is TypeBinary)
			//	throw new Exception(Name + " Error : value type is binary.");
		}

		internal TypeList(SortedDictionary<string, Type> types)
		{
			types.Add(Name, this);
			types.Add("array", this);
		}

		public override string Name => "list";
		public override bool IsNeedNegativeCheck => ValueType.IsNeedNegativeCheck;

	}
}
