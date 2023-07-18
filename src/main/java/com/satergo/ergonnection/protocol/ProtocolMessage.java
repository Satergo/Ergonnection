package com.satergo.ergonnection.protocol;

import com.satergo.ergonnection.VLQOutputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public interface ProtocolMessage {

	void serialize(VLQOutputStream out) throws IOException;

	int code();

	default byte[] toByteArray() throws IOException {
		try (ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			 VLQOutputStream out = new VLQOutputStream(bytes)) {
			serialize(out);
			out.flush();
			return bytes.toByteArray();
		}
	}
}
