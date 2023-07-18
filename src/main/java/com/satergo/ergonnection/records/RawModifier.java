package com.satergo.ergonnection.records;

import com.satergo.ergonnection.VLQReader;
import com.satergo.ergonnection.VLQWriter;
import com.satergo.ergonnection.protocol.ProtocolRecord;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public record RawModifier(int typeId, byte[] id, byte[] object) implements ProtocolRecord {

	public RawModifier {
		if (id.length != 32) throw new IllegalArgumentException("id must be of length 32");
	}

	public static RawModifier deserialize(int typeId, DataInputStream in) throws IOException {
		return new RawModifier(typeId, in.readNBytes(32), in.readNBytes((int) VLQReader.readUInt(in)));
	}

	@Override
	public void serialize(DataOutputStream out) throws IOException {
		out.write(id);
		VLQWriter.writeUInt(out, object.length);
		out.write(object);
	}
}
