package com.nosqlrevolution;

import java.util.Iterator;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequestBuilder;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;

/**
 * Used to automatically scroll through a set of search results 
 * using the ElasticSearch scroll search type.
 * 
 * @author cbrown
 * @param <E>
 */
public class ScrollCursor<E> extends Cursor<E> {
    private final SearchScrollRequestBuilder scrollBuilder;
    private final int totalSize;
    
    public ScrollCursor(Class<E> e, SearchRequestBuilder searchBuilder, Client client) throws Exception {
        // TODO: may need to check for null here.
        this.e = e;
        if (searchBuilder.request().searchType() == SearchType.SCAN) {
            SearchResponse response = searchBuilder.execute().actionGet();
            hits = response.getHits();
            totalSize = (int)hits.getTotalHits();

            this.scrollBuilder = client.prepareSearchScroll(response.getScrollId())
                    .setScroll(new TimeValue(600000));
        } else {
            throw new Exception("SearchRequest must be of type scan.");
        }
    }

    @Override
    public Iterator<E> iterator() {
        return new ScrollCursorIterator(e, scrollBuilder, totalSize);
    }
}
