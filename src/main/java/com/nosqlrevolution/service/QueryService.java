package com.nosqlrevolution.service;

import com.google.common.collect.Lists;
import com.nosqlrevolution.WriteOperation;
import com.nosqlrevolution.util.JsonUtil;
import com.nosqlrevolution.util.QueryUtil;
import org.codehaus.jackson.map.ObjectMapper;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.count.CountRequestBuilder;
import org.elasticsearch.action.count.CountResponse;
import org.elasticsearch.action.delete.DeleteRequestBuilder;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.deletebyquery.DeleteByQueryRequestBuilder;
import org.elasticsearch.action.deletebyquery.DeleteByQueryResponse;
import org.elasticsearch.action.get.*;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.search.SearchHits;

/**
 *
 * @author cbrown
 */
public class QueryService {
    private final Client client;
    private final ObjectMapper mapper = new ObjectMapper();
    private final WriteOperation DEFAULT_WRITE = new WriteOperation();
    
    public QueryService(Client client) {
        this.client = client;
    }
    
    /**
     * Return the first document from this index and type
     * 
     * @param index
     * @param type
     * @return 
     */
    public String getSingle(String index, String type) {
        SearchRequestBuilder builder = client.prepareSearch(index)
                .setSearchType(SearchType.QUERY_THEN_FETCH)
                .setTypes(type)
                .setFilter(QueryUtil.getMatchAllFilter())
                .setFrom(0)
                .setSize(1);
        
        SearchResponse response = builder.execute().actionGet();
        
        // Update the SearchQuery results
        SearchHits h = response.getHits();
        if (h.getHits().length > 0) {
            return h.getHits()[0].getSourceAsString();
        } else {
            return null;
        }
    }
    
    /**
     * Return a single document using the rest time get api
     * 
     * @param index
     * @param type
     * @param id
     * @return 
     */
    public String realTimeGet(String index, String type, String id) {
        GetRequestBuilder builder = client.prepareGet()
                .setIndex(index)
                .setType(type)
                .setId(id)
                .setRealtime(Boolean.TRUE);
        
        GetResponse response = builder.execute().actionGet();
        return response.getSourceAsString();
    }

    /**
     * Return a list of documents using the real time get api
     * 
     * @param index
     * @param type
     * @param ids
     * @return 
     */
    public String[] realTimeMultiGet(String index, String type, String[] ids) {
        MultiGetRequestBuilder builder = client.prepareMultiGet()
                .add(index, type, Lists.asList(ids[0], ids))
                .setRealtime(Boolean.TRUE);
        
        MultiGetResponse response = builder.execute().actionGet();
        MultiGetItemResponse[] responses = response.getResponses();
        
        String[] out = new String[responses.length];
        for (int i=0; i<responses.length; i++) {
            out[i] = responses[i].getResponse().getSourceAsString();
        }
        return out;
    }
    
    /**
     * Return a count of all documents from many indexes and types
     * 
     * @param indexes
     * @param types
     * @return 
     */
    public long count(String[] indexes, String[] types) {
        CountRequestBuilder builder = client.prepareCount()
                .setIndices(indexes)
                .setTypes(types);
        
        CountResponse response = builder.execute().actionGet();
        return response.getCount();
    }

    /**
     * Return a count of all documents from this index and type
     * 
     * @param index
     * @param type
     * @return 
     */
    public long count(String index, String type) {
        CountRequestBuilder builder = client.prepareCount()
                .setIndices(index)
                .setTypes(type);
        
        CountResponse response = builder.execute().actionGet();
        return response.getCount();
    }

    /**
     * Index a single document using the standard indexing api
     * Passes in a default WriteOperation
     * 
     * @param index
     * @param type
     * @param source
     * @param id 
     */
    public void index(String index, String type, String source, String id) {
        index(DEFAULT_WRITE, index, type, source, id);
    }
    /**
     * Index a single document using the standard indexing api
     * 
     * @param write
     * @param index
     * @param type
     * @param source
     * @param id 
     */
    public void index(WriteOperation write, String index, String type, String source, String id) {
        IndexRequestBuilder builder = client.prepareIndex()
                .setIndex(index)
                .setType(type)
                .setSource(source)
                .setId(id)
                
                // Operations from WriteOperation
                .setConsistencyLevel(write.getConsistencyLevel())
                .setRefresh(write.getRefresh());
        
        IndexResponse response = builder.execute().actionGet();

        // TODO: could return version of the object
        //response.version();

        // TODO: could return id of the object
        //response.id();
    }

    /**
     * Index a number of documents using the bulk indexing api
     * Passes in a default WriteOperation
     * 
     * @param index
     * @param type
     * @param source 
     */
    public void bulkIndex(String index, String type, String[] source) {
        bulkIndex(DEFAULT_WRITE, index, type, source);
    }
    /**
     * Index a number of documents using the bulk indexing api
     * 
     * @param write
     * @param index
     * @param type
     * @param source 
     */
    public void bulkIndex(WriteOperation write, String index, String type, String[] source) {
        BulkRequestBuilder builder = client.prepareBulk();

        for (String json: source) {
            builder.add(client.prepareIndex(index, type)
                    // TODO: need to pass in an id field if available.
                .setId(JsonUtil.getId(json, null))
                .setSource(json)
                                    
                // Operations from WriteOperation
                .setConsistencyLevel(write.getConsistencyLevel())
                .setRefresh(write.getRefresh())
                );
        }
        
        BulkResponse response = builder.execute().actionGet();
        
        // TODO: Need to return failures at some point
    }
    
    /**
     * Delete a single document
     * Passes in a default WriteOperation
     * 
     * @param index
     * @param type
     * @param id
     * @return 
     */
    public boolean delete(String index, String type, String id) {
        return delete(DEFAULT_WRITE, index, type, id);
    }
    /**
     * Delete a single document
     *
     * @param write
     * @param index
     * @param type
     * @param id
     * @return 
     */
    public boolean delete(WriteOperation write, String index, String type, String id) {
        DeleteRequestBuilder builder = client.prepareDelete()
                .setIndex(index)
                .setType(type)
                .setId(id)
                
                // Operations from WriteOperation
                .setConsistencyLevel(write.getConsistencyLevel())
                .setRefresh(write.getRefresh());
        
        DeleteResponse response = builder.execute().actionGet();
        return ! response.isNotFound();
        // TODO: Could return if found.
        //response.notFound();
    }
    
    /**
     * Delete all documents as DeleteByQuery
     * Passes in a default WriteOperation
     * 
     * @param index
     * @param type
     * @param id
     * @return 
     */
    public boolean deleteAll(String index, String type, String[] ids) {
        return deleteAll(DEFAULT_WRITE, index, type, ids);
    }
    /**
     * Delete all documents as DeleteByQuery
     * 
     * @param write
     * @param index
     * @param type
     * @param id
     * @return 
     */
    public boolean deleteAll(WriteOperation write, String index, String type, String[] id) {
        DeleteByQueryRequestBuilder builder = client.prepareDeleteByQuery()
                .setIndices(index)
                .setTypes(type)
                .setQuery(QueryUtil.getIdQuery(id))
                
                // Operations from WriteOperation
                .setConsistencyLevel(write.getConsistencyLevel());
        
                // TODO: test no refresh for delete by query
                //.setRefresh(write.getRefresh());
        
        DeleteByQueryResponse response = builder.execute().actionGet();
        
        // TODO: Need to iterate through and return a response
        return true;
    }
    
    /**
     * Refresh the given index.
     * 
     * @param index 
     */
    public void refresh(String index) {
        client.admin().indices().refresh(new RefreshRequest(index)).actionGet();
    }
}
