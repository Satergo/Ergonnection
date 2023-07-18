package com.satergo.ergonnection.protocol;

import com.satergo.ergonnection.VLQOutputStream;

import java.io.IOException;

public interface ProtocolModifier {

	void serialize(VLQOutputStream out) throws IOException;

	int typeId();
}
