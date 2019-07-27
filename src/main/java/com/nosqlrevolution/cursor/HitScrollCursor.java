package com.nosqlrevolution.cursor;

import java.util.Iterator;
import org.elasticsearch.action.search.SearchScrollRequestBuilder;
import org.elasticsearch.search.SearchHit;

/**
 * Used to automatically scroll through a set of search results 
 * using the ElasticSearch scroll search type.
 * 
 * @author cbrown
 */
public class HitScrollCursor extends Cursor<SearchHit> {
    private final SearchScrollRequestBuilder scrollBuilder;
    private final CursorIterator<SearchHit> iterator;
    
    public HitScrollCursor(SearchScrollRequestBuilder scrollBuilder) throws Exception {
        // TODO: may need to check for null here.
        this.scrollBuilder = scrollBuilder;
        this.iterator = new HitScrollCursorIterator(scrollBuilder);
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
