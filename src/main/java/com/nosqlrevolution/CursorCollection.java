package com.nosqlrevolution;

import java.util.Collection;
import java.util.Iterator;
import org.elasticsearch.search.SearchHits;

/**
 *
 * @author cbrown
 */
public class CursorCollection<E> implements Collection<E> {
    private E e;
    private SearchHits hits;

    public CursorCollection(E e, SearchHits hits) {
        this.e = e;
        this.hits = hits;
    }
    
    public int size() {
        return (int)hits.getTotalHits();
    }

    public boolean isEmpty() {
        return hits.getTotalHits() == 0;
    }

    public Iterator<E> iterator() {
        return new CursorIterator(e, hits);
    }

    // NotImplemented
    // Possibly pass these through to ES and reload.
    public boolean contains(Object o) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Object[] toArray() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public <T> T[] toArray(T[] a) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public boolean add(E e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean remove(Object o) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean containsAll(Collection<?> c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean addAll(Collection<? extends E> c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void clear() {
        throw new UnsupportedOperationException("Not supported yet.");
    }   
}
