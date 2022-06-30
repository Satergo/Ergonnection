package com.satergo.ergonnection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class StreamUTF8 {
	private StreamUTF8() {}

	public static void writeByteLen(OutputStream out, String s) throws IOException {
		if (s.length() > 255) throw new IllegalArgumentException("too long");
		byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
		out.write(bytes.length);
		out.write(bytes);
	}

	public static String readByteLen(InputStream in) throws IOException {
		int len = in.read();
		byte[] bytes = in.readNBytes(len);
		return new String(bytes, StandardCharsets.UTF_8);
	}
}
