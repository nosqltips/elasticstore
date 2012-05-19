package com.nosqlrevolution;

import com.nosqlrevolution.query.Query;
import java.util.List;

/**
 * This class represents all of the fun things we can do with an ElasticSearch index
 * @author cbrown
 */
public abstract class Index<T> {
    private ElasticStore store;
    private String index;
    private String type;
    private String idField;
    
    public Index(ElasticStore store, String index, String type) throws Exception {
        if ((store == null) || (! store.isInitialized())) {
            throw new Exception("ElasticStore is not initialized!!!!");
        }
        this.store = store;
        this.index = index;
        this.type = type;
    }

    /**
     * Return a count of all documents in this index or indexes.
     * 
     * @return 
     */
    public abstract long count();
    
    /**
     * Return a count of documents in this index or indexes specified by the query.
     * 
     * @param qb
     * @return 
     */
    public abstract long count(Query qb);
    
    /**
     * Return a Cursor containing a list of all documents in this index or indexes.
     * 
     * @return 
     */
    public Cursor find() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Return a Cursor containing a list of documents in this index or indexes specified by the query.
     * 
     * @param qb
     * @return 
     */
    public Cursor find(Query qb) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Return a Cursor containing a list of documents in this index or indexes specified by the query.
     * The documents will be returned as specified by Class.
     * 
     * @param qb
     * @param clazz
     * @return 
     */
    public Cursor find(Query qb, Class clazz) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Return a single document this index as specified by id.
     * The documents will be returned as specified by Type.
     * 
     * @param id
     * @return 
     */
    public T findOneById(String id) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    /**
     * Return a single document this index as specified by Class.
     * 
     * @param id
     * @param clazz
     * @return 
     */
    public Class findOneById(String id, Class clazz) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    /**
     * Return a Cursor containing a list of documents in this index or indexes specified by the list of ids.
     * 
     * @param ids
     * @return 
     */
    public T[] findManyById(String... ids) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    /**
     * Return a Cursor containing a list of documents in this index or indexes specified by the list of ids.
     * The documents will be returned as specified by Class.
     * 
     * @param clazz
     * @param ids
     * @return 
     */
    public Class[] findManyById(Class clazz, String... ids) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    /**
     * Return a single document from the entire index.
     * The document will be returned as specified by Type.
     * 
     * @return 
     */
    public T findOne() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    /**
     * Return a single document from the entire index.
     * The document will be returned as specified by Class.
     * 
     * @param clazz
     * @return 
     */
    public Class findOne(Class clazz) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    /**
     * Return a single document from the entire index specified by the query.
     * The document will be returned as specified by Type.
     * 
     * @param qb
     * @return 
     */
    public T findOne(Query qb) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    /**
     * Return a single document from the entire index specified by the query.
     * The document will be returned as specified by Class.
     * 
     * @param qb
     * @param clazz
     * @return 
     */
    public Class findOne(Query qb, Class clazz) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Remove documents from the index specified by ids.
     * 
     * @param ids
     * @return 
     */
    public OperationStatus removeById(String... ids) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    /**
     * Remove documents from the index specified by list of ids.
     * 
     * @param ids
     * @return 
     */
    public OperationStatus removeById(List<String> ids) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    /**
     * Remove specified document from the index.
     * Id is specified by annotation, specified field, or id or _id fields.
     * Error is generated if document id cannot be found.
     * 
     * @param t
     * @return 
     */
    public OperationStatus remove(T t) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    /**
     * Remove documents from the index as specified by the query.
     * 
     * @param qb
     * @return 
     */
    public OperationStatus remove(Query qb) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    /**
     * Write one or more documents to the index.
     * 
     * @param t
     * @return 
     */
    public OperationStatus write(T... t) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    /**
     * Write one or more documents to the index.
     * Write parameters can be specified by a WriteBuilder.
     * 
     * @param builder
     * @param t
     * @return 
     */
    public OperationStatus write(WriteBuilder builder, T... t) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    /**
     * Write one or more documents to the index.
     * 
     * @param t
     * @return 
     */
    public OperationStatus write(List<? extends T> t) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    /**
     * Write one or more documents to the index.
     * Write parameters can be specified by a WriteBuilder.
     * 
     * @param builder
     * @param t
     * @return 
     */
    public OperationStatus write(WriteBuilder builder, List<? extends T> t) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    // Accessor methods
    protected String getIndex() {
        return index;
    }

    protected String getType() {
        return type;
    }
    
    protected Index setType(String type) {
        this.type = type;
        return this;
    }

    protected Index addType(String type) {
        // TODO: Implement this call
        return this;
    }

    protected String getIdField() {
        return idField;
    }
    
    protected Index setIdField(String idField) {
        this.idField = idField;
        return this;
    }
}
