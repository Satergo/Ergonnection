package com.satergo.ergonnection.messages;

import com.satergo.ergonnection.VLQInputStream;
import com.satergo.ergonnection.VLQOutputStream;
import com.satergo.ergonnection.protocol.ProtocolMessage;
import com.satergo.ergonnection.records.RawModifier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record ModifierResponse(List<RawModifier> rawModifiers) implements ProtocolMessage {

	public static final int CODE = 33;

	public static ModifierResponse deserialize(VLQInputStream in) throws IOException {
		int typeId = in.readByte();
		int count = (int) in.readUnsignedInt();
		ArrayList<RawModifier> rawModifiers = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			rawModifiers.add(RawModifier.deserialize(typeId, in));
		}
		return new ModifierResponse(Collections.unmodifiableList(rawModifiers));
	}

	@Override
	public void serialize(VLQOutputStream out) throws IOException {
		out.writeUnsignedInt(rawModifiers.size());
		for (RawModifier rawModifier : rawModifiers) {
			rawModifier.serialize(out);
		}
	}

	@Override public int code() { return CODE; }
}
