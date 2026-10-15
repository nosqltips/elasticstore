package com.nosqlrevolution;

import co.elastic.clients.json.JsonData;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.nosqlrevolution.cursor.Cursor;
import java.util.*;
import org.junit.*;
import static org.junit.Assert.*;
import static com.nosqlrevolution.query.Condition.*;
import static com.nosqlrevolution.query.ElasticStoreQuery.select;

/** Public API contracts, compiled without raw Index casts and exercised against Elasticsearch. */
public class LiveApiContractTest {
    private ElasticStore store;
    private String name;
    public static class Document {
        public String id, value;
        public Document() { }
        Document(String id, String value) { this.id = id; this.value = value; }
    }
    @Before public void setup() throws Exception {
        String endpoint = System.getProperty("elasticstore.test.url");
        Assume.assumeNotNull(endpoint);
        store = new ElasticStore().withEndpoints(endpoint).execute();
        name = "elasticstore-contract-" + UUID.randomUUID();
        store.getClient().indices().create(r -> r.index(name)
                .settings(s -> s.numberOfShards("1").numberOfReplicas("0"))
                .mappings(m -> m.properties("id", p -> p.keyword(k -> k))));
    }
    @After public void close() throws Exception {
        try { if (name != null) store.getClient().indices().delete(r -> r.index(name)); }
        finally { if (store != null) store.close(); }
    }
    @Test(timeout=30000) public void typedReadOverloadsAndScrollsSupportProjectionTypes() throws Exception {
        Index<Document> index = store.getIndex(Document.class, name);
        index.applyMapping("{}");
        index.write(new Document("a", "first"), new Document("b", "second"), new Document("c", "third"));
        index.refresh();
        assertNotNull(index.find());
        assertNotNull(index.find(Map.class));
        assertEquals("b", index.find(select().where(field("id").equal("b"))).id);
        assertEquals("b", index.find(select().where(field("id").equal("b")), Map.class).get("id"));
        assertEquals("a", index.findById("a", Map.class).get("id"));
        assertNull(index.find(select().where(field("id").equal("missing"))));
        try (Cursor<Document> cursor = index.findAllScroll(select().sort("id").skip(1).limit(2).pageSize(1))) {
            assertEquals(2, cursor.size());
            Iterator<Document> iterator = cursor.iterator();
            assertEquals("b", iterator.next().id);
            assertEquals("c", iterator.next().id);
            assertFalse(iterator.hasNext());
        }
        try (Cursor<Hit<JsonData>> cursor = index.findAllScrollHit(select().sort("id").pageSize(1))) {
            List<String> ids = new ArrayList<>();
            for (Hit<JsonData> hit : cursor) ids.add(hit.id());
            assertEquals(Arrays.asList("a", "b", "c"), ids);
        }
        try (Cursor<Map> cursor = index.findAllById(Map.class, "b", "missing", "a")) {
            Iterator<Map> iterator = cursor.iterator();
            assertEquals("b", iterator.next().get("id"));
            assertNull(iterator.next());
            assertEquals("a", iterator.next().get("id"));
        }
    }
    @Test(timeout=30000) public void stringIndexReadPathsReturnJsonStringsAndBufferedWritesFlush() throws Exception {
        Index<String> index = store.getIndex(String.class, name);
        index.setBulkSize(2);
        index.addBulk("{\"id\":\"a\",\"value\":\"first\"}");
        index.addBulk("{\"id\":\"b\",\"value\":\"second\"}");
        index.addBulk("{\"id\":\"c\",\"value\":\"third\"}");
        assertTrue(index.flushBulk());
        index.refresh();
        assertEquals(3, index.count());
        assertTrue(index.find().startsWith("{"));
        assertTrue(index.find(select().where(field("id").equal("b"))).contains("second"));
        try (Cursor<String> cursor = index.findAll(select().sort("id").pageSize(1))) {
            List<String> documents = new ArrayList<>();
            for (String document : cursor) documents.add(document);
            assertEquals(3, documents.size());
            assertTrue(documents.get(2).contains("third"));
        }
        try (Cursor<String> cursor = index.findAllScroll(select().limit(1))) {
            assertTrue(cursor.iterator().next().startsWith("{"));
        }
        try (Cursor<String> cursor = index.findAllById("a", "missing")) {
            Iterator<String> iterator = cursor.iterator();
            assertTrue(iterator.next().contains("first"));
            assertNull(iterator.next());
        }
        try (Cursor<Map> cursor = index.findAllById(Map.class, "b")) {
            assertEquals("second", cursor.iterator().next().get("value"));
        }
        assertTrue(index.removeById(Arrays.asList("a", "b")).succeeded());
        assertTrue(index.remove(index.findById("c")).succeeded());
        index.refresh();
        assertEquals(0, index.count());
    }
    @Test(timeout=30000) public void jsonDataIndexWritesAndReadsJsonDataWithoutClassCasts() throws Exception {
        Index<JsonData> index = store.getIndex(JsonData.class, name);
        JsonData document = new com.nosqlrevolution.util.MappingUtil<JsonData>().get("{\"id\":\"json-data\",\"value\":\"first\"}", JsonData.class);
        assertEquals("json-data", index.write(document).getItems().get(0).getId());
        assertEquals("first", index.findById("json-data").toJson().asJsonObject().getString("value"));
        index.refresh();
        assertNotNull(index.find());
        try (Cursor<JsonData> cursor = index.findAll()) { assertNotNull(cursor.iterator().next().toJson()); }
        try (Cursor<JsonData> cursor = index.findAllById("json-data", "missing")) {
            Iterator<JsonData> iterator = cursor.iterator();
            assertNotNull(iterator.next().toJson());
            assertNull(iterator.next());
        }
        assertTrue(index.remove(document).succeeded());
        assertNull(index.findById("json-data"));
    }
    @Test(timeout=30000) public void childIdsAndNullIdsDoNotBecomeDocumentIdentity() throws Exception {
        Index<String> index = store.getIndex(String.class, name);
        OperationStatus status = index.write("{\"child\":{\"id\":\"shared\"}}", "{\"id\":null,\"child\":{\"id\":\"shared\"}}");
        assertEquals(2, status.getSuccessCount());
        String first = status.getItems().get(0).getId(), second = status.getItems().get(1).getId();
        assertFalse(first.equals(second));
        assertFalse("shared".equals(first));
        assertFalse("null".equals(second));
        index.refresh();
        assertEquals(2, index.count());
    }
}
