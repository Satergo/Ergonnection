package com.satergo.ergonnection;

import com.satergo.ergonnection.protocol.ProtocolMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class MessageListener {

	private record FilteredConsumer<T>(Predicate<T> filter, Consumer<T> consumer) implements Consumer<T> {
		@Override
		public void accept(T t) {
			if (filter.test(t)) consumer.accept(t);
		}
	}

	private final ErgoSocket ergoSocket;

	private final ArrayList<Consumer<ProtocolMessage>> consumers = new ArrayList<>();

	MessageListener(ErgoSocket ergoSocket) {
		this.ergoSocket = ergoSocket;
		start();
	}

	public void all(Consumer<ProtocolMessage> consumer) {
		consumers.add(consumer);
	}

	@SuppressWarnings("unchecked")
	public <T extends ProtocolMessage>void specific(Class<T> clazz, Consumer<T> consumer) {
		all((Consumer<ProtocolMessage>) new FilteredConsumer<>(clazz::isInstance, consumer));
	}

	public <T extends ProtocolMessage>void cancel(Consumer<T> consumer) {
		for (Iterator<Consumer<ProtocolMessage>> iterator = consumers.iterator(); iterator.hasNext();) {
			Consumer<ProtocolMessage> entry = iterator.next();
			if (entry instanceof FilteredConsumer<ProtocolMessage> f) {
				if (f.consumer == consumer) iterator.remove();
			} else if (entry == consumer) iterator.remove();
		}
	}

	private void start() {
		new Thread(() -> {
			while (true) {
				for (Consumer<ProtocolMessage> consumer : consumers) {
					try {
						consumer.accept(ergoSocket.acceptMessage());
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}
			}
		}).start();
	}
}
