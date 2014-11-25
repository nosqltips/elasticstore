package com.nosqlrevolution.cursor;

import java.util.Iterator;
import org.elasticsearch.action.search.SearchScrollRequestBuilder;

/**
 * Used to automatically scroll through a set of search results 
 * using the ElasticSearch scroll search type.
 * 
 * @author cbrown
 * @param <E>
 */
public class ScrollCursor<E> extends Cursor<E> {
    private final SearchScrollRequestBuilder scrollBuilder;
    private final CursorIterator<E> iterator;
    
    public ScrollCursor(Class<E> e, SearchScrollRequestBuilder scrollBuilder) throws Exception {
        // TODO: may need to check for null here.
        this.e = e;
        this.scrollBuilder = scrollBuilder;
        this.iterator = new ScrollCursorIterator(e, scrollBuilder);
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
