﻿using System.IO;
using Zeze.Gen.Types;

namespace Zeze.Gen.cs
{
    public class Construct : Visitor
    {
		private readonly StreamWriter sw;
		private readonly Variable variable;
		private readonly string prefix;

		public static void Make(Bean bean, StreamWriter sw, string prefix)
		{
			sw.WriteLine(prefix + "public " + bean.Name + "() : this(0)");
			sw.WriteLine(prefix + "{");
			sw.WriteLine(prefix + "}");
			sw.WriteLine();
            sw.WriteLine(prefix + "public " + bean.Name + "(int _varId_) : base(_varId_)");
            sw.WriteLine(prefix + "{");
            foreach (Variable var in bean.Variables)
                var.VariableType.Accept(new Construct(sw, var, prefix + "    "));
            sw.WriteLine(prefix + "}");
            sw.WriteLine();
        }

        public Construct(StreamWriter sw, Variable variable, string prefix)
		{
			this.sw = sw;
			this.variable = variable;
			this.prefix = prefix;
		}

		private void Initial()
		{
            string value = variable.Initial;
			if (value.Length > 0)
			{
                string varname = variable.NamePrivate;
				sw.WriteLine(prefix + varname + " = " + value + ";");
			}
		}

        public void Visit(Bean type)
        {
            string typeName = TypeName.GetName(type);
            sw.WriteLine(prefix + variable.NamePrivate + " = new " + typeName + "(" + variable.Id + ");");
        }

        public void Visit(BeanKey type)
        {
            string typeName = TypeName.GetName(type);
            sw.WriteLine(prefix + variable.NamePrivate + " = new " + typeName + "();");
        }

        public void Visit(TypeByte type)
        {
            Initial();
        }

        public void Visit(TypeDouble type)
        {
            Initial();
        }

        public void Visit(TypeInt type)
        {
            Initial();
        }

        public void Visit(TypeLong type)
        {
            Initial();
        }

        public void Visit(TypeBool type)
        {
            Initial();
        }

        public void Visit(TypeBinary type)
        {
            sw.WriteLine(prefix + variable.NamePrivate + " = Zeze.Net.Binary.Empty;");
        }

        public void Visit(TypeString type)
        {
            string value = variable.Initial;
            string varname = variable.NamePrivate;
            sw.WriteLine(prefix + varname + " = \"" + value + "\";");
        }

        public void Visit(TypeList type)
        {
            string typeName = TypeName.GetName(type);
            sw.WriteLine(prefix + variable.NamePrivate + " = new " + typeName + "(ObjectId + " + variable.Id + ", _v => new Log_" + variable.NamePrivate + "(this, _v));");
        }

        public void Visit(TypeSet type)
        {
            string typeName = TypeName.GetName(type);
            sw.WriteLine(prefix + variable.NamePrivate + " = new " + typeName + "(ObjectId + " + variable.Id + ", _v => new Log_" + variable.NamePrivate + "(this, _v));");
        }

        public void Visit(TypeMap type)
        {
            string typeName = TypeName.GetName(type);
            sw.WriteLine(prefix + variable.NamePrivate + " = new " + typeName + "(ObjectId + " + variable.Id + ", _v => new Log_" + variable.NamePrivate + "(this, _v));");
            var key = TypeName.GetName(type.KeyType);
            var value = type.ValueType.IsNormalBean
                ? TypeName.GetName(type.ValueType) + "ReadOnly"
                : TypeName.GetName(type.ValueType);
            var readonlyTypeName = $"Zeze.Transaction.Collections.PMapReadOnly<{key},{value},{TypeName.GetName(type.ValueType)}>";
            sw.WriteLine($"{prefix}{variable.NamePrivate}ReadOnly = new {readonlyTypeName}({variable.NamePrivate});");
        }

        public void Visit(TypeFloat type)
        {
            Initial();
        }

        public void Visit(TypeShort type)
        {
            Initial();
        }

        public void Visit(TypeDynamic type)
        {
            sw.WriteLine(prefix + variable.NamePrivate + " = new Zeze.Transaction.DynamicBean"
                + $"({variable.Id}, GetSpecialTypeIdFromBean_{variable.NameUpper1}, CreateBeanFromSpecialTypeId_{variable.NameUpper1});");
        }
    }
}
