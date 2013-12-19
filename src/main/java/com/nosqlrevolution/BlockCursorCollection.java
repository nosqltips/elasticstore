package com.nosqlrevolution;

import java.util.Iterator;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.search.SearchHits;

/**
 *
 * @author cbrown
 * @param <E>
 */
public class BlockCursorCollection<E> extends CursorCollection<E> {
    private final SearchRequestBuilder builder;
    private int from = 0;
    private int size = 0;

    public BlockCursorCollection(Class<E> e, SearchHits firstHits, SearchRequestBuilder builder, int from, int size) {
        this.e = e;
        this.hits = firstHits;
        this.builder = builder;
        this.from = from;
        this.size = size;
    }

    @Override
    public Iterator<E> iterator() {
        return new BlockCursorIterator(e, hits, builder, from, size);
    }
}
