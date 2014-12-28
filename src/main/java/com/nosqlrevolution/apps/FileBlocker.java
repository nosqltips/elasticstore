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
    
    /**
     * Take a file input and break the file into blocks for processing.
     * 
     * @param file
     * @param blockSize
     * @throws IOException 
     */
    public FileBlocker(String file, int blockSize) throws IOException {
        super(blockSize);
        this.filename = file;
        inFile = new File(filename);
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
    
    /**
     * Break the file contents into blocks and return total number of documents when finished.
     * 
     * @return
     * @throws Exception 
     */
    @Override    
    public Integer call() throws Exception {
        String s;
        while ((s = bufReader.readLine()) != null) {
            if (s != null && ! s.isEmpty()) {
                super.bufferNext(s);
            }
        }
        
        super.bufferNext(null);
        return super.totalCount;
    }

    /**
     * Close all of the opened resources.
     * 
     * @throws IOException 
     */
    @Override
    public void shudown() throws IOException {
        bufReader.close();
        reader.close();
        if (gis != null) {
            gis.close();
        }
        fis.close();
    }
}