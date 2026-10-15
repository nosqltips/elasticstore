package com.nosqlrevolution.query;

import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.nosqlrevolution.query.Condition.CompletedCondition;
import com.nosqlrevolution.service.QueryResolverService;
import java.util.Arrays;
import java.util.Objects;

/** Fluent query configuration. Conditions are immutable; compilation never changes this builder. */
public class ElasticStoreQuery {
    private String[] fields;
    private String[] sort = new String[0];
    private String[] highlights;
    private CompletedCondition[] conditions = new CompletedCondition[0];
    private long skip;
    private long limit;
    private int pageSize = 1000;
    private Query query;

    public static ElasticStoreQuery select(String... fields) { return new ElasticStoreQuery().setFields(fields); }
    public static ElasticStoreQuery select() { return new ElasticStoreQuery(); }

    public ElasticStoreQuery where(CompletedCondition... conditions) {
        Objects.requireNonNull(conditions, "Conditions");
        for (CompletedCondition condition : conditions) Objects.requireNonNull(condition, "Condition");
        this.conditions = conditions.clone();
        query = null;
        return this;
    }

    /** Raw Elasticsearch escape hatch. Replaces fluent conditions; where() replaces it in turn. */
    public ElasticStoreQuery setQuery(Query query) {
        this.query = query;
        conditions = new CompletedCondition[0];
        return this;
    }

    public Query getQuery() { return query; }
    public Query toQuery() { return QueryResolverService.compile(this); }
    public CompletedCondition[] getConditions() { return conditions.clone(); }
    public String[] getFields() { return fields == null ? null : fields.clone(); }
    public ElasticStoreQuery setFields(String... fields) {
        this.fields = names(fields, "Fields");
        return this;
    }

    /** Sort specifications accept an optional leading + (ascending) or - (descending). */
    public ElasticStoreQuery sort(String... sort) {
        this.sort = names(Objects.requireNonNull(sort, "Sort fields"), "Sort fields");
        for (String field : sort) {
            String name = field.startsWith("+") || field.startsWith("-") ? field.substring(1) : field;
            if (name.isEmpty()) throw new IllegalArgumentException("Sort field must not be blank");
        }
        return this;
    }
    public ElasticStoreQuery sort(String field, SortOrder order) {
        Objects.requireNonNull(order, "Sort order");
        names(new String[] { field }, "Sort field");
        String[] appended = Arrays.copyOf(sort, sort.length + 1);
        appended[sort.length] = (order == SortOrder.Desc ? "-" : "+") + field;
        return sort(appended);
    }
    public String[] getSort() { return sort.clone(); }
    public void setHighlights(String... highlights) { this.highlights = names(highlights, "Highlights"); }
    public String[] getHighlights() { return highlights == null ? null : highlights.clone(); }
    public ElasticStoreQuery highlight(String... fields) { setHighlights(fields); return this; }

    public ElasticStoreQuery skip(long skip) {
        if (skip < 0) throw new IllegalArgumentException("Skip must not be negative");
        this.skip = skip;
        return this;
    }
    public long getSkip() { return skip; }
    /** Zero means unlimited, retaining the original query default. */
    public ElasticStoreQuery limit(long limit) {
        if (limit < 0) throw new IllegalArgumentException("Limit must not be negative");
        this.limit = limit;
        return this;
    }
    public long getLimit() { return limit; }
    public ElasticStoreQuery pageSize(int size) {
        if (size <= 0) throw new IllegalArgumentException("Page size must be positive");
        pageSize = size;
        return this;
    }
    public ElasticStoreQuery blockSize(long size) {
        if (size <= 0 || size > Integer.MAX_VALUE) throw new IllegalArgumentException("Page size must be a positive int");
        return pageSize((int) size);
    }
    public long getBlockSize() { return pageSize; }
    public int getSize() { return pageSize; }
    public void setSize(int size) { pageSize(size); }
    public int getFrom() { return Math.toIntExact(skip); }
    public void setFrom(int from) { skip(from); }

    public ElasticStoreQuery copy() {
        ElasticStoreQuery copy = new ElasticStoreQuery();
        copy.fields = getFields();
        copy.sort = getSort();
        copy.highlights = getHighlights();
        copy.conditions = getConditions();
        copy.skip = skip;
        copy.limit = limit;
        copy.pageSize = pageSize;
        copy.query = query;
        return copy;
    }

    private static String[] names(String[] values, String label) {
        if (values == null) return null;
        for (String value : values) {
            if (value == null || value.trim().isEmpty()) throw new IllegalArgumentException(label + " must not contain blank names");
        }
        return values.clone();
    }
}
