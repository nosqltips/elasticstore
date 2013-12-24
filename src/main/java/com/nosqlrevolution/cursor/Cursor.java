package com.nosqlrevolution.cursor;

import java.util.AbstractCollection;
import java.util.Collection;
import org.elasticsearch.search.SearchHits;

/**
 * Custom implementation of Collection<E> supporting ElasticSearch for the backend.
 * 
 * @author cbrown
 * @param <E>
 */
public abstract class Cursor<E> extends AbstractCollection<E> {
    protected Class<E> e;
    protected SearchHits hits;

    @Override
    public int size() {
        return (int)hits.getTotalHits();
    }

    @Override
    public boolean isEmpty() {
        return hits.getTotalHits() == 0;
    }

    @Override
    public boolean contains(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean add(E e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }   
}
