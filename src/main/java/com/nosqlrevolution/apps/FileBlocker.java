package com.nosqlrevolution.apps;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;

/** Reads JSON lines from a plain or gzip file. Owns one reader and its underlying streams. */
public class FileBlocker extends AbstractBlocker {
    private final BufferedReader reader;
    private final long totalDocs;

    public FileBlocker(String filename, int blockSize, int limit, int sample) throws IOException {
        super(blockSize, limit, sample);
        File file = new File(filename);
        totalDocs = limit > 0 ? limit : countDocuments(file);
        reader = openReader(file);
    }

    private static BufferedReader openReader(File file) throws IOException {
        InputStream input = new FileInputStream(file);
        try {
            if (file.getName().endsWith(".gz") || file.getName().endsWith("gzip")) {
                input = new GZIPInputStream(input);
            }
            return new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
        } catch (IOException ex) {
            try { input.close(); } catch (IOException cleanup) { ex.addSuppressed(cleanup); }
            throw ex;
        }
    }

    @Override
    public Integer call() throws Exception {
        String line;
        while ((line = reader.readLine()) != null) {
            if (!line.isEmpty() && !bufferNext(line)) break;
        }
        bufferNext(null);
        return totalCount;
    }

    @Override
    public long getTotalDocs() { return totalDocs; }

    @Override
    public void shudown() throws IOException { reader.close(); }

    private static long countDocuments(File file) throws IOException {
        long count = 0;
        try (BufferedReader input = openReader(file)) {
            String line;
            while ((line = input.readLine()) != null) {
                if (!line.isEmpty()) count++;
            }
        }
        return count;
    }
}
