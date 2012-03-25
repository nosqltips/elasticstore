package com.nosqlrevolution;

/**
 * This class represents all of the fun things we can do with an ElasticSearch index
 * @author cbrown
 */
public class Index {
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
    
    public long count(QueryBuilder qb) {
        return 0;
    }
    
    public Cursor find() {
        return null;
    }

    public Cursor find(QueryBuilder qb) {
        return null;
    }

    public Cursor find(QueryBuilder qb, Class clazz) {
        return null;
    }

    public Object findById(String id) {
        return null;
    }
    
    public Class findById(String id, Class clazz) {
        return null;
    }
    
    public Object findOne() {
        return null;
    }
    
    public Object findOne(QueryBuilder qb) {
        return null;
    }
    
    public Class findOne(QueryBuilder qb, Class clazz) {
        return null;
    }
    
    // TODO: can do this type safety? inflection? json?
    public Object findAndModify(QueryBuilder qb, Object update) {
        return null;
    }

    // TODO: can do this type safety? inflection? json?
    public Class findAndModify(QueryBuilder qb, Object update, Class clazz) {
        return null;
    }
    
    public Object findAndDelete(QueryBuilder qb) {
        return null;
    }

    public Class findAndDelete(QueryBuilder qb, Class clazz) {
        return null;
    }
    
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
