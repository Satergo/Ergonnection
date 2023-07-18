package com.satergo.ergonnection.messages;

import com.satergo.ergonnection.VLQInputStream;
import com.satergo.ergonnection.VLQOutputStream;
import com.satergo.ergonnection.protocol.ProtocolMessage;
import com.satergo.ergonnection.records.Peer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record Peers(List<Peer> peers) implements ProtocolMessage {

	public static final int CODE = 2;

	public static Peers deserialize(VLQInputStream in) throws IOException {
		ArrayList<Peer> peers = new ArrayList<>();
		int peerCount = in.readInt();
		for (int i = 0; i < peerCount; i++) {
			Peer peer = Peer.deserialize(in);
			peers.add(peer);
		}
		return new Peers(Collections.unmodifiableList(peers));
	}

	@Override
	public void serialize(VLQOutputStream out) throws IOException {
		out.writeInt(peers.size());
		for (Peer peer : peers) {
			peer.serialize(out);
		}
	}

	@Override public int code() { return CODE; }
}
