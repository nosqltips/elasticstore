package com.nosqlrevolution.util;

import com.nosqlrevolution.enums.Wildcard;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import static org.elasticsearch.index.query.QueryBuilders.*;
import static org.elasticsearch.index.query.FilterBuilders.*;
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
    
    public static QueryBuilder getFilteredQuery(QueryBuilder qb1, FilterBuilder fb1) {
        return filteredQuery(qb1, fb1);
    }

    public static QueryBuilder getMatchAllQuery() {
        return matchAllQuery();
    }

    public static FilterBuilder getMatchAllFilter() {
        return matchAllFilter();
    }
   
    public static QueryBuilder getIdQuery(String[] ids) {
        return idsQuery()
                .addIds(ids);
    }
    
    public static QueryBuilder getInQuery(String field, String[] values) {
        return inQuery(field, values);
    }
    
    public static SortBuilder getIdSort() {
        SortBuilder sort = new FieldSortBuilder("id");
        sort.order(SortOrder.ASC);
        return sort;
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
    
    public static FilterBuilder getFilterBuilder(String field, String term) {
        Wildcard wild = ParseUtil.isWildcard(term);
        if (wild == Wildcard.NONE) {
            //System.out.println("Filter Wildcard.NONE " + term);
            return termFilter(field, term);
        } else if (wild == Wildcard.PREFIX) {
            //System.out.println("Filter Wildcard.PREFIX " + term.substring(0, term.length() -1));
            return prefixFilter(field, term.substring(0, term.length() -1));
        } else {
            // no wildcard filter
            //System.out.println("Filter Wildcard.FULL " + term);
            return null;
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
    
    // TODO: range filters are also available
    public static FilterBuilder getRangeFilterBuilder(String field, String from, String to) {
        if (from == null && to == null) {
            return null;
        }
        
        // Range <
        if (from == null) {
            return rangeFilter(field)
                .lt(to);
        }
        
        // Range >
        if (to == null) {
            return rangeFilter(field)
                .gt(from);
        }

        // Range inclusive
        return rangeFilter(field)
            .from(from)
            .to(to);
    }
    
    public static QueryBuilder getTextQuery(String field, String terms) {
        return matchQuery(field, terms)
                .slop(1)
                .analyzer("simple");                
    }    
 }
