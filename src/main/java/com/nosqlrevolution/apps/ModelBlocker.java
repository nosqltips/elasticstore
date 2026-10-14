package com.nosqlrevolution.apps;

import com.fasterxml.jackson.databind.ObjectMapper;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import com.nosqlrevolution.util.QueryUtil;
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
    private final Cursor<Hit<JsonData>> cursor;
    private final long totalDocs;
    
    /**
     * Take a file input and break the file into blocks for processing.
     * 
     * @param store
     * @param query
     * @param index
     * @param blockSize
     * @param limit
     * @param sample
     * @throws IOException 
     */
    public ModelBlocker(ElasticStore store, ElasticStoreQuery query, String index, int blockSize, int limit, int sample) throws IOException, Exception {
        super(blockSize, limit, sample);
        Index esIndex = store.getIndex(JsonData.class, index);
        if (query != null) {
            cursor = esIndex.findAllScrollHit(query);
        } else {
            ElasticStoreQuery all = new ElasticStoreQuery();
            all.setQuery(QueryUtil.getMatchAllQuery());
            cursor = esIndex.findAllScrollHit(all);
        }
        totalDocs = limit <= 0 ? cursor.size() : limit;
    }
    
    @Override    
    public Integer call() throws Exception {
        for (Hit<JsonData> hit: cursor) {
            if (hit.source() == null) throw new IllegalStateException("Search hit has no document source");
            ExportModel model = new ExportModel().setId(hit.id())
                    .setJson(hit.source().toJson().toString());
            if (!super.bufferNext(mapper.writeValueAsString(model))) {
                break;
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
        cursor.close();
    }
}