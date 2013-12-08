package com.nosqlrevolution;

import java.util.AbstractCollection;
import java.util.Collection;
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
public class ScrollCursor<E> extends AbstractCollection {
    private final E e;
    private final SearchScrollRequestBuilder scrollBuilder;
    private final int totalSize;
    
    public ScrollCursor(E e, SearchScrollRequestBuilder scrollBuilder, int totalSize) {
        // TODO: may need to check for null here.
        this.e = e;
        this.scrollBuilder = scrollBuilder;
        this.totalSize = totalSize;
    }
    
    public ScrollCursor(E e, SearchRequestBuilder searchBuilder, Client client) throws Exception {
        // TODO: may need to check for null here.
        this.e = e;
        if (searchBuilder.request().searchType() == SearchType.SCAN) {
            SearchResponse response = searchBuilder.execute().actionGet();
            totalSize = (int)response.getHits().getTotalHits();

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

    public Collection<E> collection() {
        return new ScrollCursorCollection(e, scrollBuilder, totalSize);
    }

    @Override
    public int size() {
        return totalSize;
    }
}
