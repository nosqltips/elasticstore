package com.nosqlrevolution;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import org.elasticsearch.search.SearchHits;

/**
 *
 * @author cbrown
 * @param <E>
 */
public class Cursor<E> extends AbstractCollection {
    private final E e;
    private final SearchHits hits;
    
    public Cursor(E e, SearchHits hits) {
        // TODO: may need to check for null here.
        this.e = e;
        this.hits = hits;
    }
    
    @Override
    public Iterator<E> iterator() {
        return new CursorIterator(e, hits);
    }

    public Collection<E> collection() {
        return new CursorCollection(e, hits);
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
