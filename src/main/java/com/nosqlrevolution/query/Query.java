package com.nosqlrevolution.query;

import com.nosqlrevolution.query.Condition.CompletedCondition;
import org.elasticsearch.index.query.QueryBuilder;

/**
 *
 * @author cbrown
 */
public class Query {
    private String[] fields;
    protected String[] sort;
    private String[] highlights;
    protected CompletedCondition[] conditions;
    private int from;
    private int size;
    protected long limit;
    protected long skip;
    protected long blockSize;
    private QueryBuilder queryBuilder;

    public static Query select(String[] fields) {
        return new Query().setFields(fields);
    }

    public static Query select() {
        return new Query();
    }
    
    public String[] getSort() {
        return sort;
    }
    
    public Query sort(String... sort) {
        this.sort = sort;
        return this;
    }

    public Query where(CompletedCondition... conditions) {
        this.conditions = conditions;
        return this;
    }

    public String[] getHighlights() {
        return highlights;
    }

    public void setHighlights(String... highlights) {
        this.highlights = highlights;
    }

    public long getSkip() {
        return skip;
    }
    
    public Query skip(long skip) {
        this.skip = skip;
        return this;
    }

    public int getFrom() {
        return from;
    }

    public void setFrom(int from) {
        this.from = from;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
    
    public long getLimit() {
        return limit;
    }
    
    public Query limit(long limit) {
        this.limit = limit;
        return this;
    }
    
    public long getBlockSize() {
        return blockSize;
    }
    
    public Query blockSize(long blockSize) {
        this.blockSize = blockSize;
        return this;
    }

    public QueryBuilder getQueryBuilder() {
        return queryBuilder;
    }

    public Query setQueryBuilder(QueryBuilder queryBuilder) {
        this.queryBuilder = queryBuilder;
        return this;
    }

    public CompletedCondition[] getConditions() {
        return conditions;
    }
    
    public String[] getFields() {
        return fields;
    }

    public Query setFields(String[] fields) {
        this.fields = fields;
        return this;
    }
}
