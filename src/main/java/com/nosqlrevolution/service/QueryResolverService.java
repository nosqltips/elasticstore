package com.nosqlrevolution.service;

import com.nosqlrevolution.query.Condition.CompletedCondition;
import com.nosqlrevolution.query.Query;
import com.nosqlrevolution.util.QueryUtil;
import java.util.ArrayList;
import java.util.List;
import org.elasticsearch.index.query.QueryBuilder;

/**
 * This class resolves queries using the query builder objects.
 * 
 * @author cbrown
 */
public class QueryResolverService {
    public static Query resolve(Query query) {
        if (query == null) {
            return null;
        }
        
        if (query.getQueryBuilder() != null) {
            return query;
        }
        
        
        List<QueryBuilder> builders = resolveConditions(query.getConditions());
        
        if (builders.isEmpty()) {
            return null;
        } else if (builders.size() == 1) {
            query.setQueryBuilder(builders.get(0));
            return query;
        } else {
            
            // Need to return the proper querybuilder at some point.
            return null;
        }
    }
    
    private static List<QueryBuilder> resolveConditions(CompletedCondition[] conditions) {
        List<QueryBuilder> builders = new ArrayList<QueryBuilder>();
        for (CompletedCondition condition: conditions) {
            
            // Check to make sure that we have some reasonable values.
            if (condition.getOperator() == null || condition.getValues()== null) {
                continue;
            }
            
            // Make sure at least one of the fields are populated.
            if (condition.getField() == null && condition.getSpecialField() == null) {
                continue;
            }
            
            switch(condition.getOperator()) {
                case AND:
                    builders.add(QueryUtil.getTermQuery(condition.getField(), condition.getValue()));
                    break;
                case SHOULD:
                    builders.add(QueryUtil.getTermQuery(condition.getField(), condition.getValue()));
                    break;
                case OR:
                    builders.add(QueryUtil.getTermQuery(condition.getField(), condition.getValue()));
                    break;
                case NOT:
                    builders.add(QueryUtil.getTermQuery(condition.getField(), condition.getValue()));
                    break;
                case MUST:
                    builders.add(QueryUtil.getTermQuery(condition.getField(), condition.getValue()));
                    break;
                case MUST_NOT:
                    builders.add(QueryUtil.getTermQuery(condition.getField(), condition.getValue()));
                    break;

                case IN:
                    builders.add(QueryUtil.getTermQuery(condition.getField(), condition.getValue()));
                    break;
                case NOT_IN:
                    builders.add(QueryUtil.getTermQuery(condition.getField(), condition.getValue()));
                    break;

                case EQUAL:
                    builders.add(QueryUtil.getTermQuery(condition.getField(), condition.getValue()));
                    break;
                case NOT_EQUAL:
                    builders.add(QueryUtil.getTermQuery(condition.getField(), condition.getValue()));
                    break;
                case GT:
                    builders.add(QueryUtil.getTermQuery(condition.getField(), condition.getValue()));
                    break;
                case GTE:
                    builders.add(QueryUtil.getTermQuery(condition.getField(), condition.getValue()));
                    break;
                case LT:
                    builders.add(QueryUtil.getTermQuery(condition.getField(), condition.getValue()));
                    break;
                case LTE:
                    builders.add(QueryUtil.getTermQuery(condition.getField(), condition.getValue()));
                    break;
                }
        }
        
        return builders;
    }
}
