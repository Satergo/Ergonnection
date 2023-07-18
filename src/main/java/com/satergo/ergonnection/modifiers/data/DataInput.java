package com.satergo.ergonnection.modifiers.data;

/**
 * Inputs that are used to enrich script context, but won't be spent by the transaction
 */
public record DataInput(byte[] boxId) {

	public DataInput {
		if (boxId.length != 32) throw new IllegalArgumentException("must be length 32");
	}
}
