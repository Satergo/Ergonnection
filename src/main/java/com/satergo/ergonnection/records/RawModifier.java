package com.satergo.ergonnection.records;

import com.satergo.ergonnection.VLQInputStream;
import com.satergo.ergonnection.VLQOutputStream;
import com.satergo.ergonnection.protocol.ProtocolRecord;

import java.io.IOException;

public record RawModifier(int typeId, byte[] id, byte[] object) implements ProtocolRecord {

	public RawModifier {
		if (id.length != 32) throw new IllegalArgumentException("id must be of length 32");
	}

	public static RawModifier deserialize(int typeId, VLQInputStream in) throws IOException {
		return new RawModifier(typeId, in.readNBytes(32), in.readNBytes((int) in.readUnsignedInt()));
	}

	@Override
	public void serialize(VLQOutputStream out) throws IOException {
		out.write(id);
		out.writeUnsignedInt(object.length);
		out.write(object);
	}
}
