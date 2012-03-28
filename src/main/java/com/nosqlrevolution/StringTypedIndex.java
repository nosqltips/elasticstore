package com.nosqlrevolution;

import java.util.List;

/**
 * Accept and return json strings
 * 
 * @author cbrown
 */
public class StringTypedIndex extends Index<String> {
    public StringTypedIndex(ElasticStore store, String[] indexes) throws Exception {
        super(store, indexes);
    }

    @Override
    public Cursor find() {
        return super.find();
    }

    @Override
    public Cursor find(QueryBuilder qb) {
        return super.find(qb);
    }

    @Override
    public Cursor find(QueryBuilder qb, Class clazz) {
        return super.find(qb, clazz);
    }

    @Override
    public String findById(Object id) {
        return super.findById(id);
    }
    
    @Override
    public Class findById(Object id, Class clazz) {
        return super.findById(id, clazz);
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
    public String findOne(QueryBuilder qb) {
        return super.findOne(qb);
    }
    
    @Override
    public Class findOne(QueryBuilder qb, Class clazz) {
        return super.findOne(qb, clazz);
    }
        
    @Override
    public OperationStatus write(String json) {
        return super.write(json);
    }
    
    @Override
    public OperationStatus write(String json, WriteBuilder builder) {
        return super.write(json, builder);
    }
    
    @Override
    public OperationStatus write(String[] json) {
        return super.write(json);
    }
    
    @Override
    public OperationStatus write(String[] json, WriteBuilder builder) {
        return super.write(json, builder);
    }
    
    @Override
    public OperationStatus write(List<? extends String> json) {
        return super.write(json);
    }
    
    @Override
    public OperationStatus write(List<? extends String> json, WriteBuilder builder) {
        return super.write(json, builder);
    }
}
