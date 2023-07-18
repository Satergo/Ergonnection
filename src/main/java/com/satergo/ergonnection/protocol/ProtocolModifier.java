package com.satergo.ergonnection.protocol;

import java.io.DataOutputStream;
import java.io.IOException;

public interface ProtocolModifier {

	void serialize(DataOutputStream out) throws IOException;

	int typeId();
}
