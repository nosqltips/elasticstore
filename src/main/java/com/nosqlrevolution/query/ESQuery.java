package com.nosqlrevolution.query;

import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.nosqlrevolution.query.Condition.CompletedCondition;

/** Compatibility facade; new callers can use ElasticStoreQuery directly. */
@Deprecated
public abstract class ESQuery extends ElasticStoreQuery {
    public static ESQuery select(String... fields) { return new ESQueryImpl().setFields(fields); }
    public static ESQuery select() { return new ESQueryImpl(); }
    @Override public ESQuery setFields(String... fields) { super.setFields(fields); return this; }
    @Override public ESQuery where(CompletedCondition... conditions) { super.where(conditions); return this; }
    @Override public ESQuery sort(String... sort) { super.sort(sort); return this; }
    @Override public ESQuery sort(String field, SortOrder order) { super.sort(field, order); return this; }
    @Override public ESQuery skip(long skip) { super.skip(skip); return this; }
    @Override public ESQuery limit(long limit) { super.limit(limit); return this; }
    @Override public ESQuery blockSize(long size) { super.blockSize(size); return this; }
    @Override public ESQuery pageSize(int size) { super.pageSize(size); return this; }
    @Override public ESQuery highlight(String... fields) { super.highlight(fields); return this; }
    @Override public ESQuery setQuery(Query query) { super.setQuery(query); return this; }
}
