package com.nosqlrevolution;

import com.nosqlrevolution.query.Query;
import com.nosqlrevolution.service.QueryService;
import com.nosqlrevolution.util.ReflectionUtil;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * Accept and return strongly typed objects.
 * 
 * @author cbrown
 */
public class TypedIndex<T> extends Index<T> {
    private static final Logger logger = Logger.getLogger(TypedIndex.class.getName());
    private T t;
    private QueryService service;
    private ObjectMapper mapper = new ObjectMapper();
    
    public TypedIndex(T t, ElasticStore store, String index, String iType) throws Exception {
        super (store, index, iType);
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
    public T find() {
        return super.find();
    }
    
    @Override
    public Class find(Class clazz) {
        return super.find(clazz);
    }
    
    @Override
    public T find(Query qb) {
        return super.find(qb);
    }
    
    @Override
    public Class find(Query qb, Class clazz) {
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
    public T findById(String id) {
        String s = service.realTimeGet(getIndex(), getType(), id);
        return getMapping(s);
    }
    
    @Override
    public Class findById(String id, Class clazz) {
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
    public T[] findAllById(String... ids) {
        String[] s = service.realTimeMultiGet(getIndex(), getType(), ids);
        T[] out = (T[]) Array.newInstance(t.getClass(), s.length);
        for (int i=0; i<s.length; i++) {
            out[i] = getMapping(s[i]);
        }
        return out;
    }
    
    @Override
    public Class[] findAllById(Class clazz, String... ids) {
        return super.findAllById(clazz, ids);
    }
    
    @Override
    public OperationStatus removeById(String... ids) {
        return super.removeById(ids);
    }
    
    @Override
    public OperationStatus removeById(List<String> ids) {
        return super.removeById(ids);
    }
    
    @Override
    public OperationStatus remove(T t) {
       boolean r = service.delete(getIndex(), getType(), ReflectionUtil.getId(t, getIdField()));
       return null;
    }
    
    @Override
    public OperationStatus remove(Query qb) {
        return super.remove(qb);
    }
    
    @Override
    public OperationStatus write(T... t) {
        // Optimize for single value
        try {
            service.index(getIndex(), getType(), mapper.writeValueAsString(t[0]), ReflectionUtil.getId(t[0], getIdField()));
        } catch (IOException ex) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }
    
    @Override
    public OperationStatus write(WriteBuilder builder, T... t) {
        return super.write(builder, t);
    }
    
    @Override
    public OperationStatus write(List<? extends T> t) {
        return super.write(t);
    }
    
    @Override
    public OperationStatus write(WriteBuilder builder, List<? extends T> t) {
        return super.write(builder, t);
    }
    
    private T getMapping(String s) {
        if (s == null) { return null; }
        
        try {
            return (T) mapper.readValue(s, t.getClass());
        } catch (IOException e) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.log(Level.SEVERE, null, e);
            }
            return null;
        }
    }
}
