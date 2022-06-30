package com.satergo.ergonnection.records;

import com.satergo.ergonnection.VLQReader;
import com.satergo.ergonnection.VLQWriter;
import com.satergo.ergonnection.protocol.ProtocolRecord;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public record Modifier(byte[] typeId, byte[] object) implements ProtocolRecord {

	public Modifier {
		if (typeId.length != 32) throw new IllegalArgumentException("typeId must be of length 32");
	}

	public static Modifier deserialize(DataInputStream in) throws IOException {
		return new Modifier(in.readNBytes(32), in.readNBytes((int) VLQReader.readUInt(in)));
	}

	@Override
	public void serialize(DataOutputStream out) throws IOException {
		out.write(typeId);
		VLQWriter.writeUInt(out, object.length);
		out.write(object);
	}
}
