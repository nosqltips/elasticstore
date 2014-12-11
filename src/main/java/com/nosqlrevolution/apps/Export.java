package com.nosqlrevolution.apps;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nosqlrevolution.ElasticStore;
import com.nosqlrevolution.Index;
import com.nosqlrevolution.cursor.Cursor;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Used to download data from an ElasticSearch cluster
 * 
 * @author cbrown
 */
public class Export {
    private final static String newLine = System.lineSeparator();
    private final static ObjectMapper mapper = new ObjectMapper();
    
    public static void run(String[] args) throws IOException, Exception {
        if (args == null || args.length == 0) {
            System.out.println("No parameters, exiting.");
            System.out.println("1) hostname");
            System.out.println("2) outFilename");
            System.out.println("3) clustername");
            System.out.println("4) index");
            System.out.println("5) type");
            return;
        }
        
        String hostname = args[1];
        String outfilename = args[2];
        String clustername = args[3];
        String index = args[4];
        String type = args[5];
        
        System.out.println("hostname=" + hostname + " outfilename=" + outfilename + " clustername=" + clustername + " index=" + index + " type=" + type);
        File outFile = new File(outfilename);
        if (outFile.exists()) {
            System.out.println("File exists, exiting.");
            return;
        }
        
        // Connect to ElasticSearch
        ElasticStore store = new ElasticStore().asTransport().withClusterName(clustername).withUniCast(hostname).execute();
        Index data = store.getIndex(ExportModel.class, index, type);
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