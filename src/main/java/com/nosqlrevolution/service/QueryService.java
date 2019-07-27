package com.nosqlrevolution.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nosqlrevolution.WriteOperation;
import com.nosqlrevolution.query.Query;
import com.nosqlrevolution.util.JsonUtil;
import com.nosqlrevolution.util.QueryUtil;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.admin.indices.exists.types.TypesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.types.TypesExistsResponse;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.count.CountRequestBuilder;
import org.elasticsearch.action.count.CountResponse;
import org.elasticsearch.action.delete.DeleteRequestBuilder;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.*;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequestBuilder;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
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
        return getSingle(null, index, type);
    }
    
    /**
     * Return the first document from this index and type
     * 
     * @param query
     * @param index
     * @param type
     * @return 
     */
    public String getSingle(Query query, String index, String type) {
        SearchRequestBuilder builder = client.prepareSearch(index)
                .setSearchType(SearchType.DEFAULT)
                .setTypes(type)
                .setFrom(0)
                .setSize(1);

        Query q = QueryResolverService.resolve(query);
        if (q != null) {
            builder.setQuery(q.getQueryBuilder());
            if (query.getHighlights() != null) {
                for (String s: query.getHighlights()) {
                    builder.addHighlightedField(s);
                }
            }
        } else {
            builder.setQuery(QueryUtil.getMatchAllQuery());
        }
        
        try {
            SearchResponse response = builder.execute().actionGet();

            // Update the SearchQuery results
            SearchHits h = response.getHits();
            if (h.getHits().length > 0) {
                return h.getHits()[0].getSourceAsString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Return the all of the documents from this index and type
     * 
     * @param index
     * @param type
     * @return 
     */
    public SearchRequestBuilder findAllScroll(String index, String type) {
        return findAll(null, index, type, true);
    }
    
    /**
     * Return the all of the documents from this index and type
     * 
     * @param query
     * @param index
     * @param type
     * @return 
     */
    public SearchRequestBuilder findAllScroll(Query query, String index, String type) {
        return findAll(query, index, type, true);
    }
    
    /**
     * Return the all of the documents from this index and type
     * 
     * @param index
     * @param type
     * @return 
     */
    public SearchRequestBuilder findAll(String index, String type) {
        return findAll(null, index, type, false);
    }
    
    /**
     * Return the all of the documents from this index and type
     * 
     * @param query
     * @param index
     * @param type
     * @param scroll
     * @return 
     */
    public SearchRequestBuilder findAll(Query query, String index, String type, boolean scroll) {
        SearchRequestBuilder builder = client.prepareSearch(index)
                .setSearchType(SearchType.QUERY_THEN_FETCH)
                .setTypes(type)
                .setFrom(0)
                .setSize(100);

        // Set parameters if we're performing a scroll.
        if (scroll) {
            builder.setSearchType(SearchType.SCAN);
            builder.setScroll(new TimeValue(600000));
            builder.setSize(1000);
        }
        
        Query qb = QueryResolverService.resolve(query);
        if (qb != null) {
            builder.setQuery(qb.getQueryBuilder());
            if (query.getHighlights() != null) {
                for (String s: query.getHighlights()) {
                    builder.addHighlightedField(s);
                }
            }
        } else {
            builder.setQuery(QueryUtil.getMatchAllQuery());
        }
        
        return builder;
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
                .add(index, type, ids)
                .setRealtime(Boolean.TRUE);
        
        MultiGetResponse response = builder.execute().actionGet();
        MultiGetItemResponse[] responses = response.getResponses();
        
        String[] out = new String[responses.length];
        for (int i=0; i<responses.length; i++) {
            if (responses[i].getResponse() != null) {
                out[i] = responses[i].getResponse().getSourceAsString();
            }
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
        return count(null, indexes, types);
    }

    /**
     * Return a count of all documents from this index and type
     * 
     * @param index
     * @param type
     * @return 
     */
    public long count(String index, String type) {
        return count(null, index, type);
    }
    
    /**
     * Return a count of all documents from many indexes and types
     * 
     * @param query
     * @param indexes
     * @param types
     * @return 
     */
    public long count(Query query, String[] indexes, String[] types) {
        CountRequestBuilder builder = client.prepareCount()
                .setIndices(indexes)
                .setTypes(types);
        
        Query qb = QueryResolverService.resolve(query);
        if (qb != null) {
            builder.setQuery(qb.getQueryBuilder());
        } else {
            builder.setQuery(QueryUtil.getMatchAllQuery());
        }
        
        CountResponse response = builder.execute().actionGet();
        return response.getCount();
    }

    /**
     * Return a count of all documents from this index and type
     * 
     * @param query
     * @param index
     * @param type
     * @return 
     */
    public long count(Query query, String index, String type) {
        CountRequestBuilder builder = client.prepareCount()
                .setIndices(index)
                .setTypes(type);
        
        Query qb = QueryResolverService.resolve(query);
        if (qb != null) {
            builder.setQuery(qb.getQueryBuilder());
        } else {
            builder.setQuery(QueryUtil.getMatchAllQuery());
        }
        
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
    public void bulkIndex(String index, String type, String[] source, String idField) {
        bulkIndex(DEFAULT_WRITE, index, type, source, idField);
    }
    /**
     * Index a number of documents using the bulk indexing api
     * 
     * @param write
     * @param index
     * @param type
     * @param source 
     * @param idField 
     */
    public void bulkIndex(WriteOperation write, String index, String type, String[] source, String idField) {
        BulkRequestBuilder builder = client.prepareBulk();

        for (String json: source) {
            builder.add(client.prepareIndex(index, type)
                    // TODO: need to pass in an id field if available.
                    .setId(JsonUtil.getId(json, idField))
                    .setSource(JsonUtil.getSource(json, json))

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
        return response.isFound();
        // TODO: Could return if found.
        //response.notFound();
    }
    
    // Need to implement as a bulk operation.
    
//    /**
//     * Delete all documents as DeleteByQuery
//     * Passes in a default WriteOperation
//     * 
//     * @param index
//     * @param type
//     * @param ids
//     * @return 
//     */
//    public boolean deleteAll(String index, String type, String[] ids) {
//        return deleteAll(DEFAULT_WRITE, index, type, ids);
//    }
//    /**
//     * Delete all documents as DeleteByQuery
//     * 
//     * @param write
//     * @param index
//     * @param type
//     * @param id
//     * @return 
//     */
//    public boolean deleteAll(WriteOperation write, String index, String type, String[] id) {
//        DeleteByQueryRequestBuilder builder = client.prepareDeleteByQuery()
//                .setIndices(index)
//                .setTypes(type)
//                .setQuery(QueryUtil.getIdQuery(id))
//                
//                // Operations from WriteOperation
//                .setConsistencyLevel(write.getConsistencyLevel());
//        
//                // TODO: test no refresh for delete by query
//                //.setRefresh(write.getRefresh());
//        
//        DeleteByQueryResponse response = builder.execute().actionGet();
//        
//        // TODO: Need to iterate through and return a response
//        return true;
//    }
    
    /**
     * Refresh the given index.
     * 
     * @param index 
     */
    public void refresh(String index) {
        client.admin().indices().refresh(new RefreshRequest(index)).actionGet();
    }

    /**
     * Execute a SearchRequestBuilder and get a SearchResponse.
     * 
     * @param builder
     * @return 
     */
    public SearchHits executeBuilder(SearchRequestBuilder builder) {
        try {
            SearchResponse response = builder.execute().actionGet();
            if (response != null) {
                return response.getHits();
            }
        } catch (Exception e) {}

        return null;
    }
    
    /**
     * Execute a SearchRequestBuilder and get a SearchResponse.
     * 
     * @param builder
     * @return 
     */
    public SearchScrollRequestBuilder executeScroll(SearchRequestBuilder builder) {
        try {
            if (builder.request().searchType() == SearchType.SCAN) {
                SearchResponse response = builder.execute().actionGet();
                return client.prepareSearchScroll(response.getScrollId()).setScroll(new TimeValue(600000));
            } else {
                throw new Exception("SearchRequest must be of type scan.");
            }
        } catch (Exception e) {}

        return null;
    }
    
    /**
     * Check to see if indeces or types exists in the ElasticSearch cluster.
     * 
     * @param index
     * @param type
     * @return 
     */
    public boolean exists(String index, String type) {
        if (type == null) {
            IndicesExistsRequest indicesRequest = new IndicesExistsRequest(index);
            IndicesExistsResponse response = client.admin().indices().exists(indicesRequest).actionGet();
            return response.isExists();
        } else {
            String[] indices = new String[] {index};
            TypesExistsRequest typesRequest = new TypesExistsRequest(indices, type);
            TypesExistsResponse response = client.admin().indices().typesExists(typesRequest).actionGet();
            return response.isExists();
        }
    }
}
