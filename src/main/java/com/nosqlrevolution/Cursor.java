package com.nosqlrevolution;

import java.util.AbstractCollection;
import java.util.Iterator;
import org.elasticsearch.search.SearchHits;

/**
 *
 * @author cbrown
 */
public class Cursor<T> extends AbstractCollection {
    private T t;
    private SearchHits hits;
    
    public Cursor(T t, SearchHits hits) {
        // TODO: may need to check for null here.
        this.t = t;
        this.hits = hits;
    }
    
    @Override
    public Iterator iterator() {
        return new CursorIterator(t, hits);
    }

    @Override
    public int size() {
        return (int)hits.getTotalHits();
    }
    
    @Override
    public boolean isEmpty() {
        return hits.getTotalHits() == 0;
    }
}
