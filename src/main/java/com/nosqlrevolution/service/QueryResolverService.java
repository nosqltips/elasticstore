package com.nosqlrevolution.service;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import co.elastic.clients.json.JsonData;
import com.nosqlrevolution.query.Condition.CompletedCondition;
import com.nosqlrevolution.query.ElasticStoreQuery;
import com.nosqlrevolution.query.Field;
import com.nosqlrevolution.query.Operator;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/** Compiles immutable conditions without caching or modifying the query builder. */
public class QueryResolverService {
    public static ElasticStoreQuery resolve(ElasticStoreQuery query) {
        return query == null ? null : query.copy().setQuery(compile(query));
    }

    public static Query compile(ElasticStoreQuery query) {
        if (query == null) return Query.of(q -> q.matchAll(m -> m));
        if (query.getQuery() != null) return query.getQuery();
        CompletedCondition[] conditions = query.getConditions();
        if (conditions.length == 0) return Query.of(q -> q.matchAll(m -> m));
        return conjunction(Arrays.stream(conditions).map(QueryResolverService::compile).collect(Collectors.toList()));
    }

    private static Query conjunction(List<Query> children) {
        return Query.of(q -> q.bool(b -> b.filter(children)));
    }

    private static Query negate(Query child) { return Query.of(q -> q.bool(b -> b.mustNot(child))); }

    public static Query compile(CompletedCondition condition) {
        Operator operator = condition.getOperator();
        CompletedCondition[] children = condition.getConditions();
        if (children.length > 0) {
            List<Query> queries = Arrays.stream(children).map(QueryResolverService::compile).collect(Collectors.toList());
            switch (operator) {
                case AND: return conjunction(queries);
                case OR: return Query.of(q -> q.bool(b -> b.should(queries).minimumShouldMatch("1")));
                case NOT: return negate(conjunction(queries));
                default: throw new IllegalArgumentException("Unsupported group: " + operator);
            }
        }
        String field = condition.getSpecialField() == Field.ID ? "_id" : condition.getField();
        Object[] values = condition.getTypedValues();
        if ("_id".equals(field)) {
            if (!(operator == Operator.EQUAL || operator == Operator.NOT_EQUAL || operator == Operator.IN || operator == Operator.NOT_IN)) {
                throw new IllegalArgumentException("Document IDs support only equality and membership");
            }
            Query ids = Query.of(q -> q.ids(i -> i.values(Arrays.stream(values).map(String::valueOf).collect(Collectors.toList()))));
            return operator == Operator.NOT_EQUAL || operator == Operator.NOT_IN ? negate(ids) : ids;
        }
        switch (operator) {
            case EQUAL: return Query.of(q -> q.term(t -> t.field(field).value(value(values[0]))));
            case NOT_EQUAL: return negate(Query.of(q -> q.term(t -> t.field(field).value(value(values[0])))));
            case IN:
            case NOT_IN:
                Query terms = Query.of(q -> q.terms(t -> t.field(field).terms(v -> v.value(
                        Arrays.stream(values).map(QueryResolverService::value).collect(Collectors.toList())))));
                return operator == Operator.NOT_IN ? negate(terms) : terms;
            case GT: case GTE: case LT: case LTE:
                if (values[0] instanceof Boolean) throw new IllegalArgumentException("Range bounds cannot be boolean");
                RangeQuery.Builder range = new RangeQuery.Builder().field(field);
                JsonData bound = JsonData.of(values[0]);
                switch (operator) {
                    case GT: range.gt(bound); break;
                    case GTE: range.gte(bound); break;
                    case LT: range.lt(bound); break;
                    case LTE: range.lte(bound); break;
                    default: throw new AssertionError(operator);
                }
                return range.build()._toQuery();
            case EXISTS: return Query.of(q -> q.exists(e -> e.field(field)));
            case MATCH: return text(field, values[0], false);
            case MATCH_PHRASE: return Query.of(q -> q.matchPhrase(m -> m.field(field).query((String) values[0])));
            case MUST: case SHOULD: case MUST_NOT:
                List<Query> matches = Arrays.stream(values).map(v -> text(field, v, operator == Operator.MUST)).collect(Collectors.toList());
                if (operator == Operator.MUST) return conjunction(matches);
                if (operator == Operator.MUST_NOT) return Query.of(q -> q.bool(b -> b.mustNot(matches)));
                return Query.of(q -> q.bool(b -> b.should(matches).minimumShouldMatch("1")));
            default: throw new IllegalArgumentException("Unsupported condition: " + operator);
        }
    }

    private static Query text(String field, Object text, boolean allWords) {
        return Query.of(q -> q.match(m -> m.field(field).query((String) text).operator(allWords
                ? co.elastic.clients.elasticsearch._types.query_dsl.Operator.And
                : co.elastic.clients.elasticsearch._types.query_dsl.Operator.Or)));
    }

    private static FieldValue value(Object value) {
        if (value instanceof String) return FieldValue.of((String) value);
        if (value instanceof Boolean) return FieldValue.of((Boolean) value);
        if (value instanceof Float || value instanceof Double) return FieldValue.of(((Number) value).doubleValue());
        return FieldValue.of(((Number) value).longValue());
    }
}
