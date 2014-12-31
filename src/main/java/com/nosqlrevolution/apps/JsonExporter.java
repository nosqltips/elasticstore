package com.nosqlrevolution.apps;

import java.io.BufferedOutputStream;
import java.util.List;
import java.util.concurrent.Callable;

/**
 *
 * @author cbrown
 */
public class JsonExporter implements Callable<CountsAndBytes> {
    private final byte[] newLine = System.lineSeparator().getBytes();
    private final BufferedOutputStream stream;
    private final List<String> data;
    private int totalCount = 0;
    private int totalBytes = 0;
    
    public JsonExporter(BufferedOutputStream stream, List<String> data) {
        this.stream = stream;
        this.data = data;
    }
    
    @Override
    public CountsAndBytes call() throws Exception {
        for (String s: data) {
            byte[] bytes = s.getBytes();
            stream.write(bytes);
            stream.write(newLine);
            totalCount += 1;
            totalBytes = bytes.length;
        }

        stream.flush();
        return new CountsAndBytes(totalCount, totalBytes);
    }
}