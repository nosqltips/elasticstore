package com.nosqlrevolution.apps;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Used to download data from an ElasticSearch cluster
 * 
 * @author cbrown
 */
public abstract class AbstractPoolRunner {
    public abstract void run(String[] args) throws IOException, Exception;
    protected abstract Callable<CountsAndBytes> getNextCallable(List<String> nextBlock) throws Exception;
    
    protected void run(AbstractBlocker blocker, int threads) throws IOException, Exception {
        try (BufferedBlocker<String> source = blocker) {
            if (threads <= 0) throw new IllegalArgumentException("Threads must be positive");
            blocker.setPrefetch(threads);
            try (ThreadPools executors = new ThreadPools(source, threads)) {
                java.util.concurrent.CompletionService<CountsAndBytes> completed =
                        threads == 1 ? null : new java.util.concurrent.ExecutorCompletionService<>(executors.workers);
                Future<Integer> reader = executors.startReader();
                long totalDocs = blocker.getTotalDocs();
                int active = 0;
                boolean last = false;
                int totalCount = 0;
                long totalBytes = 0;
                long startTime = System.currentTimeMillis();
                long elapsedTimeStart = startTime;
                while (!last || active > 0) {
                    CountsAndBytes counts;
                    if (threads == 1) {
                        List<String> block = blocker.getNext();
                        if (block == null) break;
                        counts = getNextCallable(block).call();
                    } else {
                        while (!last && active < threads) {
                            List<String> block = blocker.getNext();
                            if (block == null) {
                                last = true;
                            } else {
                                Callable<CountsAndBytes> worker = getNextCallable(block);
                                completed.submit(() -> {
                                    try { return worker.call(); }
                                    catch (Exception | Error ex) { blocker.fail(ex); throw ex; }
                                });
                                active++;
                            }
                        }
                        if (active == 0) break;
                        counts = completed.take().get();
                        active--;
                    }
                    totalCount += counts.getCounts();
                    totalBytes += counts.getBytes();
                    elapsedTimeStart = printStatus(totalCount, counts.getCounts(),
                            elapsedTimeStart, startTime, counts.getBytes(), totalDocs);
                }
                reader.get();
                printTotals(totalCount, startTime, totalBytes);
            }
        }
        System.out.println("Done");
    }

    /** Try-with-resources keeps cleanup failures from hiding the pipeline failure. */
    private static final class ThreadPools implements AutoCloseable {
        private final BufferedBlocker<String> source;
        private final ExecutorService readerExecutor = Executors.newSingleThreadExecutor();
        private final ExecutorService workers;
        private Future<Integer> reader;

        ThreadPools(BufferedBlocker<String> source, int threads) {
            this.source = source;
            workers = threads == 1 ? null : Executors.newFixedThreadPool(threads);
        }

        Future<Integer> startReader() {
            reader = readerExecutor.submit(() -> {
                try { return source.call(); }
                catch (Exception | Error ex) { source.fail(ex); throw ex; }
            });
            return reader;
        }

        @Override
        public void close() throws Exception {
            source.cancel();
            if (reader != null) reader.cancel(true);
            readerExecutor.shutdownNow();
            if (workers != null) workers.shutdownNow();
            boolean interrupted = Thread.interrupted();
            try {
                boolean readerStopped = readerExecutor.awaitTermination(10, java.util.concurrent.TimeUnit.SECONDS);
                boolean workersStopped = workers == null
                        || workers.awaitTermination(10, java.util.concurrent.TimeUnit.SECONDS);
                if (!readerStopped || !workersStopped) {
                    throw new IOException("Pipeline threads did not terminate within shutdown timeout");
                }
            } catch (InterruptedException ex) {
                interrupted = true;
                throw ex;
            } finally {
                if (interrupted) Thread.currentThread().interrupt();
            }
        }
    }

    private static long printStatus(int totalCount, int elapsedCount, long elapsedTimeStart, long startTime, long bytes, long totalDocs) {
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - elapsedTimeStart;
        long totalElapsedTime = currentTime - startTime;
        long allSeconds = totalElapsedTime / 1000;
        long minutes = allSeconds / 60;
        long seconds = allSeconds - (minutes * 60);
        float elapsedRate = (float)elapsedCount / Math.max(0.001F, elapsedTime / 1000.0F);
        float totalRate = (float)totalCount / Math.max(0.001F, totalElapsedTime / 1000.0F);

        System.out.println("Pushed " + elapsedCount + " docs in " + elapsedTime/1000.0F + " secs at " + elapsedRate + " docs/sec " + (getMegabytes(bytes) / Math.max(0.001F, elapsedTime / 1000.0F)) + " MB/sec, " + totalCount + " total docs in " + minutes + " minutes " + seconds + " seconds at " + totalRate + " docs/second " + getPercentComplete(totalCount, totalDocs) + "% complete");
        return currentTime;
    }
    
    private static void printTotals(int totalCount, long startTime, long totalBytes) {
        long currentTime = System.currentTimeMillis();
        long totalElapsedTime = currentTime - startTime;
        long allSeconds = totalElapsedTime / 1000;
        long minutes = allSeconds / 60;
        long seconds = allSeconds - (minutes * 60);
        float rate = (float)totalCount / Math.max(0.001F, totalElapsedTime / 1000.0F);

        System.out.println("Pushed " + totalCount + " total docs and " + getMegabytes(totalBytes) + "MB in " + minutes + " minutes " + seconds + " seconds at " + rate + " per second, ");
    }
    
    private static float getMegabytes(long bytes) {
        return (bytes / 1048576.0F);
    }
    
    private static float getPercentComplete(int elapsedDocs, long totalDocs) {
        return ((float)elapsedDocs / totalDocs) * 100;
    }
}