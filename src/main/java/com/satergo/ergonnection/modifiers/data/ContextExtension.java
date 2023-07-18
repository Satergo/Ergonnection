package com.satergo.ergonnection.modifiers.data;

import sigmastate.SType;
import sigmastate.Values;
import sigmastate.serialization.SigmaSerializer;
import sigmastate.utils.SigmaByteReader;
import sigmastate.utils.SigmaByteWriter;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public record ContextExtension(HashMap<Byte, Values.Value<SType>> map) {

	public static ContextExtension deserialize(SigmaByteReader sbr) {
		byte size = sbr.getByte();
		if (size < 0) throw new IllegalArgumentException();
		HashMap<Byte, Values.Value<SType>> map = new HashMap<>();
		for (int i = 0; i < size; i++) {
			byte b = sbr.getByte();
			map.put(b, sbr.getValue());
		}
		return new ContextExtension(map);
	}

	public void serialize(DataOutputStream out) throws IOException {
		out.write(map.size());
		SigmaByteWriter sbw = SigmaSerializer.startWriter();
		for (Map.Entry<Byte, Values.Value<SType>> entry : map.entrySet()) {
			sbw.put(entry.getKey());
			sbw.putValue(entry.getValue());
		}
		out.write(sbw.toBytes());
	}
}
