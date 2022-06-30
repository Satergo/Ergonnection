package com.satergo.ergonnection.records;

import com.satergo.ergonnection.*;
import com.satergo.ergonnection.protocol.ProtocolRecord;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record Peer(String agentName, Version version, String peerName, InetSocketAddress publicAddress, List<Feature> features) implements ProtocolRecord {

	public boolean hasPublicAddress() {
		return publicAddress != null;
	}

	public static Peer deserialize(DataInputStream in) throws IOException {
		String agentName = StreamUTF8.readByteLen(in);
		Version version = Version.parse(in.readByte() + "." + in.readByte() + "." + in.readByte());
		String peerName = StreamUTF8.readByteLen(in);
		boolean hasPublicAddress = in.readBoolean();
		InetSocketAddress publicAddress = null;
		if (hasPublicAddress) {
			// Protocol for some reason encodes it as length + 4
			int publicAddressLength = in.readUnsignedByte() - 4;
			publicAddress = new InetSocketAddress(
					InetAddress.getByAddress(in.readNBytes(publicAddressLength)),
					// For some reason it uses u-int instead of u-short
					(int) VLQReader.readUInt(in)
			);
		}
		ArrayList<Feature> features = new ArrayList<>();
		int featureCount = in.readUnsignedByte();
		for (int i = 0; i < featureCount; i++) {
			features.add(Feature.deserialize(in));
		}
		return new Peer(agentName, version, peerName, publicAddress, Collections.unmodifiableList(features));
	}

	@Override
	public void serialize(DataOutputStream out) throws IOException {
		StreamUTF8.writeByteLen(out, agentName);
		out.write(version().major());
		out.write(version().minor());
		out.write(version().patch());
		StreamUTF8.writeByteLen(out, peerName);
		out.writeBoolean(hasPublicAddress());
		if (hasPublicAddress()) {
			InetAddress address = publicAddress.getAddress();
			out.write(address.getAddress().length + 4);
			out.write(address.getAddress());
			VLQWriter.writeUInt(out, publicAddress.getPort());
		}
		out.write(features.size());
		for (Feature feature : features) {
			feature.serialize(out);
		}
	}
}
