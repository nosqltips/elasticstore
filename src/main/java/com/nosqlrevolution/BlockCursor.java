package com.nosqlrevolution;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.search.SearchHits;

/**
 * Used to automatically scroll through i set of search results.
 * 
 * @author cbrown
 */
public class BlockCursor<E> extends AbstractCollection {
    private E e;
    private SearchHits firstHits;
    private SearchRequestBuilder builder;
    private int from;
    private int size;
    private int totalSize;
    
    public BlockCursor(E e, SearchHits firstHits, SearchRequestBuilder builder, int from, int size) {
        // TODO: may need to check for null here.
        this.e = e;
        this.firstHits = firstHits;
        this.builder = builder;
        this.from = from;
        this.size = size;
        
        this.totalSize = (int)firstHits.getTotalHits();
    }
    
    @Override
    public Iterator<E> iterator() {
        return new BlockCursorIterator(e, firstHits, builder, from, size);
    }

    public Collection<E> collection() {
        return new BlockCursorCollection(e, firstHits, builder, from, size);
    }

    @Override
    public int size() {
        return totalSize;
    }
}
