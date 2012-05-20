package com.nosqlrevolution;

import com.nosqlrevolution.query.Query;
import com.nosqlrevolution.service.QueryService;
import com.nosqlrevolution.util.JsonUtil;
import com.nosqlrevolution.util.MappingUtil;
import com.nosqlrevolution.util.ReflectionUtil;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Accept and return strongly typed objects.
 * 
 * @author cbrown
 */
public class TypedIndex<T> extends Index<T> {
    private static final Logger logger = Logger.getLogger(TypedIndex.class.getName());
    private T t;
    private QueryService service;
    private MappingUtil<T> mapping = new MappingUtil<T>();
    
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
        String s = service.getSingle(getIndex(), getType());
        return mapping.get(t, s);
    }
    
    @Override
    public Object find(Class clazz) {
        String s = service.getSingle(getIndex(), getType());
        return mapping.asClass(s, clazz);
    }
    
    @Override
    public T find(Query qb) {
        return super.find(qb);
    }
    
    @Override
    public Object find(Query qb, Class clazz) {
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
        return mapping.get(t, s);
    }
    
    @Override
    public Object findById(String id, Class clazz) {
        String s = service.realTimeGet(getIndex(), getType(), id);
        return mapping.asClass(s, clazz);
    }

    @Override
    public T[] findAllById(String... ids) {
        String[] s = service.realTimeMultiGet(getIndex(), getType(), ids);
        T[] out = (T[]) Array.newInstance(t.getClass(), s.length);
        for (int i=0; i<s.length; i++) {
            out[i] = mapping.get(t, s[i]);
        }
        
        return out;
    }
    
    @Override
    public Object[] findAllById(Class clazz, String... ids) {
        String[] json = service.realTimeMultiGet(getIndex(), getType(), ids);
        List<Object> list = new ArrayList<Object>();
        for (String s: json) {
            list.add(mapping.asClass(s, clazz));
        }
        
        return list.toArray(new Object[list.size()]);
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
    public OperationStatus remove(T... t) {
        if (t.length == 1) {
            boolean r = service.delete(getIndex(), getType(), ReflectionUtil.getId(t, getIdField()));
        } else {
            List<String> list = new ArrayList<String>();
            for (T o: t) {
                list.add(ReflectionUtil.getId(o, getIdField()));
                // TODO: what do we do with null ids? bail with exception? Report the error with OperationStatus?
            }
            boolean r = service.deleteAll(getIndex(), getType(), list.toArray(new String[list.size()]));
        }
        return null;
    }
    
    @Override
    public OperationStatus remove(Query qb) {
        return super.remove(qb);
    }
    
    @Override
    public OperationStatus write(T... t) {
        // TODO: need to gather operation results and return
        if (t.length == 1) {
            service.index(getIndex(), getType(), mapping.asString(t[0]), ReflectionUtil.getId(t[0], getIdField()));
        } else {
            List<String> list = new ArrayList<String>();
            for (T o: t) {
                list.add(mapping.asString(o));
                // TODO: what do we do with null ids? bail with exception? Report the error with OperationStatus?
            }
            service.bulkIndex(getIndex(), getType(), list.toArray(new String[list.size()]));
        }
        
        return null;
    }
    
    @Override
    public OperationStatus write(WriteBuilder builder, T... t) {
        // TODO: need to gather operation results and return
        // TODO: implement WriteBuilder
        if (t.length == 1) {
            service.index(getIndex(), getType(), mapping.asString(t[0]), ReflectionUtil.getId(t[0], getIdField()));
        } else {
            List<String> list = new ArrayList<String>();
            for (T o: t) {
                list.add(mapping.asString(o));
                // TODO: what do we do with null ids? bail with exception? Report the error with OperationStatus?
            }
            service.bulkIndex(getIndex(), getType(), list.toArray(new String[list.size()]));
        }
        
        return null;
    }
    
    @Override
    public OperationStatus write(List<? extends T> t) {
        // TODO: need to gather operation results and return
        if (t.size() == 1) {
            service.index(getIndex(), getType(), mapping.asString(t.get(0)), ReflectionUtil.getId(t.get(0), getIdField()));
        } else {
            List<String> list = new ArrayList<String>();
            for (T o: t) {
                list.add(mapping.asString(o));
                // TODO: what do we do with null ids? bail with exception? Report the error with OperationStatus?
            }
            service.bulkIndex(getIndex(), getType(), list.toArray(new String[list.size()]));
        }
        
        return null;
    }
    
    @Override
    public OperationStatus write(WriteBuilder builder, List<? extends T> t) {
        // TODO: need to gather operation results and return
        // TODO: implement WriteBuilder
        if (t.size() == 1) {
            service.index(getIndex(), getType(), mapping.asString(t.get(0)), ReflectionUtil.getId(t.get(0), getIdField()));
        } else {
            List<String> list = new ArrayList<String>();
            for (T o: t) {
                list.add(mapping.asString(o));
                // TODO: what do we do with null ids? bail with exception? Report the error with OperationStatus?
            }
            service.bulkIndex(getIndex(), getType(), list.toArray(new String[list.size()]));
        }
        
        return null;
    }
}
