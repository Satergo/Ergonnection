package com.satergo.ergonnection.modifiers;

import com.satergo.ergonnection.VLQInputStream;
import com.satergo.ergonnection.VLQOutputStream;
import com.satergo.ergonnection.modifiers.data.DataInput;
import com.satergo.ergonnection.modifiers.data.ErgoBoxCandidate;
import com.satergo.ergonnection.modifiers.data.Input;
import com.satergo.ergonnection.modifiers.data.TokenId;
import com.satergo.ergonnection.protocol.ProtocolModifier;
import sigmastate.serialization.SigmaSerializer;
import sigmastate.utils.SigmaByteReader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @param id optional (nullable)
 * @param inputs
 * @param dataInputs
 * @param outputCandidates
 * @param size optional
 */
public record ErgoTransaction(byte[] id, List<Input> inputs, List<DataInput> dataInputs, List<ErgoBoxCandidate> outputCandidates, Integer size) implements ProtocolModifier {

	public ErgoTransaction {
		if (id != null && id.length != 32) throw new IllegalArgumentException("id must be 32 bytes");
	}

	public static final int TYPE_ID = 2;

	/**
	 * @param id optional (nullable)
	 */
	public static ErgoTransaction deserialize(byte[] id, byte[] data) throws IOException {
		VLQInputStream in = new VLQInputStream(new ByteArrayInputStream(data));
		int inputCount = in.readUnsignedShort();
		ArrayList<Input> inputs = new ArrayList<>();
		int available = in.available();
		SigmaByteReader sbr = SigmaSerializer.startReader(data, data.length - available);
		for (int i = 0; i < inputCount; i++) {
			inputs.add(Input.deserialize(sbr));
		}
		in.skipBytes(available - sbr.remaining());

		int dataInputCount = in.readUnsignedShort();
		ArrayList<DataInput> dataInputs = new ArrayList<>();
		for (int i = 0; i < dataInputCount; i++) {
			dataInputs.add(new DataInput(in.readNFully(32)));
		}

		// parse distinct ids of tokens in transaction outputs
		int tokensCount = (int) in.readUnsignedInt();
		ArrayList<TokenId> tokens = new ArrayList<>();
		for (int i = 0; i < tokensCount; i++) {
			tokens.add(new TokenId(in.readNFully(32)));
		}

		int outputCandidatesCount = in.readUnsignedShort();
		ArrayList<ErgoBoxCandidate> outputCandidates = new ArrayList<>();
		sbr.position_$eq(data.length - in.available());
		for (int i = 0; i < outputCandidatesCount; i++) {
			ErgoBoxCandidate ergoBoxCandidate = ErgoBoxCandidate.parseBodyWithIndexedDigests(tokens, sbr);
			outputCandidates.add(ergoBoxCandidate);
		}
		in.skipBytes(available - sbr.remaining());

		return new ErgoTransaction(
				id,
				Collections.unmodifiableList(inputs),
				Collections.unmodifiableList(dataInputs),
				Collections.unmodifiableList(outputCandidates),
				null);
	}

	@Override
	public void serialize(VLQOutputStream out) throws IOException {
		out.writeUnsignedShort(inputs.size());
		for (Input input : inputs) {
			input.serialize(out);
		}
		out.writeUnsignedShort(dataInputs.size());
		for (DataInput dataInput : dataInputs) {
			out.write(dataInput.boxId());
		}
		List<TokenId> distinctTokenIds = outputCandidates.stream().flatMap(oc -> oc.tokens().keySet().stream()).distinct().toList();
		out.writeUnsignedInt(distinctTokenIds.size());
		for (TokenId distinctTokenId : distinctTokenIds) {
			out.write(distinctTokenId.id());
		}
		out.writeUnsignedShort(outputCandidates.size());
		for (ErgoBoxCandidate outputCandidate : outputCandidates) {
			outputCandidate.serializeWithIndexedDigests(out, distinctTokenIds);
		}
	}

	@Override public int typeId() { return TYPE_ID; }
}
