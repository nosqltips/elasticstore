package com.nosqlrevolution.query;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.nosqlrevolution.ElasticStore;
import com.nosqlrevolution.Index;
import com.nosqlrevolution.apps.JsonImporter;
import com.nosqlrevolution.cursor.Cursor;
import com.nosqlrevolution.service.QueryService;
import java.util.*;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.junit.*;
import static com.nosqlrevolution.query.Condition.*;
import static com.nosqlrevolution.query.ElasticStoreQuery.select;
import static org.junit.Assert.*;

/** Opt-in DSL matching tests against a real server, using one disposable mapped index. */
public class LiveQueryTest {
    private ElasticsearchClient client;
    private ElasticStore store;
    private Index index;
    private String name;

    public static class Document {
        public String id;
        public int age;
        public boolean active;
        public String country;
        public String body;
    }

    @Before public void setup() throws Exception {
        String endpoint = System.getProperty("elasticstore.test.url");
        Assume.assumeNotNull(endpoint);
        client = new ElasticsearchClient(new RestClientTransport(RestClient.builder(HttpHost.create(endpoint)).build(), new JacksonJsonpMapper()));
        store = new ElasticStore().withClient(client, true);
        name = "elasticstore-dsl-" + UUID.randomUUID();
        client.indices().create(r -> r.index(name).settings(s -> s.numberOfShards("1").numberOfReplicas("0"))
                .mappings(m -> m.properties("id", p -> p.keyword(k -> k))
                        .properties("country", p -> p.keyword(k -> k))
                        .properties("age", p -> p.integer(i -> i)).properties("active", p -> p.boolean_(b -> b))
                        .properties("body", p -> p.text(t -> t))));
        List<String> documents = Arrays.asList(
                doc(1, 17, true, "US", "quick brown fox"), doc(2, 18, true, "CA", "quick fox jumps"),
                doc(3, 21, true, "MX", "slow turtle"), doc(4, 65, false, "US", "quick brown fox"),
                doc(5, 66, true, "CA", "fox quick"), doc(6, 70, true, "CA", "none"),
                doc(7, 18, true, "US", "brown turtle"), doc(8, 18, false, "CA", "quick brown fox"));
        new JsonImporter(store, name, documents, "id").call();
        client.indices().refresh(r -> r.index(name));
        index = store.getIndex(JsonData.class, name);
    }

    private String doc(int id, int age, boolean active, String country, String body) {
        return "{\"id\":\"" + id + "\",\"age\":" + age + ",\"active\":" + active
                + ",\"country\":\"" + country + "\",\"body\":\"" + body + "\"}";
    }

    @After public void cleanup() throws Exception {
        try { if (name != null) client.indices().delete(r -> r.index(name)); }
        finally { if (store != null) store.close(); }
    }

    private List<String> ids(ElasticStoreQuery query) throws Exception {
        List<String> ids = new ArrayList<>();
        try (Cursor<JsonData> cursor = index.findAll(query.pageSize(2))) {
            for (JsonData data : cursor) ids.add(data.toJson().asJsonObject().getString("id"));
        }
        return ids;
    }

    private void matches(ElasticStoreQuery query, String... expected) throws Exception {
        List<String> actual = ids(query);
        Collections.sort(actual);
        assertEquals(Arrays.asList(expected), actual);
        assertEquals(expected.length, index.count(query));
    }

    @Test(timeout = 30000) public void booleanRangeMembershipAndIdsSelectExpectedDocuments() throws Exception {
        matches(select().where(field("active").equal(true), field("age").gte(18),
                or(field("country").equal("US"), field("country").equal("CA"))), "2", "5", "6", "7");
        matches(select().where(field("age").gt(18)), "3", "4", "5", "6");
        matches(select().where(field("age").lte(18)), "1", "2", "7", "8");
        matches(select().where(field("age").in(17, 21)), "1", "3");
        matches(select().where(field("country").notIn("US", "CA")), "3");
        matches(select().where(field("active").notEqual(true)), "4", "8");
        matches(select().where(not(field("country").equal("CA"), field("active").equal(true))), "1", "3", "4", "7", "8");
        matches(select().where(field(Field.ID).in("1", "6")), "1", "6");
        matches(select().where(field("missing").exists()));
    }

    @Test(timeout = 30000) public void textOperatorsUseAnalyzedMatchAndPhraseSemantics() throws Exception {
        matches(select().where(field("body").match("quick")), "1", "2", "4", "5", "8");
        matches(select().where(field("body").matchPhrase("quick fox")), "2");
        matches(select().where(field("body").mustContain("quick fox")), "1", "2", "4", "5", "8");
        matches(select().where(field("body").shouldContain("jumps", "none")), "2", "6");
        matches(select().where(field("body").mustNotContain("quick", "turtle")), "6");
    }

    @Test(timeout = 30000) public void sortingSkipLimitProjectionAndSingleResultWorkAcrossPages() throws Exception {
        ElasticStoreQuery query = select().sort("-age", "id").skip(2).limit(3).pageSize(2);
        assertEquals(Arrays.asList("4", "3", "2"), ids(query));
        assertEquals(8, index.count(query)); // Count describes matching documents, not a selected page.
        try (Cursor<JsonData> cursor = index.findAll(select("id").sort("id").skip(7).limit(2).pageSize(1))) {
            assertEquals(1, cursor.size());
            assertEquals(1, cursor.iterator().next().toJson().asJsonObject().size());
        }
        try (Cursor<Document> cursor = index.findAll(select().sort("id").limit(1), Document.class)) {
            assertEquals("1", cursor.iterator().next().id);
        }
        assertTrue(ids(select().skip(100).limit(2)).isEmpty());
        String single = new QueryService(client).getSingle(select().sort("-age").skip(1), name);
        assertEquals("5", new com.fasterxml.jackson.databind.ObjectMapper().readTree(single).get("id").asText());
        try (Cursor<Document> cursor = store.getIndex(Document.class, name).findAll(query, Document.class)) {
            assertEquals(3, cursor.size());
            List<String> typedIds = new ArrayList<>();
            for (Document document : cursor) typedIds.add(document.id);
            assertEquals(Arrays.asList("4", "3", "2"), typedIds);
        }
    }
}
