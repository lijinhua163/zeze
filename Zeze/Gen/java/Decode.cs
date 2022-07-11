﻿using System;
using System.IO;
using Zeze.Gen.Types;

namespace Zeze.Gen.java
{
    public class Decode : Visitor
    {
        readonly Variable var;
        readonly string tmpvarname;
        readonly int id;
        readonly string bufname;
        readonly StreamWriter sw;
        readonly string prefix;
        readonly string typeVarName;

        public static void Make(Bean bean, StreamWriter sw, string prefix)
        {
            sw.WriteLine(prefix + "@Override");
            sw.WriteLine(prefix + "public void Decode(ByteBuffer _o_) {");
            sw.WriteLine(prefix + "    int _t_ = _o_.ReadByte();");
            if (bean.Variables.Count > 0)
                sw.WriteLine(prefix + "    int _i_ = _o_.ReadTagSize(_t_);");
            else
                sw.WriteLine(prefix + "    _o_.ReadTagSize(_t_);");

            int lastId = 0;
            foreach (Variable v in bean.Variables)
            {
                if (v.Transient)
                    continue;

                if (v.Id > 0)
                {
                    if (v.Id <= lastId)
                        throw new Exception("unordered var.id");
                    if (v.Id - lastId > 1)
                    {
                         sw.WriteLine(prefix + "    while ((_t_ & 0xff) > 1 && _i_ < " + v.Id + ") {");
                         sw.WriteLine(prefix + "        _o_.SkipUnknownField(_t_);");
                         sw.WriteLine(prefix + "        _i_ += _o_.ReadTagSize(_t_ = _o_.ReadByte());");
                         sw.WriteLine(prefix + "    }");
                    }
                    lastId = v.Id;
                    sw.WriteLine(prefix + "    if (_i_ == " + v.Id + ") {");
                }
                else
                    sw.WriteLine(prefix + "    {");
                v.VariableType.Accept(new Decode(v, v.Id, "_o_", sw, prefix + "        "));
                if (v.Id > 0)
                    sw.WriteLine(prefix + "        _i_ += _o_.ReadTagSize(_t_ = _o_.ReadByte());");
                sw.WriteLine(prefix + "    }");
            }

            sw.WriteLine(prefix + "    while (_t_ != 0) {");
            if (bean.Base != "")
            {
                sw.WriteLine(prefix + "        if (_t_ == 1)");
                sw.WriteLine(prefix + "        {");
                sw.WriteLine(prefix + "            base.Decode(_o_);");
                sw.WriteLine(prefix + "            return;");
                sw.WriteLine(prefix + "        }");
            }
            sw.WriteLine(prefix + "        _o_.SkipUnknownField(_t_);");
            sw.WriteLine(prefix + "        _o_.ReadTagSize(_t_ = _o_.ReadByte());");
            sw.WriteLine(prefix + "    }");
            sw.WriteLine(prefix + "}");
            sw.WriteLine();
        }

        public static void Make(BeanKey bean, StreamWriter sw, string prefix)
        {
            sw.WriteLine(prefix + "@Override");
            sw.WriteLine(prefix + "public void Decode(ByteBuffer _o_) {");
            sw.WriteLine(prefix + "    int _t_ = _o_.ReadByte();");
            if (bean.Variables.Count > 0)
                sw.WriteLine(prefix + "    int _i_ = _o_.ReadTagSize(_t_);");
            else
                sw.WriteLine(prefix + "    _o_.ReadTagSize(_t_);");

            int lastId = 0;
            foreach (Variable v in bean.Variables)
            {
                if (v.Transient)
                    continue;

                if (v.Id > 0)
                {
                    if (v.Id <= lastId)
                        throw new Exception("unordered var.id");
                    if (v.Id - lastId > 1)
                    {
                        sw.WriteLine(prefix + "    while ((_t_ & 0xff) > 1 && _i_ < " + v.Id + ") {");
                        sw.WriteLine(prefix + "        _o_.SkipUnknownField(_t_);");
                        sw.WriteLine(prefix + "        _i_ += _o_.ReadTagSize(_t_ = _o_.ReadByte());");
                        sw.WriteLine(prefix + "    }");
                    }
                    lastId = v.Id;
                    sw.WriteLine(prefix + "    if (_i_ == " + v.Id + ") {");
                }
                else
                    sw.WriteLine(prefix + "    {");
                v.VariableType.Accept(new Decode(v, v.Id, "_o_", sw, prefix + "        "));
                if (v.Id > 0)
                    sw.WriteLine(prefix + "        _i_ += _o_.ReadTagSize(_t_ = _o_.ReadByte());");
                sw.WriteLine(prefix + "    }");
            }

            sw.WriteLine(prefix + "    while (_t_ != 0) {");
            sw.WriteLine(prefix + "        _o_.SkipUnknownField(_t_);");
            sw.WriteLine(prefix + "        _o_.ReadTagSize(_t_ = _o_.ReadByte());");
            sw.WriteLine(prefix + "    }");
            sw.WriteLine(prefix + "}");
            sw.WriteLine();
        }

        public Decode(Variable var, int id, string bufname, StreamWriter sw, string prefix)
        {
            this.var = var;
            this.tmpvarname = null;
            this.id = id;
            this.bufname = bufname;
            this.sw = sw;
            this.prefix = prefix;
            this.typeVarName = "_t_";
        }

        public Decode(string tmpvarname, int id, string bufname, StreamWriter sw, string prefix, string typeVarName = null)
        {
            this.var = null;
            this.tmpvarname = tmpvarname;
            this.id = id;
            this.bufname = bufname;
            this.sw = sw;
            this.prefix = prefix;
            this.typeVarName = typeVarName ?? "_t_";
        }

        string GetVarName()
        {
            if (var != null)
                return var.Bean.IsNormalBean ? var.Getter : var.NamePrivate;
            return tmpvarname;
        }

        string AssignText(string value)
        {
            if (var != null)
                return var.Bean.IsNormalBean ? var.Setter(value) : $"{var.NamePrivate} = {value}";
            return $"{tmpvarname} = {value}";
        }

        public void Visit(TypeBool type)
        {
            if (id > 0)
                sw.WriteLine(prefix + AssignText($"{bufname}.ReadBool(_t_)") + ';');
            else
                sw.WriteLine(prefix + AssignText($"{bufname}.ReadBool()") + ';');
        }

        public void Visit(TypeByte type)
        {
            if (id > 0)
                sw.WriteLine(prefix + AssignText($"{bufname}.ReadByte(_t_)") + ';');
            else
                sw.WriteLine(prefix + AssignText($"(byte){bufname}.ReadLong()") + ';');
        }

        public void Visit(TypeShort type)
        {
            if (id > 0)
                sw.WriteLine(prefix + AssignText($"{bufname}.ReadShort(_t_)") + ';');
            else
                sw.WriteLine(prefix + AssignText($"(short){bufname}.ReadLong()") + ';');
        }

        public void Visit(TypeInt type)
        {
            if (id > 0)
                sw.WriteLine(prefix + AssignText($"{bufname}.ReadInt(_t_)") + ';');
            else
                sw.WriteLine(prefix + AssignText($"{bufname}.ReadInt()") + ';');
        }

        public void Visit(TypeLong type)
        {
            if (id > 0)
                sw.WriteLine(prefix + AssignText($"{bufname}.ReadLong(_t_)") + ';');
            else
                sw.WriteLine(prefix + AssignText($"{bufname}.ReadLong()") + ';');
        }

        public void Visit(TypeFloat type)
        {
            if (id > 0)
                sw.WriteLine(prefix + AssignText($"{bufname}.ReadFloat(_t_)") + ';');
            else
                sw.WriteLine(prefix + AssignText($"{bufname}.ReadFloat()") + ';');
        }

        public void Visit(TypeDouble type)
        {
            if (id > 0)
                sw.WriteLine(prefix + AssignText($"{bufname}.ReadDouble(_t_)") + ';');
            else
                sw.WriteLine(prefix + AssignText($"{bufname}.ReadDouble()") + ';');
        }

        public void Visit(TypeBinary type)
        {
            if (id > 0)
                sw.WriteLine(prefix + AssignText($"{bufname}.ReadBinary(_t_)") + ';');
            else
                sw.WriteLine(prefix + AssignText($"{bufname}.ReadBinary()") + ';');
        }

        public void Visit(TypeString type)
        {
            if (id > 0)
                sw.WriteLine(prefix + AssignText($"{bufname}.ReadString(_t_)") + ';');
            else
                sw.WriteLine(prefix + AssignText($"{bufname}.ReadString()") + ';');
        }

        string DecodeElement(Types.Type type, string typeVar)
        {
            switch (type)
            {
                case TypeBool:
                    return bufname + ".ReadBool(" + typeVar + ')';
                case TypeByte:
                    return bufname + ".ReadByte(" + typeVar + ')';
                case TypeShort:
                    return bufname + ".ReadShort(" + typeVar + ')';
                case TypeInt:
                    return bufname + ".ReadInt(" + typeVar + ')';
                case TypeLong:
                    return bufname + ".ReadLong(" + typeVar + ')';
                case TypeFloat:
                    return bufname + ".ReadFloat(" + typeVar + ')';
                case TypeDouble:
                    return bufname + ".ReadDouble(" + typeVar + ')';
                case TypeBinary:
                    return bufname + ".ReadBinary(" + typeVar + ')';
                case TypeString:
                    return bufname + ".ReadString(" + typeVar + ')';
                case Bean:
                case BeanKey:
                    return bufname + ".ReadBean(new " + TypeName.GetName(type) + "(), " + typeVar + ')';
                case TypeDynamic:
                    return bufname + ".ReadDynamic(new " + TypeName.GetName(type) + "(), " + typeVar + ')';
                case TypeVector2:
                    return bufname + ".ReadVector2(" + typeVar + ')';
                case TypeVector2Int:
                    return bufname + ".ReadVector2Int(" + typeVar + ')';
                case TypeVector3:
                    return bufname + ".ReadVector3(" + typeVar + ')';
                case TypeVector3Int:
                    return bufname + ".ReadVector3Int(" + typeVar + ')';
                case TypeVector4:
                    return bufname + ".ReadVector4(" + typeVar + ')';
                case TypeQuaternion:
                    return bufname + ".ReadQuaternion(" + typeVar + ')';
                default:
                    throw new Exception("invalid collection element type: " + type);
            }
        }

        public static bool IsOldStypeEncodeDecodeType(Types.Type type)
        {
            switch (type)
            {
                case TypeDynamic:
                case TypeVector2:
                case TypeVector3:
                case TypeVector4:
                case TypeVector2Int:
                case TypeVector3Int:
                case TypeQuaternion:
                    return true;
            }
            return false;
        }

        void DecodeCollection(TypeCollection type)
        {
            if (id <= 0)
                throw new Exception("invalid variable.id");
            Types.Type vt = type.ValueType;
            sw.WriteLine(prefix + "var _x_ = " + var.Getter + ';');
            sw.WriteLine(prefix + "_x_.clear();");
            sw.WriteLine(prefix + "if ((_t_ & ByteBuffer.TAG_MASK) == " + TypeTagName.GetName(type) + ") {");
            sw.Write(prefix + "    for (int _n_ = " + bufname + ".ReadTagSize(_t_ = " + bufname + ".ReadByte()); _n_ > 0; _n_--)");
            if (IsOldStypeEncodeDecodeType(vt))
            {
                sw.WriteLine(" {");
                vt.Accept(new Define("_e_", sw, prefix + "        "));
                vt.Accept(new Decode("_e_", 0, bufname, sw, prefix + "        "));
                sw.WriteLine($"{prefix}        _x_.add(_e_);");
                sw.WriteLine($"{prefix}    }}");
            }
            else
            {
                sw.WriteLine();
                sw.WriteLine(prefix + "        _x_.add(" + DecodeElement(vt, "_t_") + ");");
            }
            sw.WriteLine($"{prefix}}} else");
            sw.WriteLine(prefix + "    " + bufname + ".SkipUnknownField(_t_);");
        }

        public void Visit(TypeList type)
        {
            DecodeCollection(type);
        }

        public void Visit(TypeSet type)
        {
            DecodeCollection(type);
        }

        public void Visit(TypeMap type)
        {
            if (id <= 0)
                throw new Exception("invalid variable.id");
            Types.Type kt = type.KeyType;
            Types.Type vt = type.ValueType;
            sw.WriteLine(prefix + "var _x_ = " + var.Getter + ';');
            sw.WriteLine(prefix + "_x_.clear();");
            sw.WriteLine(prefix + "if ((_t_ & ByteBuffer.TAG_MASK) == " + TypeTagName.GetName(type) + ") {");
            sw.WriteLine(prefix + "    int _s_ = (_t_ = " + bufname + ".ReadByte()) >> ByteBuffer.TAG_SHIFT;");
            sw.WriteLine(prefix + "    for (int _n_ = " + bufname + ".ReadUInt(); _n_ > 0; _n_--) {");
            if (IsOldStypeEncodeDecodeType(kt))
            {
                kt.Accept(new Define("_k_", sw, prefix + "        "));
                kt.Accept(new Decode("_k_", 0, bufname, sw, prefix + "        ", "_s_"));
            }
            else
            {
                sw.WriteLine(prefix + "        var _k_ = " + DecodeElement(kt, "_s_") + ';');
            }
            if (IsOldStypeEncodeDecodeType(vt))
            {
                vt.Accept(new Define("_v_", sw, prefix + "        "));
                vt.Accept(new Decode("_v_", 0, bufname, sw, prefix + "        "));
            }
            else
            {
                sw.WriteLine(prefix + "        var _v_ = " + DecodeElement(vt, "_t_") + ';');
            }
            sw.WriteLine(prefix + "        _x_.put(_k_, _v_);");
            sw.WriteLine(prefix + "    }");
            sw.WriteLine(prefix + "} else");
            sw.WriteLine(prefix + "    " + bufname + ".SkipUnknownField(_t_);");
        }

        public void Visit(Bean type)
        {
            if (id > 0)
                sw.WriteLine(prefix + bufname + ".ReadBean(" + GetVarName() + ", _t_);");
            else
                sw.WriteLine(prefix + GetVarName() + ".Decode(" + bufname + ");");
        }

        public void Visit(BeanKey type)
        {
            if (id > 0)
                sw.WriteLine(prefix + bufname + ".ReadBean(" + GetVarName() + ", _t_);");
            else
                sw.WriteLine(prefix + GetVarName() + ".Decode(" + bufname + ");");
        }

        public void Visit(TypeDynamic type)
        {
            sw.WriteLine(prefix + bufname + ".ReadDynamic(" + GetVarName() + ", " + typeVarName + ");");
        }

        public void Visit(TypeQuaternion type)
        {
            sw.WriteLine(prefix + AssignText($"{bufname}.ReadQuaternion({typeVarName})") + ';');
        }

        public void Visit(TypeVector2 type)
        {
            sw.WriteLine(prefix + AssignText($"{bufname}.ReadVector2({typeVarName})") + ';');
        }

        public void Visit(TypeVector2Int type)
        {
            sw.WriteLine(prefix + AssignText($"{bufname}.ReadVector2Int({typeVarName})") + ';');
        }

        public void Visit(TypeVector3 type)
        {
            sw.WriteLine(prefix + AssignText($"{bufname}.ReadVector3({typeVarName})") + ';');
        }

        public void Visit(TypeVector3Int type)
        {
            sw.WriteLine(prefix + AssignText($"{bufname}.ReadVector3Int({typeVarName})") + ';');
        }

        public void Visit(TypeVector4 type)
        {
            sw.WriteLine(prefix + AssignText($"{bufname}.ReadVector4({typeVarName})") + ';');
        }
    }
}
