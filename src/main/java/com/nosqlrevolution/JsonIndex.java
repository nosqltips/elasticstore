package com.nosqlrevolution;

import com.nosqlrevolution.query.Query;
import com.nosqlrevolution.service.QueryService;
import com.nosqlrevolution.util.JsonUtil;
import com.nosqlrevolution.util.MappingUtil;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Accept and return json strings
 * 
 * @author cbrown
 */
public class JsonIndex<T> extends Index<String> {
    private static final Logger logger = Logger.getLogger(JsonIndex.class.getName());
    private QueryService service;
    private MappingUtil<String> mapping = new MappingUtil<String>();
    
    public JsonIndex(ElasticStore store, String index, String type) throws Exception {
        super(store, index, type);
        service = new QueryService(store.getClient());
    }

    @Override
    public long count() {
        return service.count(getIndex(), getType());
    }
    
    @Override
    public long count(Query qb) {
        // TODO need to finish this;
        return 0;
    }
    
    @Override
    public String find() {
        return service.getSingle(getIndex(), getType());
    }
    
    @Override
    public <T>T find(Class<T> clazz) {
        String s = service.getSingle(getIndex(), getType());
        return mapping.asClass(s, clazz);
    }
    
    @Override
    public String find(Query qb) {
        return super.find(qb);
    }
    
    @Override
    public <T>T find(Query qb, Class<T> clazz) {
        return super.find(qb, clazz);
    }
    
    @Override
    public Cursor findAll() {
        return super.findAll();
    }

    @Override
    public Cursor findAll(Query qb) {
        return super.findAll(qb);
    }

    @Override
    public Cursor findAll(Query qb, Class clazz) {
        return super.findAll(qb, clazz);
    }

    @Override
    public String findById(String id) {
        return service.realTimeGet(getIndex(), getType(), id);
    }
    
    @Override
    public <T>T findById(String id, Class<T> clazz) {
        String s = service.realTimeGet(getIndex(), getType(), id);
        return mapping.asClass(s, clazz);
    }
    
    @Override
    public String[] findAllById(String... ids) {
        return service.realTimeMultiGet(getIndex(), getType(), ids);
    }
    
    @Override
    public <T>T[] findAllById(Class<T> clazz, String... ids) {
        String[] json = service.realTimeMultiGet(getIndex(), getType(), ids);
        List<T> list = new ArrayList<T>();
        for (String s: json) {
            list.add(mapping.asClass(s, clazz));
        }
        
        return (T[]) Array.newInstance(clazz, list.size());
    }
            
    @Override
    public OperationStatus removeById(String... ids) {
        boolean r = service.deleteAll(getIndex(), getType(), ids);
        return null;
    }
    
    @Override
    public OperationStatus removeById(List<String> ids) {
        boolean r = service.deleteAll(getIndex(), getType(), ids.toArray(new String[ids.size()]));
        return null;
    }
    
    @Override
    public OperationStatus remove(String... json) {
        if (json.length == 1) {
            boolean r = service.delete(getIndex(), getType(), JsonUtil.getId(json[0], getIdField()));
        } else {
            List<String> ids = new ArrayList<String>();
            for (String js: json) {
                ids.add(JsonUtil.getId(js, getIdField()));
                // TODO: what do we do with null ids? bail with exception? Report the error with OperationStatus?
            }
            boolean r = service.deleteAll(getIndex(), getType(), ids.toArray(new String[ids.size()]));
        }
        return null;
    }
    
    @Override
    public OperationStatus remove(Query qb) {
        return super.remove(qb);
    }

    @Override
    public OperationStatus write(String... json) {
        // TODO: need to gather operation results and return
        if (json.length == 1) {
            service.index(getIndex(), getType(), json[0], JsonUtil.getId(json[0], getIdField()));
        } else {
            service.bulkIndex(getIndex(), getType(), json);
        }
        return null;
    }
    
    @Override
    public OperationStatus write(WriteOperation builder, String... json) {
        // TODO: need to gather operation results and return
        // TODO: implement WriteOperation
        if (json.length == 1) {
            service.index(getIndex(), getType(), json[0], JsonUtil.getId(json[0], getIdField()));
        } else {
            service.bulkIndex(getIndex(), getType(), json);
        }
        return null;
    }
    
    @Override
    public OperationStatus write(List<? extends String> json) {
        // TODO: need to gather operation results and return
        if (json.size() == 1) {
            service.index(getIndex(), getType(), json.get(0), JsonUtil.getId(json.get(0), getIdField()));
        } else {
            service.bulkIndex(getIndex(), getType(), json.toArray(new String[json.size()]));
        }
        return null;
    }
    
    @Override
    public OperationStatus write(WriteOperation builder, List<? extends String> json) {
        // TODO: need to gather operation results and return
        // TODO: implement WriteOperation
        if (json.size() == 1) {
            service.index(getIndex(), getType(), json.get(0), JsonUtil.getId(json.get(0), getIdField()));
        } else {
            service.bulkIndex(getIndex(), getType(), json.toArray(new String[json.size()]));
        }
        return null;
    }
}
