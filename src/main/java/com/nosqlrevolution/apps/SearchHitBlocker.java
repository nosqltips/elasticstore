package com.nosqlrevolution.apps;

import com.nosqlrevolution.ElasticStore;
import com.nosqlrevolution.Index;
import com.nosqlrevolution.cursor.Cursor;
import com.nosqlrevolution.query.Query;
import com.nosqlrevolution.util.QueryUtil;
import java.io.IOException;
import org.elasticsearch.search.SearchHit;

/**
 * Take a file input and break the file into blocks for processing.
 * 
 * @author cbrown
 */
public class SearchHitBlocker extends AbstractHitBlocker {
    private final Cursor<SearchHit> cursor;
    private final long totalDocs;
    
    /**
     * Take a file input and break the file into blocks for processing.
     * 
     * @param store
     * @param query
     * @param index
     * @param type
     * @param blockSize
     * @param limit
     * @param sample
     * @throws IOException 
     */
    public SearchHitBlocker(ElasticStore store, Query query, String index, String type, int blockSize, int limit, int sample) throws IOException, Exception {
        super(blockSize, limit, sample);
        Index esIndex = store.getIndex(String.class, index, type);
        if (query != null) {
            cursor = esIndex.findAllScrollHit(query);
        } else {
            Query q = new Query();
            q.setQueryBuilder(QueryUtil.getMatchAllQuery());
            cursor = esIndex.findAllScrollHit(q);
        }
        totalDocs = limit <= 0 ? cursor.size() : limit;
    }
    
    /**
     * Break the file contents into blocks and return total number of documents when finished.
     * 
     * @return
     * @throws Exception 
     */
    @Override    
    public Integer call() throws Exception {
        for (SearchHit hit: cursor) {
            if (hit != null) {
                // A false value means we've reached a limit.
                if (! super.bufferNext(hit)) {
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