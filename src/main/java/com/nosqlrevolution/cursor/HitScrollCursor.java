package com.nosqlrevolution.cursor;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import java.util.Iterator;


/**
 * Used to automatically scroll through a set of search results 
 * using the ElasticSearch scroll search type.
 * 
 * @author cbrown
 */
public class HitScrollCursor extends Cursor<Hit<JsonData>> {
    private final HitScrollCursorIterator iterator;
    
    public HitScrollCursor(SearchRequest pitRequest, ElasticsearchClient client) throws Exception {
        // TODO: may need to check for null here.
        this.iterator = new HitScrollCursorIterator(pitRequest, client);
    }

    @Override
    public void close() throws java.io.IOException { iterator.close(); }

    @Override
    public int size() {
        return iterator.totalSize;
    }

    @Override
    public boolean isEmpty() {
        return ! iterator.hasNext();
    }

    @Override
    public Iterator<Hit<JsonData>> iterator() {
        return iterator;
    }
}
