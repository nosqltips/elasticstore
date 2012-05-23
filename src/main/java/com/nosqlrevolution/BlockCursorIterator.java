package com.nosqlrevolution;

import com.nosqlrevolution.util.MappingUtil;
import java.util.Iterator;
import org.elasticsearch.search.SearchHits;

/**
 * This iterator can go back to the search engine and iterate through blocks of data returned from ES.
 * 
 * @author cbrown
 */
public class BlockCursorIterator<E> implements Iterator<E> {
    private E e;
    private MappingUtil<E> mapping = new MappingUtil<E>();
    private SearchHits hits;
    private int iter = 0;
    private int iterAll = 0;
    
    protected BlockCursorIterator(E e, SearchHits hits) {
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
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
