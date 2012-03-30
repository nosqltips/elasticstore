package com.nosqlrevolution;

/**
 *
 * @author cbrown
 */
public class QueryBuilder {
    private Object[] select;
    private Object[] in;
    private Object[] contains;
    private Object[] sort;
    private QueryExpression[] expressions;
    private long limit;
    private long skip;
    private long blockSize;

    public QueryBuilder select(Object... fields) {
        this.select = fields;
        return this;
    }

    public QueryBuilder select() {
        return this;
    }

    public QueryBuilder in(Object... ids) {
        this.in = ids;
        return this;
    }

    public QueryBuilder contains(Object... ids) {
        this.contains = ids;
        return this;
    }

    public QueryBuilder sort(Object... fields) {
        this.sort = fields;
        return this;
    }

    public QueryBuilder where(QueryExpression ... expressions) {
        this.expressions = expressions;
        return this;
    }

    public QueryBuilder skip(long skip) {
        this.skip = skip;
        return this;
    }

    public QueryBuilder limit(long limit) {
        this.limit = limit;
        return this;
    }

    public QueryBuilder blockSize(long blockSize) {
        this.blockSize = blockSize;
        return this;
    }
}
