package com.nosqlrevolution;

import com.nosqlrevolution.query.Query;
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
    public Cursor find(Query qb) {
        return super.find(qb);
    }

    @Override
    public Cursor find(Query qb, Class clazz) {
        return super.find(qb, clazz);
    }

    @Override
    public String findOneById(Object id) {
        return super.findOneById(id);
    }
    
    @Override
    public Class findOneById(Object id, Class clazz) {
        return super.findOneById(id, clazz);
    }
    
    @Override
    public String findManyById(Object... ids) {
        return super.findManyById(ids);
    }
    
    @Override
    public Class findManyById(Class clazz, Object... ids) {
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
    public OperationStatus removeById(Object... id) {
        return super.removeById(id);
    }
    
    @Override
    public OperationStatus removeById(List<Object> id) {
        return super.removeById(id);
    }
    
    // These remove may not actually work for json because we don't know what the id is
    // Assume _id? Just stuff it in and ES will create the id?
    @Override
    public OperationStatus remove(String json) {
        return super.remove(json);
    }
    
    @Override
    public OperationStatus remove(Query qb) {
        return super.remove(qb);
    }

    @Override
    public OperationStatus write(String... json) {
        return super.write(json);
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
