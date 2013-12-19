package com.nosqlrevolution;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequestBuilder;
import org.elasticsearch.search.SearchHits;

/**
 * This iterator can go back to the search engine and iterate through blocks of data returned from ES.
 * 
 * @author cbrown
 * @param <E>
 */
public class ScrollCursorIterator<E> extends CursorIterator<E> {
    private final SearchScrollRequestBuilder scrollBuilder;
    private final int totalSize;
    private boolean hasNext = true;
    
    protected ScrollCursorIterator(Class<E> e, SearchScrollRequestBuilder scrollBuilder, int totalSize) {
        this.e = e;
        this.scrollBuilder = scrollBuilder;
        this.totalSize = totalSize;
        hits = getNextPage();
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
    public E next() {
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
        E returnE = mapping.get(hits.getAt(iter).sourceAsString(), e);
        iter ++;
        iterAll ++;
        return returnE;
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
