package com.satergo.ergonnection.modifiers.data;

import java.util.Map;

public interface ErgoBoxAssets {

	long value();
	Map<TokenId, Long> tokens();

}
