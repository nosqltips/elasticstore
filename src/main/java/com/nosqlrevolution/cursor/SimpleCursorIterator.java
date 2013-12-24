package com.nosqlrevolution.cursor;

import org.elasticsearch.search.SearchHits;

/**
 * This is a simple iterator that expects all of the data to be in a single set of hits.
 * 
 * @author cbrown
 */
public class SimpleCursorIterator<E> extends CursorIterator<E> {
    
    protected SimpleCursorIterator(Class<E> e, SearchHits hits) {
        this.e = e;
        this.hits = hits;
    }
}
