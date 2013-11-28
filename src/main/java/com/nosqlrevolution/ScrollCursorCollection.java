package com.nosqlrevolution;

import java.util.Collection;
import java.util.Iterator;
import org.elasticsearch.action.search.SearchScrollRequestBuilder;

/**
 *
 * @author cbrown
 * @param <E>
 */
public class ScrollCursorCollection<E> implements Collection<E> {
    private final E e;
    private final SearchScrollRequestBuilder scrollBuilder;
    private final int totalSize;

    public ScrollCursorCollection(E e, SearchScrollRequestBuilder scrollBuilder, int totalSize) {
        this.e = e;
        this.scrollBuilder = scrollBuilder;
        this.totalSize = totalSize;
    }
    
    @Override
    public int size() {
        return totalSize;
    }

    @Override
    public boolean isEmpty() {
        return totalSize == 0;
    }

    @Override
    public Iterator<E> iterator() {
        return new ScrollCursorIterator(e, scrollBuilder);
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
