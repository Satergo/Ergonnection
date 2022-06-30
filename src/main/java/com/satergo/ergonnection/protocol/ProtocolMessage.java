package com.satergo.ergonnection.protocol;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public interface ProtocolMessage {

	void serialize(DataOutputStream out) throws IOException;

	int code();

	default byte[] toByteArray() throws IOException {
		try (ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			 DataOutputStream out = new DataOutputStream(bytes)) {
			serialize(out);
			out.flush();
			return bytes.toByteArray();
		}
	}
}
