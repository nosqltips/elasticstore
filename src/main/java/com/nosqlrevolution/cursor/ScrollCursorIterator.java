package com.nosqlrevolution.cursor;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import java.io.IOException;

/** Projects search hits into typed document sources. */
public class ScrollCursorIterator<E> extends CursorIterator<E> implements AutoCloseable {
    private final PitIterator<?> hitsIterator;
    private final Class<E> documentType;

    protected ScrollCursorIterator(Class<E> documentType, SearchRequest request, ElasticsearchClient client) {
        this(documentType, request, client, 0, 0);
    }

    protected ScrollCursorIterator(Class<E> documentType, SearchRequest request, ElasticsearchClient client, long skip, long limit) {
        this.documentType = documentType;
        hitsIterator = documentType == String.class
                ? new PitIterator<>(co.elastic.clients.json.JsonData.class, request, client, skip, limit)
                : new PitIterator<>(documentType, request, client, skip, limit);
        totalSize = hitsIterator.totalSize;
    }

    @Override
    public boolean hasNext() { return hitsIterator.hasNext(); }

    @Override
    public E next() {
        Object source = hitsIterator.next().source();
        try {
            if (source == null) throw new IllegalStateException("Search hit has no document source");
            return documentType.cast(documentType == String.class
                    ? ((co.elastic.clients.json.JsonData) source).toJson().toString() : source);
        } catch (RuntimeException failure) { throw hitsIterator.fail(failure); }
    }

    public String getPitId() { return hitsIterator.getPitId(); }

    @Override
    public void close() throws IOException { hitsIterator.close(); }
}
