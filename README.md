# Ergonnection

Ergonnection is a Java library to communicate with nodes of the [Ergo](https://ergoplatform.com/) network.

## Roadmap

- [x] Handshake
- [x] Implement all messages (both serialization and deserialization)
- [x] Implement all records (Peer, Feature, etc.)
- [x] Encoding utilities
- [x] Handling modifiers, to listen for things like transactions
- [ ] Publish on Maven central
- [ ] Tests

## Example

It can be used both in blocking mode which does not start a new thread and requires you to accept messages manually and in non-blocking mode which takes listeners and calls them from another thread.

```java
ErgoSocket ergoSocket = new ErgoSocket(new InetSocketAddress("127.0.0.1", 9030), new Peer("mysocket", "mysocket-1.0.0", Version.parse("5.0.12"), ErgoSocket.BASIC_FEATURE_SET));
ergoSocket.sendHandshake();
ergoSocket.acceptHandshake();
```

Non-blocking mode

```java
ergoSocket.listen().specific(Peers.class, message -> {
	// This code is executed in another thread
	for (Peer peer : message.peers()) {
		System.out.println(peer);
	}
});
ergoSocket.send(new GetPeers());
```

Blocking mode

```java
ergoSocket.send(new GetPeers());
while (true) {
	ProtocolMessage message = ergoSocket.acceptMessage();
	if (message instanceof Peers message) {
		for (Peer peer : message.peers()) {
			System.out.println(peer);
		}
	}
}
```

### Example: Transactions
Listen for transactions sent to the network: [TransactionLoggerExample.java](/examples/TransactionLoggerExample.java).

## Note

Please understand that ergonnection is only a networking library.
This means that the transactions you receive using this tool could very well be faked.
The blockchain is not saved and nothing is checked.
For this reason, unless you do this verification yourself,
only connect to nodes that you own if you want to guarantee correct data.