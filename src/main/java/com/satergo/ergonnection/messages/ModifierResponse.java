package com.satergo.ergonnection.messages;

import com.satergo.ergonnection.VLQReader;
import com.satergo.ergonnection.VLQWriter;
import com.satergo.ergonnection.protocol.ProtocolMessage;
import com.satergo.ergonnection.records.Modifier;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * It is actually called "Modifier", but that would be annoying to use with the record also being called Modifier.
 */
public record ModifierResponse(List<Modifier> modifiers) implements ProtocolMessage {

	public static final int CODE = 33;

	public static ModifierResponse deserialize(DataInputStream in) throws IOException {
		int count = (int) VLQReader.readUInt(in);
		ArrayList<Modifier> modifiers = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			modifiers.add(Modifier.deserialize(in));
		}
		return new ModifierResponse(Collections.unmodifiableList(modifiers));
	}

	@Override
	public void serialize(DataOutputStream out) throws IOException {
		VLQWriter.writeUInt(out, modifiers.size());
		for (Modifier modifier : modifiers) {
			modifier.serialize(out);
		}
	}

	@Override public int code() { return CODE; }
}
