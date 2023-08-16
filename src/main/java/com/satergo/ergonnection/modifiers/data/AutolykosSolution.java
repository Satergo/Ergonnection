package com.satergo.ergonnection.modifiers.data;

import com.satergo.ergonnection.VLQInputStream;
import sigmastate.basics.CryptoConstants;
import sigmastate.crypto.BigIntegers;
import sigmastate.crypto.Platform;
import sigmastate.serialization.GroupElementSerializer;
import sigmastate.serialization.SigmaSerializer;

import java.io.IOException;
import java.math.BigInteger;

public record AutolykosSolution(Platform.Ecp minerPubKey, Platform.Ecp oneTimePubKey, byte[] nonce, BigInteger distance) {

	private static final int PUBLIC_KEY_LENGTH = 33;

	public static AutolykosSolution deserialize(VLQInputStream in, byte version) throws IOException {
		if (version == 1) {
			Platform.Ecp minerPublicKey = groupElemFromBytes(in.readNFully(PUBLIC_KEY_LENGTH));
			Platform.Ecp w = groupElemFromBytes(in.readNFully(PUBLIC_KEY_LENGTH));
			byte[] nonce = in.readNFully(8);
			int dBytesLength = in.readUnsignedByte();
			BigInteger d = BigIntegers.fromUnsignedByteArray(in.readNFully(dBytesLength));
			return new AutolykosSolution(minerPublicKey, w, nonce, d);
		} else {
			Platform.Ecp minerPublicKey = groupElemFromBytes(in.readNFully(PUBLIC_KEY_LENGTH));
			byte[] nonce = in.readNFully(8);
			return new AutolykosSolution(minerPublicKey, CryptoConstants.dlogGroup().generator(), nonce, BigInteger.ZERO);
		}
	}

	private static Platform.Ecp groupElemFromBytes(byte[] bytes) {
		return GroupElementSerializer.parse(SigmaSerializer.startReader(bytes, 0));
	}
}
