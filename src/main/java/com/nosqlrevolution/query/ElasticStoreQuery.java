package com.nosqlrevolution.query;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBase;
import com.nosqlrevolution.query.Condition.CompletedCondition;


/**
 *
 * @author cbrown
 */
public class ElasticStoreQuery {
    private String[] fields;
    protected String[] sort;
    private String[] highlights;
    protected CompletedCondition[] conditions;
    private int from;
    private int size;
    protected long limit;
    protected long skip;
    protected long blockSize;
    private Query query;

    public static ElasticStoreQuery select(String[] fields) {
        return new ElasticStoreQuery().setFields(fields);
    }

    public static ElasticStoreQuery select() {
        return new ElasticStoreQuery();
    }
    
    public String[] getSort() {
        return sort;
    }
    
    public ElasticStoreQuery sort(String... sort) {
        this.sort = sort;
        return this;
    }

    public ElasticStoreQuery where(CompletedCondition... conditions) {
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
    
    public ElasticStoreQuery skip(long skip) {
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
    
    public ElasticStoreQuery limit(long limit) {
        this.limit = limit;
        return this;
    }
    
    public long getBlockSize() {
        return blockSize;
    }
    
    public ElasticStoreQuery blockSize(long blockSize) {
        this.blockSize = blockSize;
        return this;
    }

    public Query getQuery() {
        return query;
    }

    public ElasticStoreQuery setQuery(Query query) {
        this.query = query;
        return this;
    }

    public CompletedCondition[] getConditions() {
        return conditions;
    }
    
    public String[] getFields() {
        return fields;
    }

    public ElasticStoreQuery setFields(String[] fields) {
        this.fields = fields;
        return this;
    }
}
