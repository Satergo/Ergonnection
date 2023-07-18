package com.satergo.ergonnection.modifiers.data;

import com.satergo.ergonnection.VLQWriter;
import org.ergoplatform.ErgoBox;
import sigmastate.SType;
import sigmastate.Values;
import sigmastate.serialization.ErgoTreeSerializer;
import sigmastate.serialization.SigmaSerializer;
import sigmastate.utils.SigmaByteReader;
import sigmastate.utils.SigmaByteWriter;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record ErgoBoxCandidate(long value,
							   Values.ErgoTree ergoTree,
							   int creationHeight,
							   Map<TokenId, Long> additionalTokens,
							   LinkedHashMap<ErgoBox.NonMandatoryRegisterId, Values.EvaluatedValue<SType>> additionalRegisters) implements ErgoBoxAssets {

	public static ErgoBoxCandidate parseBodyWithIndexedDigests(List<TokenId> digestsInTx, SigmaByteReader sbr) {
		long value = sbr.getULong();

		Values.ErgoTree tree = ErgoTreeSerializer.DefaultSerializer().deserializeErgoTree(
				sbr, SigmaSerializer.MaxPropositionSize(), false);

		int creationHeight = sbr.getUIntExact();

		int tokenCount = sbr.getUByte();
		LinkedHashMap<TokenId, Long> tokens = new LinkedHashMap<>();
		if (digestsInTx != null) {
			for (int i = 0; i < tokenCount; i++) {
				int digestIndex = sbr.getUIntExact();
				if (digestIndex < 0 || digestIndex >= digestsInTx.size()) {
					throw new RuntimeException("failed to find token id with index " + digestIndex);
				}
				long amount = sbr.getULong();
				tokens.put(digestsInTx.get(digestIndex), amount);
			}
		} else {
			for (int i = 0; i < tokenCount; i++) {
				tokens.put(new TokenId(sbr.getBytes(32)), sbr.getULong());
			}
		}
		int registerCount = sbr.getUByte();
		LinkedHashMap<ErgoBox.NonMandatoryRegisterId, Values.EvaluatedValue<SType>> registers = new LinkedHashMap<>();
		for (int i = 0; i < registerCount; i++) {
			Values.EvaluatedValue<SType> constant = (Values.EvaluatedValue<SType>) sbr.getValue();
			registers.put(ErgoBox.nonMandatoryRegisters().apply(i), constant);
		}
		return new ErgoBoxCandidate(value, tree, creationHeight, tokens, registers);
	}

	public void serializeWithIndexedDigests(DataOutputStream out, List<TokenId> tokensInTx) throws IOException {
		VLQWriter.writeULong(out, value);

		out.write(ErgoTreeSerializer.DefaultSerializer().serializeErgoTree(ergoTree));

		VLQWriter.writeUInt(out, creationHeight);

		out.write(additionalTokens.size());
		if (tokensInTx != null) {
			for (Map.Entry<TokenId, Long> entry : additionalTokens.entrySet()) {
				VLQWriter.writeUInt(out, tokensInTx.indexOf(entry.getKey()));
				VLQWriter.writeULong(out, entry.getValue());
			}
		} else {
			for (Map.Entry<TokenId, Long> entry : additionalTokens.entrySet()) {
				out.write(entry.getKey().id());
				VLQWriter.writeULong(out, entry.getValue());
			}
		}

		out.write(additionalRegisters.size());
		SigmaByteWriter sbw = SigmaSerializer.startWriter();
		for (var entry : additionalRegisters.entrySet()) {
			sbw.putUByte(entry.getKey().asIndex());
			sbw.putValue(entry.getValue());
		}
		out.write(sbw.toBytes());
	}

	public void serializeWithoutIndexedDigests(DataOutputStream out) throws IOException {
		serializeWithIndexedDigests(out, null);
	}

	@Override
	public Map<TokenId, Long> tokens() {
		return additionalTokens;
	}
}
