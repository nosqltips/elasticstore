package com.nosqlrevolution;

import com.nosqlrevolution.query.Query;
import com.nosqlrevolution.service.QueryService;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * Accept and return json strings
 * 
 * @author cbrown
 */
public class JsonIndex extends Index<String> {
    private QueryService service;
    private ObjectMapper mapper = new ObjectMapper();
    
    public JsonIndex(ElasticStore store, String index, String type) throws Exception {
        super(store, index, type);
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
    public String findOneById(String id) {
        return service.realTimeGet(getIndex(), getType(), id);
    }
    
    @Override
    public Class findOneById(String id, Class clazz) {
        try {
            String s = service.realTimeGet(getIndex(), getType(), id);
            // Todo probably put this into a util class for broad use
            return (Class) (mapper.readValue(s, clazz.getClass()));
        } catch (IOException ex) {
            Logger.getLogger(JsonIndex.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    @Override
    public String[] findManyById(String... ids) {
        return service.realTimeMultiGet(getIndex(), getType(), ids);
    }
    
    @Override
    public Class[] findManyById(Class clazz, String... ids) {
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
    public OperationStatus removeById(String... id) {
        return super.removeById(id);
    }
    
    @Override
    public OperationStatus removeById(List<String> id) {
        return super.removeById(id);
    }
    
    // These remove may not actually work for json because we don't know what the id is
    // Assume _id? Just stuff it in and ES will create the id?
    // Maybe look for _id or id?
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
        // TODO: need to look at list, optimize for 1
        // TODO: need to pull out the id for indexing
        try {
            service.index(getIndex(), getType(), mapper.writeValueAsString(json[0]), "1");
        } catch (IOException ex) {
            Logger.getLogger(TypedIndex.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
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
