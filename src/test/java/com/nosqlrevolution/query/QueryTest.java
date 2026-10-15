package com.nosqlrevolution.query;

import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nosqlrevolution.query.Condition.CompletedCondition;
import com.nosqlrevolution.service.QueryResolverService;
import com.nosqlrevolution.service.QueryService;
import com.nosqlrevolution.util.QueryUtil;
import org.junit.Test;
import static com.nosqlrevolution.query.Condition.*;
import static com.nosqlrevolution.query.ElasticStoreQuery.select;
import static org.junit.Assert.*;

/** Query JSON assertions require no Elasticsearch server. */
public class QueryTest {
    private final ObjectMapper mapper = new ObjectMapper();
    private JsonNode json(Query query) throws Exception {
        co.elastic.clients.json.JsonpMapper jsonMapper = new co.elastic.clients.json.jackson.JacksonJsonpMapper();
        java.io.StringWriter output = new java.io.StringWriter();
        try (jakarta.json.stream.JsonGenerator generator = jsonMapper.jsonProvider().createGenerator(output)) {
            query.serialize(generator, jsonMapper);
        }
        return mapper.readTree(output.toString());
    }
    private JsonNode leaf(CompletedCondition condition) throws Exception { return json(QueryResolverService.compile(condition)); }

    @Test public void exactValuesKeepTheirTypes() throws Exception {
        assertTrue(leaf(field("age").equal(18)).at("/term/age/value").isIntegralNumber());
        assertEquals(18, leaf(field("age").equal(18)).at("/term/age/value").asInt());
        assertTrue(leaf(field("active").equal(true)).at("/term/active/value").asBoolean());
        assertEquals("18", leaf(field("age").equal("18")).at("/term/age/value").asText());
        assertEquals(Long.MAX_VALUE, leaf(field("value").equal(Long.MAX_VALUE)).at("/term/value/value").asLong());
    }

    @Test public void comparisonsUseCorrectExclusiveAndInclusiveBounds() throws Exception {
        assertEquals(18, leaf(field("age").gt(18)).at("/range/age/gt").asInt());
        assertEquals(18, leaf(field("age").gte(18)).at("/range/age/gte").asInt());
        assertEquals(18, leaf(field("age").lt(18)).at("/range/age/lt").asInt());
        assertEquals(18, leaf(field("age").lte(18)).at("/range/age/lte").asInt());
        assertTrue(leaf(field("age").gt(18)).at("/range/age/gte").isMissingNode());
        assertTrue(leaf(field("age").gt(18)).at("/range/age/gt").isIntegralNumber());
        assertTrue(leaf(field("age").gt("18")).at("/range/age/gt").isTextual());
        assertEquals(Long.MAX_VALUE, leaf(field("revision").gte(Long.MAX_VALUE)).at("/range/revision/gte").asLong());
        assertEquals("2026-01-01", leaf(field("date\"field").gte("2026-01-01"))
                .get("range").get("date\"field").get("gte").asText());
    }

    @Test public void membershipRetainsEveryValueAndType() throws Exception {
        JsonNode values = leaf(field("age").in(18, 21)).at("/terms/age");
        assertEquals(2, values.size());
        assertEquals(21, values.get(1).asInt());
        JsonNode excluded = leaf(field("country").notIn("US", "CA")).at("/bool/must_not/0/terms/country");
        assertEquals(2, excluded.size());
        assertEquals("CA", excluded.get(1).asText());
    }

    @Test public void nestedGroupsPreserveTheirMeaning() throws Exception {
        JsonNode query = leaf(and(field("active").equal(true), or(field("age").lt(18), field("age").gt(65))));
        assertEquals(2, query.at("/bool/filter").size());
        assertEquals(2, query.at("/bool/filter/1/bool/should").size());
        assertEquals("1", query.at("/bool/filter/1/bool/minimum_should_match").asText());
        JsonNode negation = leaf(not(field("a").equal("x"), field("b").equal("y")));
        assertEquals(2, negation.at("/bool/must_not/0/bool/filter").size());
        assertEquals("x", leaf(field("a").notEqual("x")).at("/bool/must_not/0/term/a/value").asText());
    }

    @Test public void topLevelConditionsAreRequiredFiltersAndEmptyQueryMatchesAll() throws Exception {
        assertEquals(2, json(select().where(field("a").equal("x"), field("b").equal("y")).toQuery()).at("/bool/filter").size());
        assertTrue(select().toQuery().isMatchAll());
        assertTrue(select().where().toQuery().isMatchAll());
    }

    @Test public void textQueriesAndLegacyContainOperatorsHaveExplicitSemantics() throws Exception {
        assertEquals("quick fox", leaf(field("body").match("quick fox")).at("/match/body/query").asText());
        assertEquals("quick fox", leaf(field("body").matchPhrase("quick fox")).at("/match_phrase/body/query").asText());
        assertEquals("and", leaf(field("body").mustContain("quick fox")).at("/bool/filter/0/match/body/operator").asText());
        assertEquals(2, leaf(field("body").shouldContain("quick", "fox")).at("/bool/should").size());
        assertEquals(2, leaf(field("body").mustNotContain("quick", "fox")).at("/bool/must_not").size());
        assertEquals("body", leaf(field("body").exists()).at("/exists/field").asText());
    }

    @Test public void metadataIdsCompileToIdsQueries() throws Exception {
        assertEquals("1", leaf(field(Field.ID).equal("1")).at("/ids/values/0").asText());
        assertEquals(2, leaf(field(Field.ID).in("1", "2")).at("/ids/values").size());
    }

    @Test public void completedConditionsAreIndependentOfBuildersAndArrays() throws Exception {
        Condition selector = field("age");
        CompletedCondition original = selector.equal(18);
        selector.gt(21);
        original.getTypedValues()[0] = 99;
        assertEquals(18, leaf(original).at("/term/age/value").asInt());
        String[] input = { "US", "CA" };
        CompletedCondition membership = field("country").in(input);
        input[0] = "changed";
        assertEquals("US", membership.getValue());
        CompletedCondition[] children = { original };
        CompletedCondition group = and(children);
        children[0] = selector.equal(99);
        assertEquals(18, leaf(group).at("/bool/filter/0/term/age/value").asInt());
    }

    @Test public void compilationDoesNotCacheStaleConditionsOrMutateConfiguration() throws Exception {
        ElasticStoreQuery query = select("age").where(field("age").equal(18));
        QueryResolverService.resolve(query);
        assertNull(query.getQuery());
        query.where(field("age").equal(21));
        assertEquals(21, json(query.toQuery()).at("/bool/filter/0/term/age/value").asInt());
        query.setQuery(Query.of(q -> q.matchAll(m -> m)));
        assertTrue(query.toQuery().isMatchAll());
        query.where(field("age").equal(25));
        assertFalse(query.toQuery().isMatchAll());
        query.getFields()[0] = "modified";
        assertEquals("age", query.getFields()[0]);
    }

    @Test public void searchRequestsHonorIndexProjectionSortAndPagination() {
        ElasticStoreQuery query = select("age", "name").where(field("age").gte(18))
                .sort("age", "-name").skip(3).limit(4).pageSize(2).highlight("name");
        SearchRequest request = new QueryService(null).findAll(query, "people", false);
        assertEquals("people", request.index().get(0));
        assertEquals(Integer.valueOf(3), request.from());
        assertEquals(Integer.valueOf(2), request.size());
        assertEquals(2, request.sort().size());
        assertEquals(SortOrder.Desc, request.sort().get(1).field().order());
        assertEquals(2, request.source().filter().includes().size());
        assertTrue(request.highlight().fields().containsKey("name"));
        assertEquals(Integer.valueOf(1), new QueryService(null).findAll(select().limit(1), "people", false).size());
    }

    @Test public void oldQueryFacadesDelegateToTheSameCompiler() {
        ESQuery query = ESQuery.select("age").where(field("age").gte(18)).limit(5);
        assertEquals(5, query.getLimit());
        assertEquals(1, query.toQuery().bool().filter().size());
        assertEquals(2, new ESQueryImpl().pageSize(2).getSize());
    }

    @Test public void legacyRangeHelpersUseTheFieldAndProperBounds() throws Exception {
        assertEquals("18", json(QueryUtil.getGtRangeQuery("age", "18")).at("/range/age/gt").asText());
        assertEquals("21", json(QueryUtil.getRangeQueryBuilder("age", null, "21")).at("/range/age/lt").asText());
        assertEquals("18", json(QueryUtil.getRangeQueryBuilder("age", "18", "21")).at("/range/age/gte").asText());
        assertEquals("21", json(QueryUtil.getRangeQueryBuilder("age", "18", "21")).at("/range/age/lte").asText());
    }

    @Test(expected = IllegalArgumentException.class) public void emptyMembershipIsRejected() { field("age").in(); }
    @Test(expected = IllegalArgumentException.class) public void emptyGroupIsRejected() { and(); }
    @Test(expected = IllegalArgumentException.class) public void removedAllFieldIsRejected() { field(Field.ALL); }
    @Test(expected = IllegalArgumentException.class) public void negativeSkipIsRejected() { select().skip(-1); }
    @Test(expected = IllegalArgumentException.class) public void zeroPageSizeIsRejected() { select().pageSize(0); }
    @Test(expected = IllegalArgumentException.class) public void nullValueIsRejected() { field("age").equal((String) null); }
    @Test(expected = IllegalArgumentException.class) public void nonfiniteNumberIsRejected() { field("value").equal(Double.NaN); }
    @Test(expected = IllegalArgumentException.class) public void booleanRangeIsRejected() { select().where(field("active").gt(true)).toQuery(); }
}
