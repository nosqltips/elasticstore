package com.nosqlrevolution;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import org.elasticsearch.action.search.SearchScrollRequestBuilder;

/**
 * Used to automatically scroll through a set of search results 
 * using the ElasticSearch scroll search type.
 * 
 * @author cbrown
 * @param <E>
 */
public class ScrollCursor<E> extends AbstractCollection {
    private final E e;
    private final SearchScrollRequestBuilder scrollBuilder;
    private final int totalSize;
    
    public ScrollCursor(E e, SearchScrollRequestBuilder scrollBuilder, int totalSize) {
        // TODO: may need to check for null here.
        this.e = e;
        this.scrollBuilder = scrollBuilder;
        this.totalSize = totalSize;
    }
    
    @Override
    public Iterator<E> iterator() {
        return new ScrollCursorIterator(e, scrollBuilder, totalSize);
    }

    public Collection<E> collection() {
        return new ScrollCursorCollection(e, scrollBuilder, totalSize);
    }

    @Override
    public int size() {
        return totalSize;
    }
}
