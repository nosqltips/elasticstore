package com.nosqlrevolution.apps;

import com.nosqlrevolution.ElasticStore;
import com.nosqlrevolution.Index;
import com.nosqlrevolution.cursor.Cursor;
import java.io.IOException;

/**
 * Take a file input and break the file into blocks for processing.
 * 
 * @author cbrown
 */
public class JsonBlocker extends AbstractBlocker {
    private final Cursor<String> cursor;
    private final long totalDocs;
    
    /**
     * Take a file input and break the file into blocks for processing.
     * 
     * @param store
     * @param index
     * @param type
     * @param blockSize
     * @param limit
     * @param sample
     * @throws IOException 
     */
    public JsonBlocker(ElasticStore store, String index, String type, int blockSize, int limit, int sample) throws IOException, Exception {
        super(blockSize, limit, sample);
        Index esIndex = store.getIndex(String.class, index, type);
        cursor = esIndex.findAll();
        totalDocs = cursor.size();
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
    
    /**
     * Close all of the opened resources.
     * 
     * @throws IOException 
     */
    @Override
    public void shudown() throws IOException {
    }
}