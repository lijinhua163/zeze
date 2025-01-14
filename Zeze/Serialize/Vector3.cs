﻿using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Zeze.Serialize
{
    public class Vector2 : Serializable
    {
        public float x { get; internal set; }
        public float y { get; internal set; }

        public Vector2()
        { 
        }

        public Vector2(float x, float y)
        {
            this.x = x;
            this.y = y;
        }

        public virtual void Decode(ByteBuffer bb)
        {
            x = bb.ReadFloat();
            y = bb.ReadFloat();
        }

        public virtual void Encode(ByteBuffer bb)
        {
            bb.WriteFloat(x);
            bb.WriteFloat(y);
        }
    }

    public class Vector3 : Vector2
    {
        public float z { get; internal set; }

        public Vector3()
        {
        }

        public Vector3(Vector2 v2)
            : base(v2.x, v2.y)
        {
        }

        public Vector3(float x, float y, float z)
            : base(x, y)
        {
            this.z = z;
        }

        public override void Decode(ByteBuffer bb)
        {
            base.Decode(bb);
            z = bb.ReadFloat();
        }

        public override void Encode(ByteBuffer bb)
        {
            base.Encode(bb);
            bb.WriteFloat(z);
        }
    }

    public class Vector4 : Vector3
    { 
        public float w { get; internal set; }

        public Vector4()
        {
        }

        public Vector4(Vector2 v2)
            : base(v2)
        {
        }

        public Vector4(Vector3 v3)
            : base(v3.x, v3.y, v3.z)
        {
        }

        public Vector4(float x, float y, float z, float w)
            : base(x, y, z)
        {
            this.w = w;
        }

        public override void Decode(ByteBuffer bb)
        {
            base.Decode(bb);
            w = bb.ReadFloat();
        }

        public override void Encode(ByteBuffer bb)
        {
            base.Encode(bb);
            bb.WriteFloat(w);
        }
    }

    public class Quaternion : Vector4
    {
        public Quaternion()
        { 
        }

        public Quaternion(Vector2 v2)
            : base(v2)
        {
        }

        public Quaternion(Vector3 v3)
            : base(v3)
        {
        }

        public Quaternion(Vector4 v4)
            : base(v4.x, v4.y, v4.z, v4.w)
        {
        }

        public Quaternion(float x, float y, float z, float w)
            : base(x, y, z, w)
        {
        }
    }

    public class Vector2Int : Serializable
    {
        public int x { get; internal set; }
        public int y { get; internal set; }

        public Vector2Int()
        { 
        }

        public Vector2Int(int x, int y)
        {
            this.x = x;
            this.y = y;
        }

        public virtual void Decode(ByteBuffer bb)
        {
            x = bb.ReadInt();
            y = bb.ReadInt();
        }

        public virtual void Encode(ByteBuffer bb)
        {
            bb.WriteInt(x);
            bb.WriteInt(y);
        }
    }

    public class Vector3Int : Vector2Int
    {
        public int z { get; internal set; }

        public Vector3Int()
        { 
        }
        
        public Vector3Int(Vector2Int v2)
            : base (v2.x, v2.y)
        {
        }

        public Vector3Int(int x, int y, int z)
            : base(x, y)
        {
            this.z = z;
        }

        public override void Decode(ByteBuffer bb)
        {
            base.Decode(bb);
            z = bb.ReadInt();
        }

        public override void Encode(ByteBuffer bb)
        {
            base.Encode(bb);
            bb.WriteInt(z);
        }
    }
}
