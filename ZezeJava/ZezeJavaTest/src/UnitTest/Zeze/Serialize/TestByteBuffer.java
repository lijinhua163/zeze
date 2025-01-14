package UnitTest.Zeze.Serialize;

import java.nio.charset.StandardCharsets;
import Zeze.Net.Binary;
import Zeze.Serialize.ByteBuffer;
import Zeze.Util.BitConverter;
import demo.Bean1;
import demo.Module1.Key;
import demo.Module1.Simple;
import demo.Module1.Value;
import junit.framework.TestCase;

public class TestByteBuffer extends TestCase {
	public void testBytes() {
		ByteBuffer bb = ByteBuffer.Allocate();
		byte[] v = new byte[0];
		bb.WriteBytes(v);
		assertEquals(1, bb.Size());
		assertEquals("00", bb.toString());
		assertEquals(BitConverter.toString(v), BitConverter.toString(bb.ReadBytes()));
		assertEquals(bb.ReadIndex, bb.WriteIndex);

		v = new byte[]{1, 2};
		bb.WriteBytes(v);
		assertEquals(3, bb.Size());
		assertEquals("02-01-02", bb.toString());
		assertEquals(BitConverter.toString(v), BitConverter.toString(bb.ReadBytes()));
		assertEquals(bb.ReadIndex, bb.WriteIndex);

		var str = "abc汉字123";
		bb.WriteString(str);
		assertEquals(13, bb.Size());
		assertEquals("0C-61-62-63-E6-B1-89-E5-AD-97-31-32-33", bb.toString());
		assertEquals(str, bb.ReadString());
		assertEquals(bb.ReadIndex, bb.WriteIndex);
	}

	public void testBasic() {
		ByteBuffer bb = ByteBuffer.Allocate();
		assertEquals(bb.ReadIndex, bb.WriteIndex);

		{
			boolean v = true;
			bb.WriteBool(v);
			assertEquals(1, bb.Size());
			assertEquals(1, bb.Bytes[bb.ReadIndex]);
			assertEquals(v, bb.ReadBool());
			assertEquals(bb.ReadIndex, bb.WriteIndex);
		}
		{
			byte v = 1;
			bb.WriteByte(v);
			assertEquals(1, bb.Size());
			assertEquals(1, bb.Bytes[bb.ReadIndex]);
			assertEquals(v, bb.ReadByte());
			assertEquals(bb.ReadIndex, bb.WriteIndex);
		}
		{
			double v = 1.1;
			bb.WriteDouble(v);
			assertEquals(8, bb.Size());
			assertEquals("9A-99-99-99-99-99-F1-3F", bb.toString());
			assertEquals(v, bb.ReadDouble());
			assertEquals(bb.ReadIndex, bb.WriteIndex);
		}
		{
			float v = 1.1f;
			bb.WriteFloat(v);
			assertEquals(4, bb.Size());
			assertEquals("CD-CC-8C-3F", bb.toString());
			assertEquals(v, bb.ReadFloat());
			assertEquals(bb.ReadIndex, bb.WriteIndex);
		}
		{
			int int4 = 0x12345678;
			bb.WriteInt4(int4);
			assertEquals(4, bb.Size());
			assertEquals("78-56-34-12", bb.toString());
			assertEquals(int4, bb.ReadInt4());
			assertEquals(bb.ReadIndex, bb.WriteIndex);
		}
		{
			long long8 = 0x1234567801020304L;
			bb.WriteLong8(long8);
			assertEquals(8, bb.Size());
			assertEquals("04-03-02-01-78-56-34-12", bb.toString());
			assertEquals(long8, bb.ReadLong8());
			assertEquals(bb.ReadIndex, bb.WriteIndex);
		}
		{
			long long8 = -12345678;
			bb.WriteLong8(long8);
			assertEquals(8, bb.Size());
			assertEquals("B2-9E-43-FF-FF-FF-FF-FF", bb.toString());
			assertEquals(long8, bb.ReadLong8());
			assertEquals(bb.ReadIndex, bb.WriteIndex);
		}
		{
			long long8 = -1;
			bb.WriteLong8(long8);
			assertEquals(8, bb.Size());
			assertEquals("FF-FF-FF-FF-FF-FF-FF-FF", bb.toString());
			assertEquals(long8, bb.ReadLong8());
			assertEquals(bb.ReadIndex, bb.WriteIndex);
		}
	}

	public void testUInt() {
		ByteBuffer bb = ByteBuffer.Allocate();
		assertEquals(bb.ReadIndex, bb.WriteIndex);

		int v = 1;
		bb.WriteUInt(v);
		assertEquals(1, bb.Size());
		assertEquals("01", bb.toString());
		assertEquals(v, bb.ReadUInt());
		assertEquals(bb.ReadIndex, bb.WriteIndex);

		v = 0x80;
		bb.WriteUInt(v);
		assertEquals(2, bb.Size());
		assertEquals("80-80", bb.toString());
		assertEquals(v, bb.ReadUInt());
		assertEquals(bb.ReadIndex, bb.WriteIndex);

		v = 0x4000;
		bb.WriteUInt(v);
		assertEquals(3, bb.Size());
		assertEquals("C0-40-00", bb.toString());
		assertEquals(v, bb.ReadUInt());
		assertEquals(bb.ReadIndex, bb.WriteIndex);

		v = 0x20_0000;
		bb.WriteUInt(v);
		assertEquals(4, bb.Size());
		assertEquals("E0-20-00-00", bb.toString());
		assertEquals(v, bb.ReadUInt());
		assertEquals(bb.ReadIndex, bb.WriteIndex);

		v = 0x1000_0000;
		bb.WriteUInt(v);
		assertEquals(5, bb.Size());
		assertEquals("F0-10-00-00-00", bb.toString());
		assertEquals(v, bb.ReadUInt());
		assertEquals(bb.ReadIndex, bb.WriteIndex);

		v = -1;
		bb.WriteUInt(v);
		assertEquals(5, bb.Size());
		assertEquals("F0-FF-FF-FF-FF", bb.toString());
		assertEquals(v, bb.ReadUInt());
		assertEquals(bb.ReadIndex, bb.WriteIndex);
	}

	public void testLong() {
		ByteBuffer bb = ByteBuffer.Allocate();
		assertEquals(bb.ReadIndex, bb.WriteIndex);

		long v = 1;
		bb.WriteLong(v);
		assertEquals(1, bb.Size());
		assertEquals("01", bb.toString());
		assertEquals(v, bb.ReadLong());
		assertEquals(bb.ReadIndex, bb.WriteIndex);

		v = 0x80L;
		bb.WriteLong(v);
		assertEquals(2, bb.Size());
		assertEquals("40-80", bb.toString());
		assertEquals(v, bb.ReadLong());
		assertEquals(bb.ReadIndex, bb.WriteIndex);

		v = 0x4000L;
		bb.WriteLong(v);
		assertEquals(3, bb.Size());
		assertEquals("60-40-00", bb.toString());
		assertEquals(v, bb.ReadLong());
		assertEquals(bb.ReadIndex, bb.WriteIndex);

		v = 0x20_0000L;
		bb.WriteLong(v);
		assertEquals(4, bb.Size());
		assertEquals("70-20-00-00", bb.toString());
		assertEquals(v, bb.ReadLong());
		assertEquals(bb.ReadIndex, bb.WriteIndex);

		v = 0x1000_0000L;
		bb.WriteLong(v);
		assertEquals(5, bb.Size());
		assertEquals("78-10-00-00-00", bb.toString());
		assertEquals(v, bb.ReadLong());
		assertEquals(bb.ReadIndex, bb.WriteIndex);

		v = 0x8_0000_0000L;
		bb.WriteLong(v);
		assertEquals(6, bb.Size());
		assertEquals("7C-08-00-00-00-00", bb.toString());
		assertEquals(v, bb.ReadLong());
		assertEquals(bb.ReadIndex, bb.WriteIndex);

		v = 0x400_0000_0000L;
		bb.WriteLong(v);
		assertEquals(7, bb.Size());
		assertEquals("7E-04-00-00-00-00-00", bb.toString());
		assertEquals(v, bb.ReadLong());
		assertEquals(bb.ReadIndex, bb.WriteIndex);

		v = 0x2_0000_0000_0000L;
		bb.WriteLong(v);
		assertEquals(8, bb.Size());
		assertEquals("7F-02-00-00-00-00-00-00", bb.toString());
		assertEquals(v, bb.ReadLong());
		assertEquals(bb.ReadIndex, bb.WriteIndex);

		v = 0x100_0000_0000_0000L;
		bb.WriteLong(v);
		assertEquals(9, bb.Size());
		assertEquals("7F-81-00-00-00-00-00-00-00", bb.toString());
		assertEquals(v, bb.ReadLong());
		assertEquals(bb.ReadIndex, bb.WriteIndex);

		v = 0x8000_0000_0000_0000L;
		bb.WriteLong(v);
		assertEquals(9, bb.Size());
		assertEquals("80-00-00-00-00-00-00-00-00", bb.toString());
		assertEquals(v, bb.ReadLong());
		assertEquals(bb.ReadIndex, bb.WriteIndex);

		v = -1;
		bb.WriteLong(v);
		assertEquals(1, bb.Size());
		assertEquals("FF", bb.toString());
		assertEquals(v, bb.ReadLong());
		assertEquals(bb.ReadIndex, bb.WriteIndex);
	}

	private static void testInt(int x) {
		ByteBuffer bb = ByteBuffer.Allocate();
		bb.WriteInt(x);
		int y = bb.ReadInt();
		assertEquals(x, y);
		assertEquals(bb.ReadIndex, bb.WriteIndex);
	}

	private static void testLong(long x) {
		ByteBuffer bb = ByteBuffer.Allocate();
		bb.WriteLong(x);
		long y = bb.ReadLong();
		assertEquals(x, y);
		assertEquals(bb.ReadIndex, bb.WriteIndex);
	}

	private static void testUInt(int x) {
		ByteBuffer bb = ByteBuffer.Allocate();
		bb.WriteUInt(x);
		int y = bb.ReadUInt();
		assertEquals(x, y);
		assertEquals(bb.ReadIndex, bb.WriteIndex);
	}

	private static void testAll(long x) {
		testInt((int)x);
		testInt((int)-x);
		testUInt((int)x);
		testUInt((int)-x);
		testLong(x);
		testLong(-x);
	}

	public void testInteger() {
		for (int i = 0; i <= 64; ++i) {
			testAll(1L << i);
			testAll((1L << i) - 1);
			testAll(((1L << i) - 1) & 0x5555_5555_5555_5555L);
			testAll(((1L << i) - 1) & 0xaaaa_aaaa_aaaa_aaaaL);
		}
		testInt(Integer.MIN_VALUE);
		testInt(Integer.MAX_VALUE);
		testLong(Integer.MIN_VALUE);
		testLong(Integer.MAX_VALUE);
		testLong(Long.MIN_VALUE);
		testLong(Long.MAX_VALUE);
		testUInt(Integer.MAX_VALUE);
	}

	public void testBean() {
		Value v = new Value();
		v.setString3("abc");
		v.setBytes8(new Binary("xyz".getBytes(StandardCharsets.UTF_8)));
		Bean1 bean1 = new Bean1(123);
		bean1.getV2().put(12, 34);
		v.getList9().add(bean1);
		Simple simple = new Simple();
		simple.getRemoved().setInt1(999);
		v.getMap16().put(new Key((short)11), simple);

		ByteBuffer bb = ByteBuffer.Allocate();
		v.Encode(bb);
		Value v2 = new Value();
		v2.Decode(bb);
		bb.ReadIndex = 0;
		ByteBuffer bb2 = ByteBuffer.Allocate();
		v2.Encode(bb2);

//		System.out.println(v);
//		System.out.println(v2);

		assertEquals(bb.Size(), bb2.Size());
		assertEquals(bb, bb2);
	}
}
