package com.nosqlrevolution.cursor;

import com.nosqlrevolution.util.MappingUtil;
import java.util.Iterator;
import java.util.List;

/**
 * This is a simple iterator that expects all of the data to be in a single set of hits.
 * 
 * @author cbrown
 * @param <E>
 */
public abstract class CursorIterator<E> implements Iterator<E> {
    protected Class<E> e;
    protected final MappingUtil<E> mapping = new MappingUtil<>();
    protected List<E> hits;
    protected int iter = 0;
    protected int iterAll = 0;
    protected int totalSize;
    
    @Override
    public boolean hasNext() {
        return iter < hits.size();
    }

    @Override
    public E next() {
        E returnE = mapping.get(hits.get(iter).toString(), e);
        iter ++;
        iterAll ++;
        return returnE;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
