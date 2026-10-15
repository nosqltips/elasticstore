package com.nosqlrevolution.util;

import co.elastic.clients.elasticsearch._types.ShardStatistics;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.nosqlrevolution.IncompleteSearchException;

/** Do not publish partial responses as complete documents or counts. */
public final class ReadIntegrity {
    private ReadIntegrity() { }

    public static void search(SearchResponse<?> response) {
        if (response.timedOut()) throw new IncompleteSearchException("Elasticsearch search timed out; results may be incomplete", response);
        if (Boolean.TRUE.equals(response.terminatedEarly())) {
            throw new IncompleteSearchException("Elasticsearch search terminated early; results may be incomplete", response);
        }
        try { shards(response.shards(), "search"); }
        catch (IllegalStateException failure) { throw new IncompleteSearchException(failure.getMessage(), response); }
    }

    public static void shards(ShardStatistics shards, String operation) {
        java.util.Objects.requireNonNull(shards, "Shard statistics");
        if (shards.failed().longValue() != 0 || !shards.failures().isEmpty()) {
            throw new IllegalStateException("Elasticsearch " + operation + " returned shard failures; results may be incomplete");
        }
    }
}
