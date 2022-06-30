package com.satergo.ergonnection;

import java.io.IOException;
import java.io.InputStream;

/**
 * @see <a href="https://en.wikipedia.org/wiki/Variable-length_quantity">Variable-length quantity on Wikipedia</a>
 * @see <a href="https://github.com/protocolbuffers/protobuf/blob/main/java/core/src/main/java/com/google/protobuf/CodedInputStream.java">protobuf/CodedInputStream.java</a>
 */
public class VLQReader {
	private VLQReader() {}

	/**
	 * @apiNote Uses VLQ then ZigZag decoding.
	 */
	public static short readShort(InputStream in) throws IOException {
		return (short) decodeZigZagInt((int) readULong(in));
	}

	public static int readUShort(InputStream in) throws IOException {
		int value = (int) readULong(in);
		if (value < 0 || value > 0xFFFF)
			throw new IllegalArgumentException(value + " is out of unsigned short range");
		return value;
	}

	/**
	 * @apiNote Uses VLQ then ZigZag decoding.
	 */
	public static int readInt(InputStream in) throws IOException {
		return decodeZigZagInt((int) readULong(in));
	}

	public static long readUInt(InputStream in) throws IOException {
		long value = readULong(in);
		if (value < 0 || value > 0xFFFFFFFFL)
			throw new IllegalArgumentException(value + " is out of unsigned int range");
		return value;
	}

	/**
	 * @apiNote Uses VLQ then ZigZag decoding.
	 */
	public static long readLong(InputStream in) throws IOException {
		return decodeZigZagLong(readULong(in));
	}

	/**
	 * Reads up to (but can be less than) 64 bits of VLQ-encoded value.
	 */
	public static long readULong(InputStream in) throws IOException {
		long result = 0;
		for (int shift = 0; shift < 64; shift += 7) {
			final byte b = (byte) in.read();
			result |= (long) (b & 0x7F) << shift;
			if ((b & 0x80) == 0) {
				return result;
			}
		}
		throw new IllegalStateException("More bytes than needed found");
	}

	/**
	 * Decodes a ZigZag-encoded integer (32 bits)
	 */
	public static int decodeZigZagInt(int n) {
		return (n >>> 1) ^ -(n & 1);
	}

	/**
	 * Decodes a ZigZag-encoded long (64 bits)
	 */
	public static long decodeZigZagLong(long n) {
		return (n >>> 1) ^ -(n & 1);
	}
}
