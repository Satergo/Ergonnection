package com.satergo.ergonnection;

import com.satergo.ergonnection.protocol.Protocol;
import com.satergo.ergonnection.protocol.ProtocolMessage;
import com.satergo.ergonnection.records.Peer;
import org.bouncycastle.jcajce.provider.digest.Blake2b;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;

public class ErgoSocket extends Socket {

	public static final byte[] MAINNET_MAGIC = { 1, 0, 2, 4 }, TESTNET_MAGIC = { 2, 0, 0, 1 };

	private final byte[] networkMagic;

	private final Version version;
	private final String agentName;
	private final String peerName;

	private Peer peer;
	private final DataInputStream in;

	public ErgoSocket(InetSocketAddress address, Version version, String agentName, String peerName, byte[] networkMagic) throws IOException {
		super(address.getAddress(), address.getPort());
		this.version = version;
		this.agentName = agentName;
		this.peerName = peerName;
		this.networkMagic = networkMagic;
		this.in = new DataInputStream(getInputStream());
	}

	/**
	 * Creates a new socket for the mainnet
	 * @param address Address to connect to
	 * @param version The version of this client
	 * @param agentName Agent name
	 * @param peerName Peer name
	 * @throws IOException socket exception
	 */
	public ErgoSocket(InetSocketAddress address, Version version, String agentName, String peerName) throws IOException {
		this(address, version, agentName, peerName, MAINNET_MAGIC);
	}

	private MessageListener messageListener;

	/**
	 * Starts a listener thread unless the socket already had one in which case it is reused
	 * @return The listener object, for adding consumers
	 */
	public MessageListener listen() {
		if (messageListener == null) messageListener = new MessageListener(this);
		return messageListener;
	}

	public boolean isBlockingMode() {
		return messageListener == null;
	}

	public void send(ProtocolMessage message) throws IOException {
		try (ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			 DataOutputStream out = new DataOutputStream(bytes)) {
			out.write(networkMagic);
			out.write(message.code());
			byte[] data = message.toByteArray();
			out.writeInt(data.length);
			byte[] checksum = new Blake2b.Blake2b256().digest(data);
			out.write(checksum, 0, 4);
			out.write(data);
			out.flush();
			getOutputStream().write(bytes.toByteArray());
		}
	}

	public ProtocolMessage acceptMessage() throws IOException {
		byte[] magic = in.readNBytes(4);
		if (!Arrays.equals(networkMagic, magic))
			throw new IllegalArgumentException("incorrect magic " + Arrays.toString(magic) + " (must be " + Arrays.toString(networkMagic) + ")");
		int code = in.readUnsignedByte();
		int length = in.readInt();
		in.skipNBytes(4); // checksum
		byte[] bytes = in.readNBytes(length);
		return Protocol.deserialize(code, bytes);
	}

	public void sendHandshake() throws IOException {
		try (ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			 DataOutputStream out = new DataOutputStream(bytes)) {
			VLQWriter.writeULong(out, System.currentTimeMillis());
			StreamUTF8.writeByteLen(out, agentName);
			out.writeByte(version.major());
			out.writeByte(version.minor());
			out.writeByte(version.patch());
			StreamUTF8.writeByteLen(out, peerName);
			out.writeByte(0); // has no public address
			out.writeByte(1); // has 1 feature
			out.write(16);
			VLQWriter.writeUShort(out, 4);
			out.write(new byte[]{0, 1, 0, 1});
			out.flush();
			getOutputStream().write(bytes.toByteArray());
		}
	}

	public void acceptHandshake() throws IOException {
		long time = VLQReader.readULong(in);
		peer = Peer.deserialize(in);
	}

	public boolean isHandshakeReceived() { return peer != null; }

	/**
	 * @return Information about the node that has been connected to.
	 */
	public Peer getPeerInfo() { return peer; }
}
