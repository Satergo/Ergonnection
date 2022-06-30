package com.satergo.ergonnection.records;

import com.satergo.ergonnection.VLQReader;
import com.satergo.ergonnection.VLQWriter;
import com.satergo.ergonnection.protocol.ProtocolRecord;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public record Header(byte[] bytes) implements ProtocolRecord {

	public static Header deserialize(DataInputStream in) throws IOException {
		return new Header(in.readNBytes(VLQReader.readUShort(in)));
	}

	@Override
	public void serialize(DataOutputStream out) throws IOException {
		VLQWriter.writeUShort(out, bytes.length);
		out.write(bytes);
	}
}
