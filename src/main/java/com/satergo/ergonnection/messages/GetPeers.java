package com.satergo.ergonnection.messages;

import com.satergo.ergonnection.protocol.ProtocolMessage;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public record GetPeers() implements ProtocolMessage {

	public static final int CODE = 1;

	public static GetPeers deserialize(DataInputStream in) {
		return new GetPeers();
	}

	@Override public void serialize(DataOutputStream out) {}
	@Override public byte[] toByteArray() { return new byte[0]; }

	@Override public int code() { return CODE; }
}
