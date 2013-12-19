package com.nosqlrevolution;

import java.util.Collection;
import java.util.Iterator;
import org.elasticsearch.search.SearchHits;

/**
 *
 * @author cbrown
 * @param <E>
 */
public class SimpleCursor<E> extends Cursor<E> {

    public SimpleCursor(Class<E> e, SearchHits hits) {
        // TODO: may need to check for null here.
        this.e = e;
        this.hits = hits;
    }
    
    @Override
    public Iterator<E> iterator() { 
        return new SimpleCursorIterator(e, hits);
    }

    public Collection<E> collection() {
        return new SimpleCursorCollection(e, hits);
    }

    @Override
    public int size() {
        return (int)hits.getTotalHits();
    }
}
