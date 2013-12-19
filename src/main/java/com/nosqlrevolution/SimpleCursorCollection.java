package com.nosqlrevolution;

import java.util.Iterator;
import org.elasticsearch.search.SearchHits;

/**
 *
 * @author cbrown
 * @param <E>
 */
public class SimpleCursorCollection<E> extends CursorCollection<E> {
    public SimpleCursorCollection(Class<E> e, SearchHits hits) {
        this.e = e;
        this.hits = hits;
    }

    public Iterator<E> iterator() {
        return new SimpleCursorIterator(e, hits);
    }
}
