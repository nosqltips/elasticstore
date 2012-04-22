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

    public abstract long count();
    
    public abstract long count(Query qb);
    
    public Cursor find() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Cursor find(Query qb) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Cursor find(Query qb, Class clazz) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public T findOneById(String id) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public Class findOneById(String id, Class clazz) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public T[] findManyById(String... ids) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public Class[] findManyById(Class clazz, String... ids) {
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

    public OperationStatus removeById(String... ids) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public OperationStatus removeById(List<String> ids) {
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
    
    public String getFirstIndex() {
        return indexes[0];
    }

    public String[] getTypes() {
        return types;
    }
    
    public Index setTypes(String... types) {
        this.types = types;
        return this;
    }

    public String getFirstType() {
        return types == null ? null : types[0];
    }

    public Index addType(String type) {
        // TODO: Implement this call
        return this;
    }
}
