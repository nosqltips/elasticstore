package com.nosqlrevolution;

import com.nosqlrevolution.util.MappingUtil;
import java.util.Iterator;
import org.elasticsearch.search.SearchHits;

/**
 * This is a simple iterator that expects all of the data to be in a single set of hits.
 * 
 * @author cbrown
 * @param <E>
 */
public abstract class CursorIterator<E> implements Iterator<E> {
    protected Class<E> e;
    protected final MappingUtil<E> mapping = new MappingUtil<E>();
    protected SearchHits hits;
    protected int iter = 0;
    protected int iterAll = 0;

    @Override
    public boolean hasNext() {
        return iter < hits.getHits().length;
    }

    @Override
    public E next() {
        E returnE = mapping.get(hits.getAt(iter).sourceAsString(), e);
        iter ++;
        iterAll ++;
        return returnE;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
