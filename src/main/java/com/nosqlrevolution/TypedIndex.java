package com.nosqlrevolution;

import java.util.List;

/**
 * Accept and return strongly typed objects.
 * 
 * @author cbrown
 */
public class TypedIndex<T> extends Index<T> {    
    T type;
    public TypedIndex(T type, ElasticStore store, String... indexes) throws Exception {
        super (store, indexes);
        this.type = type;
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
    public T findById(Object id) {
        return super.findById(id);
    }
    
    @Override
    public Class findById(Object id, Class clazz) {
        return super.findById(id, clazz);
    }

   @Override
    public T findOne() {
        return super.findOne();
    }
    
    @Override
    public Class findOne(Class clazz) {
        return super.findOne(clazz);
    }
    
    @Override
    public T findOne(QueryBuilder qb) {
        return super.findOne(qb);
    }
    
    @Override
    public Class findOne(QueryBuilder qb, Class clazz) {
        return super.findOne(qb, clazz);
    }
    
    @Override
    public OperationStatus write(T t) {
        return super.write(t);
    }
    
    @Override
    public OperationStatus write(T t, WriteBuilder builder) {
        return super.write(t, builder);
    }
    
    @Override
    public OperationStatus write(T[] t) {
        return super.write(t);
    }
    
    @Override
    public OperationStatus write(T[] t, WriteBuilder builder) {
        return super.write(t, builder);
    }
    
    @Override
    public OperationStatus write(List<? extends T> t) {
        return super.write(t);
    }
    
    @Override
    public OperationStatus write(List<? extends T> t, WriteBuilder builder) {
        return super.write(t, builder);
    }
}
