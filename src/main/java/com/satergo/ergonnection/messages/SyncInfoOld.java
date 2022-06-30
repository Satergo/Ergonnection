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

public record SyncInfoOld(List<byte[]> lastHeaderIDs) implements ProtocolMessage {

	public static final int CODE = 65;

	public static SyncInfoOld deserialize(DataInputStream in) throws IOException {
		int count = VLQReader.readUShort(in);
		ArrayList<byte[]> lastHeaderIDs = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			lastHeaderIDs.add(in.readNBytes(32));
		}
		return new SyncInfoOld(Collections.unmodifiableList(lastHeaderIDs));
	}

	@Override
	public void serialize(DataOutputStream out) throws IOException {
		VLQWriter.writeUShort(out, lastHeaderIDs.size());
		for (byte[] lastHeaderID : lastHeaderIDs) {
			out.write(lastHeaderID);
		}
	}

	@Override public int code() { return CODE; }
}
