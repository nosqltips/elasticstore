package com.nosqlrevolution.util;

import com.nosqlrevolution.enums.Wildcard;
import java.util.List;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.DisMaxQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.ExistsQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.IdsQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchPhraseQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchAllQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.PrefixQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.TermsQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.WildcardQuery;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TermsQueryField;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.json.JsonpMapper;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 *
 * @author cbrown
 */
public class QueryUtil {
    public static JsonpMapper mapper = new JacksonJsonpMapper();
    
    public static Query getDisMaxQuery(List<Query> queries) {
        return DisMaxQuery.of(b -> b
                .queries(queries))._toQuery();
    }

    public static Query getTermQuery(String field, String value) {
        return TermQuery.of(b -> b
                .field(field)
                .value(value)
            )._toQuery();
    }

    public static Query getTermsQuery(String field, String[] terms) {
        return getTermsQuery(field, Arrays.asList(terms));
    }
    
    public static Query getTermsQuery(String field, List<String> terms) {
        List<FieldValue> values = terms.stream()
                .map(x -> FieldValue.of(x))
                .collect(Collectors.toList());
        return TermsQuery.of(b -> b
                .field(field)
                .terms(TermsQueryField.of(t -> t
                        .value(values)))
        )._toQuery();
    }
    
    public static Query getIdsQuery(String field, List<String> ids) {
        return IdsQuery.of(b -> b
                .values(ids)
            )._toQuery();
    }
    
    public static Query getTermQueryNoBoost(String field, String term) {
        return TermQuery.of(b -> b
                .field(field)
                .value(term)
            )._toQuery();
    }
    
    public static Query getBoostedTermQuery(String field, String term, float boost) {
        return TermQuery.of(b -> b
                .field(field)
                .value(term)
                .boost(boost)
            )._toQuery();
    }    
        
    public static Query getGtRangeQuery(String field, String value) {
        return RangeQuery.of(b -> b
                .field(field)
                .gt(JsonData.of(value, mapper))
            )._toQuery();
    }

    public static Query getMatchQuery(String field, String term) {
        return MatchQuery.of(b -> b
                .field(field)
                .query(term)
            )._toQuery();
    }
    
    public static Query getMatchAllQuery() {
        return MatchAllQuery.of(b -> b)._toQuery();
    }

    public static Query getPhraseQuery(String field, String phrase) {
        return MatchPhraseQuery.of(b -> b
                .field(field)
                .query(phrase)
            )._toQuery();
    }
    
    public static Query getExistsQuery(String field) {
        return ExistsQuery.of(b -> b
                .field(field)
            )._toQuery();
    }
    
    public static Query getIdQuery(List<String> ids) {
        return IdsQuery.of(b -> b
                .values(ids)
            )._toQuery();
    }
    
    public static Query getWildcardQuery(String field, String term) {
        return WildcardQuery.of(b -> b
                .field(field)
                .wildcard(term)
            )._toQuery();
    }
        
    public static Query getPrefixQuery(String field, String term) {
        return PrefixQuery.of(b -> b
                .field(field)
                .value(term)
            )._toQuery();
    }
    
    public static Query getTextQuery(String field, String terms) {
        return MatchQuery.of(b -> b
                .field(field)
                .query(terms)
                .analyzer("simple")
            )._toQuery();
    }    
            
    public static Query getBooleanMustQuery(List<Query> builders) {
        return BoolQuery.of(b -> b.must(builders))._toQuery();
    }
    
    public static Query getBooleanMustQuery(Query... builders) {
        return getBooleanMustQuery(Arrays.asList(builders));
    }
    
    public static Query getBooleanMustNotQuery(Query... builders) {
        return getBooleanMustNotQuery(Arrays.asList(builders));
    }
    
    public static Query getBooleanMustNotQuery(List<Query> builders) {
        return BoolQuery.of(b -> b.mustNot(builders))._toQuery();
    }
    
    public static Query getBooleanShouldQuery(Query... builders) {
        return getBooleanShouldQuery(Arrays.asList(builders));
    }
    
    public static Query getBooleanShouldQuery(List<Query> builders) {
        return BoolQuery.of(b -> b
                .should(builders)
                .minimumShouldMatch("1")
            )._toQuery();
    }

    public SortOptions getIdSort() {
        return SortOptions.of(s -> s
            .field(f -> f
                    .field("id")
                    .order(SortOrder.Asc)
            ));
    }

    public static Query getBooleanQuery(Query[] must, Query[] should, Query[] mustnot) {
        BoolQuery.Builder b = new BoolQuery.Builder();
        if (must != null && must.length != 0) {
            b.must(Arrays.asList(must));
        }

        if (should != null && should.length != 0) {
            b.should(Arrays.asList(should));
            b.minimumShouldMatch("1");
        }

        if (mustnot != null && mustnot.length != 0) {
            b.mustNot(Arrays.asList(mustnot));
        }

        return b.build()._toQuery();
    }

    
    public static Query getQueryBuilder(String field, String term) {
        Wildcard wild = ParseUtil.isWildcard(term);
        if (null == wild) {
            //System.out.println("Query Wildcard.FULL " + term);
            return getWildcardQuery(field, term);
        } else switch (wild) {
            case NONE:
                //System.out.println("Query Wildcard.NONE " + term);
                return getTermQuery(field, term);
            case PREFIX:
                //System.out.println("Query Wildcard.PREFIX " + term.substring(0, term.length() -1));
                return getPrefixQuery(field, term.substring(0, term.length() -1));
            default:
                //System.out.println("Query Wildcard.FULL " + term);
                return getWildcardQuery(field, term);
        }       
    }

    // TODO: range filters are also available
    public static Query getRangeQueryBuilder(String field, String from, String to) {
        if (from == null && to == null) {
            return null;
        }
        
        // Range <
        if (from == null) {
            return RangeQuery.of(b -> b
                .field(field)
                .lt(JsonData.of(to, mapper))
              )._toQuery();
        }
        
        // Range >
        if (to == null) {
            return RangeQuery.of(b -> b
                .field(field)
                .gt(JsonData.of(from, mapper))
            )._toQuery();
        }
        
        // Range inclusive
        return RangeQuery.of(b -> b
            .field(field)
            .gte(JsonData.of(from, mapper))
            .lte(JsonData.of(to, mapper))
        )._toQuery();
    }
}
