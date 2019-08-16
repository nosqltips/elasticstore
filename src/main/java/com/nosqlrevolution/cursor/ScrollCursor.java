package com.nosqlrevolution.cursor;

import java.util.Iterator;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.client.RestHighLevelClient;

/**
 * Used to automatically scroll through a set of search results 
 * using the ElasticSearch scroll search type.
 * 
 * @author cbrown
 * @param <E>
 */
public class ScrollCursor<E> extends Cursor<E> {
    private final SearchRequest scrollRequest;
    private final CursorIterator<E> iterator;
    
    public ScrollCursor(Class<E> e, SearchRequest scrollRequest, RestHighLevelClient restClient) throws Exception {
        // TODO: may need to check for null here.
        this.e = e;
        this.scrollRequest = scrollRequest;
        this.iterator = new ScrollCursorIterator(e, scrollRequest, restClient);
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
    public Iterator<E> iterator() {
        return iterator;
    }
}
