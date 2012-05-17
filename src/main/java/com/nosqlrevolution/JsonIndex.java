package com.nosqlrevolution;

import com.nosqlrevolution.query.Query;
import com.nosqlrevolution.service.QueryService;
import com.nosqlrevolution.util.JsonUtil;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * Accept and return json strings
 * 
 * @author cbrown
 */
public class JsonIndex<T> extends Index<String> {
    private static final Logger logger = Logger.getLogger(JsonIndex.class.getName());
    private T t;
    private QueryService service;
    private ObjectMapper mapper = new ObjectMapper();
    
    public JsonIndex(T t, ElasticStore store, String index, String type) throws Exception {
        super(store, index, type);
        this.t = t;
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
    public Cursor find() {
        return super.find();
    }

    @Override
    public Cursor find(Query qb) {
        return super.find(qb);
    }

    @Override
    public Cursor find(Query qb, Class clazz) {
        return super.find(qb, clazz);
    }

    @Override
    public String findOneById(String id) {
        return service.realTimeGet(getIndex(), getType(), id);
    }
    
    @Override
    public Class findOneById(String id, Class clazz) {
        try {
            String s = service.realTimeGet(getIndex(), getType(), id);
            // Todo probably put this into a util class for broad use
            return (Class) (mapper.readValue(s, clazz.getClass()));
        } catch (IOException ex) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.log(Level.SEVERE, null, ex);
            }
            return null;
        }
    }
    
    @Override
    public String[] findManyById(String... ids) {
        return service.realTimeMultiGet(getIndex(), getType(), ids);
    }
    
    @Override
    public Class[] findManyById(Class clazz, String... ids) {
        return super.findManyById(clazz, ids);
    }
    
    @Override
    public String findOne() {
        return super.findOne();
    }
    
    @Override
    public Class findOne(Class clazz) {
        return super.findOne(clazz);
    }
    
    @Override
    public String findOne(Query qb) {
        return super.findOne(qb);
    }
    
    @Override
    public Class findOne(Query qb, Class clazz) {
        return super.findOne(qb, clazz);
    }
            
    @Override
    public OperationStatus removeById(String... id) {
        return super.removeById(id);
    }
    
    @Override
    public OperationStatus removeById(List<String> id) {
        return super.removeById(id);
    }
    
    @Override
    public OperationStatus remove(String json) {
        boolean r = service.delete(getIndex(), getType(), JsonUtil.getId(json, getIdField()));
        return null;
    }
    
    @Override
    public OperationStatus remove(Query qb) {
        return super.remove(qb);
    }

    @Override
    public OperationStatus write(String... json) {
        // TODO: need to look at list, optimize for 1
//        try {
            service.index(getIndex(), getType(), json[0], JsonUtil.getId(json[0], getIdField()));
//        } catch (IOException ex) {
//            if (logger.isLoggable(Level.WARNING)) {
//                logger.log(Level.SEVERE, null, ex);
//            }
//        }
        return null;
    }
    
    @Override
    public OperationStatus write(WriteBuilder builder, String... json) {
        return super.write(builder, json);
    }
    
    @Override
    public OperationStatus write(List<? extends String> json) {
        return super.write(json);
    }
    
    @Override
    public OperationStatus write(WriteBuilder builder, List<? extends String> json) {
        return super.write(builder, json);
    }
}
