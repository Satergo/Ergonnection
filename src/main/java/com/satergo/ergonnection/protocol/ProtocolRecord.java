package com.satergo.ergonnection.protocol;

import com.satergo.ergonnection.VLQOutputStream;

import java.io.IOException;

public interface ProtocolRecord {

	void serialize(VLQOutputStream out) throws IOException;
}
