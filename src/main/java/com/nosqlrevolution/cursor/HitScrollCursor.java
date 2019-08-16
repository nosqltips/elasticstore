package com.nosqlrevolution.cursor;

import java.util.Iterator;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.search.SearchHit;

/**
 * Used to automatically scroll through a set of search results 
 * using the ElasticSearch scroll search type.
 * 
 * @author cbrown
 */
public class HitScrollCursor extends Cursor<SearchHit> {
    private final SearchRequest scrollRequest;
    private final CursorIterator<SearchHit> iterator;
    
    public HitScrollCursor(SearchRequest scrollRequest, RestHighLevelClient restClient) throws Exception {
        // TODO: may need to check for null here.
        this.scrollRequest = scrollRequest;
        this.iterator = new HitScrollCursorIterator(scrollRequest, restClient);
    }

    @Override
    public int size() {
        return iterator.totalSize;
    }

    @Override
    public boolean isEmpty() {
        return ! iterator.hasNext();
    }

    @Override
    public Iterator<SearchHit> iterator() {
        return iterator;
    }
}
