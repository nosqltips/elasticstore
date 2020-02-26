package com.nosqlrevolution;

import com.nosqlrevolution.cursor.MultiGetCursor;
import com.nosqlrevolution.cursor.Cursor;
import com.nosqlrevolution.cursor.HitScrollCursor;
import com.nosqlrevolution.cursor.ScrollCursor;
import com.nosqlrevolution.query.Query;
import com.nosqlrevolution.service.QueryService;
import com.nosqlrevolution.util.JsonUtil;
import com.nosqlrevolution.util.MappingUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.search.SearchHit;

/**
 * Accept and return json strings
 * 
 * @author cbrown
 * @param <T>
 */
public class JsonIndex<T> extends Index<String> {
    private static final Logger LOGGER = Logger.getLogger(JsonIndex.class.getName());
    private final QueryService service;
    private final RestHighLevelClient restClient;
    private final MappingUtil<String> mapping = new MappingUtil<>();
    private List<String> bulk;
    
    public JsonIndex(ElasticStore store, String index, String type) throws Exception {
        super(store, index, type);
        this.restClient = store.getRestClient();
        service = new QueryService(store.getRestClient());
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
        OperationStatus status = write(bulk);
        if (bulk != null) {
            bulk.clear();
        }
        return status.succeeded();
    }

    @Override
    public long count() {
        return service.count(getIndex(), getType());
    }
    
    @Override
    public long count(Query query) {
        return service.count(query, getIndex(), getType());
    }
    
    @Override
    public String find() {
        return service.getSingle(getIndex(), getType());
    }
    
    @Override
    public <T>T find(Class<T> clazz) {
        String s = service.getSingle(getIndex(), getType());
        return mapping.get(s, clazz);
    }
    
    @Override
    public String find(Query query) {
        return service.getSingle(query, getIndex(), getType());
    }
    
    @Override
    public <T>T find(Query query, Class<T> clazz) {
        String s = service.getSingle(query, getIndex(), getType());
        return mapping.get(s, clazz);
    }
    
    @Override
    public Cursor<String> findAll() {
        SearchRequest request = service.findAllScroll(getIndex(), getType());
        if (request != null) {
            try {
                return new ScrollCursor<>(String.class, request, restClient);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Failed to create new scroll.", e);
            }
        }
        return null;
    }

    @Override
    public Cursor<String> findAllScroll(Query query) {
        SearchRequest request = service.findAllScroll(query, getIndex(), getType());
        if (request != null) {
            try {
                return new ScrollCursor<>(String.class, request, restClient);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Failed to create new scroll.", e);
            }
        }
        return null;
    }
    
    @Override
    public Cursor<SearchHit> findAllScrollHit(Query query) {
        SearchRequest request = service.findAllScroll(query, getIndex(), getType());
        if (request != null) {
            try {
                return new HitScrollCursor(request, restClient);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Failed to create new scroll.", e);
            }
        }
        return null;
    }

    @Override
    public Cursor<String> findAll(Query query) {
        SearchRequest request = service.findAllScroll(query, getIndex(), getType());
        if (request != null) {
            try {
                return new ScrollCursor<>(String.class, request, restClient);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Failed to create new scroll.", e);
            }
        }
        return null;
    }

    @Override
    public Cursor<String> findAll(Query query, Class clazz) {
        SearchRequest request = service.findAllScroll(query, getIndex(), getType());
        if (request != null) {
            try {
                return new ScrollCursor<>(String.class, request, restClient);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Failed to create new scroll.", e);
            }
        }
        return null;
    }

    @Override
    public String findById(String id) {
        return service.realTimeGet(getIndex(), getType(), id);
    }
    
    @Override
    public <T>T findById(String id, Class<T> clazz) {
        String s = service.realTimeGet(getIndex(), getType(), id);
        if (s != null) {
            return mapping.get(s, clazz);
        } else {
            return null;
        }
    }
    
    @Override
    public Cursor findAllById(String... ids) {
        String[] json = service.realTimeMultiGet(getIndex(), getType(), ids);
        return new MultiGetCursor<>(String.class, json);
    }
    
    @Override
    public Cursor<String> findAllById(Class clazz, String... ids) {
        String[] json = service.realTimeMultiGet(getIndex(), getType(), ids);        
        return new MultiGetCursor<>(clazz, json);
    }
//            
//    @Override
//    public OperationStatus removeById(String... ids) {
//        boolean r = service.deleteAll(getIndex(), getType(), ids);
//        return new OperationStatus()
//                .setSucceeded(true);
//    }
//    
//    @Override
//    public OperationStatus removeById(List<String> ids) {
//        boolean r = service.deleteAll(getIndex(), getType(), ids.toArray(new String[ids.size()]));
//        
//        return new OperationStatus()
//                .setSucceeded(true);
//    }
//    
//    @Override
//    public OperationStatus remove(String... json) {
//        if (json.length == 1) {
//            boolean r = service.delete(getIndex(), getType(), JsonUtil.getId(json[0], getIdField()));
//        } else if (json.length > 1) {
//            List<String> ids = new ArrayList<String>();
//            for (String js: json) {
//                ids.add(JsonUtil.getId(js, getIdField()));
//                // TODO: what do we do with null ids? bail with exception? Report the error with OperationStatus?
//            }
//            boolean r = service.deleteAll(getIndex(), getType(), ids.toArray(new String[ids.size()]));
//        }
//        
//        return new OperationStatus()
//                .setSucceeded(true);
//    }
    
    @Override
    public OperationStatus remove(Query query) {
        return super.remove(query);
    }

    @Override
    public OperationStatus write(String... json) {
        // TODO: need to gather operation results and return
//        if (json != null && json.length == 1) {
//            service.index(getIndex(), getType(), json[0], JsonUtil.getId(json[0], getIdField()));
//        } else if (json != null && json.length > 1) {
        if (json != null && json.length > 1) {
            service.bulkIndex(getIndex(), getType(), json, getIdField());
        }
        
        return new OperationStatus()
                .setSucceeded(true);
    }
    
    @Override
    public OperationStatus write(List<? extends String> json) {
        // TODO: need to gather operation results and return
//        if (json != null && json.size() == 1) {
//            service.index(getIndex(), getType(), json.get(0), JsonUtil.getId(json.get(0), getIdField()));
//        } else if (json != null && json.size() > 1) {
        if (json != null && json.size() > 1) {
            service.bulkIndex(getIndex(), getType(), json.toArray(new String[json.size()]), getIdField());
        }

        return new OperationStatus()
                .setSucceeded(true);
    }

    @Override
    public boolean exists() {
//        return service.exists(getType(), getType());
        return false;
    }
}
