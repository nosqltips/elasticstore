package com.nosqlrevolution;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import com.nosqlrevolution.cursor.MultiGetCursor;
import com.nosqlrevolution.cursor.Cursor;
import com.nosqlrevolution.cursor.ScrollCursor;
import com.nosqlrevolution.query.ElasticStoreQuery;
import com.nosqlrevolution.service.QueryService;
import com.nosqlrevolution.util.MappingUtil;
import com.nosqlrevolution.util.AnnotationHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Accept and return strongly typed objects.
 * 
 * @author cbrown
 * @param <T>
 */
public class TypedIndex<T> extends Index<T> {
    private static final Logger LOGGER = Logger.getLogger(TypedIndex.class.getName());
    private final Class<T> t;
    private final QueryService service;
    private final MappingUtil<T> mapping = new MappingUtil<>();
    private final ElasticsearchClient client;
    private List<T> bulk;
    
    public TypedIndex(Class<T> t, ElasticStore store, String index, ElasticsearchClient client) throws Exception {
        super (store, index);
        this.t = t;
        this.client = client;
        service = new QueryService(store.getClient());
    }

    @Override
    public void addBulk(T t) {
        if (bulk == null) {
            bulk = new ArrayList<>();
        }
        
        bulk.add(t);
        
        if (bulk.size() >= getBulkSize() && getAutoBulkFlush()) {
            flushBulk();
        }
    }

    @Override
    public boolean flushBulk() {
        OperationStatus status = write(bulk);
        bulk.clear();
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
    public T find() {
        String s = service.getSingle(getIndex());
        return mapping.get(s, t);
    }
    
    @Override
    public <T>T find(Class<T> clazz) {
        String s = service.getSingle(getIndex());
        return mapping.get(s, clazz);
    }
    
    @Override
    public T find(ElasticStoreQuery query) {
        String s = service.getSingle(query, getIndex());
        return mapping.get(s, t);
    }
    
    @Override
    public <T>T find(ElasticStoreQuery query, Class<T> clazz) {
        String s = service.getSingle(query, getIndex());
        return mapping.get(s, clazz);
    }
    
    @Override
    public Cursor<T> findAll() {
        SearchRequest request = service.findAllScroll(getIndex());
        if (request != null) {
            try {
                return new ScrollCursor<>(t, request, client);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Failed to create new scroll.", e);
            }
        }
        
        return null;
    }

    @Override
    public Cursor findAll(ElasticStoreQuery query) {
        SearchRequest request = service.findAllScroll(getIndex());
        if (request != null) {
            try {
                return new ScrollCursor<>(t, request, client);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Failed to create new scroll.", e);
            }
        }
        
        return null;
    }

    @Override
    public Cursor<T> findAll(ElasticStoreQuery query, Class clazz) {
        SearchRequest request = service.findAllScroll(getIndex());
        if (request != null) {
            try {
                return new ScrollCursor<>(t, request, client);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Failed to create new scroll.", e);
            }
        }
        
        return null;
    }

    @Override
    public T findById(String id) {
        String s = service.realTimeGet(getIndex(), id);
        if (s != null) {
            return mapping.get(s, t);
        } else {
            return null;
        }
    }
    
    @Override
    public <T>T findById(String id, Class<T> clazz) {
        String s = service.realTimeGet(getIndex(), id);
        return mapping.get(s, clazz);
    }

    @Override
    public Cursor findAllById(String... ids) {
        String[] json = service.realTimeMultiGet(getIndex(), ids);
        return new MultiGetCursor<>(t, json);
    }
    
    @Override
    public Cursor<T> findAllById(Class<T> clazz, String... ids) {
        String[] json = service.realTimeMultiGet(getIndex(), ids);        
        return new MultiGetCursor<>(clazz, json);
    }
    
//    @Override
//    public OperationStatus removeById(String... ids) {
//        OperationStatus status = new OperationStatus();
//        status.setSucceeded(service.deleteAll(getIndex(), ids));
//        return status;
//    }
//    
//    @Override
//    public OperationStatus removeById(List<String> ids) {
//        OperationStatus status = new OperationStatus();
//        status.setSucceeded(service.deleteAll(getIndex(), ids.toArray(new String[ids.size()])));
//        return status;
//    }
//    
//    @Override
//    public OperationStatus remove(T... t) {
//        OperationStatus status = new OperationStatus();
//        if (t.length == 1) {
//            status.setSucceeded(service.delete(getIndex(), AnnotationHelper.getDocumentId(t[0], getIdField())));
//        } else if (t.length > 1) {
//            List<String> list = new ArrayList<String>();
//            for (T o: t) {
//                list.add(AnnotationHelper.getDocumentId(o, getIdField()));
//                // TODO: what do we do with null ids? bail with exception? Report the error with OperationStatus?
//            }
//            status.setSucceeded(service.deleteAll(getIndex(), list.toArray(new String[list.size()])));
//        }
//        return status;
//    }
    
//    @Override
//    public OperationStatus remove(ElasticStoreQuery query) {
//        return super.remove(query);
//    }
    
    @Override
    public OperationStatus write(T... t) {
        // TODO: Need to gather results from index operation
        OperationStatus status = new OperationStatus();
        if (t.length == 1) {
            service.index(getIndex(), mapping.get(t[0]), AnnotationHelper.getDocumentId(t[0], getIdField()));
        } else if (t.length > 1) {
            List<String> list = new ArrayList<>();
            for (T o: t) {
                list.add(mapping.get(o));
                // TODO: what do we do with null ids? bail with exception? Report the error with OperationStatus?
            }
            service.bulkIndex(getIndex(), list.toArray(new String[list.size()]), getIdField());
        }
        status.setSucceeded(true);
        return status;
    }
    
    @Override
    public OperationStatus write(List<? extends T> t) {
        // TODO: need to gather operation results and return
        OperationStatus status = new OperationStatus();
        if (t.size() == 1) {
            service.index(getIndex(), mapping.get(t.get(0)), AnnotationHelper.getDocumentId(t.get(0), getIdField()));
        } else if (t.size() > 1) {
            List<String> list = new ArrayList<>();
            for (T o: t) {
                list.add(mapping.get(o));
                // TODO: what do we do with null ids? bail with exception? Report the error with OperationStatus?
            }
            service.bulkIndex(getIndex(), list.toArray(new String[list.size()]), getIdField());
        }
        status.setSucceeded(true);
        return status;
    }

    @Override
    public boolean exists() {
//        return service.exists(getType(), getType());
        return false;
    }
}
