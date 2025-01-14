﻿using System;
using System.IO;
using Zeze.Gen.Types;

namespace Zeze.Gen.cs
{
    // 现在这个类仅用于BeanKey，如果普通Bean要支持，
    // 需要使用NameUpper1进行比较，而不是NamePrivate。
    public class Equal : Visitor
    {
        readonly Variable var;
        readonly string another;
        readonly bool isEquals;
        string text;

        /// <summary>
        /// 实际上 BeanKey 很多类型都不支持，下面先尽量实现，以后可能用来实现 Bean 的 Equals.
        /// </summary>
        /// <param name="bean"></param>
        /// <param name="sw"></param>
        /// <param name="prefix"></param>
        public static void Make(BeanKey bean, StreamWriter sw, string prefix)
        {
            sw.WriteLine(prefix + "public override bool Equals(object _obj1_)");
            sw.WriteLine(prefix + "{");
            sw.WriteLine(prefix + "    if (_obj1_ == this) return true;");
            sw.WriteLine(prefix + "    if (_obj1_ is " + bean.Name + " _obj_)");
            sw.WriteLine(prefix + "    {");
            foreach (Variable var in bean.Variables)
            {
                var v = new Equal(var, "_obj_", false);
                var.VariableType.Accept(v);
                sw.WriteLine(prefix + "        if (" + v.text + ") return false;");
            }
            sw.WriteLine(prefix + "        return true;");
            sw.WriteLine(prefix + "    }");
            sw.WriteLine(prefix + "    return false;");
            sw.WriteLine(prefix + "}");
            sw.WriteLine();
        }

        public Equal(Variable var, string another, bool isEquals)
        {
            this.var = var;
            this.another = another;
            this.isEquals = isEquals;
        }

        void CommonEquals()
        {
            text = var.NamePrivate + (isEquals ? " == " : " != ") + another + "." + var.NamePrivate;
        }

        public void Visit(TypeBool type)
        {
            CommonEquals();
        }

        public void Visit(TypeByte type)
        {
            CommonEquals();
        }

        public void Visit(TypeShort type)
        {
            CommonEquals();
        }

        public void Visit(TypeInt type)
        {
            CommonEquals();
        }

        public void Visit(TypeLong type)
        {
            CommonEquals();
        }

        public void Visit(TypeFloat type)
        {
            CommonEquals();
        }

        public void Visit(TypeDouble type)
        {
            CommonEquals();
        }

        public void Visit(TypeBinary type)
        {
            text = (isEquals ? "" : "!") + var.NamePrivate + ".Equals(" + another + "." + var.NamePrivate + ")";
        }

        public void Visit(TypeString type)
        {
            text = (isEquals ? "" : "!") + var.NamePrivate + ".Equals(" + another + "." + var.NamePrivate + ")";
        }

        public void Visit(TypeList type)
        {
            throw new NotImplementedException();
        }

        public void Visit(TypeSet type)
        {
            throw new NotImplementedException();
        }

        public void Visit(TypeMap type)
        {
            throw new NotImplementedException();
        }

        public void Visit(Bean type)
        {
            text = (isEquals ? "" : "!") + var.NamePrivate + ".Equals(" + another + "." + var.NamePrivate + ")";
        }

        public void Visit(BeanKey type)
        {
            text = (isEquals ? "" : "!") + var.NamePrivate + ".Equals(" + another + "." + var.NamePrivate + ")";
        }

        public void Visit(TypeDynamic type)
        {
            throw new NotImplementedException();
        }

        public void Visit(TypeQuaternion type)
        {
            throw new NotImplementedException();
        }

        public void Visit(TypeVector2 type)
        {
            throw new NotImplementedException();
        }

        public void Visit(TypeVector2Int type)
        {
            throw new NotImplementedException();
        }

        public void Visit(TypeVector3 type)
        {
            throw new NotImplementedException();
        }

        public void Visit(TypeVector3Int type)
        {
            throw new NotImplementedException();
        }

        public void Visit(TypeVector4 type)
        {
            throw new NotImplementedException();
        }
    }
}
