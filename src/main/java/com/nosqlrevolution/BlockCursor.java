package com.nosqlrevolution;

import java.util.Iterator;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.search.SearchHits;

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
    private final int totalSize;
    
    public BlockCursor(Class<E> e, SearchHits firstHits, SearchRequestBuilder builder, int from, int size) {
        // TODO: may need to check for null here.
        this.e = e;
        this.hits = firstHits;
        this.builder = builder;
        this.from = from;
        this.size = size;
        
        this.totalSize = (int)firstHits.getTotalHits();
    }
    
    @Override
    public Iterator<E> iterator() {
        return new BlockCursorIterator(e, hits, builder, from, size);
    }

    @Override
    public int size() {
        return totalSize;
    }
}
