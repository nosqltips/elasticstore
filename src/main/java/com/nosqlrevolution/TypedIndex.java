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
    public T findOneById(Object id) {
        return super.findOneById(id);
    }
    
    @Override
    public Class findOneById(Object id, Class clazz) {
        return super.findOneById(id, clazz);
    }

    @Override
    public T findManyById(Object... ids) {
        return super.findManyById(ids);
    }
    
    @Override
    public Class findManyById(Class clazz, Object... ids) {
        throw new UnsupportedOperationException("Not supported yet.");
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
    public OperationStatus removeById(Object... ids) {
        return super.removeById(ids);
    }
    
    @Override
    public OperationStatus removeById(List<Object> ids) {
        return super.removeById(ids);
    }
    
    @Override
    public OperationStatus remove(T t) {
        return super.remove(t);
    }
    
    @Override
    public OperationStatus remove(QueryBuilder qb) {
        return super.remove(qb);
    }
    
    @Override
    public OperationStatus write(T... t) {
        return super.write(t);
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
}
