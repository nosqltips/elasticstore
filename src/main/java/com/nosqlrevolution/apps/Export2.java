package com.nosqlrevolution.apps;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nosqlrevolution.ElasticStore;
import com.nosqlrevolution.Index;
import com.nosqlrevolution.cursor.Cursor;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

/**
 * Used to download data from an ElasticSearch cluster
 * 
 * @author cbrown
 */
public class Export2 {
    private final static String newLine = System.lineSeparator();
    private final static ObjectMapper mapper = new ObjectMapper();
    
    public static void run(String[] args) throws IOException, Exception {
        ExportOptions options = new ExportOptions();
        CmdLineParser parser = new CmdLineParser(options);
        try {
            parser.parseArgument(args);
            if (options.getIndex() == null) {
                throw new CmdLineException("Index must be specified.");
            }
        } catch( CmdLineException e ) {
            System.err.println(e.getMessage());
            System.err.println("java -jar elasticstore.jar export [options...]");
            parser.printUsage(System.err);
            return;
        }        
        
        System.out.println("hostname=" + options.getHostname() + " outfilename=" + options.getOutfilename() + " clustername=" + options.getClustername() + 
                " index=" + options.getIndex() + " type=" + options.getType());
        File outFile = new File(options.getOutfilename());
        if (outFile.exists()) {
            System.out.println("File exists, exiting.");
            return;
        }
        
        // Connect to ElasticSearch
        ElasticStore store = new ElasticStore().asTransport().withClusterName(options.getClustername()).withUniCast(options.getHostname()).execute();
        Index data = store.getIndex(ExportModel.class, options.getIndex(), options.getType());
        Cursor<ExportModel> cursor = data.findAll();

        outFile.createNewFile();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outFile))) {
            int totalCount = 0;
            int elapsedCount = 0;
            long startTime = System.currentTimeMillis();
            long elapsedTimeStart = startTime;
            for (ExportModel ex: cursor) {
                writer.append(mapper.writeValueAsString(ex))
                        .append(newLine);
                totalCount ++;
                elapsedCount ++;
                if (totalCount % 1000 == 0) {
                    long newElapsedTime = printStatus(totalCount, elapsedCount, elapsedTimeStart, startTime);
                    if (newElapsedTime != elapsedTimeStart) {
                        elapsedCount = 0;
                    }
                    elapsedTimeStart = newElapsedTime;
                }
            }
            
            printStatus(totalCount, elapsedCount, elapsedTimeStart, startTime);
            printTotals(totalCount, startTime);
            writer.flush();
        }
    }
    
    private static long printStatus(int totalCount, int elapsedCount, long elapsedTimeStart, long startTime) {
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - elapsedTimeStart;
        if (elapsedTime > 1000) {
            long totalElapsedTime = currentTime - startTime;
            long allSeconds = totalElapsedTime / 1000;
            long minutes = allSeconds / 60;
            long seconds = allSeconds - (minutes * 60);
            float rate = (float)elapsedCount / (elapsedTime / 1000.0F);
            
            System.out.println("Pulled " + elapsedCount + " docs in " + minutes + " minutes " + seconds + " seconds at " + rate + " per second, " + totalCount + " total docs.");
            return currentTime;
        } else {
            return elapsedTimeStart;
        }
    }
    
    private static void printTotals(int totalCount, long startTime) {
        long currentTime = System.currentTimeMillis();
        long totalElapsedTime = currentTime - startTime;
        long allSeconds = totalElapsedTime / 1000;
        long minutes = allSeconds / 60;
        long seconds = allSeconds - (minutes * 60);
        float rate = (float)totalCount / (totalElapsedTime / 1000.0F);

        System.out.println("Pulled " + totalCount + " total docs in " + minutes + " minutes " + seconds + " seconds at " + rate + " per second, ");
    }
}