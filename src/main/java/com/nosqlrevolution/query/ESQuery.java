package com.nosqlrevolution.query;

import com.nosqlrevolution.query.Condition.CompletedCondition;

/**
 *
 * @author cbrown
 */
public abstract class ESQuery {
    protected String[] fields;
    protected String[] sort;
    protected CompletedCondition[] conditions;
    protected long limit;
    protected long skip;
    protected long blockSize;

    public static ESQuery select(String... fields) {
        return new ESQueryImpl().setFields(fields);
    }

    public static ESQuery select() {
        return new ESQueryImpl();
    }
    
    public ESQuery sort(String... sort) {
        this.sort = sort;
        return this;
    }

    public ESQuery where(CompletedCondition... conditions) {
        this.conditions = conditions;
        return this;
    }

    public ESQuery skip(long skip) {
        this.skip = skip;
        return this;
    }
    
    public ESQuery limit(long limit) {
        this.limit = limit;
        return this;
    }
    
    public ESQuery blockSize(long blockSize) {
        this.blockSize = blockSize;
        return this;
    }
}
