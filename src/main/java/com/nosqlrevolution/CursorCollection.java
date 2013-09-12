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
        throw new UnsupportedOperationException();
    }

    public Object[] toArray() {
        throw new UnsupportedOperationException();
    }

    public <T> T[] toArray(T[] a) {
        throw new UnsupportedOperationException();
    }
    
    public boolean add(E e) {
        throw new UnsupportedOperationException();
    }

    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    public boolean containsAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    public boolean addAll(Collection<? extends E> c) {
        throw new UnsupportedOperationException();
    }

    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    public void clear() {
        throw new UnsupportedOperationException();
    }   
}
