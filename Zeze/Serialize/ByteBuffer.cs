﻿using System;
using System.Collections.Generic;
using System.Text;
using System.Runtime.CompilerServices;
using System.Diagnostics.CodeAnalysis;

namespace Zeze.Serialize
{
    public sealed class ByteBuffer
    {
        public byte[] Bytes { get; private set; }
        public int ReadIndex { get; set; }
        public int WriteIndex { get; set; }
        public int Capacity { get { return Bytes.Length; } }
        public int Size { get { return WriteIndex - ReadIndex; } }

        public static ByteBuffer Wrap(byte[] bytes)
        {
            return new ByteBuffer(bytes, 0, bytes.Length);
        }

        public static ByteBuffer Wrap(byte[] bytes, int offset, int length)
        {
            ByteBuffer.VerifyArrayIndex(bytes, offset, length);
            return new ByteBuffer(bytes, offset, offset + length);
        }

        public static ByteBuffer Wrap(Zeze.Net.Binary binary)
        {
            return Wrap(binary.Bytes, binary.Offset, binary.Count);
        }

        public static ByteBuffer Allocate()
        {
            return Allocate(1024);
        }

        public static ByteBuffer Allocate(int capacity)
        {
            // add pool?
            // 缓存 ByteBuffer 还是 byte[] 呢？
            // 最大的问题是怎么归还？而且 Bytes 是公开的，可能会被其他地方引用，很难确定什么时候回收。
            // buffer 使用2的幂，数量有限，使用简单策略即可。
            // Dictionary<capacity, List<byte[]>> pool;
            // socket的内存可以归还。
            return new ByteBuffer(capacity);
        }

        private ByteBuffer(int capacity)
        {
            this.Bytes = new byte[ToPower2(capacity)];
            this.ReadIndex = 0;
            this.WriteIndex = 0;
        }

        private ByteBuffer(byte[] bytes, int readIndex, int writeIndex)
        {
            this.Bytes = bytes;
            this.ReadIndex = readIndex;
            this.WriteIndex = writeIndex;
        }

        [MethodImpl(MethodImplOptions.AggressiveInlining)]
        public void FreeInternalBuffer()
        {
            Bytes = Array.Empty<byte>();
            Reset();
        }

        [MethodImpl(MethodImplOptions.AggressiveInlining)]
        public void Append(byte b)
        {
            EnsureWrite(1);
            Bytes[WriteIndex] = b;
            WriteIndex += 1;
        }

        [MethodImpl(MethodImplOptions.AggressiveInlining)]
        public void Append(byte[] bs)
        {
            Append(bs, 0, bs.Length);
        }

        [MethodImpl(MethodImplOptions.AggressiveInlining)]
        public void Append(byte[] bs, int offset, int len)
        {
            EnsureWrite(len);
            Buffer.BlockCopy(bs, offset, Bytes, WriteIndex, len);
            WriteIndex += len;
        }

        [MethodImpl(MethodImplOptions.AggressiveInlining)]
        public void Replace(int writeIndex, byte[] src)
        {
            Replace(writeIndex, src, 0, src.Length);
        }

        [MethodImpl(MethodImplOptions.AggressiveInlining)]
        public void Replace(int writeIndex, byte[] src, int offset, int len)
        {
            if (writeIndex < this.ReadIndex || writeIndex + len > this.WriteIndex)
                throw new Exception();
            Buffer.BlockCopy(src, offset, this.Bytes, writeIndex, len);
        }

        [MethodImpl(MethodImplOptions.AggressiveInlining)]
        public void BeginWriteWithSize4(out int state)
        {
            state = Size;
            EnsureWrite(4);
            WriteIndex += 4;
        }

        [MethodImpl(MethodImplOptions.AggressiveInlining)]
        public void EndWriteWithSize4(int state)
        {
            var oldWriteIndex = state + ReadIndex;
            Replace(oldWriteIndex, BitConverter.GetBytes(WriteIndex - oldWriteIndex - 4));
        }

        [MethodImpl(MethodImplOptions.AggressiveInlining)]
        public void BeginWriteSegment(out int oldSize)
        {
            oldSize = Size;
            EnsureWrite(1);
            WriteIndex += 1;
        }

        [MethodImpl(MethodImplOptions.AggressiveInlining)]
        public void EndWriteSegment(int oldSize)
        {
            int startPos = ReadIndex + oldSize;
            int segmentSize = WriteIndex - startPos - 1;

            // 0 111 1111
            if (segmentSize < 0x80)
            {
                Bytes[startPos] = (byte)segmentSize;
            }
            else if (segmentSize < 0x4000) // 10 11 1111, -
            {
                EnsureWrite(1);
                Bytes[WriteIndex] = Bytes[startPos + 1];
                Bytes[startPos + 1] = (byte)segmentSize;

                Bytes[startPos] = (byte)((segmentSize >> 8) | 0x80);
                WriteIndex += 1;
            }
            else if (segmentSize < 0x200000) // 110 1 1111, -,-
            {
                EnsureWrite(2);
                Bytes[WriteIndex + 1] = Bytes[startPos + 2];
                Bytes[startPos + 2] = (byte)segmentSize;

                Bytes[WriteIndex] = Bytes[startPos + 1];
                Bytes[startPos + 1] = (byte)(segmentSize >> 8);

                Bytes[startPos] = (byte)((segmentSize >> 16) | 0xc0);
                WriteIndex += 2;
            }
            else if (segmentSize < 0x10000000) // 1110 1111,-,-,-
            {
                EnsureWrite(3);
                Bytes[WriteIndex + 2] = Bytes[startPos + 3];
                Bytes[startPos + 3] = (byte)segmentSize;

                Bytes[WriteIndex + 1] = Bytes[startPos + 2];
                Bytes[startPos + 2] = (byte)(segmentSize >> 8);

                Bytes[WriteIndex] = Bytes[startPos + 1];
                Bytes[startPos + 1] = (byte)(segmentSize >> 16);

                Bytes[startPos] = (byte)((segmentSize >> 24) | 0xe0);
                WriteIndex += 3;
            }
            else
            {
                throw new Exception("exceed max segment size");
            }
        }

        [MethodImpl(MethodImplOptions.AggressiveInlining)]
        private void ReadSegment(out int startIndex, out int segmentSize)
        {
            EnsureRead(1);
            int h = Bytes[ReadIndex++];

            startIndex = ReadIndex;

            if (h < 0x80)
            {
                segmentSize = h;
                ReadIndex += segmentSize;
            }
            else if (h < 0xc0)
            {
                EnsureRead(1);
                segmentSize = ((h & 0x3f) << 8) | Bytes[ReadIndex];
                int endPos = ReadIndex + segmentSize;
                Bytes[ReadIndex] = Bytes[endPos];
                ReadIndex += segmentSize + 1;
            }
            else if (h < 0xe0)
            {
                EnsureRead(2);
                segmentSize = ((h & 0x1f) << 16) | ((int)Bytes[ReadIndex] << 8) | Bytes[ReadIndex + 1];
                int endPos = ReadIndex + segmentSize;
                Bytes[ReadIndex] = Bytes[endPos];
                Bytes[ReadIndex + 1] = Bytes[endPos + 1];
                ReadIndex += segmentSize + 2;
            }
            else if (h < 0xf0)
            {
                EnsureRead(3);
                segmentSize = ((h & 0x0f) << 24) | ((int)Bytes[ReadIndex] << 16) | ((int)Bytes[ReadIndex + 1] << 8) | Bytes[ReadIndex + 2];
                int endPos = ReadIndex + segmentSize;
                Bytes[ReadIndex] = Bytes[endPos];
                Bytes[ReadIndex + 1] = Bytes[endPos + 1];
                Bytes[ReadIndex + 2] = Bytes[endPos + 2];
                ReadIndex += segmentSize + 3;
            }
            else
            {
                throw new Exception("exceed max size");
            }
            if (ReadIndex > WriteIndex)
            {
                throw new Exception("segment data not enough");
            }
        }

        [MethodImpl(MethodImplOptions.AggressiveInlining)]
        public void BeginReadSegment(out int saveState)
        {
            ReadSegment(out int startPos, out int _);

            saveState = ReadIndex;
            ReadIndex = startPos;
        }

        [MethodImpl(MethodImplOptions.AggressiveInlining)]
        public void EndReadSegment(int saveState)
        {
            ReadIndex = saveState;
        }

        /// <summary>
        /// 这个方法把剩余可用数据移到buffer开头。
        /// 【注意】这个方法会修改ReadIndex，WriteIndex。
        /// 最好仅在全部读取写入处理完成以后调用处理一次，
        /// 为下一次写入读取做准备。
        /// </summary>
        [MethodImpl(MethodImplOptions.AggressiveInlining)]
        public void Campact()
        {
            int size = this.Size;
            if (size > 0)
            {
                if (ReadIndex > 0)
                {
                    Buffer.BlockCopy(Bytes, ReadIndex, Bytes, 0, size);
                    ReadIndex = 0;
                    WriteIndex = size;
                }
            }
            else
            {
                Reset();
            }
        }

        [MethodImpl(MethodImplOptions.AggressiveInlining)]
        public byte[] Copy()
        {
            byte[] copy = new byte[Size];
            Buffer.BlockCopy(Bytes, ReadIndex, copy, 0, Size);
            return copy;
        }

        [MethodImpl(MethodImplOptions.AggressiveInlining)]
        public void Reset()
        {
            ReadIndex = WriteIndex = 0;
        }

        [MethodImpl(MethodImplOptions.AggressiveInlining)]
        private static int ToPower2(int needSize)
        {
            int size = 1024;
            while (size < needSize)
                size <<= 1;
            return size;
        }


        [MethodImpl(MethodImplOptions.AggressiveInlining)]
        public void EnsureWrite(int size)
        {
            int newSize = WriteIndex + size;
            if (newSize > Capacity)
            {
                byte[] newBytes = new byte[ToPower2(newSize)];
                WriteIndex -= ReadIndex;
                Buffer.BlockCopy(Bytes, ReadIndex, newBytes, 0, WriteIndex);
                ReadIndex = 0;
                Bytes = newBytes;
            }
        }

        [MethodImpl(MethodImplOptions.AggressiveInlining)]
        private void EnsureRead(int size)
        {
            if (ReadIndex + size > WriteIndex)
                 throw new Exception("EnsureRead " + size);
        }

        public void WriteBool(bool b)
        {
            EnsureWrite(1);
            Bytes[WriteIndex++] = (byte)(b ? 1 : 0);
        }

        public bool ReadBool()
        {
            EnsureRead(1);
            return Bytes[ReadIndex++] != 0;
        }

        public void WriteByte(byte x)
        {
            EnsureWrite(1);
            Bytes[WriteIndex++] = x;
        }

        public byte ReadByte()
        {
            EnsureRead(1);
            return Bytes[ReadIndex++];
        }

        public void WriteShort(short x)
        {
            if (x >= 0)
            {
                if (x < 0x80)
                {
                    EnsureWrite(1);
                    Bytes[WriteIndex++] = (byte)x;
                    return;
                }

                if (x < 0x4000)
                {
                    EnsureWrite(2);
                    Bytes[WriteIndex + 1] = (byte)x;
                    Bytes[WriteIndex] = (byte)((x >> 8) | 0x80);
                    WriteIndex += 2;
                    return;
                }
            }
            EnsureWrite(3);
            Bytes[WriteIndex] = 0xff;
            Bytes[WriteIndex + 2] = (byte)x;
            Bytes[WriteIndex + 1] = (byte)(x >> 8);
            WriteIndex += 3;
        }

        public short ReadShort()
        {
            EnsureRead(1);
            int h = Bytes[ReadIndex];
            if (h < 0x80)
            {
                ReadIndex++;
                return (short)h;
            }
            if (h < 0xc0)
            {
                EnsureRead(2);
                int x = ((h & 0x3f) << 8) | Bytes[ReadIndex + 1];
                ReadIndex += 2;
                return (short)x;
            }
            if ((h == 0xff))
            {
                EnsureRead(3);
                int x = (Bytes[ReadIndex + 1] << 8) | Bytes[ReadIndex + 2];
                ReadIndex += 3;
                return (short)x;
            }
            throw new Exception();
        }

        [MethodImpl(MethodImplOptions.AggressiveInlining)]
        public void WriteInt4(int x)
        {
            byte[] bs = BitConverter.GetBytes(x);
            //if (false == BitConverter.IsLittleEndian)
            //    Array.Reverse(bs);
            Append(bs);
        }

        [MethodImpl(MethodImplOptions.AggressiveInlining)]
        public int ReadInt4()
        {
            EnsureRead(4);
            int x = BitConverter.ToInt32(Bytes, ReadIndex);
            ReadIndex += 4;
            return x;
        }

        [MethodImpl(MethodImplOptions.AggressiveInlining)]
        public void WriteLong8(long x)
        {
            byte[] bs = BitConverter.GetBytes(x);
            //if (false == BitConverter.IsLittleEndian)
            //    Array.Reverse(bs);
            Append(bs);
        }

        [MethodImpl(MethodImplOptions.AggressiveInlining)]
        public long ReadLong8()
        {
            EnsureRead(8);
            long x = BitConverter.ToInt64(Bytes, ReadIndex);
            ReadIndex += 8;
            return x;
        }

        [MethodImpl(MethodImplOptions.AggressiveInlining)]
        public void WriteInt(int x)
        {
            WriteUint((uint)x);
        }

        [MethodImpl(MethodImplOptions.AggressiveInlining)]
        public int ReadInt()
        {
            return (int)ReadUint();
        }

        public void WriteUint(uint x)
        {
            // 0 111 1111
            if (x < 0x80)
            {
                EnsureWrite(1);
                Bytes[WriteIndex++] = (byte)x;
            }
            else if (x < 0x4000) // 10 11 1111, -
            {
                EnsureWrite(2);
                Bytes[WriteIndex + 1] = (byte)x;
                Bytes[WriteIndex] = (byte)((x >> 8) | 0x80);
                WriteIndex += 2;
            }
            else if (x < 0x200000) // 110 1 1111, -,-
            {
                EnsureWrite(3);
                Bytes[WriteIndex + 2] = (byte)x;
                Bytes[WriteIndex + 1] = (byte)(x >> 8);
                Bytes[WriteIndex] = (byte)((x >> 16) | 0xc0);
                WriteIndex += 3;
            }
            else if (x < 0x10000000) // 1110 1111,-,-,-
            {
                EnsureWrite(4);
                Bytes[WriteIndex + 3] = (byte)x;
                Bytes[WriteIndex + 2] = (byte)(x >> 8);
                Bytes[WriteIndex + 1] = (byte)(x >> 16);
                Bytes[WriteIndex] = (byte)((x >> 24) | 0xe0);
                WriteIndex += 4;
            }
            else
            {
                EnsureWrite(5);
                Bytes[WriteIndex] = 0xf0;
                Bytes[WriteIndex + 4] = (byte)x;
                Bytes[WriteIndex + 3] = (byte)(x >> 8);
                Bytes[WriteIndex + 2] = (byte)(x >> 16);
                Bytes[WriteIndex + 1] = (byte)(x >> 24);
                WriteIndex += 5;
            }
        }

        public uint ReadUint()
        {
            EnsureRead(1);
            uint h = Bytes[ReadIndex];
            if (h < 0x80)
            {
                ReadIndex++;
                return h;
            }
            else if (h < 0xc0)
            {
                EnsureRead(2);
                uint x = ((h & 0x3f) << 8) | Bytes[ReadIndex + 1];
                ReadIndex += 2;
                return x;
            }
            else if (h < 0xe0)
            {
                EnsureRead(3);
                uint x = ((h & 0x1f) << 16) | ((uint)Bytes[ReadIndex + 1] << 8) | Bytes[ReadIndex + 2];
                ReadIndex += 3;
                return x;
            }
            else if (h < 0xf0)
            {

                EnsureRead(4);
                uint x = ((h & 0x0f) << 24) | ((uint)Bytes[ReadIndex + 1] << 16) | ((uint)Bytes[ReadIndex + 2] << 8) | Bytes[ReadIndex + 3];
                ReadIndex += 4;
                return x;
            }
            else
            {
                EnsureRead(5);
                uint x = ((uint)Bytes[ReadIndex + 1] << 24) | ((uint)(Bytes[ReadIndex + 2] << 16)) | ((uint)Bytes[ReadIndex + 3] << 8) | Bytes[ReadIndex + 4];
                ReadIndex += 5;
                return x;
            }
        }

        [MethodImpl(MethodImplOptions.AggressiveInlining)]
        public void WriteLong(long x)
        {
            WriteUlong((ulong)x);
        }

        [MethodImpl(MethodImplOptions.AggressiveInlining)]
        public long ReadLong()
        {
            return (long)ReadUlong();
        }

        public void WriteUlong(ulong x)
        {
            // 0 111 1111
            if (x < 0x80)
            {
                EnsureWrite(1);
                Bytes[WriteIndex++] = (byte)x;
            }
            else if (x < 0x4000) // 10 11 1111, -
            {
                EnsureWrite(2);
                Bytes[WriteIndex + 1] = (byte)x;
                Bytes[WriteIndex] = (byte)((x >> 8) | 0x80);
                WriteIndex += 2;
            }
            else if (x < 0x200000) // 110 1 1111, -,-
            {
                EnsureWrite(3);
                Bytes[WriteIndex + 2] = (byte)x;
                Bytes[WriteIndex + 1] = (byte)(x >> 8);
                Bytes[WriteIndex] = (byte)((x >> 16) | 0xc0);
                WriteIndex += 3;
            }
            else if (x < 0x10000000) // 1110 1111,-,-,-
            {
                EnsureWrite(4);
                Bytes[WriteIndex + 3] = (byte)x;
                Bytes[WriteIndex + 2] = (byte)(x >> 8);
                Bytes[WriteIndex + 1] = (byte)(x >> 16);
                Bytes[WriteIndex] = (byte)((x >> 24) | 0xe0);
                WriteIndex += 4;
            }
            else if (x < 0x800000000L) // 1111 0xxx,-,-,-,-
            {
                EnsureWrite(5);
                Bytes[WriteIndex + 4] = (byte)x;
                Bytes[WriteIndex + 3] = (byte)(x >> 8);
                Bytes[WriteIndex + 2] = (byte)(x >> 16);
                Bytes[WriteIndex + 1] = (byte)(x >> 24);
                Bytes[WriteIndex] = (byte)((x >> 32) | 0xf0);
                WriteIndex += 5;
            }
            else if (x < 0x40000000000L) // 1111 10xx, 
            {
                EnsureWrite(6);
                Bytes[WriteIndex + 5] = (byte)x;
                Bytes[WriteIndex + 4] = (byte)(x >> 8);
                Bytes[WriteIndex + 3] = (byte)(x >> 16);
                Bytes[WriteIndex + 2] = (byte)(x >> 24);
                Bytes[WriteIndex + 1] = (byte)(x >> 32);
                Bytes[WriteIndex] = (byte)((x >> 40) | 0xf8);
                WriteIndex += 6;
            }
            else if (x < 0x2000000000000L) // 1111 110x,
            {
                EnsureWrite(7);
                Bytes[WriteIndex + 6] = (byte)x;
                Bytes[WriteIndex + 5] = (byte)(x >> 8);
                Bytes[WriteIndex + 4] = (byte)(x >> 16);
                Bytes[WriteIndex + 3] = (byte)(x >> 24);
                Bytes[WriteIndex + 2] = (byte)(x >> 32);
                Bytes[WriteIndex + 1] = (byte)(x >> 40);
                Bytes[WriteIndex] = (byte)((x >> 48) | 0xfc);
                WriteIndex += 7;
            }
            else if (x < 0x100000000000000L) // 1111 1110
            {
                EnsureWrite(8);
                Bytes[WriteIndex + 7] = (byte)x;
                Bytes[WriteIndex + 6] = (byte)(x >> 8);
                Bytes[WriteIndex + 5] = (byte)(x >> 16);
                Bytes[WriteIndex + 4] = (byte)(x >> 24);
                Bytes[WriteIndex + 3] = (byte)(x >> 32);
                Bytes[WriteIndex + 2] = (byte)(x >> 40);
                Bytes[WriteIndex + 1] = (byte)(x >> 48);
                Bytes[WriteIndex] = 0xfe;
                WriteIndex += 8;
            }
            else // 1111 1111
            {
                EnsureWrite(9);
                Bytes[WriteIndex] = 0xff;
                Bytes[WriteIndex + 8] = (byte)x;
                Bytes[WriteIndex + 7] = (byte)(x >> 8);
                Bytes[WriteIndex + 6] = (byte)(x >> 16);
                Bytes[WriteIndex + 5] = (byte)(x >> 24);
                Bytes[WriteIndex + 4] = (byte)(x >> 32);
                Bytes[WriteIndex + 3] = (byte)(x >> 40);
                Bytes[WriteIndex + 2] = (byte)(x >> 48);
                Bytes[WriteIndex + 1] = (byte)(x >> 56);
                WriteIndex += 9;
            }
        }

        public ulong ReadUlong()
        {
            EnsureRead(1);
            uint h = Bytes[ReadIndex];
            if (h < 0x80)
            {
                ReadIndex++;
                return h;
            }
            else if (h < 0xc0)
            {
                EnsureRead(2);
                uint x = ((h & 0x3f) << 8) | Bytes[ReadIndex + 1];
                ReadIndex += 2;
                return x;
            }
            else if (h < 0xe0)
            {
                EnsureRead(3);
                uint x = ((h & 0x1f) << 16) | ((uint)Bytes[ReadIndex + 1] << 8) | Bytes[ReadIndex + 2];
                ReadIndex += 3;
                return x;
            }
            else if (h < 0xf0)
            {
                EnsureRead(4);
                uint x = ((h & 0x0f) << 24) | ((uint)Bytes[ReadIndex + 1] << 16) | ((uint)Bytes[ReadIndex + 2] << 8) | Bytes[ReadIndex + 3];
                ReadIndex += 4;
                return x;
            }
            else if (h < 0xf8)
            {
                EnsureRead(5);
                uint xl = ((uint)Bytes[ReadIndex + 1] << 24) | ((uint)(Bytes[ReadIndex + 2] << 16)) | ((uint)Bytes[ReadIndex + 3] << 8) | (Bytes[ReadIndex + 4]);
                uint xh = h & 0x07;
                ReadIndex += 5;
                return ((ulong)xh << 32) | xl;
            }
            else if (h < 0xfc)
            {
                EnsureRead(6);
                uint xl = ((uint)Bytes[ReadIndex + 2] << 24) | ((uint)(Bytes[ReadIndex + 3] << 16)) | ((uint)Bytes[ReadIndex + 4] << 8) | (Bytes[ReadIndex + 5]);
                uint xh = ((h & 0x03) << 8) | Bytes[ReadIndex + 1];
                ReadIndex += 6;
                return ((ulong)xh << 32) | xl;
            }
            else if (h < 0xfe)
            {
                EnsureRead(7);
                uint xl = ((uint)Bytes[ReadIndex + 3] << 24) | ((uint)(Bytes[ReadIndex + 4] << 16)) | ((uint)Bytes[ReadIndex + 5] << 8) | (Bytes[ReadIndex + 6]);
                uint xh = ((h & 0x01) << 16) | ((uint)Bytes[ReadIndex + 1] << 8) | Bytes[ReadIndex + 2];
                ReadIndex += 7;
                return ((ulong)xh << 32) | xl;
            }
            else if (h < 0xff)
            {
                EnsureRead(8);
                uint xl = ((uint)Bytes[ReadIndex + 4] << 24) | ((uint)(Bytes[ReadIndex + 5] << 16)) | ((uint)Bytes[ReadIndex + 6] << 8) | (Bytes[ReadIndex + 7]);
                uint xh = /*((h & 0x01) << 24) |*/ ((uint)Bytes[ReadIndex + 1] << 16) | ((uint)Bytes[ReadIndex + 2] << 8) | Bytes[ReadIndex + 3];
                ReadIndex += 8;
                return ((ulong)xh << 32) | xl;
            }
            else
            {
                EnsureRead(9);
                uint xl = ((uint)Bytes[ReadIndex + 5] << 24) | ((uint)(Bytes[ReadIndex + 6] << 16)) | ((uint)Bytes[ReadIndex + 7] << 8) | (Bytes[ReadIndex + 8]);
                uint xh = ((uint)Bytes[ReadIndex + 1] << 24) | ((uint)Bytes[ReadIndex + 2] << 16) | ((uint)Bytes[ReadIndex + 3] << 8) | Bytes[ReadIndex + 4];
                ReadIndex += 9;
                return ((ulong)xh << 32) | xl;
            }
        }

        [MethodImpl(MethodImplOptions.AggressiveInlining)]
        public void WriteFloat(float x)
        {
            byte[] bs = BitConverter.GetBytes(x);
            //if (false == BitConverter.IsLittleEndian)
            //    Array.Reverse(bs);
            Append(bs);
        }

        [MethodImpl(MethodImplOptions.AggressiveInlining)]
        public float ReadFloat()
        {
            EnsureRead(4);
            float x = BitConverter.ToSingle(Bytes, ReadIndex);
            ReadIndex += 4;
            return x;
        }

        [MethodImpl(MethodImplOptions.AggressiveInlining)]
        public void WriteDouble(double x)
        {
            byte[] bs = BitConverter.GetBytes(x);
            //if (false == BitConverter.IsLittleEndian)
            //    Array.Reverse(bs);
            Append(bs);
        }

        [MethodImpl(MethodImplOptions.AggressiveInlining)]
        public double ReadDouble()
        {
            EnsureRead(8);
            double x = BitConverter.ToDouble(Bytes, ReadIndex);
            ReadIndex += 8;
            return x;
        }

        [MethodImpl(MethodImplOptions.AggressiveInlining)]
        public void WriteString(string x)
        {
            WriteBytes(Encoding.UTF8.GetBytes(x));
        }

        public string ReadString()
        {
            int n = ReadInt();
            EnsureRead(n);
            string x = Encoding.UTF8.GetString(Bytes, ReadIndex, n);
            ReadIndex += n;
            return x;
        }

        [MethodImpl(MethodImplOptions.AggressiveInlining)]
        public void WriteBytes(byte[] x)
        {
            WriteBytes(x, 0, x.Length);
        }

        public void WriteBytes(byte[] x, int offset, int length)
        {
            WriteInt(length);
            EnsureWrite(length);
            Buffer.BlockCopy(x, offset, Bytes, WriteIndex, length);
            WriteIndex += length;
        }

        [MethodImpl(MethodImplOptions.AggressiveInlining)]
        public void WriteBinary(Zeze.Net.Binary binary)
        {
            WriteBytes(binary.Bytes, binary.Offset, binary.Count);
        }

        public static bool BinaryNoCopy { get; set; } = false; // 没有线程保护
        // XXX 对于byte[]类型直接使用引用，不拷贝。全局配置，只能用于Linkd这种纯转发的程序，优化。

        public Zeze.Net.Binary ReadBinary()
        {
            if (BinaryNoCopy)
                return new Net.Binary(ReadByteBuffer());
            return new Zeze.Net.Binary(ReadBytes());
        }

        public byte[] ReadBytes()
        {
            int n = ReadInt();
            EnsureRead(n);
            byte[] x = new byte[n];
            Buffer.BlockCopy(Bytes, ReadIndex, x, 0, n);
            ReadIndex += n;
            return x;
        }

        [MethodImpl(MethodImplOptions.AggressiveInlining)]
        public void SkipBytes()
        {
            int n = ReadInt();
            EnsureRead(n);
            ReadIndex += n;
        }

        [MethodImpl(MethodImplOptions.AggressiveInlining)]
        public void SkipBytes4()
        {
            int n = ReadInt4();
            EnsureRead(n);
            ReadIndex += n;
        }

        /// <summary>
        /// 会推进ReadIndex，但是返回的ByteBuffer和原来的共享内存。
        /// </summary>
        /// <returns></returns>
        [MethodImpl(MethodImplOptions.AggressiveInlining)]
        public ByteBuffer ReadByteBuffer()
        {
            int n = ReadInt();
            EnsureRead(n);
            int cur = ReadIndex;
            ReadIndex += n;
            return ByteBuffer.Wrap(Bytes, cur, n);
        }

        [MethodImpl(MethodImplOptions.AggressiveInlining)]
        public void WriteByteBuffer(ByteBuffer o)
        {
            WriteBytes(o.Bytes, o.ReadIndex, o.Size);
        }

        public override string ToString()
        {
            return BitConverter.ToString(Bytes, ReadIndex, Size);
        }

        public override bool Equals(object obj)
        {
            return (obj is ByteBuffer other) && Equals(other);
        }

        public bool Equals(ByteBuffer other)
        {
            if (other == null)
                return false;

            if (this.Size != other.Size)
                return false;

            for (int i = 0, n = this.Size; i < n; i++)
            {
                if (Bytes[ReadIndex + i] != other.Bytes[other.ReadIndex + i])
                    return false;
            }

            return true;
        }

        public static int calc_hashnr(long value)
        {
            return calc_hashnr(value.ToString());
        }

        public static int calc_hashnr(string str)
        {
            return calc_hashnr(Encoding.UTF8.GetBytes(str));
        }

        public static int calc_hashnr(byte[] keys)
        {
            return calc_hashnr(keys, 0, keys.Length);
        }

        public static int calc_hashnr(byte[] keys, int offset, int len) 
        {
            int end = offset + len;
            uint hash = 0;
            for (int i = offset; i < end; ++i)
            {
                hash *= 16777619;
                hash ^= (uint)keys[i];
            }            
            return (int)hash; 
        }

        public override int GetHashCode()
        {
            return (int)calc_hashnr(Bytes, ReadIndex, Size);
        }

        // 只能增加新的类型定义，增加时记得同步 SkipUnknownField
        public const int
        INT = 0,
        LONG = 1,
        STRING = 2,
        BOOL = 3,
        BYTE = 4,
        SHORT = 5,
        FLOAT = 6,
        DOUBLE = 7,
        BYTES = 8,
        LIST = 9,
        SET = 10,
        MAP = 11,
        BEAN = 12,
        DYNAMIC = 13,
        TAG_MAX = 31;

        public const int TAG_SHIFT = 5;
        public const int TAG_MASK = (1 << TAG_SHIFT) - 1;
        public const int ID_MASK = (1 << (31 - TAG_SHIFT)) - 1;

        /*
        // 在生成代码的时候使用这个方法检查。生成后的代码不使用这个方法。
        // 可以定义的最大 Variable.Id 为 Zeze.Transaction.Bean.MaxVariableId
        public static int MakeTagId(int tag, int id)
        {
            if (tag < 0 || tag > TAG_MAX)
                throw new OverflowException("tag < 0 || tag > TAG_MAX");
            if (id < 0 || id > ID_MASK)
                throw new OverflowException("id < 0 || id > ID_MASK");

            return (id << TAG_SHIFT) | tag;
        }

        public static int GetTag(int tagid)
        {
            return tagid & TAG_MASK;
        }

        public static int GetId(int tagid)
        {
        }
        */

        public static void VerifyArrayIndex(byte[] bytes, int offset, int length)
        {
            if (offset < 0 || offset > bytes.Length)
                throw new Exception($"{bytes.Length},{offset},{length}");
            int endindex = offset + length;
            if (endindex < 0 || endindex > bytes.Length)
                throw new Exception($"{bytes.Length},{offset},{length}");
            if (offset > endindex)
                throw new Exception($"{bytes.Length},{offset},{length}");
        }

        public static ByteBuffer Encode(Serializable sa)
        {
            ByteBuffer bb = ByteBuffer.Allocate();
            sa.Encode(bb);
            return bb;
        }

        public static void SkipUnknownField(int tagid, ByteBuffer bb)
        {
            int tagType = tagid & TAG_MASK;
            switch (tagType)
            {
                case BOOL:
                    bb.ReadBool();
                    break;
                case BYTE:
                    bb.ReadByte();
                    break;
                case SHORT:
                    bb.ReadShort();
                    break;
                case INT:
                    bb.ReadInt();
                    break;
                case LONG:
                    bb.ReadLong();
                    break;
                case FLOAT:
                    bb.ReadFloat();
                    break;
                case DOUBLE:
                    bb.ReadDouble();
                    break;
                case STRING:
                case BYTES:
                case LIST:
                case SET:
                case MAP:
                case BEAN:
                    bb.SkipBytes();
                    break;
                case DYNAMIC:
                    bb.ReadLong8();
                    bb.SkipBytes();
                    break;
                default:
                    throw new Exception("SkipUnknownField");
            }
        }

        public static void BuildString<T>(StringBuilder sb, IEnumerable<T> c)
        {
            sb.Append("[");
            foreach (var e in c)
            {
                sb.Append(e);
                sb.Append(",");
            }
            sb.Append("]");
        }


        public static void BuildString<TK, TV>(StringBuilder sb, IDictionary<TK, TV> dic)
        {
            sb.Append("{");
            foreach (var e in dic)
            {
                sb.Append(e.Key).Append(':');
                sb.Append(e.Value).Append(',');
            }
            sb.Append('}');
        }

        public static bool Equals(byte[] left, byte[] right)
        {
            if (left == null || right == null)
            {
                return left == right;
            }
            if (left.Length != right.Length)
            {
                return false;
            }
            for (int i = 0; i < left.Length; i++)
            {
                if (left[i] != right[i])
                {
                    return false;
                }
            }
            return true;
        }

        public static int Compare(byte[] left, byte[] right)
        {
            if (left == null || right == null)
            {
                if (left == right) // both null
                    return 0;
                if (left == null) // null is small
                    return -1;
                return 1;
            }
            if (left.Length != right.Length)
            {
                return left.Length.CompareTo(right.Length); // shorter is small
            }

            for (int i = 0; i < left.Length; i++)
            {
                int c = left[i].CompareTo(right[i]);
                if (0 != c)
                    return c;
            }
            return 0;
        }

        [MethodImpl(MethodImplOptions.AggressiveInlining)]
        public static byte[] Copy(byte[] src)
        {
            byte[] result = new byte[src.Length];
            Buffer.BlockCopy(src, 0, result, 0, src.Length);
            return result;
        }

        [MethodImpl(MethodImplOptions.AggressiveInlining)]
        public static byte[] Copy(byte[] src, int offset, int length)
        {
            byte[] result = new byte[length];
            Buffer.BlockCopy(src, offset, result, 0, length);
            return result;
        }
    }
}
