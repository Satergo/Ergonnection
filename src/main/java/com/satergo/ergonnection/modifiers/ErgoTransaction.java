package com.satergo.ergonnection.modifiers;

import com.satergo.ergonnection.VLQReader;
import com.satergo.ergonnection.VLQWriter;
import com.satergo.ergonnection.modifiers.data.DataInput;
import com.satergo.ergonnection.modifiers.data.ErgoBoxCandidate;
import com.satergo.ergonnection.modifiers.data.Input;
import com.satergo.ergonnection.modifiers.data.TokenId;
import com.satergo.ergonnection.protocol.ProtocolModifier;
import sigmastate.serialization.SigmaSerializer;
import sigmastate.utils.SigmaByteReader;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
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
		DataInputStream in = new DataInputStream(new ByteArrayInputStream(data));
		int inputCount = VLQReader.readUShort(in);
		ArrayList<Input> inputs = new ArrayList<>();
		int available = in.available();
		SigmaByteReader sbr = SigmaSerializer.startReader(data, data.length - available);
		for (int i = 0; i < inputCount; i++) {
			inputs.add(Input.deserialize(sbr));
		}
		in.skipBytes(available - sbr.remaining());

		int dataInputCount = VLQReader.readUShort(in);
		ArrayList<DataInput> dataInputs = new ArrayList<>();
		for (int i = 0; i < dataInputCount; i++) {
			dataInputs.add(new DataInput(in.readNBytes(32)));
		}

		// parse distinct ids of tokens in transaction outputs
		int tokensCount = (int) VLQReader.readUInt(in);
		ArrayList<TokenId> tokens = new ArrayList<>();
		for (int i = 0; i < tokensCount; i++) {
			tokens.add(new TokenId(in.readNBytes(32)));
		}

		int outputCandidatesCount = VLQReader.readUShort(in);
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
	public void serialize(DataOutputStream out) throws IOException {
		VLQWriter.writeUShort(out, inputs.size());
		for (Input input : inputs) {
			input.serialize(out);
		}
		VLQWriter.writeUShort(out, dataInputs.size());
		for (DataInput dataInput : dataInputs) {
			out.write(dataInput.boxId());
		}
		List<TokenId> distinctTokenIds = outputCandidates.stream().flatMap(oc -> oc.additionalTokens().keySet().stream()).distinct().toList();
		VLQWriter.writeUInt(out, distinctTokenIds.size());
		for (TokenId distinctTokenId : distinctTokenIds) {
			out.write(distinctTokenId.id());
		}
		VLQWriter.writeUShort(out, outputCandidates.size());
		for (ErgoBoxCandidate outputCandidate : outputCandidates) {
			outputCandidate.serializeWithIndexedDigests(out, distinctTokenIds);
		}
	}

	@Override public int typeId() { return TYPE_ID; }
}
