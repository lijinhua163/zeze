﻿using Zeze.Gen.Types;

namespace Zeze.Gen.ts
{
    public class TypeTagName : Visitor
    {
        public string Name { get; private set; }
 
        public static string GetName(Type type)
        {
            TypeTagName v = new();
            type.Accept(v);
            return v.Name;
        }

        public void Visit(TypeBool type)
        {
            Name = "Zeze.ByteBuffer.INTEGER";
        }

        public void Visit(TypeByte type)
        {
            Name = "Zeze.ByteBuffer.INTEGER";
        }

        public void Visit(TypeShort type)
        {
            Name = "Zeze.ByteBuffer.INTEGER";
        }

        public void Visit(TypeInt type)
        {
            Name = "Zeze.ByteBuffer.INTEGER";
        }

        public void Visit(TypeLong type)
        {
            Name = "Zeze.ByteBuffer.INTEGER";
        }

        public void Visit(TypeFloat type)
        {
            Name = "Zeze.ByteBuffer.FLOAT";
        }

        public void Visit(TypeDouble type)
        {
            Name = "Zeze.ByteBuffer.DOUBLE";
        }

        public void Visit(TypeBinary type)
        {
            Name = "Zeze.ByteBuffer.BYTES";
        }

        public void Visit(TypeString type)
        {
            Name = "Zeze.ByteBuffer.BYTES";
        }

        public void Visit(TypeList type)
        {
            Name = "Zeze.ByteBuffer.LIST";
        }

        public void Visit(TypeSet type)
        {
            Name = "Zeze.ByteBuffer.LIST";
        }

        public void Visit(TypeMap type)
        {
            Name = "Zeze.ByteBuffer.MAP";
        }

        public void Visit(Bean type)
        {
            Name = "Zeze.ByteBuffer.BEAN";
        }

        public void Visit(BeanKey type)
        {
            Name = "Zeze.ByteBuffer.BEAN";
        }

        public void Visit(TypeDynamic type)
        {
            Name = "Zeze.ByteBuffer.DYNAMIC";
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
