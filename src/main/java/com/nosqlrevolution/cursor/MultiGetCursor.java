package com.nosqlrevolution.cursor;

import java.util.Iterator;

/**
 * Used to automatically scroll through a set of search results.
 * 
 * @author cbrown
 * @param <E>
 */
public class MultiGetCursor<E> extends Cursor<E> {
    String[] json;
    
    public MultiGetCursor(Class<E> e, String[] json) {
        // TODO: may need to check for null here.
        this.e = java.util.Objects.requireNonNull(e, "Document type");
        this.json = java.util.Objects.requireNonNull(json, "Documents").clone();
    }
    
    @Override
    public Iterator<E> iterator() {
        return new MultiGetCursorIterator<>(e, json);
    }

    @Override
    public int size() {
        return json.length;
    }

    @Override
    public boolean isEmpty() {
        return json.length == 0;
    }

}
