package com.nosqlrevolution;

import java.util.Iterator;
import org.elasticsearch.action.search.SearchScrollRequestBuilder;

/**
 *
 * @author cbrown
 * @param <E>
 */
public class ScrollCursorCollection<E> extends CursorCollection<E> {
    private final SearchScrollRequestBuilder scrollBuilder;
    private final int totalSize;

    public ScrollCursorCollection(Class<E> e, SearchScrollRequestBuilder scrollBuilder, int totalSize) {
        this.e = e;
        this.scrollBuilder = scrollBuilder;
        this.totalSize = totalSize;
    }
    
    @Override
    public int size() {
        return totalSize;
    }

    @Override
    public boolean isEmpty() {
        return totalSize == 0;
    }

    @Override
    public Iterator<E> iterator() {
        return new ScrollCursorIterator(e, scrollBuilder, totalSize);
    }
}
