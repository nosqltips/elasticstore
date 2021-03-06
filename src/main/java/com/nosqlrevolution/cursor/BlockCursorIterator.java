package com.nosqlrevolution.cursor;

import com.nosqlrevolution.apps.ExportModel;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

/**
 * This iterator can go back to the search engine and iterate through blocks of data returned from ES.
 * 
 * @author cbrown
 * @param <E>
 */
public class BlockCursorIterator<E> extends CursorIterator<E> {
    private final SearchRequestBuilder builder;
    private int from = 0;
    private int size = 0;
    
    protected BlockCursorIterator(Class<E> e, SearchRequestBuilder builder, int from, int size) {
        this.e = e;
        this.builder = builder;
        this.from = from;
        this.size = size;
        hits = getNextPage();
        totalSize = hits.getTotalHits() != null ? (int) hits.getTotalHits().value : 0;
    }
    
    @Override
    public boolean hasNext() {
        return iterAll < totalSize;
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
        SearchHit hit = hits.getAt(iter);
        String source = hit.getSourceAsString();
        E returnE;
        
        if (e == ExportModel.class) {
            returnE = (E) new ExportModel()
                    .setId(hit.getId())
                    .setJson(source);
        } else {
            returnE = mapping.get(source, e);
        }

        iter ++;
        iterAll ++;
        return returnE;
    }
    
    private SearchHits getNextPage() {
        // increment next page
        builder.setFrom(from * size);
        builder.setSize(size);
        
        // Get search response
        SearchResponse response = builder.execute().actionGet();
        from ++;

        // Return the next set of hits
        return response.getHits();
    }
}
