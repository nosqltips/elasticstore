package com.nosqlrevolution.util;

import com.nosqlrevolution.enums.Wildcard;
import java.util.List;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import static org.elasticsearch.index.query.QueryBuilders.*;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortOrder;

/**
 *
 * @author cbrown
 */
public class QueryUtil {
    public static QueryBuilder getTermQuery(String field, String value) {
        return termQuery(field, value);
    }

    public static QueryBuilder getTermsQueryBuilder(String field, String[] terms) {
        return termsQuery(field, terms);
    }
    
    public static QueryBuilder getTermQueryBuilder(String field, String term) {
        return termQuery(field, term)
                .boost(1.0F);
    }
    
    public static QueryBuilder getIdsQueryBuilder(String field, List<String> ids) {
        return termsQuery(field, ids);
    }
    
    public static QueryBuilder getTermQueryNoBoostBuilder(String field, String term) {
        return termQuery(field, term);
    }
    
    public static QueryBuilder getBoostedTermQueryBuilder(String field, String term, float boost) {
        return termQuery(field, term)
                .boost(boost);
    }    
        
    public static QueryBuilder getGtRangeQuery(String field, String value) {
        return rangeQuery(field)
                .gte(value);
    }

    public static QueryBuilder getMatchQuery(String field, String term) {
        return matchQuery(field, term);
    }
    
    public static QueryBuilder getMatchAllQuery() {
        return matchAllQuery();
    }

    public static QueryBuilder getPhraseQuery(String field, String phrase) {
        return matchPhraseQuery(field, phrase);
    }
    
    public static QueryBuilder getExistsQuery(String field) {
        return existsQuery(field);
    }
    
    public static QueryBuilder getIdQuery(String[] ids) {
        return idsQuery()
                .addIds(ids);
    }
    
    public static SortBuilder getIdSort() {
        SortBuilder sort = new FieldSortBuilder("id");
        sort.order(SortOrder.ASC);
        return sort;
    }

    public static QueryBuilder getBooleanMustQuery(QueryBuilder... builders) {
        BoolQueryBuilder bq = boolQuery();
        for (QueryBuilder builder : builders) {
            if (builder != null) {
                bq.must(builder);
            }
        }
        return bq;
    }
    
    public static QueryBuilder getBooleanMustNotQuery(QueryBuilder... builders) {
        BoolQueryBuilder bq = boolQuery();
        for (QueryBuilder builder : builders) {
            if (builder != null) {
                bq.mustNot(builder);
            }
        }
        return bq;
    }
    
    public static QueryBuilder getBooleanShouldQuery(List<QueryBuilder> builders) {
        BoolQueryBuilder bq = boolQuery();
        for (QueryBuilder builder : builders) {
            if (builder != null) {
                bq.should(builder);
            }
        }
        
        bq.minimumNumberShouldMatch(1);
        return bq;
    }
    
    public static QueryBuilder getBooleanShouldQuery(QueryBuilder... builders) {
        BoolQueryBuilder bq = boolQuery();
        for (QueryBuilder builder : builders) {
            if (builder != null) {
                bq.should(builder);
            }
        }
        
        bq.minimumNumberShouldMatch(1);
        return bq;
    }
    
    public static QueryBuilder getQueryBuilder(String field, String term) {
        Wildcard wild = ParseUtil.isWildcard(term);
        if (wild == Wildcard.NONE) {
            //System.out.println("Query Wildcard.NONE " + term);
            return termQuery(field, term);
        } else if (wild == Wildcard.PREFIX) {
            //System.out.println("Query Wildcard.PREFIX " + term.substring(0, term.length() -1));
            return prefixQuery(field, term.substring(0, term.length() -1));
        } else {
            //System.out.println("Query Wildcard.FULL " + term);
            return wildcardQuery(field, term);
        }       
    }

    // TODO: range filters are also available
    public static QueryBuilder getRangeQueryBuilder(String field, String from, String to) {
        if (from == null && to == null) {
            return null;
        }
        
        // Range <
        if (from == null) {
            return rangeQuery(field)
                .lt(to);
        }
        
        // Range >
        if (to == null) {
            return rangeQuery(field)
                .gt(from);
        }

        // Range inclusive
        return rangeQuery(field)
            .from(from)
            .to(to);
    }
    
    public static QueryBuilder getTextQuery(String field, String terms) {
        return matchQuery(field, terms)
                .slop(1)
                .analyzer("simple");                
    }    
 }
