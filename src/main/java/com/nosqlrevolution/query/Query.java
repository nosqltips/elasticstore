package com.nosqlrevolution.query;

import com.nosqlrevolution.query.Condition.CompletedCondition;

/**
 *
 * @author cbrown
 */
public abstract class Query {
    protected String[] fields;
    protected String[] sort;
    protected CompletedCondition[] conditions;
    protected long limit;
    protected long skip;
    protected long blockSize;

    public static Query select(String... fields) {
        return new QueryImpl().setFields(fields);
    }

    public static Query select() {
        return new QueryImpl();
    }
    
    public Query sort(String... sort) {
        this.sort = sort;
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
}
