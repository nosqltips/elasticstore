package com.nosqlrevolution.service;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.nosqlrevolution.query.Condition.CompletedCondition;
import static com.nosqlrevolution.util.QueryUtil.*;
import java.util.ArrayList;
import java.util.List;

/**
 * This class resolves queries using the query builder objects.
 * 
 * @author cbrown
 */
public class QueryResolverService {
    public static com.nosqlrevolution.query.ElasticStoreQuery resolve(com.nosqlrevolution.query.ElasticStoreQuery query) {
        if (query == null) {
            return null;
        }
        
        if (query.getQuery() != null) {
            return query;
        }
        
        List<Query> queries = resolveConditions(query.getConditions());
        
        if (queries.isEmpty()) {
            return null;
        } else if (queries.size() == 1) {
            query.setQuery(queries.get(0));
            return query;
        } else {
            
            // Need to return the proper querybuilder at some point.
            return null;
        }
    }
    
    private static List<Query> resolveConditions(CompletedCondition[] conditions) {
        List<Query> builders = new ArrayList<Query>();
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
                    builders.add(getTermQuery(condition.getField(), condition.getValue()));
                    break;
                case SHOULD:
                    builders.add(getTermQuery(condition.getField(), condition.getValue()));
                    break;
                case OR:
                    builders.add(getTermQuery(condition.getField(), condition.getValue()));
                    break;
                case NOT:
                    builders.add(getTermQuery(condition.getField(), condition.getValue()));
                    break;
                case MUST:
                    builders.add(getTermQuery(condition.getField(), condition.getValue()));
                    break;
                case MUST_NOT:
                    builders.add(getTermQuery(condition.getField(), condition.getValue()));
                    break;

                case IN:
                    builders.add(getTermQuery(condition.getField(), condition.getValue()));
                    break;
                case NOT_IN:
                    builders.add(getTermQuery(condition.getField(), condition.getValue()));
                    break;

                case EQUAL:
                    builders.add(getTermQuery(condition.getField(), condition.getValue()));
                    break;
                case NOT_EQUAL:
                    builders.add(getTermQuery(condition.getField(), condition.getValue()));
                    break;
                case GT:
                    builders.add(getTermQuery(condition.getField(), condition.getValue()));
                    break;
                case GTE:
                    builders.add(getTermQuery(condition.getField(), condition.getValue()));
                    break;
                case LT:
                    builders.add(getTermQuery(condition.getField(), condition.getValue()));
                    break;
                case LTE:
                    builders.add(getTermQuery(condition.getField(), condition.getValue()));
                    break;
                }
        }
        
        return builders;
    }
}
