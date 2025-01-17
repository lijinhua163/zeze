﻿using System.IO;
using Zeze.Gen.Types;

namespace Zeze.Gen.java
{
    public class Assign : Visitor
    {
        readonly StreamWriter sw;
        readonly Variable var;
        readonly string prefix;

        public static void Make(Bean bean, StreamWriter sw, string prefix)
        {
            sw.WriteLine(prefix + "public void Assign(" + bean.Name + " other) {");
            foreach (Variable var in bean.Variables)
                var.VariableType.Accept(new Assign(var, sw, prefix + "    "));
            sw.WriteLine(prefix + "}");
            sw.WriteLine();
        }

        public Assign(Variable var, StreamWriter sw, string prefix)
        {
            this.var = var;
            this.sw = sw;
            this.prefix = prefix;
        }

        public void Visit(TypeBool type)
        {
            sw.WriteLine(prefix + var.Setter($"other.{var.Getter}") + ";");
        }

        public void Visit(TypeByte type)
        {
            sw.WriteLine(prefix + var.Setter($"other.{var.Getter}") + ";");
        }

        public void Visit(TypeShort type)
        {
            sw.WriteLine(prefix + var.Setter($"other.{var.Getter}") + ";");
        }

        public void Visit(TypeInt type)
        {
            sw.WriteLine(prefix + var.Setter($"other.{var.Getter}") + ";");
        }

        public void Visit(TypeLong type)
        {
            sw.WriteLine(prefix + var.Setter($"other.{var.Getter}") + ";");
        }

        public void Visit(TypeFloat type)
        {
            sw.WriteLine(prefix + var.Setter($"other.{var.Getter}") + ";");
        }

        public void Visit(TypeDouble type)
        {
            sw.WriteLine(prefix + var.Setter($"other.{var.Getter}") + ";");
        }

        public void Visit(TypeBinary type)
        {
            sw.WriteLine(prefix + var.Setter($"other.{var.Getter}") + ";");
        }

        public void Visit(TypeString type)
        {
            sw.WriteLine(prefix + var.Setter($"other.{var.Getter}") + ";");
        }

        public void Visit(TypeList type)
        {
            sw.WriteLine(prefix + var.Getter + ".clear();");
            string copyif = type.ValueType.IsNormalBean ? "e.Copy()" : "e";

            sw.WriteLine(prefix + "for (var e : other." + var.Getter +")");
            sw.WriteLine(prefix + "    " + var.Getter + ".add(" + copyif + ");");
        }

        public void Visit(TypeSet type)
        {
            sw.WriteLine(prefix + var.Getter + ".clear();");
            string copyif = type.ValueType.IsNormalBean ? "e.Copy()" : "e"; // set 里面现在不让放 bean，先这样写吧。

            sw.WriteLine(prefix + "for (var e : other." + var.Getter + ")");
            sw.WriteLine(prefix + "    " + var.Getter + ".add(" + copyif + ");");
        }

        public void Visit(TypeMap type)
        {
            sw.WriteLine(prefix + var.Getter + ".clear();");
            string copyif = type.ValueType.IsNormalBean ? "e.getValue().Copy()" : "e.getValue()";

            sw.WriteLine(prefix + "for (var e : other." + var.Getter + ".entrySet())");
            sw.WriteLine(prefix + "    " + var.Getter + ".put(e.getKey(), " + copyif + ");");
        }

        public void Visit(Bean type)
        {
            sw.WriteLine(prefix + var.Getter + ".Assign(other." + var.Getter + ");");
        }

        public void Visit(BeanKey type)
        {
            sw.WriteLine(prefix + var.Setter($"other.{var.Getter}") + ";");
        }

        public void Visit(TypeDynamic type)
        {
            sw.WriteLine(prefix + var.Getter + ".Assign(other." + var.Getter + ");");
        }

        public void Visit(TypeQuaternion type)
        {
            sw.WriteLine(prefix + var.Setter($"other.{var.Getter}") + ";");
        }

        public void Visit(TypeVector2 type)
        {
            sw.WriteLine(prefix + var.Setter($"other.{var.Getter}") + ";");
        }

        public void Visit(TypeVector2Int type)
        {
            sw.WriteLine(prefix + var.Setter($"other.{var.Getter}") + ";");
        }

        public void Visit(TypeVector3 type)
        {
            sw.WriteLine(prefix + var.Setter($"other.{var.Getter}") + ";");
        }

        public void Visit(TypeVector3Int type)
        {
            sw.WriteLine(prefix + var.Setter($"other.{var.Getter}") + ";");
        }

        public void Visit(TypeVector4 type)
        {
            sw.WriteLine(prefix + var.Setter($"other.{var.Getter}") + ";");
        }
    }
}
