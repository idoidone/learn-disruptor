package com.iamvtn.disruptor;

import java.nio.ByteBuffer;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;

public class HelloDisruptor {
	public static void main(String[] arg) throws InterruptedException {
//		version3Translators();
//		lamdaJava8();
		fashion();
	}

	public static void handleEvent(LongEvent event, long sequence, boolean endOfBatch) {
		System.out.println(event);
	}

	public static void translate(LongEvent event, long sequence, ByteBuffer buffer) {
		event.set(buffer.getLong(0));
	}

	public static void fashion() throws InterruptedException {
		// Executor that will be used to construct new threads for consumers
		Executor executor = Executors.newCachedThreadPool();

		// Specify the size of the ring buffer, must be power of 2.
		int bufferSize = 1024;

		// Construct the Disruptor
		Disruptor<LongEvent> disruptor = new Disruptor<>(LongEvent::new, bufferSize, executor);

		// Connect the handler
		disruptor.handleEventsWith(HelloDisruptor::handleEvent);

		// Start the Disruptor, starts all threads running
		disruptor.start();

		// Get the ring buffer from the Disruptor to be used for publishing.
		RingBuffer<LongEvent> ringBuffer = disruptor.getRingBuffer();

		ByteBuffer bb = ByteBuffer.allocate(8);
		for (long l = 0; true; l++) {
			bb.putLong(0, l);
			ringBuffer.publishEvent(HelloDisruptor::translate, bb);
			Thread.sleep(1000);
		}
	}

	public static void lamdaJava8() throws InterruptedException {
		// Executor that will be used to construct new threads for consumers
		Executor executor = Executors.newCachedThreadPool();

		// Specify the size of the ring buffer, must be power of 2.
		int bufferSize = 1024;

		// Construct the Disruptor
		Disruptor<LongEvent> disruptor = new Disruptor<>(LongEvent::new, bufferSize, executor);

		// Connect the handler
		disruptor.handleEventsWith((event, sequence, endOfBatch) -> System.out.println("Event: " + event));

		// Start the Disruptor, starts all threads running
		disruptor.start();

		// Get the ring buffer from the Disruptor to be used for publishing.
		RingBuffer<LongEvent> ringBuffer = disruptor.getRingBuffer();

		ByteBuffer bb = ByteBuffer.allocate(8);
		for (long l = 0; true; l++) {
			bb.putLong(0, l);
			ringBuffer.publishEvent((event, sequence, buffer) -> event.set(buffer.getLong(0)), bb);
			Thread.sleep(1000);
		}
	}

	public static void version3Translators() throws InterruptedException {
		// Executor that will be used to construct new threads for consumers
		Executor executor = Executors.newCachedThreadPool();

		// The factory for the event
		LongEventFactory factory = new LongEventFactory();

		// Specify the size of the ring buffer, must be power of 2.
		int bufferSize = 1024;

		// Construct the Disruptor
		Disruptor<LongEvent> disruptor = new Disruptor<>(factory, bufferSize, executor);

		// Connect the handler
		disruptor.handleEventsWith(new LongEventHandler());

		// Start the Disruptor, starts all threads running
		disruptor.start();

		// Get the ring buffer from the Disruptor to be used for publishing.
		RingBuffer<LongEvent> ringBuffer = disruptor.getRingBuffer();

		LongEventProducer producer = new LongEventProducer(ringBuffer);

		ByteBuffer bb = ByteBuffer.allocate(8);
		for (long l = 0; true; l++) {
			bb.putLong(0, l);
			producer.onData(bb);
			Thread.sleep(1000);
		}
	}
}
