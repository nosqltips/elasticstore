package com.nosqlrevolution;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import com.nosqlrevolution.cursor.MultiGetCursor;
import com.nosqlrevolution.cursor.Cursor;
import com.nosqlrevolution.cursor.HitScrollCursor;
import com.nosqlrevolution.cursor.ScrollCursor;
import com.nosqlrevolution.query.ElasticStoreQuery;
import com.nosqlrevolution.service.QueryService;
import com.nosqlrevolution.util.MappingUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Accept and return json strings
 * 
 * @author cbrown
 * @param <T>
 */
public class JsonIndex<T> extends Index<String> {
    private static final Logger LOGGER = Logger.getLogger(JsonIndex.class.getName());
    private final QueryService service;
    private final ElasticsearchClient client;
    private final MappingUtil<String> mapping = new MappingUtil<>();
    private List<String> bulk;
    
    public JsonIndex(ElasticStore store, String index) throws Exception {
        super(store, index);
        this.client = store.getClient();
        service = new QueryService(store.getClient());
    }

    @Override
    public void addBulk(String json) {
        if (bulk == null) {
            bulk = new ArrayList<>();
        }
        
        bulk.add(json);
        
        if (bulk.size() >= getBulkSize() && getAutoBulkFlush()) {
            flushBulk();
        }
    }

    @Override
    public boolean flushBulk() {
        if (bulk == null || bulk.isEmpty()) return true;
        OperationStatus status = write(bulk);
        if (bulk != null) {
            bulk.clear();
        }
        return status.succeeded();
    }

    @Override
    public long count() {
        return service.count(getIndex());
    }
    
    @Override
    public long count(ElasticStoreQuery query) {
        return service.count(query, getIndex());
    }
    
    @Override
    public String find() {
        return service.getSingle(getIndex());
    }
    
    @Override
    public <T>T find(Class<T> clazz) {
        String s = service.getSingle(getIndex());
        return mapping.get(s, clazz);
    }
    
    @Override
    public String find(ElasticStoreQuery query) {
        return service.getSingle(query, getIndex());
    }
    
    @Override
    public <T>T find(ElasticStoreQuery query, Class<T> clazz) {
        String s = service.getSingle(query, getIndex());
        return mapping.get(s, clazz);
    }
    
    @Override
    public Cursor<String> findAll() {
        SearchRequest request = service.findAllScroll(getIndex());
        if (request != null) {
            try {
                return new ScrollCursor<>(String.class, request, client);
            } catch (Exception e) {
                throw new IllegalStateException("Failed to create document cursor", e);
            }
        }
        return null;
    }

    @Override
    public Cursor<String> findAllScroll(ElasticStoreQuery query) {
        SearchRequest request = service.findAllScroll(query, getIndex());
        if (request != null) {
            try {
                return new ScrollCursor<>(String.class, request, client, query == null ? 0 : query.getSkip(), query == null ? 0 : query.getLimit());
            } catch (Exception e) {
                throw new IllegalStateException("Failed to create document cursor", e);
            }
        }
        return null;
    }
    
    @Override
    public Cursor<Hit<JsonData>> findAllScrollHit(ElasticStoreQuery query) {
        SearchRequest request = service.findAllScroll(query, getIndex());
        if (request != null) {
            try {
                return new HitScrollCursor(request, client, query == null ? 0 : query.getSkip(), query == null ? 0 : query.getLimit());
            } catch (Exception e) {
                throw new IllegalStateException("Failed to create document cursor", e);
            }
        }
        return null;
    }

    @Override
    public Cursor<String> findAll(ElasticStoreQuery query) {
        SearchRequest request = service.findAllScroll(query, getIndex());
        if (request != null) {
            try {
                return new ScrollCursor<>(String.class, request, client, query == null ? 0 : query.getSkip(), query == null ? 0 : query.getLimit());
            } catch (Exception e) {
                throw new IllegalStateException("Failed to create document cursor", e);
            }
        }
        return null;
    }

    @Override
    public <E> Cursor<E> findAll(ElasticStoreQuery query, Class<E> clazz) {
        SearchRequest request = service.findAllScroll(query, getIndex());
        if (request != null) {
            try {
                return new ScrollCursor<>(clazz, request, client, query == null ? 0 : query.getSkip(), query == null ? 0 : query.getLimit());
            } catch (Exception e) {
                throw new IllegalStateException("Failed to create document cursor", e);
            }
        }
        return null;
    }

    @Override
    public String findByIdWithRouting(String id, String routing) {
        return service.realTimeGet(getIndex(), id, routing);
    }

    @Override
    public OperationStatus write(List<? extends String> json, DocumentOptions options) {
        java.util.Objects.requireNonNull(json, "Documents");
        java.util.Objects.requireNonNull(options, "Document options");
        if (options.getSequenceNumber() != null && json.size() != 1) {
            throw new IllegalArgumentException("Conditional write requires exactly one document");
        }
        String[] sources = json.toArray(new String[0]);
        String[] ids = new String[sources.length];
        DocumentOptions[] all = new DocumentOptions[sources.length];
        for (int i = 0; i < sources.length; i++) {
            ids[i] = com.nosqlrevolution.util.JsonUtil.getId(sources[i], getIdField());
            all[i] = options;
        }
        return service.bulkIndexWithIds(getIndex(), sources, ids, all);
    }

    @Override
    public String findById(String id) {
        return service.realTimeGet(getIndex(), id);
    }
    
    @Override
    public <T>T findById(String id, Class<T> clazz) {
        String s = service.realTimeGet(getIndex(), id);
        if (s != null) {
            return mapping.get(s, clazz);
        } else {
            return null;
        }
    }
    
    @Override
    public Cursor<String> findAllById(String... ids) {
        String[] json = service.realTimeMultiGet(getIndex(), ids);
        return new MultiGetCursor<>(String.class, json);
    }
    
    @Override
    public <E> Cursor<E> findAllById(Class<E> clazz, String... ids) {
        String[] json = service.realTimeMultiGet(getIndex(), ids);        
        return new MultiGetCursor<>(clazz, json);
    }
    
    @Override
    public OperationStatus remove(ElasticStoreQuery query) {
        return super.remove(query);
    }

    @Override
    public OperationStatus write(String... json) {
        return service.bulkIndex(getIndex(), json, getIdField());
    }

    @Override
    public OperationStatus write(List<? extends String> json) {
        return write(json == null ? null : json.toArray(new String[json.size()]));
    }

    @Override
    public boolean exists() {
        return service.exists(getIndex());
    }
}
