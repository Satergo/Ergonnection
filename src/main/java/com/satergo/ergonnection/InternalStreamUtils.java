package com.satergo.ergonnection;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class InternalStreamUtils {
	private InternalStreamUtils() {}

	public static void writeUTF8ByteLen(VLQOutputStream out, String s) throws IOException {
		if (s.length() > 255) throw new IllegalArgumentException("too long");
		byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
		out.write(bytes.length);
		out.write(bytes);
	}

	public static String readUTF8ByteLen(VLQInputStream in) throws IOException {
		int len = in.read();
		byte[] bytes = in.readNFully(len);
		return new String(bytes, StandardCharsets.UTF_8);
	}

	/**
	 * @throws java.io.EOFException if this input stream reaches the end before reading all the bytes.
	 */
	public static byte[] readNFully(DataInputStream in, int length) throws IOException {
		byte[] bytes = new byte[length];
		in.readFully(bytes);
		return bytes;
	}
}
