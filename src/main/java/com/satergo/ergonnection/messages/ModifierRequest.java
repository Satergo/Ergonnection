package com.satergo.ergonnection.messages;

import com.satergo.ergonnection.VLQInputStream;
import com.satergo.ergonnection.VLQOutputStream;
import com.satergo.ergonnection.protocol.ProtocolMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record ModifierRequest(int typeId, List<byte[]> elements) implements ProtocolMessage {

	public static final int CODE = 22;

	public static ModifierRequest deserialize(VLQInputStream in) throws IOException {
		int typeId = in.readUnsignedByte();
		int count = (int) in.readUnsignedInt();
		ArrayList<byte[]> elements = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			elements.add(in.readNBytes(32));
		}
		return new ModifierRequest(typeId, Collections.unmodifiableList(elements));
	}

	@Override
	public void serialize(VLQOutputStream out) throws IOException {
		out.write(typeId);
		out.writeUnsignedInt(elements.size());
		for (byte[] element : elements) {
			out.write(element);
		}
	}

	@Override public int code() { return CODE; }
}
