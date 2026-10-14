package com.nosqlrevolution.apps;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nosqlrevolution.ElasticStore;
import com.nosqlrevolution.Index;
import com.nosqlrevolution.cursor.Cursor;
import com.nosqlrevolution.query.ElasticStoreQuery;
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
    public ModelBlocker(ElasticStore store, ElasticStoreQuery query, String index, int blockSize, int limit, int sample) throws IOException, Exception {
        super(blockSize, limit, sample);
        Index esIndex = store.getIndex(ExportModel.class, index);
        if (query != null) {
            cursor = esIndex.findAll(query);
        } else {
            cursor = esIndex.findAll();
        }
        totalDocs = limit <= 0 ? cursor.size() : limit;
    }
    
    @Override    
    public Integer call() throws Exception {
        try {
            for (ExportModel model: cursor) {
                if (model != null) {
                    if (!super.bufferNext(mapper.writeValueAsString(model))) {
                        break;
                    }
                }
            }

            super.bufferNext(null);
            return super.totalCount;
        } catch (Exception | Error ex) {
            fail(ex);
            throw ex;
        }
    }

    @Override
    public long getTotalDocs() {
        return totalDocs;
    }
    
    @Override
    public void shudown() throws IOException {
    }
}