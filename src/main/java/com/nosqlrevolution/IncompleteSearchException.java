package com.nosqlrevolution;

import co.elastic.clients.elasticsearch.core.SearchResponse;

/** A search response that must not be presented as a complete result. */
public class IncompleteSearchException extends IllegalStateException {
    private final SearchResponse<?> response;

    public IncompleteSearchException(String message, SearchResponse<?> response) {
        super(message);
        this.response = java.util.Objects.requireNonNull(response, "Search response");
    }

    /** Retains diagnostics and the latest PIT ID for callers that own a native-search PIT. */
    public SearchResponse<?> getResponse() { return response; }
}
