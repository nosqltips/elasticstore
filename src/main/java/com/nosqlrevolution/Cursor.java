package com.nosqlrevolution;

import java.util.AbstractCollection;
import org.elasticsearch.search.SearchHits;

/**
 *
 * @author cbrown
 * @param <E>
 */
public abstract class Cursor<E> extends AbstractCollection {
    protected Class<E> e;
    protected SearchHits hits;
}
