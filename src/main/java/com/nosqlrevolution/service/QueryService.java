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
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nosqlrevolution.query.ElasticStoreQuery;
import com.nosqlrevolution.OperationStatus;
import com.nosqlrevolution.DocumentOptions;
import com.nosqlrevolution.OperationException;
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
        SearchRequest request = searchRequest(query, index, false, true);

        try {
            SearchResponse response = client.search(request, JsonData.class);

            List<Hit<JsonData>> hits = response.hits().hits();
            for (Hit<JsonData> hit: hits) {
                if (hit.source() == null) throw new IllegalStateException("Document source is unavailable");
                return hit.source().toJson().toString();
            }
        } catch (IOException ex) {
            throw new java.io.UncheckedIOException("Elasticsearch search failed", ex);
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
        return searchRequest(query, index, scroll, false);
    }

    private SearchRequest searchRequest(ElasticStoreQuery query, String index, boolean scroll, boolean single) {
        ElasticStoreQuery options = query == null ? new ElasticStoreQuery() : query.copy();
        int size = single ? 1 : options.getSize();
        if (!single && options.getLimit() > 0) size = (int) Math.min(size, options.getLimit());
        SearchRequest.Builder builder = new SearchRequest.Builder()
                .query(options.toQuery()).size(size).from(scroll ? 0 : options.getFrom())
                .trackTotalHits(TrackHits.of(t -> t.enabled(true)))
                .storedFields("_routing").source(source -> source.fetch(true));
        if (options.getFields() != null) {
            builder.source(source -> source.filter(filter -> filter.includes(Arrays.asList(options.getFields()))));
        }
        if (options.getHighlights() != null && options.getHighlights().length > 0) {
            Map<String, HighlightField> highlights = new HashMap<>();
            for (String field : options.getHighlights()) highlights.put(field, HighlightField.of(h -> h.type(HighlighterType.Plain)));
            builder.highlight(h -> h.fields(highlights));
        }
        boolean shardSort = false;
        for (String spec : options.getSort()) {
            boolean descending = spec.startsWith("-");
            String field = spec.startsWith("+") || descending ? spec.substring(1) : spec;
            shardSort |= field.equals("_shard_doc");
            builder.sort(sort -> sort.field(f -> f.field(field).order(descending ? SortOrder.Desc : SortOrder.Asc)));
        }
        if (scroll) {
            if (!shardSort) builder.sort(sort -> sort.field(f -> f.field("_shard_doc").order(SortOrder.Asc)));
            try {
                OpenPointInTimeResponse response = client.openPointInTime(r -> r.index(index).keepAlive(t -> t.time("6000s")));
                builder.pit(p -> p.id(response.id()).keepAlive(t -> t.time("6000s")));
            } catch (IOException ex) {
                throw new java.io.UncheckedIOException("Cannot open Elasticsearch point in time", ex);
            }
        } else {
            builder.index(index);
        }
        return builder.build();
    }

    /**
     * Return a single document using the rest time get api
     *
     * @param index
     * @param id
     * @return
     */
    public String realTimeGet(String index, String id) {
        return realTimeGet(index, id, null);
    }

    public String realTimeGet(String index, String id, String routing) {
        try {
            GetResponse<JsonData> response = client.get(r -> r.index(index).id(id).routing(routing).realtime(true), JsonData.class);
            if (!response.found()) return null;
            if (response.source() == null) throw new IllegalStateException("Document source is unavailable");
            return response.source().toJson().toString();
        } catch (IOException ex) {
            throw new java.io.UncheckedIOException("Elasticsearch get failed", ex);
        }
    }

    /**
     * Return a list of documents using the real time get api
     *
     * @param index
     * @param ids
     * @return
     */
    public String[] realTimeMultiGet(String index, String[] ids) {
        return realTimeMultiGet(index, ids, null);
    }

    public String[] realTimeMultiGet(String index, String[] ids, String routing) {
        java.util.Objects.requireNonNull(ids, "Document IDs");
        if (ids.length == 0) return new String[0];
        for (String id : ids) requireId(id);
        try {
            co.elastic.clients.elasticsearch.core.MgetResponse<JsonData> response = client.mget(
                    r -> r.index(index).routing(routing).ids(Arrays.asList(ids)), JsonData.class);
            String[] result = new String[response.docs().size()];
            for (int i = 0; i < result.length; i++) {
                co.elastic.clients.elasticsearch.core.mget.MultiGetResponseItem<JsonData> item = response.docs().get(i);
                if (item.isFailure()) throw new IllegalStateException("Elasticsearch multi-get item failed: " + item.failure().error());
                if (item.result().found()) {
                    if (item.result().source() == null) throw new IllegalStateException("Document source is unavailable");
                    result[i] = item.result().source().toJson().toString();
                }
            }
            return result;
        } catch (IOException ex) {
            throw new java.io.UncheckedIOException("Elasticsearch multi-get failed", ex);
        }
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
            throw new java.io.UncheckedIOException("Elasticsearch count failed", e);
        }
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
            throw new java.io.UncheckedIOException("Elasticsearch count failed", e);
        }
    }

    /**
     * Index a single document using the standard indexing api
     *
     * @param index
     * @param source
     * @param id
     */
    public OperationStatus index(String index, String source, String id) {
        return index(index, source, id, DocumentOptions.DEFAULT);
    }

    public OperationStatus index(String index, String source, String id, DocumentOptions options) {
        validateOptions(id, options);
        JsonData document = document(source);
        try {
            IndexResponse response = client.index(r -> r.index(index).id(id).document(document)
                    .routing(options.getRouting()).ifSeqNo(options.getSequenceNumber()).ifPrimaryTerm(options.getPrimaryTerm())
                    .version(options.getExternalVersion()).versionType(options.getExternalVersion() == null ? null
                            : co.elastic.clients.elasticsearch._types.VersionType.External));
            return new OperationStatus(java.util.Collections.singletonList(new OperationStatus.Item(
                    response.index(), response.id(), response.result().jsonValue(),
                    response.result() == Result.Created ? 201 : 200, response.version(),
                    com.nosqlrevolution.util.ClientCompatibility.sequenceNumber(response),
                    com.nosqlrevolution.util.ClientCompatibility.primaryTerm(response), null, null)));
        }
        catch (ElasticsearchException ex) {
            if (ex.status() == 409) throw conflict(index, id, ex);
            throw ex;
        } catch (IOException ex) {
            // The 8.2 transport reports some index conflicts as low-level response errors.
            if (ex instanceof org.elasticsearch.client.ResponseException
                    && ((org.elasticsearch.client.ResponseException) ex).getResponse().getStatusLine().getStatusCode() == 409) {
                throw conflict(index, id, ex);
            }
            throw new java.io.UncheckedIOException("Elasticsearch index failed", ex);
        }
    }

    private OperationException conflict(String index, String id, Throwable cause) {
        OperationStatus status = new OperationStatus(java.util.Collections.singletonList(new OperationStatus.Item(
                index, id, null, 409, null, null, null, "version_conflict_engine_exception",
                "Document changed or the supplied external version is stale")));
        OperationException failure = new OperationException("Elasticsearch write conflict", status);
        failure.initCause(cause);
        return failure;
    }

    private JsonData document(String source) {
        return JsonUtil.document(JsonUtil.parseObject(source), client._jsonpMapper());
    }

    /**
     * Index a number of documents using the bulk indexing api
     *
     * @param index
     * @param source
     * @param idField
     */
    public OperationStatus bulkIndex(String index, String[] source, String idField) {
        java.util.Objects.requireNonNull(source, "Documents");
        String[] ids = new String[source.length];
        ObjectNode[] documents = new ObjectNode[source.length];
        for (int i = 0; i < source.length; i++) {
            documents[i] = JsonUtil.parseObject(source[i]);
            ids[i] = JsonUtil.getIdFromDocument(documents[i], idField);
        }
        DocumentOptions[] options = new DocumentOptions[source.length];
        Arrays.fill(options, DocumentOptions.DEFAULT);
        return bulkIndexParsed(index, documents, ids, options);
    }

    public OperationStatus bulkIndexWithIds(String index, String[] source, String[] ids) {
        DocumentOptions[] options = new DocumentOptions[source.length];
        java.util.Arrays.fill(options, DocumentOptions.DEFAULT);
        return bulkIndexWithIds(index, source, ids, options);
    }

    public OperationStatus bulkIndexWithIds(String index, String[] source, String[] ids, DocumentOptions[] options) {
        java.util.Objects.requireNonNull(source, "Documents");
        ObjectNode[] documents = new ObjectNode[source.length];
        for (int i = 0; i < source.length; i++) documents[i] = JsonUtil.parseObject(source[i]);
        return bulkIndexParsed(index, documents, ids, options);
    }

    /** Submits a previously validated block without parsing its JSON again. */
    public OperationStatus bulkIndexParsed(String index, ObjectNode[] source, String[] ids, DocumentOptions[] options) {
        if (source.length != options.length) throw new IllegalArgumentException("Each document needs an options slot");
        if (source.length != ids.length) throw new IllegalArgumentException("Each document needs an ID slot");
        if (source.length == 0) return new OperationStatus(java.util.Collections.emptyList());
        BulkRequest.Builder request = new BulkRequest.Builder();
        for (int i = 0; i < source.length; i++) {
            JsonData document = JsonUtil.document(source[i], client._jsonpMapper());
            String id = ids[i];
            DocumentOptions option = options[i];
            validateOptions(id, option);
            request.operations(op -> op.index(item -> item.index(index).id(id).document(document)
                    .routing(option.getRouting()).ifSeqNo(option.getSequenceNumber()).ifPrimaryTerm(option.getPrimaryTerm())
                    .version(option.getExternalVersion()).versionType(option.getExternalVersion() == null ? null
                            : co.elastic.clients.elasticsearch._types.VersionType.External)));
        }
        try {
            BulkResponse response = client.bulk(request.build());
            return bulkStatus(response, false);
        } catch (IOException ex) { throw new java.io.UncheckedIOException("Elasticsearch bulk write failed", ex); }
    }

    private OperationStatus bulkStatus(BulkResponse response, boolean deleting) {
        java.util.List<OperationStatus.Item> items = new java.util.ArrayList<>();
        for (co.elastic.clients.elasticsearch.core.bulk.BulkResponseItem item : response.items()) {
            boolean missing = deleting && item.status() == 404 && "not_found".equals(item.result());
            items.add(new OperationStatus.Item(item.index(), item.id(), item.result(), item.status(),
                    item.version(), item.seqNo(), item.primaryTerm(),
                    missing || item.error() == null ? null : item.error().type(),
                    missing || item.error() == null ? null : item.error().reason()));
        }
        OperationStatus status = new OperationStatus(items);
        if (!status.succeeded()) throw new OperationException(
                "Elasticsearch bulk " + (deleting ? "delete" : "write") + " failed", status);
        return status;
    }

    public boolean exists(String index) {
        try { return client.indices().exists(r -> r.index(index)).value(); }
        catch (IOException ex) { throw new java.io.UncheckedIOException("Elasticsearch index exists failed", ex); }
    }

    public void refresh(String index) {
        try { client.indices().refresh(r -> r.index(index)); }
        catch (IOException ex) { throw new java.io.UncheckedIOException("Elasticsearch refresh failed", ex); }
    }

    public OperationStatus deleteAll(String index, String[] ids) {
        DocumentOptions[] options = new DocumentOptions[ids.length];
        java.util.Arrays.fill(options, DocumentOptions.DEFAULT);
        return deleteAll(index, ids, options);
    }

    public OperationStatus deleteAll(String index, String[] ids, DocumentOptions[] options) {
        if (ids.length != options.length) throw new IllegalArgumentException("Each ID needs an options slot");
        java.util.Objects.requireNonNull(ids, "Document IDs");
        if (ids.length == 0) return new OperationStatus(java.util.Collections.emptyList());
        BulkRequest.Builder request = new BulkRequest.Builder();
        for (int i = 0; i < ids.length; i++) {
            String id = ids[i];
            requireId(id);
            DocumentOptions option = options[i];
            validateOptions(id, option);
            request.operations(op -> op.delete(d -> d.index(index).id(id)
                    .routing(option.getRouting()).ifSeqNo(option.getSequenceNumber()).ifPrimaryTerm(option.getPrimaryTerm())
                    .version(option.getExternalVersion()).versionType(option.getExternalVersion() == null ? null
                            : co.elastic.clients.elasticsearch._types.VersionType.External)));
        }
        try {
            BulkResponse response = client.bulk(request.build());
            return bulkStatus(response, true);
        } catch (IOException ex) { throw new java.io.UncheckedIOException("Elasticsearch bulk delete failed", ex); }
    }

    public void deleteMatching(String index, ElasticStoreQuery query) {
        if (query == null) throw new IllegalArgumentException("An explicit delete query is required");
        if (query.getSkip() != 0 || query.getLimit() != 0) {
            throw new IllegalArgumentException("Delete queries do not support skip or limit");
        }
        try {
            co.elastic.clients.elasticsearch.core.DeleteByQueryResponse response = client.deleteByQuery(
                    r -> r.index(index).query(query.toQuery()));
            if (response.timedOut() || !response.failures().isEmpty() || response.versionConflicts() > 0) {
                throw new IllegalStateException("Elasticsearch delete by query did not complete without failures");
            }
        } catch (IOException ex) { throw new java.io.UncheckedIOException("Elasticsearch delete by query failed", ex); }
    }

    private static void validateOptions(String id, DocumentOptions options) {
        java.util.Objects.requireNonNull(options, "Document options");
        if (options.getSequenceNumber() != null || options.getExternalVersion() != null) requireId(id);
    }

    private static void requireId(String id) {
        if (id == null || id.trim().isEmpty()) throw new IllegalArgumentException("A document ID is required");
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
            throw new java.io.UncheckedIOException("Elasticsearch delete failed", ie);
        }
    }

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

}
