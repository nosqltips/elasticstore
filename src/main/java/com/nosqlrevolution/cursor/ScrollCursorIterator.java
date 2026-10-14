package com.nosqlrevolution.cursor;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import java.io.IOException;

/** Projects search hits into typed document sources. */
public class ScrollCursorIterator<E> extends CursorIterator<E> implements AutoCloseable {
    private final PitIterator<E> hitsIterator;

    protected ScrollCursorIterator(Class<E> documentType, SearchRequest request, ElasticsearchClient client) {
        hitsIterator = new PitIterator<>(documentType, request, client);
        totalSize = hitsIterator.totalSize;
    }

    @Override
    public boolean hasNext() { return hitsIterator.hasNext(); }

    @Override
    public E next() {
        E source = hitsIterator.next().source();
        if (source == null) throw new IllegalStateException("Search hit has no document source");
        return source;
    }

    public String getPitId() { return hitsIterator.getPitId(); }

    @Override
    public void close() throws IOException { hitsIterator.close(); }
}
