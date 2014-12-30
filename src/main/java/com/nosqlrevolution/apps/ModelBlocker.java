package com.nosqlrevolution.apps;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nosqlrevolution.ElasticStore;
import com.nosqlrevolution.Index;
import com.nosqlrevolution.cursor.Cursor;
import java.io.IOException;

/**
 * Take a file input and break the file into blocks for processing.
 * 
 * @author cbrown
 */
public class ModelBlocker extends AbstractBlocker {
    private final ObjectMapper mapper = new ObjectMapper();
    private final Cursor<ExportModel> cursor;
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
    public ModelBlocker(ElasticStore store, String index, String type, int blockSize, int limit, int sample) throws IOException, Exception {
        super(blockSize, limit, sample);
        Index esIndex = store.getIndex(ExportModel.class, index, type);
        cursor = esIndex.findAll();
        totalDocs = cursor.size();
    }
    
    @Override    
    public Integer call() throws Exception {
        for (ExportModel model: cursor) {
            if (model != null) {
                try {
                    // A false value means we're reached a limit.
                    if (! super.bufferNext(mapper.writeValueAsString(model))) {
                        break;
                    }
                } catch (JsonProcessingException jpe) {
                    jpe.printStackTrace();
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
    }
}