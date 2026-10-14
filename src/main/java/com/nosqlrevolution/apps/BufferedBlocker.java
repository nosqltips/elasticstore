package com.nosqlrevolution.apps;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/** Single-reader bounded handoff. Terminal state never competes with data for queue space. */
public abstract class BufferedBlocker<T> implements Callable<Integer>, AutoCloseable {
    private static final long CHECK_INTERVAL_MS = 100;
    protected int totalCount;
    private final int blockSize;
    private final int limit;
    private final int sample;
    private int sampleCount;
    private BlockingQueue<List<T>> ready = new ArrayBlockingQueue<>(2);
    private List<T> building = new ArrayList<>();
    private volatile boolean done;
    private final AtomicReference<Throwable> failure = new AtomicReference<>();

    protected BufferedBlocker(int blockSize, int limit, int sample) {
        if (blockSize <= 0) throw new IllegalArgumentException("Block size must be positive");
        this.blockSize = blockSize;
        this.limit = limit;
        this.sample = sample;
    }

    /** Set once by the runner, before starting the reader. */
    void setPrefetch(int capacity) {
        ready = new ArrayBlockingQueue<>(Math.max(2, capacity));
    }

    protected boolean bufferNext(T item) throws InterruptedException {
        checkFailure();
        if (done) throw new IllegalStateException("Reader already completed");
        if (item == null) {
            publish();
            done = true;
            ready.offer(java.util.Collections.emptyList());
            return true;
        }
        sampleCount++;
        if (sample <= 0 || sampleCount % sample == 0) {
            building.add(item);
            totalCount++;
        }
        if (building.size() >= blockSize) publish();
        return limit <= 0 || totalCount < limit;
    }

    private void publish() throws InterruptedException {
        if (building.isEmpty()) return;
        // Timed queue operations also observe cancellation when no consumer remains.
        while (!ready.offer(building, CHECK_INTERVAL_MS, TimeUnit.MILLISECONDS)) checkFailure();
        building = new ArrayList<>();
        checkFailure();
    }

    public List<T> getNext() {
        try {
            while (true) {
                checkFailure();
                // Read done before polling: the producer publishes its last block before done.
                boolean finished = done;
                List<T> block = finished ? ready.poll()
                        : ready.poll(CHECK_INTERVAL_MS, TimeUnit.MILLISECONDS);
                checkFailure();
                if (block != null) return block.isEmpty() ? null : block;
                if (finished) return null;
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted waiting for reader", ex);
        }
    }

    public void fail(Throwable cause) {
        if (failure.compareAndSet(null, java.util.Objects.requireNonNull(cause))) {
            ready.offer(java.util.Collections.emptyList());
        }
    }

    public void cancel() { fail(new CancellationException("Pipeline stopped")); }

    private void checkFailure() {
        Throwable cause = failure.get();
        if (cause != null) throw new IllegalStateException("Pipeline failed", cause);
    }

    public abstract long getTotalDocs();
    public abstract void shudown() throws IOException;

    @Override
    public void close() throws IOException { shudown(); }
}
