package com.nosqlrevolution.query;

import com.nosqlrevolution.query.Condition.CompletedCondition;

/**
 *
 * @author cbrown
 */
public class ESQueryImpl extends ESQuery {

    public ESQueryImpl() {}

    public static ESQuery select(String... fields) {
        return new ESQueryImpl().setFields(fields);
    }

    public static ESQuery select() {
        return new ESQueryImpl();
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
    
    protected ESQuery setFields(String... fields) {
        this.fields = fields;
        return this;
    }        
}
