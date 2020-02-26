package com.nosqlrevolution.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nosqlrevolution.query.Query;
import com.nosqlrevolution.util.JsonUtil;
import com.nosqlrevolution.util.QueryUtil;
import java.io.IOException;
import static org.elasticsearch.action.DocWriteResponse.Result.DELETED;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.*;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;

/**
 *
 * @author cbrown
 */
public class QueryService {
    private final RestHighLevelClient restClient;
    private final ObjectMapper MAPPER = new ObjectMapper();
    
    public QueryService(RestHighLevelClient restClient) {
        this.restClient = restClient;
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
        SearchSourceBuilder builder = new SearchSourceBuilder()
                .from(0)
                .size(1)
                .trackTotalHits(true);

        Query q = QueryResolverService.resolve(query);
        if (q != null) {
            builder.query(q.getQueryBuilder());
            if (q.getFields() != null) {
                builder.fetchSource(q.getFields(), null);
            }
            if (query.getHighlights() != null) {
                HighlightBuilder highlightBuilder = new HighlightBuilder(); 
                for (String s: query.getHighlights()) {
                    HighlightBuilder.Field highlightField = new HighlightBuilder.Field(s); 
                    highlightBuilder.field(highlightField);
                }
                builder.highlighter(highlightBuilder);
            }
        } else {
            builder.query(QueryUtil.getMatchAllQuery());
        }

        SearchRequest request = new SearchRequest(index)
                .searchType(SearchType.DEFAULT)
                .source(builder);
        
        try {
            SearchResponse response = restClient.search(request, RequestOptions.DEFAULT);

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
    public SearchRequest findAllScroll(String index, String type) {
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
    public SearchRequest findAllScroll(Query query, String index, String type) {
        return findAll(query, index, type, true);
    }
    
    /**
     * Return the all of the documents from this index and type
     * 
     * @param index
     * @param type
     * @return 
     */
    public SearchRequest findAll(String index, String type) {
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
    public SearchRequest findAll(Query query, String index, String type, boolean scroll) {
        SearchSourceBuilder builder = new SearchSourceBuilder();
        
        Query q = QueryResolverService.resolve(query);
        if (q != null) {
            builder.query(q.getQueryBuilder())
                    .from(0)
                    .size(10000)
                    .trackTotalHits(true);
            if (q.getFields() != null) {
                builder.fetchSource(q.getFields(), null);
            }
            if (query.getHighlights() != null) {
                HighlightBuilder highlightBuilder = new HighlightBuilder(); 
                for (String s: query.getHighlights()) {
                    HighlightBuilder.Field highlightField = new HighlightBuilder.Field(s); 
                    highlightBuilder.field(highlightField);
                }
                builder.highlighter(highlightBuilder);
            }
        } else {
            builder.query(QueryUtil.getMatchAllQuery());
        }

        SearchRequest request = new SearchRequest(index)
                .searchType(SearchType.DEFAULT)
                .source(builder);

        if (scroll) {
            request.scroll(new TimeValue(600000));
        }
        
        return request;
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
        GetRequest getRequest = new GetRequest()
                .index(index)
                .id(id)
                .realtime(Boolean.TRUE);        
        try {
            GetResponse response = restClient.get(getRequest, RequestOptions.DEFAULT);
            return response.getSourceAsString();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        return null;
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
        MultiGetRequest multiRequest = null;
        try {
            multiRequest = new MultiGetRequest()
                    .add(index, type, ids, new FetchSourceContext(false), null, null, false)
                    .realtime(Boolean.TRUE);
        } catch (IOException ie) {
            ie.printStackTrace();
        }

        try {
            MultiGetResponse response = restClient.multiGet(multiRequest, RequestOptions.DEFAULT);
            MultiGetItemResponse[] responses = response.getResponses();
            
            String[] out = new String[responses.length];
            for (int i=0; i<responses.length; i++) {
                if (responses[i].getResponse() != null) {
                    out[i] = responses[i].getResponse().getSourceAsString();
                }
            }
            
            return out;
        } catch (IOException ie) {
            ie.printStackTrace();
        }

        return null;
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
        SearchSourceBuilder builder = new SearchSourceBuilder()
                .from(0)
                .size(0)
                .trackTotalHits(true);
        
        Query q = QueryResolverService.resolve(query);
        if (q != null) {
            builder.query(q.getQueryBuilder());
        } else {
            builder.query(QueryUtil.getMatchAllQuery());
        }

        SearchRequest request = new SearchRequest(indexes)
                .searchType(SearchType.DEFAULT)
                .source(builder);
        
        try {
            SearchResponse response = restClient.search(request, RequestOptions.DEFAULT);
            if (response.getHits().getTotalHits() != null) {
                return response.getHits().getTotalHits().value;
            }
        } catch (IOException e) {
        }
        
        return 0;
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
        SearchSourceBuilder builder = new SearchSourceBuilder()
                .from(0)
                .size(0)
                .trackTotalHits(true);
        
        Query q = QueryResolverService.resolve(query);
        if (q != null) {
            builder.query(q.getQueryBuilder());
        } else {
            builder.query(QueryUtil.getMatchAllQuery());
        }

        SearchRequest request = new SearchRequest(index)
                .searchType(SearchType.DEFAULT)
                .source(builder);
        try {
            SearchResponse response = restClient.search(request, RequestOptions.DEFAULT);
            if (response.getHits().getTotalHits() != null) {
                return response.getHits().getTotalHits().value;
            }
        } catch (IOException e) {
        }
        
        return 0;
    }
    
    /**
     * Index a single document using the standard indexing api
     * 
     * @param index
     * @param type
     * @param source
     * @param id 
     */
    public void index(String index, String type, String source, String id) {
        if (source != null && ! source.isEmpty()) {
            IndexRequest request = new IndexRequest()
                    .index(index)
                    .source(source)
                    .id(id);

            try {
                IndexResponse response = restClient.index(request, RequestOptions.DEFAULT);
            } catch (IOException ie) {
                ie.printStackTrace();
            }
        }

        // TODO: could return version of the object
        //response.version();

        // TODO: could return id of the object
        //response.id();
    }
    /**
     * Index a number of documents using the bulk indexing api
     * 
     * @param index
     * @param type
     * @param source 
     * @param idField 
     */
    public void bulkIndex(String index, String type, String[] source, String idField) {
        if (source != null && source.length > 0) {
            BulkRequest bulkRequest = new BulkRequest();

            for (String json: source) {
                bulkRequest.add(new IndexRequest()
                        .index(index)
                        .id(JsonUtil.getId(json, idField))
                        .source(JsonUtil.removeIdFromSource(json, idField), XContentType.JSON));
            }

            try {
                BulkResponse response = restClient.bulk(bulkRequest, RequestOptions.DEFAULT);
                if (response.hasFailures()) {
                    System.out.println(response.buildFailureMessage());
                }
            } catch (IOException ie) {
                ie.printStackTrace();
            }
        }
        
        // TODO: Need to return failures at some point
    }
    /**
     * Delete a single document
     *
     * @param index
     * @param type
     * @param id
     * @return 
     */
    public boolean delete(String index, String type, String id) {
        DeleteRequest request = new DeleteRequest(index, id);
        try {
            DeleteResponse response = restClient.delete(request, RequestOptions.DEFAULT);
            return response.getResult().equals(DELETED);
        } catch (IOException ie) {
            ie.printStackTrace();
        }
        
        return false;
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
    
//    /**
//     * Refresh the given index.
//     * 
//     * @param index 
//     */
//    public void refresh(String index) {
//        restClient.admin().indices().refresh(new RefreshRequest(index)).actionGet();
//    }

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
    
//    /**
//     * Execute a SearchRequestBuilder and get a SearchResponse.
//     * 
//     * @param builder
//     * @return 
//     */
//    public SearchScrollRequestBuilder executeScroll(SearchRequestBuilder builder) {
//        try {
//            if (builder.request().searchType() == SearchType.QUERY_THEN_FETCH) {
//                SearchResponse response = builder.execute().actionGet();
//                return restClient.prepareSearchScroll(response.getScrollId()).setScroll(new TimeValue(600000));
//            } else {
//                throw new Exception("SearchRequest must be of type scan.");
//            }
//        } catch (Exception e) {}
//
//        return null;
//    }
    
//    /**
//     * Check to see if indeces or types exists in the ElasticSearch cluster.
//     * 
//     * @param index
//     * @param type
//     * @return 
//     */
//    public boolean exists(String index, String type) {
//        if (type == null) {
//            IndicesExistsRequest indicesRequest = new IndicesExistsRequest(index);
//            IndicesExistsResponse response = restClient.admin().indices().exists(indicesRequest).actionGet();
//            return response.isExists();
//        } else {
//            String[] indices = new String[] {index};
//            TypesExistsRequest typesRequest = new TypesExistsRequest(indices, type);
//            TypesExistsResponse response = restClient.admin().indices().typesExists(typesRequest).actionGet();
//            return response.isExists();
//        }
//    }
}
