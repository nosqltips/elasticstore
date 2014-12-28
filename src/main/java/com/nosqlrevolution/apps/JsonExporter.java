package com.nosqlrevolution.apps;

import java.io.BufferedWriter;
import java.util.List;
import java.util.concurrent.Callable;

/**
 *
 * @author cbrown
 */
public class JsonExporter implements Callable<Integer> {
    private final String newLine = System.lineSeparator();
    private final BufferedWriter writer;
    private final List<String> data;
    private int totalCount = 0;
    
    public JsonExporter(BufferedWriter writer, List<String> data) {
        this.writer = writer;
        this.data = data;
    }
    
    @Override
    public Integer call() throws Exception {
        for (String s: data) {
            writer.append(s).append(newLine);
            totalCount += 1;
        }

        writer.flush();
        return totalCount;
    }
}