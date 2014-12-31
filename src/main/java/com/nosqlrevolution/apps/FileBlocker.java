package com.nosqlrevolution.apps;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.zip.GZIPInputStream;

/**
 * Take a file input and break the file into blocks for processing.
 * 
 * @author cbrown
 */
public class FileBlocker extends AbstractBlocker {
    private final String filename;
    private final File inFile;
    private final FileInputStream fis;
    private final GZIPInputStream gis;
    private final Reader reader;
    private final BufferedReader bufReader;
    private final long totalDocs;
    
    /**
     * Take a file input and break the file into blocks for processing.
     * 
     * @param file
     * @param blockSize
     * @param limit
     * @param sample
     * @throws IOException 
     */
    public FileBlocker(String file, int blockSize, int limit, int sample) throws IOException {
        super(blockSize, limit, sample);
        this.filename = file;
        inFile = new File(filename);

        // Read the total number of documents first
        totalDocs = limit <= 0 ? getDocCount(inFile) : limit;
        
        // Set up our file access now.
        fis = new FileInputStream(inFile);
        if (filename.endsWith(".gz") || filename.endsWith("gzip")) {
            gis = new GZIPInputStream(fis);
            reader = new InputStreamReader(gis);
        } else {
            gis = null;
            reader = new InputStreamReader(fis);
        }
        bufReader = new BufferedReader(reader);
    }
    
    @Override    
    public Integer call() throws Exception {
        String s;
        while ((s = bufReader.readLine()) != null) {
            if (s != null && ! s.isEmpty()) {
                // A false value means we're reached a limit.
                if (! super.bufferNext(s)) {
                    break;
                }
            }
        }
        
        super.bufferNext(null);
        return super.totalCount;
    }

    @Override
    public long getTotalDocs() {
        return totalDocs;
    }

    @Override
    public void shudown() throws IOException {
        bufReader.close();
        reader.close();
        if (gis != null) {
            gis.close();
        }
        fis.close();
    }
    
    /**
     * Open the file and read through all of the lines to get a total document count.
     * It is assumed that there is one document per line.
     * 
     * @param file
     * @return 
     */
    private long getDocCount(File file) {
        System.out.println("Counting documents in file ...");
        long startTime = System.currentTimeMillis();
                
        FileInputStream localFis = null;
        GZIPInputStream localGis = null;
        InputStreamReader localReader = null;
        BufferedReader localBuffer = null;
        long total = 0;
        try {
            localFis = new FileInputStream(file);
            if (file.getName().endsWith(".gz") || file.getName().endsWith("gzip")) {
                localGis = new GZIPInputStream(localFis);
                localReader = new InputStreamReader(localGis);
            } else {
                localReader = new InputStreamReader(localFis);
            }

            localBuffer = new BufferedReader(localReader);
            
            String s;
            while ((s = localBuffer.readLine()) != null) {
                total ++;
            }
        } catch (IOException ie) {
            ie.printStackTrace();
        } finally {
            try {
                if (localBuffer != null) {
                    localBuffer.close();
                }
                if (localReader != null) {
                    localReader.close();
                }
                if (localGis != null) {
                    localGis.close();
                }
                if (localFis != null) {
                    localFis.close();
                }
            } catch (IOException ie) {
                ie.printStackTrace();
            }
        }

        System.out.println("Counted " + total + " docs in " + ((System.currentTimeMillis() - startTime) / 1000) + " seconds");

        return total;
    }
}