package com.satergo.ergonnection.messages;

import com.satergo.ergonnection.VLQReader;
import com.satergo.ergonnection.VLQWriter;
import com.satergo.ergonnection.protocol.ProtocolMessage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record ModifierRequest(int typeId, List<byte[]> elements) implements ProtocolMessage {

	public static final int CODE = 22;

	public static ModifierRequest deserialize(DataInputStream in) throws IOException {
		int typeId = in.readUnsignedByte();
		int count = (int) VLQReader.readUInt(in);
		ArrayList<byte[]> elements = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			elements.add(in.readNBytes(32));
		}
		return new ModifierRequest(typeId, Collections.unmodifiableList(elements));
	}

	@Override
	public void serialize(DataOutputStream out) throws IOException {
		out.write(typeId);
		VLQWriter.writeUInt(out, elements.size());
		for (byte[] element : elements) {
			out.write(element);
		}
	}

	@Override public int code() { return CODE; }
}
