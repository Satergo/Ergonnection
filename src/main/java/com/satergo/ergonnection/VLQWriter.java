package com.satergo.ergonnection;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @see <a href="https://en.wikipedia.org/wiki/Variable-length_quantity">Variable-length quantity on Wikipedia</a>
 * @see <a href="https://github.com/protocolbuffers/protobuf/blob/a85bbad37905e882f89973e9fa7836ff79d02024/java/core/src/main/java/com/google/protobuf/CodedOutputStream.java">protobuf/CodedOutputStream.java</a>
 */
public class VLQWriter {
	private VLQWriter() {}

	/**
	 * Writes a signed short encoded with ZigZag and VLQ.
	 * Both negative and positive values are supported, but due to ZigZag, encoding positive
	 * values is done less efficiently than by {@link #writeUInt}.
	 * Use {@link #writeUInt} to encode values that are positive.
	 *
	 * @apiNote The resulting varint uses ZigZag encoding as well, which is more efficient at
	 *       encoding negative values than pure VLQ.
	 * @param x signed int
	 */
	public static void writeShort(OutputStream out, short x) throws IOException {
		writeULong(out, encodeZigZagInt(x));
	}

	/**
	 * @param x Unsigned short (0-0xFFFF) represented as int
	 * @throws IllegalArgumentException for values not in unsigned short range
	 */
	public static void writeUShort(OutputStream out, int x) throws IOException {
		if (x < 0 || x > 0xFFFF)
			throw new IllegalArgumentException(x + " is out of unsigned short range");
		writeUInt(out, x);
	}

	/**
	 * Writes a signed int encoded with ZigZag and VLQ.
	 * Both negative and positive values are supported, but due to ZigZag, encoding positive
	 * values is done less efficiently than by {@link #writeUInt}.
	 * Use {@link #writeUInt} to encode values that are positive.
	 *
	 * @apiNote The resulting varint uses ZigZag encoding as well, which is more efficient at
	 *       encoding negative values than pure VLQ.
	 */
	public static void writeInt(OutputStream out, int x) throws IOException {
		writeULong(out, encodeZigZagInt(x));
	}

	/**
	 * Writes an unsigned int encoded with VLQ.
	 * Only positive values are supported. Use {@link #writeInt}
	 * to encode negative and positive values.
	 *
	 * @param x Unsigned int (0-0xFFFFFFFF) represented as long
	 * @throws IllegalArgumentException for values not in unsigned int range
	 */
	public static void writeUInt(OutputStream out, long x) throws IOException {
		if (x < 0 || x > 0xFFFFFFFFL)
			throw new IllegalArgumentException(x + " is out of unsigned int range");
		writeULong(out, x);
	}

	/**
	 * Writes a signed long encoded with VLQ and ZigZag.
	 * Both negative and positive values are supported, but due to ZigZag, encoding positive
	 * values is done less efficiently than by {@link #writeULong}.
	 * Use {@link #writeULong} to encode values that are positive.
	 *
	 * @apiNote The resulting varint uses ZigZag encoding as well, which is more efficient at
	 *       encoding negative values than pure VLQ.
	 */
	public static void writeLong(OutputStream out, long x) throws IOException {
		writeULong(out, encodeZigZagLong(x));
	}

	/**
	 * Writes a signed long value encoded with VLQ.
	 * Both negative and positive values are supported, but only positive values are encoded
	 * efficiently, negative values are taking a toll and use six bytes. Use {@link #writeLong}
	 * to encode negative and positive values.
	 *
	 * @apiNote Don't use it for negative values, the resulting varint is always ten
	 *       bytes long – it is, effectively, treated like a very large unsigned integer.
	 *       If you use {@link #writeLong}, the resulting varint uses ZigZag encoding,
	 *       which is much more efficient.
	 */
	public static void writeULong(OutputStream out, long x) throws IOException {
		byte[] buffer = new byte[10];
		int position = 0;
		long value = x;
		while (true) {
			if ((value & ~0x7FL) == 0) {
				buffer[position++] = (byte) value;
				out.write(buffer, 0, position);
				return;
			} else {
				buffer[position++] = (byte) (((int) value & 0x7F) | 0x80);
				value >>>= 7;
			}
		}
	}

	public static int encodeZigZagInt(final int n) {
		// Note: the right-shift must be arithmetic
		return (n << 1) ^ (n >> 31);
	}

	public static long encodeZigZagLong(final long n) {
		// Note:  the right-shift must be arithmetic
		return (n << 1) ^ (n >> 63);
	}
}
