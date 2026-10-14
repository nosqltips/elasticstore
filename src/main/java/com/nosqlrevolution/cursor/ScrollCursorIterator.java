package com.nosqlrevolution.cursor;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.HitsMetadata;
import co.elastic.clients.elasticsearch.core.search.PointInTimeReference;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nosqlrevolution.apps.ExportModel;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This iterator can go back to the search engine and iterate through blocks of data returned from ES.
 * 
 * @author cbrown
 * @param <E>
 */
public class ScrollCursorIterator<E> extends CursorIterator<Hit<E>> {
    private static final Logger LOGGER = Logger.getLogger(ScrollCursorIterator.class.getName());
    private static final ObjectMapper MAPPER = new JacksonJsonpMapper().objectMapper();
    private final SearchRequest initialRequest;
    private final ElasticsearchClient client;
    private boolean hasNext = true;
    private String pitId = null;
    private List<String> searchAfter = null;

    protected ScrollCursorIterator(Class<Hit<E>> e, SearchRequest initialRequest, ElasticsearchClient client) {
        this.e = e;
        this.initialRequest = initialRequest;
        this.client = client;
        HitsMetadata<E> initial = getInitialPage();
        hits = initial.hits();
        totalSize = initial.total() != null ? (int) initial.total().value() : 0;
    }
    
    @Override
    public boolean hasNext() {
        if (iter < hits.size()) return true;
        if (!hasNext) return false;
        hits = getNextPage().hits();
        iter = 0;
        return !hits.isEmpty();
    }

    @Override
    public Hit<E> next() {
        if (!hasNext()) throw new java.util.NoSuchElementException();
        Hit<E> hit = hits.get(iter++);
        iterAll++;
        return hit;
    }

    private HitsMetadata<E> getInitialPage() {
        try {
            // Initialize the scroll
            initialRequest.pit();
            
            // Get search response
            SearchResponse response = client.search(initialRequest, e.getClass());
            pitId = response.pitId();
            searchAfter = getSearchAfter(response);
            
            System.out.println("pitId=" + pitId);
            if (response.hits().hits().isEmpty()) {
                hasNext = false;
            }
            
            // Return the next set of hits
            return response.hits();
        } catch (IOException ie) {
            throw new java.io.UncheckedIOException("Elasticsearch paging failed", ie);
        }

    }
    
    private HitsMetadata<E> getNextPage() {
        try {
            SearchRequest request = SearchRequest.of(r -> r
                .size(10000)
                .query(initialRequest.query())
                .highlight(initialRequest.highlight())
                .sort(initialRequest.sort())
                .pit(PointInTimeReference.of(p -> p.
                        id(pitId)))
                .searchAfter(searchAfter)
            );
            
            // Get search response
            SearchResponse response = client.search(request, e.getClass());
            pitId = response.pitId();
            
            if (response.hits().hits().isEmpty()) {
                hasNext = false;
            } else {
                pitId = response.pitId();
                searchAfter = getSearchAfter(response);
            }
            
            // Return the next set of hits
            return response.hits();
        } catch (IOException ie) {
            throw new java.io.UncheckedIOException("Elasticsearch paging failed", ie);
        }

    }

        private List<String> getSearchAfter(SearchResponse response) {
        List<Hit<JsonData>> h = response.hits().hits();
        if (h.isEmpty()) return null;
        Hit<JsonData> hit = h.get(h.size() -1);
        return hit.sort();
    }


    public String getPitId() {
        return pitId;
    }
}
