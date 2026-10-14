package com.nosqlrevolution.apps;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;

/** A bounded handoff with explicit completion, failure and cancellation. */
public abstract class BufferedBlocker<T> implements Callable<Integer> {
    protected int totalCount;
    private final int blockSize;
    private final int limit;
    private final int sample;
    private int sampleCount;
    private int capacity = 2;
    private final Queue<List<T>> ready = new ArrayDeque<>();
    private List<T> building = new ArrayList<>();
    private boolean done;
    private Throwable failure;

    protected BufferedBlocker(int blockSize, int limit, int sample) {
        if (blockSize <= 0) throw new IllegalArgumentException("Block size must be positive");
        this.blockSize = blockSize;
        this.limit = limit;
        this.sample = sample;
    }

    synchronized void setPrefetch(int capacity) {
        this.capacity = Math.max(2, capacity);
    }

    protected synchronized boolean bufferNext(T item) throws InterruptedException {
        checkFailure();
        if (item == null) {
            publish();
            done = true;
            notifyAll();
            return true;
        }
        if (done) throw new IllegalStateException("Reader already completed");
        if (sample <= 0 || sampleCount >= sample) {
            building.add(item);
            totalCount++;
            sampleCount++;
        }
        if (building.size() >= blockSize) publish();
        return limit <= 0 || totalCount < limit;
    }

    private void publish() throws InterruptedException {
        if (building.isEmpty()) return;
        while (ready.size() >= capacity && failure == null) wait();
        checkFailure();
        ready.add(building);
        building = new ArrayList<>();
        notifyAll();
    }

    public synchronized List<T> getNext() {
        while (ready.isEmpty() && !done && failure == null) {
            try {
                wait();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Interrupted waiting for reader", ex);
            }
        }
        checkFailure();
        List<T> block = ready.poll();
        notifyAll();
        return block;
    }

    public synchronized void fail(Throwable cause) {
        if (failure == null) failure = cause;
        done = true;
        notifyAll();
    }

    public void cancel() {
        fail(new java.util.concurrent.CancellationException("Pipeline stopped"));
    }

    private void checkFailure() {
        if (failure != null) throw new IllegalStateException("Pipeline failed", failure);
    }

    public abstract long getTotalDocs();
    public abstract void shudown() throws IOException;
}
