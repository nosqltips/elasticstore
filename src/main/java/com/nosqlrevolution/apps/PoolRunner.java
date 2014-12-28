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
public abstract class PoolRunner {
    public abstract void run(String[] args) throws IOException, Exception;
    protected abstract Callable getNextCallable(List<String> nextBlock) throws Exception;
    
    protected void run(AbstractBlocker blocker, int threads) throws IOException, Exception {
        ExecutorService single = Executors.newSingleThreadExecutor();
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        
        single.submit(blocker);
        Stack<Future<Integer>> futureStack = new Stack<>();
        
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
        int elapsedCount = 0;
        long startTime = System.currentTimeMillis();
        long elapsedTimeStart = startTime;
        while (! futureStack.empty()) {
            Future<Integer> future = futureStack.pop();
            int count = future.get();
            elapsedCount += count;
            totalCount += count;
            
            long newElapsedTime = printStatus(totalCount, elapsedCount, elapsedTimeStart, startTime);
            if (newElapsedTime != elapsedTimeStart) {
                elapsedCount = 0;
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
        
        printTotals(totalCount, startTime);
        
        blocker.shudown();
        single.shutdown();
        pool.shutdown();
        System.out.println("Done");
    }
    
    private static long printStatus(int totalCount, int elapsedCount, long elapsedTimeStart, long startTime) {
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - elapsedTimeStart;
        long totalElapsedTime = currentTime - startTime;
        long allSeconds = totalElapsedTime / 1000;
        long minutes = allSeconds / 60;
        long seconds = allSeconds - (minutes * 60);
        float elapsedRate = (float)elapsedCount / (elapsedTime / 1000.0F);
        float totalRate = (float)totalCount / (totalElapsedTime / 1000.0F);

        System.out.println("Pushed " + elapsedCount + " docs in " + elapsedTime /1000.0F + " seconds at " + elapsedRate + " docs/second, " + totalCount + " total docs in " + minutes + " minutes " + seconds + " seconds at " + totalRate + " docs/second");
        return currentTime;
    }
    
    private static void printTotals(int totalCount, long startTime) {
        long currentTime = System.currentTimeMillis();
        long totalElapsedTime = currentTime - startTime;
        long allSeconds = totalElapsedTime / 1000;
        long minutes = allSeconds / 60;
        long seconds = allSeconds - (minutes * 60);
        float rate = (float)totalCount / (totalElapsedTime / 1000.0F);

        System.out.println("Pushed " + totalCount + " total docs in " + minutes + " minutes " + seconds + " seconds at " + rate + " per second, ");
    }
}