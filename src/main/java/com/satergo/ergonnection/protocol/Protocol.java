package com.satergo.ergonnection.protocol;

import com.satergo.ergonnection.VLQReader;
import com.satergo.ergonnection.messages.*;
import com.satergo.ergonnection.modifiers.ErgoTransaction;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Protocol {

	public interface MessageDeserializer<T extends ProtocolMessage> {
		T deserialize(DataInputStream in) throws IOException;
	}

	public interface ModifierDeserializer<T extends ProtocolModifier> {
		T deserialize(byte[] id, byte[] bytes) throws IOException;
	}

	public static final Map<Integer, MessageDeserializer<?>> messageDeserializers = new HashMap<>();
	public static final Map<Integer, ModifierDeserializer<?>> modifierDeserializers = new HashMap<>();

	static {
		messageDeserializers.put(GetPeers.CODE, GetPeers::deserialize);
		messageDeserializers.put(Peers.CODE, Peers::deserialize);
		messageDeserializers.put(SyncInfoNew.CODE, in -> {
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
		messageDeserializers.put(Inv.CODE, Inv::deserialize);
		messageDeserializers.put(ModifierRequest.CODE, ModifierRequest::deserialize);
		messageDeserializers.put(ModifierResponse.CODE, ModifierResponse::deserialize);

		modifierDeserializers.put(ErgoTransaction.TYPE_ID, ErgoTransaction::deserialize);
	}

	public static ProtocolMessage deserializeMessage(int code, byte[] data) throws IOException {
		try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(data))) {
			MessageDeserializer<?> deserializer = messageDeserializers.get(code);
			if (deserializer == null) throw new UnsupportedOperationException("Unsupported message with code " + code);
			return deserializer.deserialize(in);
		}
	}

	public static ProtocolModifier deserializeModifier(int typeId, byte[] id, byte[] data) throws IOException {
		ModifierDeserializer<?> deserializer = modifierDeserializers.get(typeId);
		if (deserializer == null) throw new UnsupportedOperationException("Unsupported modifier with type ID " + typeId);
		return deserializer.deserialize(id, data);
	}
}
