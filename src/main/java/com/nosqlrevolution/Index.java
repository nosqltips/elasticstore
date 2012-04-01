package com.nosqlrevolution;

import com.nosqlrevolution.query.Query;
import java.util.List;

/**
 * This class represents all of the fun things we can do with an ElasticSearch index
 * @author cbrown
 */
public abstract class Index<T> {
    private ElasticStore store;
    private String[] indexes;
    private String[] types;
    
    public Index(ElasticStore store, String... indexes) throws Exception {
        if ((store == null) || (! store.isInitialized())) {
            throw new Exception("ElasticStore is not initialized!!!!");
        }
        this.store = store;
        this.indexes = indexes;
    }

    public long count() {
        return 0;
    }
    
    public long count(Query qb) {
        return 0;
    }
    
    public Cursor find() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Cursor find(Query qb) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Cursor find(Query qb, Class clazz) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public T findOneById(Object id) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public Class findOneById(Object id, Class clazz) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public T findManyById(Object... ids) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public Class findManyById(Class clazz, Object... ids) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public T findOne() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public Class findOne(Class clazz) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public T findOne(Query qb) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public Class findOne(Query qb, Class clazz) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public OperationStatus removeById(Object... ids) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public OperationStatus removeById(List<Object> ids) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public OperationStatus remove(T t) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public OperationStatus remove(Query qb) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public OperationStatus write(T... t) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public OperationStatus write(WriteBuilder builder, T... t) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public OperationStatus write(List<? extends T> t) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public OperationStatus write(WriteBuilder builder, List<? extends T> t) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    // Accessor methods
    public String[] getIndexes() {
        return indexes;
    }
    
    public String[] getTypes() {
        return types;
    }
    
    public Index setTypes(String... types) {
        this.types = types;
        return this;
    }

    public Index addType(String type) {
        // TODO: Implement this call
        return this;
    }
}
