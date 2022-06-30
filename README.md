# Ergonnection

Ergonnection is a Java library to communicate with nodes of the [Ergo](https://ergoplatform.com/) network.

## Roadmap

- [x] Handshake
- [x] Implement all messages (both serialization and deserialization)
- [x] Implement all records (Peer, Feature, etc.)
- [x] Encoding utilities
- [ ] (In-progress) Built-in handling of modifiers which would make listening for things like transactions very easy and straightforward
- [ ] Publish on Maven central

## Example

It can be used both in blocking mode which does not start a new thread and requires you to accept messages manually and in non-blocking mode which takes listeners and calls them from another thread.

```java
ErgoSocket ergoSocket = new ErgoSocket(new InetSocketAddress("127.0.0.1", 9030), Version.parse("4.0.32"), "mysocket", "mysocket-1.0.0");
ergoSocket.sendHandshake();
ergoSocket.acceptHandshake();
```

Non-blocking mode

```java
ergoSocket.listen().specific(Peers.class, message -> {
	// This code is executed in another thread
	System.out.println("Peers:");
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