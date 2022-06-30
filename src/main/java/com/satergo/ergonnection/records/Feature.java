package com.satergo.ergonnection.records;

import com.satergo.ergonnection.protocol.ProtocolRecord;
import com.satergo.ergonnection.VLQReader;
import com.satergo.ergonnection.VLQWriter;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public record Feature(int id, byte[] data) implements ProtocolRecord {

	public static Feature deserialize(DataInputStream in) throws IOException {
		return new Feature(in.readUnsignedByte(), in.readNBytes(VLQReader.readUShort(in)));
	}

	@Override
	public void serialize(DataOutputStream out) throws IOException {
		out.write(id);
		VLQWriter.writeUShort(out, data.length);
		out.write(data);
	}
}
