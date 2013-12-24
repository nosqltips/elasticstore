package com.nosqlrevolution.cursor;

/**
 * This iterator can go back to the search engine and iterate through blocks of data returned from ES.
 * 
 * @author cbrown
 * @param <E>
 */
public class MultiGetCursorIterator<E> extends CursorIterator<E> {
    private final String[] json;
    
    protected MultiGetCursorIterator(Class<E> e, String[] json) {
        this.e = e;
        this.json = json;
    }
    
    @Override
    public boolean hasNext() {
        return iter < json.length;
    }

    @Override
    public E next() {
        E returnE = mapping.get(json[iter], e);
        iter ++;
        return returnE;
    }
}
