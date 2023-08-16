package com.satergo.ergonnection.records;

import com.satergo.ergonnection.VLQInputStream;
import com.satergo.ergonnection.VLQOutputStream;
import com.satergo.ergonnection.protocol.ProtocolRecord;

import java.io.IOException;

public record Header(byte[] bytes) implements ProtocolRecord {

	public static Header deserialize(VLQInputStream in) throws IOException {
		return new Header(in.readNFully(in.readUnsignedShort()));
	}

	@Override
	public void serialize(VLQOutputStream out) throws IOException {
		out.writeUnsignedShort(bytes.length);
		out.write(bytes);
	}
}
