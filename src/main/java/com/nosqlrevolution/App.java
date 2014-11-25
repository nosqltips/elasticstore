package com.nosqlrevolution;

import com.nosqlrevolution.cursor.Cursor;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 *
 * @author cbrown
 */
public class App {

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws IOException, Exception {
        if (args == null || args.length == 0) {
            System.out.println("No parameters, exiting.");
            System.out.println("1) hostname");
            System.out.println("2) outFilename");
            System.out.println("3) clustername");
            System.out.println("4) index");
            System.out.println("5) type");
            return;
        }
        
        String hostname = args[0];
        String outfilename = args[1];
        String clustername = args[2];
        String index = args[3];
        String type = args[4];
        
        System.out.println("hostname=" + hostname + " outfilename=" + outfilename + " clustername=" + clustername + " index=" + index + " type=" + type);
        File outFile = new File(outfilename);
        if (outFile.exists()) {
            System.out.println("File exists, exiting.");
            return;
        }
        
        // Connect to ElasticSearch
        ElasticStore store = new ElasticStore().asTransport().withClusterName(clustername).withUniCast(hostname).execute();
        Index data = store.getIndex(String.class, index, type);
        Cursor<String> cursor = data.findAll();

        outFile.createNewFile();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outFile))) {
            String newLine = System.lineSeparator();
            
            int totalCount = 0;
            int elapsedCount = 0;
            long startTime = System.currentTimeMillis();
            long elapsedTimeStart = startTime;
            for (String s: cursor) {
                writer.append(s).append(newLine);
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
