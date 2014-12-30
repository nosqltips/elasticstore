package com.nosqlrevolution.apps;

import java.io.BufferedOutputStream;
import java.util.List;
import java.util.concurrent.Callable;

/**
 *
 * @author cbrown
 */
public class JsonExporter implements Callable<Integer> {
    private final byte[] newLine = System.lineSeparator().getBytes();
    private final BufferedOutputStream stream;
    private final List<String> data;
    private int totalCount = 0;
    
    public JsonExporter(BufferedOutputStream stream, List<String> data) {
        this.stream = stream;
        this.data = data;
    }
    
    @Override
    public Integer call() throws Exception {
        for (String s: data) {
            stream.write(s.getBytes());
            stream.write(newLine);
            totalCount += 1;
        }

        stream.flush();
        return totalCount;
    }
}