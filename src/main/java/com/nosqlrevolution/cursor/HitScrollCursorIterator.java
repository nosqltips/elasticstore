package com.nosqlrevolution.cursor;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequestBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

/**
 * This iterator can go back to the search engine and iterate through blocks of data returned from ES.
 * 
 * @author cbrown
 */
public class HitScrollCursorIterator extends CursorIterator<SearchHit> {
    private final SearchScrollRequestBuilder scrollBuilder;
    private boolean hasNext = true;
    
    protected HitScrollCursorIterator(SearchScrollRequestBuilder scrollBuilder) {
        this.scrollBuilder = scrollBuilder;
        hits = getNextPage();
        totalSize = (int)hits.getTotalHits();
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

    private SearchHits getNextPage() {
        // Get search response
        SearchResponse response = scrollBuilder.execute().actionGet();

        if (response.getHits().getHits().length == 0) {
            hasNext = false;
        }
        // Return the next set of hits
        return response.getHits();
    }
}
