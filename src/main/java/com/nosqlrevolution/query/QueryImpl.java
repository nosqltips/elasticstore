package com.nosqlrevolution.query;

import com.nosqlrevolution.query.Condition.CompletedCondition;

/**
 *
 * @author cbrown
 */
public class QueryImpl extends Query {

    public QueryImpl() {}

    public static Query select(String... fields) {
        return new QueryImpl().setFields(fields);
    }

    public static Query select() {
        return new QueryImpl();
    }

    public String[] getSort() {
        return sort;
    }

    public CompletedCondition[] getConditions() {
        return conditions;
    }

    public long getSkip() {
        return skip;
    }

    public long getLimit() {
        return limit;
    }
    
    public long getBlockSize() {
        return blockSize;
    }
    
    public String[] getFields() {
        return fields;
    }
    
    protected Query setFields(String... fields) {
        this.fields = fields;
        return this;
    }        
}
