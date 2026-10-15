package com.nosqlrevolution;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.JsonData;
import com.nosqlrevolution.annotation.DocumentId;
import com.nosqlrevolution.annotation.UUIDProvider;
import com.nosqlrevolution.cursor.Cursor;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import org.junit.*;
import static org.junit.Assert.*;
import static com.nosqlrevolution.query.Condition.*;
import static com.nosqlrevolution.query.ElasticStoreQuery.select;

/** Opt-in tests of the public connector and CRUD APIs, using disposable indices. */
public class LiveCrudTest {
    private ElasticStore store;
    private ElasticsearchClient client;
    private String indexName;
    private final List<String> cleanup = new ArrayList<>();
    private String endpoint;

    public static class Document {
        @DocumentId @UUIDProvider public String key;
        public String value;
        public Document() { }
        Document(String key, String value) { this.key = key; this.value = value; }
    }

    @Before public void setup() throws Exception {
        endpoint = System.getProperty("elasticstore.test.url");
        Assume.assumeNotNull(endpoint);
        store = new ElasticStore().withEndpoints(endpoint).execute();
        client = store.getClient();
        indexName = create();
    }
    private String create() throws Exception {
        String name = "elasticstore-crud-" + UUID.randomUUID();
        client.indices().create(r -> r.index(name).settings(s -> s.numberOfShards("1").numberOfReplicas("0"))
                .mappings(m -> m.properties("key", p -> p.keyword(k -> k))));
        cleanup.add(name);
        return name;
    }
    @After public void close() throws Exception {
        try { if (client != null) for (String index : cleanup) client.indices().delete(r -> r.index(index)); }
        finally { if (store != null) store.close(); }
    }

    @Test(timeout=30000) public void typedWritesKeepIdentityAndGetAndMultiGetReturnSources() throws Exception {
        Index<Document> index = store.getIndex(Document.class, indexName);
        Document document = new Document(null, "first");
        OperationStatus created = index.write(document);
        assertTrue(created.succeeded());
        assertEquals(1, created.getSuccessCount());
        OperationStatus.Item item = created.getItems().get(0);
        assertEquals(document.key, item.getId());
        assertEquals(indexName, item.getIndex());
        assertEquals("created", item.getResult());
        assertEquals(201, item.getStatus());
        assertNotNull(item.getVersion());
        assertNotNull(item.getSequenceNumber());
        assertNotNull(item.getPrimaryTerm());
        String generated = document.key;
        assertNotNull(generated);
        assertEquals(generated, index.findById(generated).key);
        document.value = "updated";
        OperationStatus updated = index.write(document);
        assertEquals("updated", updated.getItems().get(0).getResult());
        assertTrue(updated.getItems().get(0).getVersion() > item.getVersion());
        assertEquals(generated, document.key);
        assertEquals("updated", index.findById(generated).value);
        index.write(Arrays.asList(new Document("two", "second"), new Document("three", "third")));
        index.refresh();
        assertEquals(3, index.count());
        assertNull(index.findById("missing"));
        try (Cursor<Document> found = index.findAllById("three", "missing", "two")) {
            Iterator<Document> iterator = found.iterator();
            assertEquals(3, found.size());
            assertEquals("third", iterator.next().value);
            assertNull(iterator.next());
            assertEquals("second", iterator.next().value);
            assertFalse(iterator.hasNext());
            try { iterator.next(); fail("Expected exhausted iterator"); }
            catch (NoSuchElementException expected) { }
        }
        Index json = store.getIndex(JsonData.class, indexName);
        try (Cursor<JsonData> found = json.findAllById("two", "missing")) {
            Iterator<JsonData> iterator = found.iterator();
            assertEquals("second", iterator.next().toJson().asJsonObject().getString("value"));
            assertNull(iterator.next());
        }
    }

    @Test(timeout=30000) public void publicDeleteExistsAndRefreshMethodsWork() throws Exception {
        Index<Document> index = store.getIndex(Document.class, indexName);
        assertTrue(index.exists());
        assertFalse(store.getIndex(Document.class, indexName + "-absent").exists());
        assertTrue(index.flushBulk());
        index.write(new Document("a", "first"), new Document("b", "second"), new Document("c", "third"));
        index.refresh();
        assertEquals(3, index.count());
        OperationStatus deleted = index.removeById("a", "missing");
        assertTrue(deleted.succeeded());
        assertEquals(2, deleted.getSuccessCount());
        assertEquals("deleted", deleted.getItems().get(0).getResult());
        assertEquals("missing", deleted.getItems().get(1).getId());
        assertEquals("not_found", deleted.getItems().get(1).getResult());
        assertEquals(404, deleted.getItems().get(1).getStatus());
        assertTrue(index.remove(new Document("b", "second")).succeeded());
        index.refresh();
        try { index.remove(select().limit(1)); fail("Expected paginated delete rejection"); }
        catch (IllegalArgumentException expected) { }
        try { index.remove(select().skip(1)); fail("Expected paginated delete rejection"); }
        catch (IllegalArgumentException expected) { }
        assertTrue(index.remove(select().where(field("key").equal("c"))).succeeded());
        index.refresh();
        assertEquals(0, index.count());
        Document missingId = new Document(null, "missing");
        try { index.remove(missingId); fail("Expected missing identity failure"); }
        catch (IllegalArgumentException expected) { assertNull(missingId.key); }
    }

    @Test(timeout=30000) public void jsonWritesExposeServerGeneratedIds() throws Exception {
        Index<String> index = store.getIndex(String.class, indexName);
        OperationStatus status = index.write("{\"value\":\"generated\"}");
        assertTrue(status.succeeded());
        assertEquals(1, status.getSuccessCount());
        String id = status.getItems().get(0).getId();
        assertNotNull(id);
        assertTrue(index.findById(id).contains("generated"));
        assertEquals(0, index.write(new String[0]).getItems().size());
    }

    @Test(timeout=30000) public void rejectedSingleAndBulkWritesDoNotReportSuccess() throws Exception {
        client.indices().putMapping(r -> r.index(indexName).properties("value", p -> p.integer(i -> i)));
        Index<Document> index = store.getIndex(Document.class, indexName);
        try { index.write(new Document("bad", "not-an-integer")); fail("Expected rejected single write"); }
        catch (co.elastic.clients.elasticsearch._types.ElasticsearchException expected) { }
        try {
            index.write(Arrays.asList(new Document("valid", "42"), new Document("bad", "not-an-integer")));
            fail("Expected rejected bulk write");
        } catch (OperationException expected) {
            OperationStatus status = expected.getStatus();
            assertFalse(status.succeeded());
            assertEquals(1, status.getSuccessCount());
            assertEquals(1, status.getFailureCount());
            assertEquals("valid", status.getItems().get(0).getId());
            assertTrue(status.getItems().get(0).succeeded());
            OperationStatus.Item failure = status.getItems().get(1);
            assertEquals("bad", failure.getId());
            assertEquals(400, failure.getStatus());
            assertNotNull(failure.getErrorType());
            assertNotNull(failure.getErrorReason());
        }
        index.refresh();
        assertEquals(1, index.count());
    }

    @Test(timeout=30000) public void cliImportExportAndTransferUseRealConnectionSettings() throws Exception {
        String restored = create();
        String transferred = create();
        Path directory = Files.createTempDirectory("elasticstore-cli");
        Path input = directory.resolve("input.json");
        Path exported = directory.resolve("exported.json");
        try {
            Files.write(input, Arrays.asList("{\"id\":\"one\",\"value\":\"é\"}", "{\"id\":\"two\",\"value\":\"second\"}"), StandardCharsets.UTF_8);
            App.main(new String[] { "import", "-h", endpoint, "-i", indexName, "-f", input.toString(), "-b", "2" });
            store.getIndex(String.class, indexName).refresh();
            App.main(new String[] { "export", "-h", endpoint, "-i", indexName, "-f", exported.toString(), "-b", "2" });
            assertEquals(2, Files.readAllLines(exported, StandardCharsets.UTF_8).size());
            App.main(new String[] { "import", "-h", endpoint, "-i", restored, "-f", exported.toString() });
            store.getIndex(String.class, restored).refresh();
            assertEquals(2, store.getIndex(String.class, restored).count());
            App.main(new String[] { "transfer", "-sh", endpoint, "-dh", endpoint, "-si", indexName, "-di", transferred, "-b", "2" });
            store.getIndex(String.class, transferred).refresh();
            assertEquals(2, store.getIndex(String.class, transferred).count());
        } finally {
            Files.deleteIfExists(input); Files.deleteIfExists(exported); Files.delete(directory);
        }
    }
}
