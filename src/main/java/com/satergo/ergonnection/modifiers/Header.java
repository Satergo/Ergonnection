package com.satergo.ergonnection.modifiers;

import com.satergo.ergonnection.VLQInputStream;
import com.satergo.ergonnection.VLQOutputStream;
import com.satergo.ergonnection.modifiers.data.AutolykosSolution;
import com.satergo.ergonnection.protocol.ProtocolModifier;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public record Header(byte[] id, byte version, byte[] parentId, byte[] adProofsRoot, byte[] stateRoot, byte[] transactionsRoot,
					 long timestamp, long nBits, int height, byte[] extensionHash, byte[] votes, AutolykosSolution powSolution) implements ProtocolModifier {

	public static final int TYPE_ID = 101;

	/**
	 * Block version during mainnet launch
	 */
	public static final byte INITIAL_VERSION = 1;

	public static Header deserialize(byte[] id, byte[] data) throws IOException {
		VLQInputStream in = new VLQInputStream(new ByteArrayInputStream(data));
		byte version = in.readByte();
		byte[] parentId = in.readNBytes(32);
		byte[] adProofsRoot = in.readNBytes(32);
		byte[] transactionsRoot = in.readNBytes(32);
		byte[] stateRoot = in.readNBytes(33);
		long timestamp = in.readUnsignedLong();
		byte[] extensionHash = in.readNBytes(32);
		byte[] nBitsBytes = in.readNBytes(4);
		long nBits = ((nBitsBytes[0] & 0xFFL) << 24) | ((nBitsBytes[1] & 0xFFL) << 16) | ((nBitsBytes[2] & 0xFFL) << 8) | (nBitsBytes[3] & 0xFFL);
		int height = Math.toIntExact(in.readUnsignedInt());
		byte[] votes = in.readNBytes(3);
		if (version > INITIAL_VERSION) {
			int newFieldsSize = in.readUnsignedByte();
			in.skipNBytes(newFieldsSize);
		}
		AutolykosSolution powSolution = AutolykosSolution.deserialize(in, version);
		return new Header(id, version, parentId, adProofsRoot, stateRoot, transactionsRoot, timestamp, nBits, height, extensionHash, votes, powSolution);
	}

	@Override
	public void serialize(VLQOutputStream out) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public int typeId() {
		return TYPE_ID;
	}
}
