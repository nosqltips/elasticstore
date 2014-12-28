package com.nosqlrevolution.apps;

import com.nosqlrevolution.Index;
import com.nosqlrevolution.cursor.Cursor;
import java.io.IOException;

/**
 * Take a file input and break the file into blocks for processing.
 * 
 * @author cbrown
 */
public class ElasticsearchBlocker extends AbstractBlocker {
    private final Cursor<String> cursor;
    
    /**
     * Take a file input and break the file into blocks for processing.
     * 
     * @param index
     * @param blockSize
     * @throws IOException 
     */
    public ElasticsearchBlocker(Index index, int blockSize) throws IOException {
        super(blockSize);
        cursor = index.findAll();
    }
    
    /**
     * Break the file contents into blocks and return total number of documents when finished.
     * 
     * @return
     * @throws Exception 
     */
    @Override    
    public Integer call() throws Exception {
        for (String s: cursor) {
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
    }
}