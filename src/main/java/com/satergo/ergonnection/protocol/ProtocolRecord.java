package com.satergo.ergonnection.protocol;

import java.io.DataOutputStream;
import java.io.IOException;

public interface ProtocolRecord {

	void serialize(DataOutputStream out) throws IOException;
}
