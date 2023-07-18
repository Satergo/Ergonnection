package com.satergo.ergonnection.messages;

import com.satergo.ergonnection.VLQReader;
import com.satergo.ergonnection.VLQWriter;
import com.satergo.ergonnection.protocol.ProtocolMessage;
import com.satergo.ergonnection.records.RawModifier;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record ModifierResponse(List<RawModifier> rawModifiers) implements ProtocolMessage {

	public static final int CODE = 33;

	public static ModifierResponse deserialize(DataInputStream in) throws IOException {
		int typeId = in.readByte();
		int count = (int) VLQReader.readUInt(in);
		ArrayList<RawModifier> rawModifiers = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			rawModifiers.add(RawModifier.deserialize(typeId, in));
		}
		return new ModifierResponse(Collections.unmodifiableList(rawModifiers));
	}

	@Override
	public void serialize(DataOutputStream out) throws IOException {
		VLQWriter.writeUInt(out, rawModifiers.size());
		for (RawModifier rawModifier : rawModifiers) {
			rawModifier.serialize(out);
		}
	}

	@Override public int code() { return CODE; }
}
