using System;
using System.Collections.Generic;
using System.Text;
using System.Text.RegularExpressions;
using System.Xml;

namespace Zeze.Gen.Types
{
	public class Variable
	{
		public Type Bean { get; private set; } // Bean or BeanKey
		public string Name { get; private set; }
		public string NamePinyin => Program.ToPinyin(Name);
		public string NamePrivate => "_" + Name;
		public string NameUpper1 => Name.Substring(0, 1).ToUpper() + Name.Substring(1);
		public string NameGetter => Type.Equals("bool") ? $"is{NameUpper1}" : $"get{NameUpper1}";
		public string NameSetter => $"set{NameUpper1}";
		public string Getter => $"{NameGetter}()";
		public string Setter(string value) { return $"{NameSetter}({value})"; }
		public int Id { get; private set; }
		public string Type { get; private set; }
		public string Key { get; private set; }
		public string Value { get; private set; }
		public string Initial { get; private set; }
		public string Comment { get; private set; }
		public string Validator { get; private set; }
		public bool AllowNegative { get; private set; } = false;
		public bool Transient { get; private set; } = false;
		public string FixSize { get; private set; }

		public DynamicParams DynamicParams { get; } = new();

		public string GetBeanFullName()
		{
			if (Bean is Bean)
				return ((Bean)Bean).FullName;

			if (Bean is BeanKey)
				return ((BeanKey)Bean).FullName;

			throw new Exception("Variable holder is not a bean");
		}

		string ParseDynamicBase(string type)
		{
			var dbase = type.Trim().Split(':');
			if (dbase.Length == 0)
				throw new Exception($"error type define, no type: '{type}'"); // impossible
			if (dbase.Length == 1)
				return dbase[0].Trim(); // 普通类型或者没有指定基类的dynamic
			if (dbase.Length > 2)
				throw new Exception($"error type define, too many base: '{type}'");

			dbase[0] = dbase[0].Trim();
			dbase[1] = dbase[1].Trim();

			if (false == string.IsNullOrEmpty(DynamicParams.Base))
				throw new Exception($"error type define, '{type}'");
			DynamicParams.Base = dbase[1];
			if (!DynamicParams.Base.Contains('.'))
				DynamicParams.Base = ((Bean)Bean).Space.Path(".", DynamicParams.Base);
			if (false == dbase[0].Equals("dynamic"))
				throw new Exception($"error type define, only dynamic has base: '{type}'");
			return dbase[0];
		}

		void ParseType()
		{
			var typeTemplate = Type.Split('[');
			if (typeTemplate.Length == 0)
				throw new Exception($"error type define: no type, impossible. '{Type}'");

			var typesaved = Type;
			Type = ParseDynamicBase(typeTemplate[0]);

			if (typeTemplate.Length == 1)
				return; // 非模板类型。

			if (typeTemplate.Length > 2 || false == typeTemplate[1].EndsWith("]"))
				throw new Exception($"error type define: '{Type}'");

			var keyvalue = typeTemplate[1].Remove(typeTemplate[1].Length - 1).Split(',');
			switch (keyvalue.Length)
			{
				case 0:
					throw new Exception($"error type define, key value is 0: '{Type}'");

				case 1:
					if (Value.Length > 0)
						throw new Exception($"error type define, value has present.");
					Value = ParseDynamicBase(keyvalue[0]);
					break;

				case 2:
					if (Key.Length > 0)
						throw new Exception($"error type define, key has present.");
					if (Value.Length > 0)
						throw new Exception($"error type define, value has present.");
					Key = keyvalue[0].Trim();
					Value = ParseDynamicBase(keyvalue[1]);
					break;

				default:
					throw new Exception($"error type define, too many template params: '{typesaved}'");
			}
			// ParseDynamicBase 上面调用了多次，只会成功一次。
		}

		public Variable(Type bean, XmlElement self)
		{
			Bean = bean;
			Name = self.GetAttribute("name").Trim();
			Program.CheckReserveName(Name);
			Id = int.Parse(self.GetAttribute("id"));
			if (Id <= 0 || Id > global::Zeze.Transaction.Bean.MaxVariableId)
				throw new Exception("variable id invalid. range [1, " + global::Zeze.Transaction.Bean.MaxVariableId + "] @" + GetBeanFullName());
			Type = self.GetAttribute("type").Trim();
			if (string.IsNullOrEmpty(Type))
				throw new Exception("Variable Type Can Not Be Empty.");
			Key = self.GetAttribute("key").Trim();
			Value = self.GetAttribute("value").Trim();
			Initial = self.GetAttribute("default").Trim();
			Validator = self.GetAttribute("validator").Trim();
			string attr = self.GetAttribute("AllowNegative");
			if (attr.Length > 0)
				AllowNegative = bool.Parse(attr);
			Transient = self.GetAttribute("transient").Equals("true");
			FixSize = self.GetAttribute("FixSize");

			Comment = self.GetAttribute("comment");
			if (Comment.Length == 0)
			{
				XmlNode c = self.NextSibling;
				if (c != null && XmlNodeType.Text == c.NodeType)
				{
					Comment = c.InnerText.Trim();
					Regex regex = new Regex("[\r\n]");
					Comment = regex.Replace(Comment, "");
				}
			}
			if (Comment.Length > 0)
				Comment = " // " + Comment;

			ParseType();

			XmlNodeList childNodes = self.ChildNodes;
			foreach (XmlNode node in childNodes)
			{
				if (XmlNodeType.Element != node.NodeType)
					continue;

				XmlElement e = (XmlElement)node;
                string nodename = e.Name;
				switch (e.Name)
				{
					case "value":
						DynamicParams.Beans.Add(e.GetAttribute("bean"));
						break;
					case "GetSpecialTypeIdFromBean":
						DynamicParams.GetSpecialTypeIdFromBean = e.GetAttribute("value");
						break;
					case "CreateBeanFromSpecialTypeId":
						DynamicParams.CreateBeanFromSpecialTypeId = e.GetAttribute("value");
						break;
					default:
						throw new Exception("node=" + nodename);
				}
			}
			// 不再支持直接在Attribute中定义dynamic包含的Bean类型。XXX 怎么报错。
			//foreach (string b in Value.Split(','))
			//	dynamicValue.Add(b.Trim());
		}

		public Type VariableType { get; private set; }

		public void Compile(ModuleSpace space)
		{
            VariableType = Types.Type.Compile(space, Type, Key, Value, this);
			if (VariableType is TypeList list)
			{
				if (false == string.IsNullOrEmpty(FixSize))
					list.FixSize = int.Parse(FixSize);
			}
		}
	}
}
