package com.nosqlrevolution.query;

import com.nosqlrevolution.query.Condition.CompletedCondition;

/**
 *
 * @author cbrown
 */
public class Query {
    private Object[] select;
    private Object[] in;
    private Object[] contains;
    private Object[] sort;
    private CompletedCondition[] conditions;
    private long limit;
    private long skip;
    private long blockSize;

    public static Query select(Object... fields) {
        return new Query().setFields(fields);
    }

    public static Query select() {
        return new Query();
    }

    public Query in(Object... ids) {
        this.in = ids;
        return this;
    }

    public Query contains(Object... ids) {
        this.contains = ids;
        return this;
    }

    public Query sort(Object... fields) {
        this.sort = fields;
        return this;
    }

    public Query where(CompletedCondition... conditions) {
        this.conditions = conditions;
        return this;
    }

    public Query skip(long skip) {
        this.skip = skip;
        return this;
    }

    public Query limit(long limit) {
        this.limit = limit;
        return this;
    }

    public Query blockSize(long blockSize) {
        this.blockSize = blockSize;
        return this;
    }
    
    private Query setFields(Object... fields) {
        this.setFields(fields);
        return this;
    }
}
