package com.nosqlrevolution.cursor;

import java.util.Iterator;
import org.elasticsearch.action.search.SearchRequestBuilder;

/**
 * Used to automatically scroll through a set of search results.
 * 
 * @author cbrown
 * @param <E>
 */
public class BlockCursor<E> extends Cursor<E> {
    private final SearchRequestBuilder builder;
    private final int from;
    private final int size;
    private final BlockCursorIterator<E> iterator;
    
    public BlockCursor(Class<E> e, SearchRequestBuilder builder, int from, int size) {
        // TODO: may need to check for null here.
        this.e = e;
        this.builder = builder;
        this.from = from;
        this.size = size;
        iterator = new BlockCursorIterator(e, builder, from, size);
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
