package com.nosqlrevolution.cursor;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

/**
 * This iterator can go back to the search engine and iterate through blocks of data returned from ES.
 * 
 * @author cbrown
 */
public class HitScrollCursorIterator extends CursorIterator<SearchHit> {
    private static final Logger LOGGER = Logger.getLogger(HitScrollCursorIterator.class.getName());
    private final SearchRequest initialRequest;
    private final RestHighLevelClient restClient;
    private boolean hasNext = true;
    private String scrollId = null;
    
    protected HitScrollCursorIterator(SearchRequest initialRequest, RestHighLevelClient restClient) {
        this.initialRequest = initialRequest;
        this.restClient = restClient;
        hits = getInitialPage();
        totalSize = hits.getTotalHits() != null ? (int) hits.getTotalHits().value : 0;
    }
    
    @Override
    public boolean hasNext() {
        if (! hasNext) { 
            return false;
        } else {
            return iterAll < totalSize;
        }
    }

    @Override
    public SearchHit next() {
        // Need to check if we need to get the next block of data.
        if (iter >= hits.getHits().length) {
            hits = getNextPage();
            iter = 0;
            
            // TODO: What to do if we get a call for next and we don't have any results?
            if (hits.getHits().length == 0) {
                return null;
            }
        }
        
        // Return the next object
        SearchHit hit = hits.getAt(iter);
       
        iter ++;
        iterAll ++;
        
        return hit;
    }

    private SearchHits getInitialPage() {
        try {
            // Get search response
            SearchResponse response = restClient.search(initialRequest, RequestOptions.DEFAULT);
            scrollId = response.getScrollId();
            
            if (response.getHits().getHits().length == 0) {
                hasNext = false;
            }
            
            // Return the next set of hits
            return response.getHits();
        } catch (IOException ie) {
            LOGGER.log(Level.SEVERE, null, ie);
        }
        
        return null;
    }

    private SearchHits getNextPage() {
        try {
            SearchScrollRequest scrollRequst = new SearchScrollRequest(scrollId);
            scrollRequst.scroll(initialRequest.scroll());
            
            // Get search response
            SearchResponse response = restClient.search(initialRequest, RequestOptions.DEFAULT);
            scrollId = response.getScrollId();
            
            if (response.getHits().getHits().length == 0) {
                hasNext = false;
            }
            
            // Return the next set of hits
            return response.getHits();
        } catch (IOException ie) {
            LOGGER.log(Level.SEVERE, null, ie);
        }
        
        return null;
    }

    public String getScrollId() {
        return scrollId;
    }
}
