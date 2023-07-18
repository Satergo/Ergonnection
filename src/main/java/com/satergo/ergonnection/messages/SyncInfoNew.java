package com.satergo.ergonnection.messages;

import com.satergo.ergonnection.VLQInputStream;
import com.satergo.ergonnection.VLQOutputStream;
import com.satergo.ergonnection.protocol.ProtocolMessage;
import com.satergo.ergonnection.records.Header;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record SyncInfoNew(List<Header> headers) implements ProtocolMessage {

	public static final int CODE = 65;

	public static SyncInfoNew deserialize(VLQInputStream in) throws IOException {
		if (in.readUnsignedShort() != 0) throw new IllegalArgumentException();
		if (in.readByte() != -1) throw new IllegalArgumentException();
		int count = in.readUnsignedByte();
		ArrayList<Header> headers = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			headers.add(Header.deserialize(in));
		}
		return new SyncInfoNew(Collections.unmodifiableList(headers));
	}

	@Override
	public void serialize(VLQOutputStream out) throws IOException {
		out.writeUnsignedShort(0);
		out.write(-1);
		out.write(headers.size());
		for (Header header : headers) {
			header.serialize(out);
		}
	}

	@Override public int code() { return CODE; }
}
