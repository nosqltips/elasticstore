package com.nosqlrevolution.apps;

import com.nosqlrevolution.ElasticStore;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

/**
 * Used to download data from an ElasticSearch cluster
 * 
 * @author cbrown
 */
public class Import {
    public static void run(String[] args) throws IOException, Exception {        
        ImportOptions options = new ImportOptions();
        CmdLineParser parser = new CmdLineParser(options);
        try {
            parser.parseArgument(args);
        } catch( CmdLineException e ) {
            System.err.println(e.getMessage());
            System.err.println("java -jar elasticstore.jar import [options...]");
            parser.printUsage(System.err);
            return;
        }        

        System.out.println("hostname=" + options.getHostname() + " inFilename=" + options.getInfilename() + " clustername=" + options.getClustername() + 
                " index=" + options.getIndex() + " type=" + options.getType() + " threads=" + options.getThreads()+ " blockSize=" + options.getBlockSize());
        File inFile = new File(options.getInfilename());
        if (! inFile.exists()) {
            System.out.println("File does not exist, exiting.");
            return;
        }
        
        // Connect to ElasticSearch
        ElasticStore store = new ElasticStore().asTransport().withClusterName(options.getClustername()).withUniCast(options.getHostname()).execute();

        ExecutorService single = Executors.newSingleThreadExecutor();
        ExecutorService pool = Executors.newFixedThreadPool(options.getThreads());
        
        FileBlocker blocker = new FileBlocker(options.getInfilename(), options.getBlockSize());
        single.submit(blocker);
        
        Stack<Future<Integer>> futureStack = new Stack<>();
        
        // Initial creation of 
        for (int i = 0; i < options.getThreads(); i++) {
            List<String> nextBlock = blocker.getNext();
            // Break 
            if (nextBlock == null) {
                break;
            }
            futureStack.push(pool.submit(
                    new JsonImporter(store.getIndex(String.class, options.getIndex(), options.getType()), nextBlock)
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
                    futureStack.push(pool.submit(new JsonImporter(store.getIndex(String.class, options.getIndex(), options.getType()), nextBlock)));
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