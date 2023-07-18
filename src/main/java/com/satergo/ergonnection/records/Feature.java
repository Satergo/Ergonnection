package com.satergo.ergonnection.records;

import com.satergo.ergonnection.VLQInputStream;
import com.satergo.ergonnection.VLQOutputStream;
import com.satergo.ergonnection.protocol.ProtocolRecord;

import java.io.IOException;

public record Feature(int id, byte[] data) implements ProtocolRecord {

	public static Feature deserialize(VLQInputStream in) throws IOException {
		return new Feature(in.readUnsignedByte(), in.readNBytes(in.readUnsignedShort()));
	}

	@Override
	public void serialize(VLQOutputStream out) throws IOException {
		out.write(id);
		out.writeUnsignedShort(data.length);
		out.write(data);
	}
}
