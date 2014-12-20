package com.nosqlrevolution.apps;

import com.nosqlrevolution.ElasticStore;
import com.nosqlrevolution.Index;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Used to download data from an ElasticSearch cluster
 * 
 * @author cbrown
 */
public class Import {
    public static void run(String[] args) throws IOException, Exception {
        if (args == null || args.length == 0) {
            System.out.println("No parameters, exiting.");
            System.out.println("1) hostname");
            System.out.println("2) inFilename");
            System.out.println("3) clustername");
            System.out.println("4) index");
            System.out.println("5) type");
            return;
        }
        
        String hostname = args[1];
        String inFilename = args[2];
        String clustername = args[3];
        String index = args[4];
        String type = args[5];
        int THREADS = 2;
        
        System.out.println("hostname=" + hostname + " inFilename=" + inFilename + " clustername=" + clustername + " index=" + index + " type=" + type);
        File inFile = new File(inFilename);
        if (! inFile.exists()) {
            System.out.println("File does not exist, exiting.");
            return;
        }
        
        // Connect to ElasticSearch
        ElasticStore store = new ElasticStore().asTransport().withClusterName(clustername).withUniCast(hostname).execute();

        ExecutorService single = Executors.newSingleThreadExecutor();
        ExecutorService pool = Executors.newFixedThreadPool(THREADS);
        
        FileBlocker blocker = new FileBlocker(inFilename);
        single.submit(blocker);
        
        Stack<Future<Integer>> futureStack = new Stack<>();
        
        // Initial creation of 
        for (int i = 0; i < THREADS; i++) {
            List<String> nextBlock = blocker.getNext();
            // Break 
            if (nextBlock == null) {
                break;
            }
            futureStack.push(pool.submit(
                    new JsonImporter(store.getIndex(String.class, index, type), nextBlock)
            ));
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
                    futureStack.push(pool.submit(new JsonImporter(store.getIndex(String.class, index, type), nextBlock)));
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
//        if (elapsedTime > 1000) {
            long totalElapsedTime = currentTime - startTime;
            long allSeconds = totalElapsedTime / 1000;
            long minutes = allSeconds / 60;
            long seconds = allSeconds - (minutes * 60);
            float elapsedRate = (float)elapsedCount / (elapsedTime / 1000.0F);
            float totalRate = (float)totalCount / (totalElapsedTime / 1000.0F);
            
            System.out.println("Pushed " + elapsedCount + " docs in " + elapsedTime /1000.0F + " seconds at " + elapsedRate + " docs/second, " + totalCount + " total docs in " + minutes + " minutes " + seconds + " seconds at " + totalRate + " docs/second");
            return currentTime;
//        } else {
//            return elapsedTimeStart;
//        }
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