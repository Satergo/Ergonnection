package com.satergo.ergonnection.protocol;

import com.satergo.ergonnection.VLQReader;
import com.satergo.ergonnection.messages.*;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Protocol {

	public interface MessageDeserializer<T extends ProtocolMessage> {
		T deserialize(DataInputStream in) throws IOException;
	}

	public static final Map<Integer, MessageDeserializer<?>> deserializers = new HashMap<>();

	static {
		deserializers.put(GetPeers.CODE, GetPeers::deserialize);
		deserializers.put(Peers.CODE, Peers::deserialize);
		deserializers.put(SyncInfoNew.CODE, in -> {
			in.mark(Integer.MAX_VALUE);
			if (VLQReader.readUShort(in) == 0 && in.readByte() == -1) {
				in.reset();
				in.mark(0);
				return SyncInfoNew.deserialize(in);
			} else {
				in.reset();
				in.mark(0);
				return SyncInfoOld.deserialize(in);
			}
		});
		deserializers.put(Inv.CODE, Inv::deserialize);
		deserializers.put(ModifierRequest.CODE, ModifierRequest::deserialize);
		deserializers.put(ModifierResponse.CODE, ModifierResponse::deserialize);
	}

	public static ProtocolMessage deserialize(int code, byte[] data) throws IOException {
		try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(data))) {
			MessageDeserializer<?> deserializer = deserializers.get(code);
			if (deserializer == null) throw new IllegalArgumentException("Unsupported message with code " + code);
			return deserializer.deserialize(in);
		}
	}
}
