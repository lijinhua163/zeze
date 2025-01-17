﻿using System.IO;
using Zeze.Gen.Types;

namespace Zeze.Gen.rrjava
{
    public class Construct : Visitor
    {
		readonly StreamWriter sw;
		readonly Variable variable;
		readonly string prefix;

		public static void Make(Bean bean, StreamWriter sw, string prefix)
		{
            sw.WriteLine(prefix + "public " + bean.Name + "() {");
            sw.WriteLine(prefix + "     this(0);");
            sw.WriteLine(prefix + "}");
            sw.WriteLine();
            sw.WriteLine(prefix + "public " + bean.Name + "(int _varId_) {");
            sw.WriteLine(prefix + "    super(_varId_);");
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

		void Initial()
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
            string typeName = TypeName.GetSimpleName(type);
            sw.WriteLine(prefix + variable.NamePrivate + $" = new {typeName}({BoxingName.GetBoxingName(type.ValueType)}.class);");
            sw.WriteLine(prefix + variable.NamePrivate + $".VariableId = {variable.Id};");
        }

        public void Visit(TypeSet type)
        {
            string typeName = TypeName.GetSimpleName(type);
            sw.WriteLine(prefix + variable.NamePrivate + $" = new {typeName}({BoxingName.GetBoxingName(type.ValueType)}.class);");
            sw.WriteLine(prefix + variable.NamePrivate + $".VariableId = {variable.Id};");
        }

        public void Visit(TypeMap type)
        {
            string typeName = TypeName.GetSimpleName(type);
            sw.WriteLine(prefix + variable.NamePrivate + $" = new {typeName}({BoxingName.GetBoxingName(type.KeyType)}.class, {BoxingName.GetBoxingName(type.ValueType)}.class);");
            sw.WriteLine(prefix + variable.NamePrivate + $".VariableId = {variable.Id};");
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
            sw.WriteLine(prefix + variable.NamePrivate + " = new Zeze.Raft.RocksRaft.DynamicBean"
                + $"({variable.Id}, GetSpecialTypeIdFromBean_{variable.NameUpper1}, CreateBeanFromSpecialTypeId_{variable.NameUpper1});");
        }

        public void Visit(TypeQuaternion type)
        {
            throw new System.NotImplementedException();
        }

        public void Visit(TypeVector2 type)
        {
            throw new System.NotImplementedException();
        }

        public void Visit(TypeVector2Int type)
        {
            throw new System.NotImplementedException();
        }

        public void Visit(TypeVector3 type)
        {
            throw new System.NotImplementedException();
        }

        public void Visit(TypeVector3Int type)
        {
            throw new System.NotImplementedException();
        }

        public void Visit(TypeVector4 type)
        {
            throw new System.NotImplementedException();
        }
    }
}
