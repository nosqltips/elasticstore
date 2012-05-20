package com.nosqlrevolution.service;

import com.google.common.collect.Lists;
import com.nosqlrevolution.util.QueryUtil;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
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
    // TODO should these be final?
    private Client client;
    private ObjectMapper mapper = new ObjectMapper();
    
    public QueryService(Client client) {
        this.client = client;
    }
    
    public String getSingle(String index, String type) {
        SearchRequestBuilder builder = client.prepareSearch(index)
                .setSearchType(SearchType.QUERY_THEN_FETCH)
                .setTypes(type)
                .setFilter(QueryUtil.getMatchAllFilter())
                .setFrom(0)
                .setSize(1);
        
        SearchResponse response = builder.execute().actionGet();
        
        // Update the SearchQuery results
        SearchHits h = response.hits();
        if (h.getHits().length > 0) {
            return h.getHits()[0].sourceAsString();
        } else {
            return null;
        }
    }
    
    public String realTimeGet(String index, String type, String id) {
        GetRequestBuilder builder = client.prepareGet()
                .setIndex(index)
                .setType(type)
                .setId(id)
                .setRealtime(Boolean.TRUE);
        
        GetResponse response = builder.execute().actionGet();
        return response.sourceAsString();
    }

    public String[] realTimeMultiGet(String index, String type, String[] ids) {
        MultiGetRequestBuilder builder = client.prepareMultiGet()
                .add(index, type, Lists.asList(ids[0], ids))
                .setRealtime(Boolean.TRUE);
        
        MultiGetResponse response = builder.execute().actionGet();
        MultiGetItemResponse[] responses = response.responses();
        
        String[] out = new String[responses.length];
        for (int i=0; i<responses.length; i++) {
            out[i] = responses[i].getResponse().sourceAsString();
        }
        return out;
    }
    
    public long count(String[] indexes, String[] types) {
        CountRequestBuilder builder = client.prepareCount()
                .setIndices(indexes)
                .setTypes(types);
        
        CountResponse response = builder.execute().actionGet();
        return response.getCount();
    }

    public long count(String index, String type) {
        CountRequestBuilder builder = client.prepareCount()
                .setIndices(index)
                .setTypes(type);
        
        CountResponse response = builder.execute().actionGet();
        return response.getCount();
    }

    public void index(String index, String type, String source, String id) {
        IndexRequestBuilder builder = client.prepareIndex()
                .setIndex(index)
                .setType(type)
                .setSource(source)
                .setId(id);
                // TODO: could set consistency level
                
                // TODO: possible option for realtime search
                //.setRefresh(true)
        
        IndexResponse response = builder.execute().actionGet();

        // TODO: could return version of the object
        //response.version();

        // TODO: could return id of the object
        //response.id();
}

    public void bulkIndex(String index, String type, String[] source) {
        BulkRequestBuilder builder = client.prepareBulk();
                // TODO: could set consistency level
                //.setConsistencyLevel(WriteConsistencyLevel.DEFAULT)
                
                // TODO: possible option for realtime search
                //.setRefresh(true)

        // Maybe should try to pull out _id or id
        for (String json: source) {
            builder.add(client.prepareIndex(index, type)
                .setId(grabId(json))
                .setSource(json)
                );
        }
        
        BulkResponse response = builder.execute().actionGet();
        
        // TODO: Need to return failures at some point
    }
    
    public boolean delete(String index, String type, String id) {
        DeleteRequestBuilder builder = client.prepareDelete()
                .setIndex(index)
                .setType(type)
                .setId(id);
                
                // TODO: can set these as some point
                //.setRefresh()
                //.setOperationThreaded()
                //.setConsistencyLevel()
        
        DeleteResponse response = builder.execute().actionGet();
        return ! response.notFound();
        // TODO: Could return if found.
        //response.notFound();
    }
    
    
    public boolean deleteAll(String index, String type, String[] id) {
        DeleteByQueryRequestBuilder builder = client.prepareDeleteByQuery()
                .setIndices(index)
                .setTypes(type)
                .setQuery(QueryUtil.getIdQuery(id));
                
                // TODO: can set these as some point
                //.setRefresh()
                //.setOperationThreaded()
                //.setConsistencyLevel()
        
        DeleteByQueryResponse response = builder.execute().actionGet();
        
        // TODO: Need to iterate through and return a response
        return true;
    }

    private String grabId(String json) {
        try {
            JsonNode rootNode = mapper.readValue(json, JsonNode.class);
            String id = rootNode.findValue("id").getTextValue();
            if (id == null) {
                id = rootNode.findValue("_id").getTextValue();
            }
            return id;
        } catch (IOException ex) {
            Logger.getLogger(QueryService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}
