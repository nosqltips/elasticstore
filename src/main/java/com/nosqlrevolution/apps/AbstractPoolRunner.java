package com.nosqlrevolution.apps;

import java.io.IOException;
import java.util.List;
import java.util.Stack;
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
        ExecutorService master = Executors.newSingleThreadExecutor();
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        
        master.submit(blocker);
        long totalDocs = blocker.getTotalDocs();
        
        Stack<Future<CountsAndBytes>> futureStack = new Stack<>();
        
        // Initial creation of 
        for (int i = 0; i < threads; i++) {
            List<String> nextBlock = blocker.getNext();
            // Break 
            if (nextBlock == null) {
                break;
            }
            futureStack.push(pool.submit(getNextCallable(nextBlock)));
        }

        boolean last = false;
        int totalCount = 0;
        long totalBytes = 0;
        long startTime = System.currentTimeMillis();
        long elapsedTimeStart = startTime;
        while (! futureStack.empty()) {
            Future<CountsAndBytes> future = futureStack.pop();
            CountsAndBytes cbs = future.get();
            int count = cbs.getCounts();
            totalCount += count;
            long bytes = cbs.getBytes();
            totalBytes += bytes;
            
            long newElapsedTime = printStatus(totalCount, count, elapsedTimeStart, startTime, bytes, totalDocs);
            if (newElapsedTime != elapsedTimeStart) {
                elapsedTimeStart = newElapsedTime;
            }
            
            if (! last) {
                // Create new pool entry
                List<String> nextBlock = blocker.getNext();
                // Break 
                if (nextBlock == null) {
                    last = true;
                } else {
                    futureStack.push(pool.submit(getNextCallable(nextBlock)));
                }
            }
        }
        
        printTotals(totalCount, startTime, totalBytes);
        
        blocker.shudown();
        master.shutdown();
        pool.shutdown();
        System.out.println("Done");
    }
    
    private static long printStatus(int totalCount, int elapsedCount, long elapsedTimeStart, long startTime, long bytes, long totalDocs) {
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - elapsedTimeStart;
        long totalElapsedTime = currentTime - startTime;
        long allSeconds = totalElapsedTime / 1000;
        long minutes = allSeconds / 60;
        long seconds = allSeconds - (minutes * 60);
        float elapsedRate = (float)elapsedCount / (elapsedTime / 1000.0F);
        float totalRate = (float)totalCount / (totalElapsedTime / 1000.0F);

        System.out.println("Pushed " + elapsedCount + " docs in " + elapsedTime/1000.0F + " secs at " + elapsedRate + " docs/sec " + getMegabytes(bytes) + " MB/sec, " + totalCount + " total docs in " + minutes + " minutes " + seconds + " seconds at " + totalRate + " docs/second " + getPercentComplete(totalCount, totalDocs) + "% complete");
        return currentTime;
    }
    
    private static void printTotals(int totalCount, long startTime, long totalBytes) {
        long currentTime = System.currentTimeMillis();
        long totalElapsedTime = currentTime - startTime;
        long allSeconds = totalElapsedTime / 1000;
        long minutes = allSeconds / 60;
        long seconds = allSeconds - (minutes * 60);
        float rate = (float)totalCount / (totalElapsedTime / 1000.0F);

        System.out.println("Pushed " + totalCount + " total docs and " + getMegabytes(totalBytes) + "MB in " + minutes + " minutes " + seconds + " seconds at " + rate + " per second, ");
    }
    
    private static float getMegabytes(long bytes) {
        return (bytes / 1048576.0F);
    }
    
    private static float getPercentComplete(int elapsedDocs, long totalDocs) {
        return ((float)elapsedDocs / totalDocs) * 100;
    }
}