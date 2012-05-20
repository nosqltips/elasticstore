package com.nosqlrevolution;

import com.nosqlrevolution.util.MappingUtil;
import java.util.Iterator;
import org.elasticsearch.search.SearchHits;

/**
 *
 * @author cbrown
 */
public class CursorIterator<T> implements Iterator<T> {
    private T t;
    private MappingUtil<T> mapping = new MappingUtil<T>();
    private SearchHits hits;
    private int iter = 0;
    private int iterAll = 0;
    
    // TODO: need to iterate through large sets going back to the search service.
    protected CursorIterator(T t, SearchHits hits) {
        this.t = t;
        this.hits = hits;
    }

    @Override
    public boolean hasNext() {
        return iter < hits.getHits().length;
    }

    @Override
    public T next() {
        T returnT = mapping.get(t, hits.getAt(iter).sourceAsString());
        iter ++;
        iterAll ++;
        return returnT;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
