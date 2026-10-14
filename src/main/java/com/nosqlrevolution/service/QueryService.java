package com.nosqlrevolution.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.FieldSort;
import co.elastic.clients.elasticsearch._types.Result;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.Time;
import co.elastic.clients.elasticsearch.core.CountRequest;
import co.elastic.clients.elasticsearch.core.CountResponse;
import co.elastic.clients.elasticsearch.core.GetRequest;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.DeleteRequest;
import co.elastic.clients.elasticsearch.core.DeleteResponse;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.OpenPointInTimeRequest;
import co.elastic.clients.elasticsearch.core.OpenPointInTimeResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.HighlightField;
import co.elastic.clients.elasticsearch.core.search.HighlighterType;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.HitsMetadata;
import co.elastic.clients.elasticsearch.core.search.SourceFilter;
import co.elastic.clients.elasticsearch.core.search.TrackHits;
import co.elastic.clients.json.JsonData;
import com.nosqlrevolution.query.ElasticStoreQuery;
import com.nosqlrevolution.util.JsonUtil;
import com.nosqlrevolution.util.QueryUtil;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author cbrown
 */
public class QueryService {
    private final ElasticsearchClient client;
    
    public QueryService(ElasticsearchClient client) {
        this.client = client;
    }
    
    /**
     * Return the first document from this index
     * 
     * @param index
     * @return 
     */
    public String getSingle(String index) {
        return getSingle(null, index);
    }
    
    /**
     * Return the first document from this index
     * 
     * @param query
     * @param index
     * @return 
     */
    public String getSingle(com.nosqlrevolution.query.ElasticStoreQuery query, String index) {
        SearchRequest.Builder builder = new SearchRequest.Builder()
                .index(index)
                .from(0)
                .size(1);

        com.nosqlrevolution.query.ElasticStoreQuery q = QueryResolverService.resolve(query);
        if (q != null) {
            builder.query(q.getQuery());
            if (q.getFields() != null) {
                List<String> includes = Arrays.asList(q.getFields());
                builder.source(s -> s.filter(SourceFilter.of(i -> i.includes(includes))));
            }

            if (query.getHighlights() != null) {
                Map<String, HighlightField> highlights = new HashMap<>();
                for (String s: query.getHighlights()) {
                    highlights.put(s, 
                            HighlightField.of(h -> h
                                    .type(HighlighterType.Plain)
                            )
                    );
                }
                
                builder.highlight(h -> h
                    .fields(highlights)
                );
            }
        } else {
            builder.query(QueryUtil.getMatchAllQuery());
        }
        
        try {
            SearchResponse response = client.search(builder.build(), JsonData.class);

            List<Hit<JsonData>> hits = response.hits().hits();
            for (Hit<JsonData> hit: hits) {
                return hit.source().toJson().toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Return the all of the documents from this index
     * 
     * @param index
     * @return 
     */
    public SearchRequest findAllScroll(String index) {
        return findAll(null, index, true);
    }
    
    /**
     * Return the all of the documents from this index
     * 
     * @param query
     * @param index
     * @return 
     */
    public SearchRequest findAllScroll(com.nosqlrevolution.query.ElasticStoreQuery query, String index) {
        return findAll(query, index, true);
    }
    
    /**
     * Return the all of the documents from this index
     * 
     * @param index
     * @return 
     */
    public SearchRequest findAll(String index) {
        return findAll(null, index, false);
    }

    /**
     * Return the all of the documents from this index
     * 
     * @param query
     * @param index
     * @param scroll
     * @return 
     */
    public SearchRequest findAll(com.nosqlrevolution.query.ElasticStoreQuery query, String index, boolean scroll) {
        SearchRequest.Builder builder = new SearchRequest.Builder()
                .from(0)
                .size(1000)
                .trackTotalHits(TrackHits.of(t -> t.enabled(true)));
        
        ElasticStoreQuery q = QueryResolverService.resolve(query);
        if (q != null) {
            builder.query(q.getQuery());
            if (q.getFields() != null) {
                List<String> includes = Arrays.asList(q.getFields());
                builder.source(s -> s.filter(SourceFilter.of(i -> i.includes(includes))));
            }
            if (query.getHighlights() != null) {

                Map<String, HighlightField> highlights = new HashMap<>();
                for (String s: query.getHighlights()) {
                    highlights.put(s, 
                            HighlightField.of(h -> h
                                    .type(HighlighterType.Plain)
                            )
                    );
                }
                
                builder.highlight(h -> h
                    .fields(highlights)
                );
            }
            if (query.getSort() != null && query.getSort().length > 0) {
                builder.sort(s -> s
                        .field(f -> f
                                .field(query.getSort()[0]))
                );
            }
        } else {
            builder.query(QueryUtil.getMatchAllQuery());
        }

        if (scroll) {
            try {
                OpenPointInTimeRequest openPit = OpenPointInTimeRequest.of(o -> o
                        .index(index)
                        .keepAlive(Time.of(t -> t.time("6000S")))
                    );
                
                OpenPointInTimeResponse pitResponse = client.openPointInTime(openPit);
                            
                builder.pit(p -> p.id(pitResponse.id()));
                builder.sort(SortOptions.of(s-> s.field(FieldSort.of(f -> f.field("_shard_doc").order(SortOrder.Asc)))));
            } catch (IOException ex) {
                throw new IllegalStateException("Cannot open Elasticsearch point in time", ex);
            } catch (ElasticsearchException ex) {
                throw new IllegalStateException("Cannot open Elasticsearch point in time", ex);
            }
        }

        SearchRequest request = builder.build();
        System.out.println("builder=" + request);
        
        return request;
    }
    
    /**
     * Return a single document using the rest time get api
     * 
     * @param index
     * @param id
     * @return 
     */
    public String realTimeGet(String index, String id) {
        GetRequest.Builder getRequest = new GetRequest.Builder()
                .index(index)
                .id(id)
                .realtime(Boolean.TRUE);        
        try {
            GetResponse<Hit> response = client.get(getRequest.build(), Hit.class);
            return response.source().toString();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        return null;
    }

    /**
     * Return a list of documents using the real time get api
     * 
     * @param index
     * @param ids
     * @return 
     */
    public String[] realTimeMultiGet(String index, String[] ids) {
//        MultiGetRequest multiRequest = null;
//        try {
//            multiRequest = new MultiGetRequest()
//                    //.add(index, type, ids, new FetchSourceContext(false), null, null, false)
//                    .realtime(Boolean.TRUE);
//        } catch (Exception ie) {
//            ie.printStackTrace();
//        }
//
//        try {
//            MultiGetResponse response = client.multiGet(multiRequest, RequestOptions.DEFAULT);
//            MultiGetItemResponse[] responses = response.getResponses();
//            
//            String[] out = new String[responses.length];
//            for (int i=0; i<responses.length; i++) {
//                if (responses[i].getResponse() != null) {
//                    out[i] = responses[i].getResponse().getSourceAsString();
//                }
//            }
//            
//            return out;
//        } catch (IOException ie) {
//            ie.printStackTrace();
//        }

        return null;
    }
    
    /**
     * Return a count of all documents from many indexes
     * 
     * @param indexes
     * @return 
     */
    public long count(String[] indexes) {
        return count(null, indexes);
    }

    /**
     * Return a count of all documents from this index
     * 
     * @param index
     * @return 
     */
    public long count(String index) {
        return count(null, index);
    }
    
    /**
     * Return a count of all documents from many indexes
     * 
     * @param query
     * @param indexes
     * @return 
     */
    public long count(com.nosqlrevolution.query.ElasticStoreQuery query, String[] indexes) {
        CountRequest.Builder builder = new CountRequest.Builder()
            .index(Arrays.asList(indexes));

        ElasticStoreQuery q = QueryResolverService.resolve(query);
        if (q != null) {
            builder.query(q.getQuery());
        } else {
            builder.query(QueryUtil.getMatchAllQuery());
        }

        try {
            CountResponse response = client.count(builder.build());
            return response.count();
        } catch (IOException e) {
        }
        
        return 0;
    }

    /**
     * Return a count of all documents from this index
     * 
     * @param query
     * @param index
     * @return 
     */
    public long count(com.nosqlrevolution.query.ElasticStoreQuery query, String index) {
        CountRequest.Builder builder = new CountRequest.Builder()
                .index(Arrays.asList(index));    
        
        ElasticStoreQuery q = QueryResolverService.resolve(query);
        if (q != null) {
            builder.query(q.getQuery());
        } else {
            builder.query(QueryUtil.getMatchAllQuery());
        }
        
        try {
            CountResponse response = client.count(builder.build());
            return response.count();
        } catch (IOException e) {
        }
        
        return 0;
    }
    
    /**
     * Index a single document using the standard indexing api
     * 
     * @param index
     * @param source
     * @param id 
     */
    public void index(String index, String source, String id) {
        if (source != null && ! source.isEmpty()) {
            IndexRequest.Builder request = new IndexRequest.Builder()
                    .index(index)
                    .document(source)
                    .id(id);

            try {
                IndexResponse response = client.index(request.build());
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
     * @param source 
     * @param idField 
     */
    public void bulkIndex(String index, String[] source, String idField) {
        if (source != null && source.length > 0) {
            BulkRequest.Builder bulkRequest = new BulkRequest.Builder();
            
            for (String json: source) {
                JsonData document;
                try (jakarta.json.stream.JsonParser parser = client._jsonpMapper().jsonProvider()
                        .createParser(new java.io.StringReader(JsonUtil.removeIdFromSource(json, idField)))) {
                    document = JsonData.from(parser, client._jsonpMapper());
                }
                bulkRequest.operations(
                    op -> op
                    .index(idx -> idx
                        .index(index)
                        .id(JsonUtil.getId(json, idField))
                        .document(document)
                    )
                );
             }

            try {
                BulkResponse response = client.bulk(bulkRequest.build());
                if (response.errors()) {
                    throw new IllegalStateException("Elasticsearch bulk write failed: " + response.items());
                }
            } catch (IOException ie) {
                throw new java.io.UncheckedIOException("Elasticsearch bulk write failed", ie);
            }
        }
        
    }
    
    /**
     * Delete a single document
     *
     * @param index
     * @param id
     * @return 
     */
    public boolean delete(String index, String id) {
        DeleteRequest.Builder request = new DeleteRequest.Builder()
                .index(index)
                .id(id);
        try {
            DeleteResponse response = client.delete(request.build());
            return response.result().equals(Result.Deleted);
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
//     * @param ids
//     * @return 
//     */
//    public boolean deleteAll(String index, String[] ids) {
//        return deleteAll(DEFAULT_WRITE, index, ids);
//    }
//    /**
//     * Delete all documents as DeleteByQuery
//     * 
//     * @param write
//     * @param index
//     * @param id
//     * @return 
//     */
//    public boolean deleteAll(WriteOperation write, String index String[] id) {
//        DeleteByQueryRequestBuilder builder = client.prepareDeleteByQuery()
//                .setIndices(index)
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
//        client.admin().indices().refresh(new RefreshRequest(index)).actionGet();
//    }

    /**
     * Execute a SearchRequestBuilder and get a SearchResponse.
     * 
     * @param request
     * @return 
     */
    public HitsMetadata<Hit> executeBuilder(SearchRequest request) {
        try {
            SearchResponse<Hit> response = client.search(request, Hit.class);
            if (response != null) {
                return response.hits();
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
//                return client.prepareSearchScroll(response.getScrollId()).setScroll(new TimeValue(600000));
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
//            IndicesExistsResponse response = client.admin().indices().exists(indicesRequest).actionGet();
//            return response.isExists();
//        } else {
//            String[] indices = new String[] {index};
//            TypesExistsRequest typesRequest = new TypesExistsRequest(indices, type);
//            TypesExistsResponse response = client.admin().indices().typesExists(typesRequest).actionGet();
//            return response.isExists();
//        }
//    }
}
