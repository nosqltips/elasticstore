package com.nosqlrevolution;

import java.util.Collection;
import java.util.Iterator;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.search.SearchHits;

/**
 *
 * @author cbrown
 * @param <E>
 */
public class BlockCursorCollection<E> implements Collection<E> {
    private final E e;
    private final SearchHits firstHits;
    private final SearchRequestBuilder builder;
    private int from = 0;
    private int size = 0;

    public BlockCursorCollection(E e, SearchHits firstHits, SearchRequestBuilder builder, int from, int size) {
        this.e = e;
        this.firstHits = firstHits;
        this.builder = builder;
        this.from = from;
        this.size = size;
    }
    
    @Override
    public int size() {
        return (int)firstHits.getTotalHits();
    }

    @Override
    public boolean isEmpty() {
        return firstHits.getTotalHits() == 0;
    }

    @Override
    public Iterator<E> iterator() {
        return new BlockCursorIterator(e, firstHits, builder, from, size);
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
