package com.nosqlrevolution.service;

import com.nosqlrevolution.query.Query;
import org.elasticsearch.index.query.QueryBuilder;

/**
 * This class resolves queries using the query builder objects.
 * 
 * @author cbrown
 */
public class QueryResolverService {
    public static QueryBuilder resolve(Query query) {
        if (query == null) {
            return null;
        }
        
        
        return null;
    }
}
