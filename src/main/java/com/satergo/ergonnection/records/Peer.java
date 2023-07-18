package com.satergo.ergonnection.records;

import com.satergo.ergonnection.StreamUTF8;
import com.satergo.ergonnection.VLQInputStream;
import com.satergo.ergonnection.VLQOutputStream;
import com.satergo.ergonnection.Version;
import com.satergo.ergonnection.protocol.ProtocolRecord;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public record Peer(String agentName, String peerName, Version version, List<Feature> features,
				   InetSocketAddress publicAddress) implements ProtocolRecord {

	public Peer {
		if (agentName.length() > 255) throw new IllegalArgumentException("agentName is too long (max 255)");
		if (peerName.length() > 255) throw new IllegalArgumentException("peerName is too long (max 255)");
		Objects.requireNonNull(version, "version");
		Objects.requireNonNull(features, "features");
	}

	public Peer(String agentName, String peerName, Version version, List<Feature> features) {
		this(agentName, peerName, version, features, null);
	}

	public boolean hasPublicAddress() {
		return publicAddress != null;
	}

	public static Peer deserialize(VLQInputStream in) throws IOException {
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
					// For some reason, it uses u-int instead of u-short
					(int) in.readUnsignedInt()
			);
		}
		ArrayList<Feature> features = new ArrayList<>();
		int featureCount = in.readUnsignedByte();
		for (int i = 0; i < featureCount; i++) {
			features.add(Feature.deserialize(in));
		}
		return new Peer(agentName, peerName, version, Collections.unmodifiableList(features), publicAddress);
	}

	@Override
	public void serialize(VLQOutputStream out) throws IOException {
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
			out.writeUnsignedInt(publicAddress.getPort());
		}
		out.write(features.size());
		for (Feature feature : features) {
			feature.serialize(out);
		}
	}
}
