package com.satergo.ergonnection;

import com.satergo.ergonnection.protocol.Protocol;
import com.satergo.ergonnection.protocol.ProtocolMessage;
import com.satergo.ergonnection.records.Feature;
import com.satergo.ergonnection.records.Peer;
import org.bouncycastle.jcajce.provider.digest.Blake2b;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ErgoSocket extends Socket {

	public static final byte[] MAINNET_MAGIC = { 1, 0, 2, 4 }, TESTNET_MAGIC = { 2, 0, 0, 1 };

	/**
	 * A basic feature set specifying that the state is "utxo", the client is verifying transactions, has no PoPoW suffix, and 1 block is stored.
	 */
	public static final List<Feature> BASIC_FEATURE_SET = Collections.singletonList(new Feature(16, new byte[] { 0, 1, 0, 1 }));

	private final byte[] networkMagic;

	private final Peer self;

	private Peer peer;
	private final DataInputStream in;

	/**
	 * Creates a new socket for the mainnet
	 *
	 * @param address      Address to connect to
	 * @param self         A Peer object that represent this client itself (this is the data that will be sent to the target peer)
	 * @param networkMagic The magic bytes of this network, see {@link #MAINNET_MAGIC} and {@link #TESTNET_MAGIC}
	 * @throws IOException socket exception
	 */
	public ErgoSocket(InetSocketAddress address, Peer self, byte[] networkMagic) throws IOException {
		super(address.getAddress(), address.getPort());
		this.self = self;
		this.networkMagic = networkMagic;
		this.in = new DataInputStream(getInputStream());
	}

	/**
	 * Creates a new socket for the mainnet
	 *
	 * @param address   Address to connect to
	 * @param self      A Peer object that represent this client itself (this is the data that will be sent to the target peer)
	 * @throws IOException socket exception
	 */
	public ErgoSocket(InetSocketAddress address, Peer self) throws IOException {
		this(address, self, MAINNET_MAGIC);
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
		return Protocol.deserializeMessage(code, bytes);
	}

	public void sendHandshake() throws IOException {
		try (ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			 DataOutputStream out = new DataOutputStream(bytes)) {
			VLQWriter.writeULong(out, System.currentTimeMillis());
			self.serialize(out);
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
