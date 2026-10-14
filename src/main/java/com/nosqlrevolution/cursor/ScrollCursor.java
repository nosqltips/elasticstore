package com.nosqlrevolution.cursor;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import java.util.Iterator;

/**
 * Used to automatically scroll through a set of search results 
 * using the ElasticSearch scroll search type.
 * 
 * @author cbrown
 * @param <E>
 */
public class ScrollCursor<E> extends Cursor<E> {
    private final ScrollCursorIterator<E> iterator;
    
    public ScrollCursor(Class<E> e, SearchRequest pitRequest, ElasticsearchClient client) throws Exception {
        // TODO: may need to check for null here.
        this.e = e;
        this.iterator = new ScrollCursorIterator<>(e, pitRequest, client);
    }

    @Override
    public void close() throws java.io.IOException { iterator.close(); }

    @Override
    public int size() {
        return iterator.totalSize;
    }

    @Override
    public boolean isEmpty() {
        return ! iterator.hasNext();
    }

    @Override
    public Iterator<E> iterator() {
        return iterator;
    }
}
