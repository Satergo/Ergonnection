package com.satergo.ergonnection;

import java.io.FilterInputStream;
import java.io.InputStream;

public class VLQInputStream extends FilterInputStream {
	/**
	 * Creates a VLQInputStream that uses the specified
	 * underlying InputStream.
	 *
	 * @param in the specified input stream
	 */
	public VLQInputStream(InputStream in) {
		super(in);
	}
}
