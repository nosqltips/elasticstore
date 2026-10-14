package com.nosqlrevolution.cursor;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.json.JsonData;
import java.io.IOException;
import java.io.UncheckedIOException;

/** Pages full hits, including document IDs and search metadata. */
public class HitScrollCursorIterator extends PitIterator<JsonData> {
    protected HitScrollCursorIterator(SearchRequest request, ElasticsearchClient client) {
        super(JsonData.class, request, client);
    }

    public void clearScrollContext() {
        try { close(); }
        catch (IOException ex) { throw new UncheckedIOException("Cannot close Elasticsearch PIT", ex); }
    }

    @Override
    public String getPitId() { return super.getPitId(); }
}
