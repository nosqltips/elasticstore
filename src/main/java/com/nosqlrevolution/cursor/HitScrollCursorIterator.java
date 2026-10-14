package com.nosqlrevolution.cursor;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.HitsMetadata;
import co.elastic.clients.elasticsearch.core.ClosePointInTimeRequest;
import co.elastic.clients.elasticsearch.core.ClosePointInTimeResponse;
import co.elastic.clients.elasticsearch.core.search.PointInTimeReference;
import co.elastic.clients.json.JsonData;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This iterator can go back to the search engine and iterate through blocks of data returned from ES.
 * 
 * @author cbrown
 */
public class HitScrollCursorIterator extends CursorIterator<Hit<JsonData>> {
    private static final Logger LOGGER = Logger.getLogger(HitScrollCursorIterator.class.getName());
    private final SearchRequest initialRequest;
    private final ElasticsearchClient client;
    private boolean hasNext = true;
    private String pitId = null;
    private List<String> searchAfter = null;
    
    protected HitScrollCursorIterator(SearchRequest initialRequest, ElasticsearchClient client) {
        this.initialRequest = initialRequest;
        this.client = client;
        HitsMetadata<JsonData> initial = getInitialPage();
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
    public Hit<JsonData> next() {
        if (!hasNext()) throw new java.util.NoSuchElementException();
        Hit<JsonData> hit = hits.get(iter++);
        iterAll++;
        return hit;
    }

    private HitsMetadata<JsonData> getInitialPage() {
        try {
            System.out.println("Getting initialPage " + initialRequest);
            // Get search response
            SearchResponse response = client.search(initialRequest, JsonData.class);
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

    private HitsMetadata<JsonData> getNextPage() {
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
            
//            System.out.println("Getting nextPage " + request);
            // Get search response
            SearchResponse response = client.search(request, JsonData.class);
            System.out.println("Getting nextPage " + response.hits().hits().size() + " after=" + searchAfter);
            
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

    /**
     * Clear the scroll context
     */
    public void clearScrollContext() {
        ClosePointInTimeRequest request = ClosePointInTimeRequest.of(b -> b.id(pitId));
        System.out.println("clearScrollContext");
        try {
            ClosePointInTimeResponse response = client.closePointInTime(request);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        } catch (ElasticsearchException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    public String getPitId() {
        return pitId;
    }
}
