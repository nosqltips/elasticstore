package com.nosqlrevolution;

import com.nosqlrevolution.util.MappingUtil;
import java.util.Iterator;
import org.elasticsearch.search.SearchHits;

/**
 * This is a simple iterator that expects all of the data to be in a single set of hits.
 * 
 * @author cbrown
 */
public class CursorIterator<E> implements Iterator<E> {
    private E e;
    private MappingUtil<E> mapping = new MappingUtil<E>();
    private SearchHits hits;
    private int iter = 0;
    private int iterAll = 0;
    
    protected CursorIterator(E e, SearchHits hits) {
        this.e = e;
        this.hits = hits;
    }

    @Override
    public boolean hasNext() {
        return iter < hits.getHits().length;
    }

    @Override
    public E next() {
        E returnE = mapping.get(e, hits.getAt(iter).sourceAsString());
        iter ++;
        iterAll ++;
        return returnE;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
