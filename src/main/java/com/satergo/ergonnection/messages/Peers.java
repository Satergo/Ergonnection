package com.satergo.ergonnection.messages;

import com.satergo.ergonnection.VLQReader;
import com.satergo.ergonnection.VLQWriter;
import com.satergo.ergonnection.protocol.ProtocolMessage;
import com.satergo.ergonnection.records.Peer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record Peers(List<Peer> peers) implements ProtocolMessage {

	public static final int CODE = 2;

	public static Peers deserialize(DataInputStream in) throws IOException {
		ArrayList<Peer> peers = new ArrayList<>();
		int peerCount = VLQReader.readInt(in);
		for (int i = 0; i < peerCount; i++) {
			Peer peer = Peer.deserialize(in);
			peers.add(peer);
		}
		return new Peers(Collections.unmodifiableList(peers));
	}

	@Override
	public void serialize(DataOutputStream out) throws IOException {
		VLQWriter.writeInt(out, peers.size());
		for (Peer peer : peers) {
			peer.serialize(out);
		}
	}

	@Override public int code() { return CODE; }
}
