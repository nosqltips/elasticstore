package com.nosqlrevolution.cursor;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Time;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import java.io.IOException;
import com.nosqlrevolution.util.ReadIntegrity;
import java.io.UncheckedIOException;
import java.util.NoSuchElementException;

/** Shared paging for both document and metadata cursors. Owns the request's PIT. */
class PitIterator<T> extends CursorIterator<Hit<T>> implements AutoCloseable {
    private final Class<T> documentType;
    private final SearchRequest initialRequest;
    private final ElasticsearchClient client;
    private String pitId;
    private boolean exhausted;
    private boolean closed;
    private RuntimeException failure;
    private long skip;
    private final long limit;
    private long returned;

    PitIterator(Class<T> documentType, SearchRequest request, ElasticsearchClient client) {
        this(documentType, request, client, 0, 0);
    }

    PitIterator(Class<T> documentType, SearchRequest request, ElasticsearchClient client, long skip, long limit) {
        if (skip < 0 || limit < 0) throw new IllegalArgumentException("Skip and limit must not be negative");
        this.skip = skip;
        this.limit = limit;
        this.documentType = documentType;
        this.initialRequest = request;
        this.client = client;
        pitId = request.pit() == null ? null : request.pit().id();
        try {
            SearchResponse<T> response = fetch(request);
            long count = response.hits().total() == null ? 0 : response.hits().total().value();
            count = Math.max(0, count - skip);
            if (limit > 0) count = Math.min(count, limit);
            totalSize = (int) Math.min(count, Integer.MAX_VALUE);
        } catch (RuntimeException ex) {
            try { close(); } catch (Exception cleanup) { ex.addSuppressed(cleanup); }
            throw ex;
        }
    }

    private SearchResponse<T> fetch(SearchRequest request) {
        try {
            SearchResponse<T> response = client.search(request, documentType);
            if (response.pitId() != null) pitId = response.pitId();
            ReadIntegrity.search(response);
            hits = response.hits().hits();
            iter = 0;
            exhausted = hits.isEmpty();
            return response;
        } catch (IOException ex) {
            throw fail(new UncheckedIOException("Elasticsearch paging failed", ex));
        } catch (RuntimeException ex) {
            throw fail(ex);
        }
    }

    private RuntimeException fail(RuntimeException cause) {
        failure = cause;
        try { close(); }
        catch (Exception cleanup) { if (cleanup != cause) cause.addSuppressed(cleanup); }
        return cause;
    }

    @Override
    public boolean hasNext() {
        if (failure != null) throw failure;
        if (closed || limit > 0 && returned >= limit) return false;
        while (skip > 0 && available()) {
            int skipped = (int) Math.min(skip, hits.size() - iter);
            iter += skipped;
            skip -= skipped;
        }
        return available();
    }

    private boolean available() {
        if (closed) return false;
        if (iter < hits.size()) return true;
        if (exhausted) return false;
        SearchRequest.Builder next = new SearchRequest.Builder()
                .allowPartialSearchResults(false)
                .timeout(initialRequest.timeout())
                .size(initialRequest.size())
                .query(initialRequest.query())
                .source(initialRequest.source())
                .storedFields(initialRequest.storedFields())
                .highlight(initialRequest.highlight())
                .sort(initialRequest.sort())
                .searchAfter(hits.get(hits.size() - 1).sort());
        if (pitId != null) {
            Time keepAlive = initialRequest.pit() == null || initialRequest.pit().keepAlive() == null
                    ? Time.of(t -> t.time("6000s")) : initialRequest.pit().keepAlive();
            next.pit(p -> p.id(pitId).keepAlive(keepAlive));
        } else {
            next.index(initialRequest.index());
        }
        fetch(next.build());
        return !exhausted;
    }

    @Override
    public Hit<T> next() {
        if (!hasNext()) throw new NoSuchElementException();
        iterAll++;
        returned++;
        return hits.get(iter++);
    }

    String getPitId() { return pitId; }

    @Override
    public void close() throws IOException {
        if (!closed) {
            closed = true;
            if (pitId != null) client.closePointInTime(r -> r.id(pitId));
        }
    }
}
