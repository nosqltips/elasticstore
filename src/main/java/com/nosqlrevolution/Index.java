package com.nosqlrevolution;

import com.nosqlrevolution.query.Query;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class represents all of the fun things we can do with an ElasticSearch index
 * @author cbrown
 * @param <T>
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
     * @param query
     * @return 
     */
    public abstract long count(Query query);
    
    /**
     * Return a single document from the entire index.
     * The document will be returned as specified by Type.
     * 
     * @return 
     */
    public T find() {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Return a single document from the entire index.
     * The document will be returned as specified by Class.
     * 
     * @param <T>
     * @param clazz
     * @return 
     */
    public <T>T find(Class<T> clazz) {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Return a single document from the entire index specified by the query.
     * The document will be returned as specified by Type.
     * 
     * @param query
     * @return 
     */
    public T find(Query query) {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Return a single document from the entire index specified by the query.
     * The document will be returned as specified by Class.
     * 
     * @param <T>
     * @param query
     * @param clazz
     * @return 
     */
    public <T>T find(Query query, Class<T> clazz) {
        throw new UnsupportedOperationException();
    }

    /**
     * Return a Cursor containing a list of all documents in this index or indexes.
     * 
     * @return 
     */
    public Cursor findAll() {
        throw new UnsupportedOperationException();
    }

    /**
     * Return a Cursor containing a list of documents in this index or indexes specified by the query.
     * 
     * @param query
     * @return 
     */
    public Cursor findAll(Query query) {
        throw new UnsupportedOperationException();
    }

    /**
     * Return a Cursor containing a list of documents in this index or indexes specified by the query.
     * The documents will be returned as specified by Class.
     * 
     * @param query
     * @param clazz
     * @return 
     */
    public Cursor findAll(Query query, Class clazz) {
        throw new UnsupportedOperationException();
    }

    /**
     * Return a single document this index as specified by id.
     * The documents will be returned as specified by Type.
     * 
     * @param id
     * @return 
     */
    public T findById(String id) {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Return a single document this index as specified by Class.
     * 
     * @param <T>
     * @param id
     * @param clazz
     * @return 
     */
    public <T>T findById(String id, Class<T> clazz) {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Return a Cursor containing a list of documents in this index or indexes specified by the list of ids.
     * 
     * @param ids
     * @return 
     */
    public Cursor findAllById(String... ids) {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Return a Cursor containing a list of documents in this index or indexes specified by the list of ids.
     * The documents will be returned as specified by Class.
     * 
     * @param clazz
     * @param ids
     * @return 
     */
    public Cursor<T> findAllById(Class<T> clazz, String... ids) {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Remove documents from the index specified by ids.
     * 
     * @param ids
     * @return 
     */
    public OperationStatus removeById(String... ids) {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Remove documents from the index specified by list of ids.
     * 
     * @param ids
     * @return 
     */
    public OperationStatus removeById(List<String> ids) {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Remove specified document from the index.
     * Id is specified by annotation, specified field, or id or _id fields.
     * Error is generated if document id cannot be found.
     * 
     * @param t
     * @return 
     */
    public OperationStatus remove(T... t) {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Remove documents from the index as specified by the query.
     * 
     * @param query
     * @return 
     */
    public OperationStatus remove(Query query) {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Write one or more documents to the index.
     * 
     * @param t
     * @return 
     */
    public OperationStatus write(T... t) {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Write one or more documents to the index.
     * Write parameters can be specified by a WriteOperation.
     * 
     * @param builder
     * @param t
     * @return 
     */
    public OperationStatus write(WriteOperation builder, T... t) {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Write one or more documents to the index.
     * 
     * @param t
     * @return 
     */
    public OperationStatus write(List<? extends T> t) {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Write one or more documents to the index.
     * Write parameters can be specified by a WriteOperation.
     * 
     * @param builder
     * @param t
     * @return 
     */
    public OperationStatus write(WriteOperation builder, List<? extends T> t) {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Applies the json schema document to this index.
     * If this index already has a schema, then essentially only new fields can be added.
     * Existing fields cannot be modified.
     * 
     * @param mapping
     */
    public void applyMapping(String mapping) {
        store.applyMapping(mapping, true, type, index);
    }
    
    public void refresh() {
        // TODO: maybe do something better here.
        try {
            store.refreshIndex(this);
        } catch (Exception ex) {
            Logger.getLogger(Index.class.getName()).log(Level.SEVERE, null, ex);
        }
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
