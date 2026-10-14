package com.nosqlrevolution.apps;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nosqlrevolution.ElasticStore;
import com.nosqlrevolution.cursor.Cursor;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.Callable;
import org.apache.http.HttpHost;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RestClient;
import org.junit.*;
import static org.junit.Assert.*;

/** Opt-in integration tests: creates and removes only unique test indices. */
public class LivePipelineTest {
    private static final int DOCUMENTS = 1021;
    private final ObjectMapper mapper = new ObjectMapper();
    private final List<String> indices = new ArrayList<>();
    private RestClient http;
    private ElasticsearchClient client;
    private ElasticStore store;

    public static class Document {
        public String id;
        public String name;
        public int value;
    }

    @Before
    public void connect() throws Exception {
        String endpoint = System.getProperty("elasticstore.test.url");
        Assume.assumeNotNull(endpoint);
        http = RestClient.builder(HttpHost.create(endpoint)).build();
        client = new ElasticsearchClient(new RestClientTransport(http, new JacksonJsonpMapper()));
        // Inject the local HTTP client; the legacy CLI connector uses HTTPS and credentials.
        store = new ElasticStore();
        java.lang.reflect.Field field = ElasticStore.class.getDeclaredField("client");
        field.setAccessible(true);
        field.set(store, client);
    }

    @After
    public void cleanup() throws Exception {
        try {
            if (client != null) {
                for (String index : indices) client.indices().delete(r -> r.index(index));
            }
        } finally {
            if (store != null) store.close();
            else if (http != null) http.close();
        }
    }

    private String index() throws Exception {
        String index = "elasticstore-test-" + UUID.randomUUID().toString();
        client.indices().create(r -> r.index(index).settings(s -> s.numberOfShards("1").numberOfReplicas("0")));
        indices.add(index);
        return index;
    }

    private interface Sink { Callable<CountsAndBytes> create(List<String> block) throws Exception; }

    private void process(AbstractBlocker source, int workers, Sink sink) throws Exception {
        new AbstractPoolRunner() {
            public void run(String[] args) throws Exception { run(source, workers); }
            protected Callable<CountsAndBytes> getNextCallable(List<String> block) throws Exception {
                return sink.create(block);
            }
        }.run(new String[0]);
    }

    private Map<String, JsonNode> documents(String index) throws Exception {
        Map<String, JsonNode> result = new HashMap<>();
        try (Cursor<Document> cursor = store.getIndex(Document.class, index).findAll()) {
            for (Document document : cursor) {
                assertNull("Duplicate document ID", result.put(document.id, mapper.valueToTree(document)));
            }
        }
        return result;
    }

    private long openContexts() throws Exception {
        JsonNode nodes = mapper.readTree(EntityUtils.toString(http.performRequest(
                new Request("GET", "/_nodes/stats/indices/search")).getEntity())).get("nodes");
        long contexts = 0;
        for (JsonNode node : nodes) contexts += node.path("indices").path("search").path("open_contexts").asLong();
        return contexts;
    }

    @Test(timeout = 60000)
    public void importExportAndTransferPreserveEveryDocument() throws Exception {
        String original = index();
        String restored = index();
        String transferred = index();
        Map<String, JsonNode> expected = new HashMap<>();
        List<String> lines = new ArrayList<>();
        for (int i = 0; i < DOCUMENTS; i++) {
            Document document = new Document();
            document.id = "id-" + i;
            document.name = "record é " + i;
            document.value = i;
            JsonNode json = mapper.valueToTree(document);
            expected.put(document.id, json);
            lines.add(json.toString());
        }
        Path input = Files.createTempFile("elasticstore-live", ".json");
        try {
            Files.write(input, lines, StandardCharsets.UTF_8);
            process(new FileBlocker(input.toString(), 17, 0, 0), 4,
                    block -> new JsonImporter(store, original, block, "id"));
            client.indices().refresh(r -> r.index(original));
            assertEquals(expected, documents(original));

            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            try (BufferedOutputStream output = new BufferedOutputStream(bytes)) {
                process(new JsonBlocker(store, null, original, 17, 0, 0), 1,
                        block -> new JsonExporter(output, block));
            }
            List<String> exported = Arrays.asList(new String(bytes.toByteArray(), StandardCharsets.UTF_8).split("\\R"));
            assertEquals(DOCUMENTS, exported.size());
            Files.write(input, exported, StandardCharsets.UTF_8);
            process(new FileBlocker(input.toString(), 17, 0, 0), 2,
                    block -> new JsonImporter(store, restored, block, "id"));
            client.indices().refresh(r -> r.index(restored));
            assertEquals(expected, documents(restored));

            process(new JsonBlocker(store, null, original, 17, 0, 0), 3,
                    block -> new JsonImporter(store, transferred, block, "id"));
            client.indices().refresh(r -> r.index(transferred));
            assertEquals(expected, documents(transferred));
            assertEquals("PIT contexts leaked", 0, openContexts());
            System.out.println("Live round trip: " + DOCUMENTS + " documents matched after import, export/reimport and transfer");
        } finally { Files.deleteIfExists(input); }
    }

    @Test(timeout = 30000)
    public void rejectedBulkStopsPipelineAndReleasesReaderPit() throws Exception {
        String source = index();
        String destination = index();
        new JsonImporter(store, source, Collections.singletonList(
                "{\"id\":\"one\",\"value\":\"not-a-number\"}"), "id").call();
        client.indices().refresh(r -> r.index(source));
        client.indices().putMapping(r -> r.index(destination).properties("value", p -> p.integer(i -> i)));
        try {
            process(new JsonBlocker(store, null, source, 17, 0, 0), 2,
                    block -> new JsonImporter(store, destination, block, "id"));
            fail("Expected Elasticsearch mapping rejection");
        } catch (Exception ex) {
            Throwable cause = ex;
            while (cause.getCause() != null) cause = cause.getCause();
            assertTrue(cause.getMessage().contains("bulk write failed"));
        }
        assertEquals("PIT contexts leaked after failure", 0, openContexts());
    }
}
