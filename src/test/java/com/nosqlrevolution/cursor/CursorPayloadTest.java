package com.nosqlrevolution.cursor;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nosqlrevolution.ElasticStore;
import com.nosqlrevolution.Index;
import com.nosqlrevolution.JsonIndex;
import com.nosqlrevolution.apps.JsonBlocker;
import com.nosqlrevolution.apps.ModelBlocker;
import com.nosqlrevolution.apps.JsonImporter;
import com.nosqlrevolution.apps.AbstractPoolRunner;
import com.nosqlrevolution.apps.CountsAndBytes;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.junit.*;
import static org.junit.Assert.*;

/** Uses real client serialization over loopback, without an Elasticsearch installation. */
public class CursorPayloadTest {
    private final ObjectMapper mapper = new ObjectMapper();
    private HttpServer server;
    private RestClientTransport transport;
    private ElasticsearchClient client;
    private final List<JsonNode> searches = new ArrayList<>();
    private final List<String> searchParameters = new ArrayList<>();
    private String closedPit;
    private boolean failSearch;
    private boolean failClose;
    private boolean missingSource;
    private boolean partialLaterPage;
    private boolean failBulk;
    private final List<JsonNode> bulkSources = new ArrayList<>();

    public static class Document {
        public String name;
    }

    @Before
    public void setup() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            String path = exchange.getRequestURI().getPath();
            java.io.ByteArrayOutputStream requestBytes = new java.io.ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = exchange.getRequestBody().read(buffer)) != -1) {
                requestBytes.write(buffer, 0, bytesRead);
            }
            String text = new String(requestBytes.toByteArray(), StandardCharsets.UTF_8);
            JsonNode body = path.endsWith("/_bulk") ? null : mapper.readTree(text);
            String response;
            int status = 200;
            if (path.endsWith("/_bulk")) {
                String[] lines = text.split("\\n");
                List<String> results = new ArrayList<>();
                for (int i = 1; i < lines.length; i += 2) {
                    bulkSources.add(mapper.readTree(lines[i]));
                    results.add("{\"index\":{\"_index\":\"test\",\"_id\":\"1\",\"status\":"
                            + (failBulk ? "400,\"error\":{\"type\":\"test_failure\",\"reason\":\"write failed\"}" : "201") + "}}");
                }
                response = "{\"took\":1,\"errors\":" + failBulk + ",\"items\":["
                        + String.join(",", results) + "]}";
            } else if (path.endsWith("/_search")) {
                searches.add(body);
                searchParameters.add(exchange.getRequestURI().getRawQuery());
                if (failSearch) {
                    status = 500;
                    response = "{\"error\":{\"type\":\"test_failure\",\"reason\":\"page failed\"},\"status\":500}";
                } else {
                    int after = body.has("search_after") ? body.get("search_after").get(0).asInt() : 0;
                    String hits = after == 0 ? hit(1) + "," + hit(2) : after == 2 ? hit(3) : "";
                    response = "{\"took\":1,\"timed_out\":" + (partialLaterPage && after == 2) + ",\"_shards\":{\"total\":1,\"successful\":1,\"failed\":0},"
                            + "\"pit_id\":\"pit-" + (after == 0 ? 1 : after == 2 ? 2 : 3) + "\","
                            + "\"hits\":{\"total\":{\"value\":3,\"relation\":\"eq\"},\"hits\":[" + hits + "]}}";
                }
            } else if (exchange.getRequestMethod().equals("DELETE")) {
                closedPit = body.get("id").asText();
                response = "{\"succeeded\":" + !failClose + ",\"num_freed\":1}";
            } else {
                response = "{\"id\":\"pit-0\",\"_shards\":{\"total\":1,\"successful\":1,\"failed\":0}}";
            }
            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.getResponseHeaders().set("X-Elastic-Product", "Elasticsearch");
            exchange.sendResponseHeaders(status, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });
        server.start();
        transport = new RestClientTransport(RestClient.builder(new HttpHost("127.0.0.1",
                server.getAddress().getPort(), "http")).build(), new JacksonJsonpMapper());
        client = new ElasticsearchClient(transport);
    }

    private String hit(int id) {
        return "{\"_index\":\"test\",\"_id\":\"" + id + "\"," + (missingSource ? "" : "\"_source\":{\"name\":\"doc" + id
                + "\"},") + "\"sort\":[" + id + "]}";
    }

    @After
    public void cleanup() throws IOException {
        try { if (transport != null) transport.close(); }
        finally { if (server != null) server.stop(0); }
    }

    private SearchRequest request() {
        return SearchRequest.of(r -> r.size(2).pit(p -> p.id("pit-0"))
                .source(s -> s.filter(f -> f.includes("name"))));
    }

    private ElasticStore store() {
        // Exercise the real typed factory; this fixture owns and closes the transport.
        return new ElasticStore().withClient(client);
    }

    @Test(timeout = 5000)
    public void documentCursorReturnsModelsAndPreservesPagingOptions() throws Exception {
        List<String> names = new ArrayList<>();
        try (Cursor<Document> cursor = new ScrollCursor<>(Document.class, request(), client)) {
            assertEquals(3, cursor.size());
            for (Document document : cursor) names.add(document.name);
        }
        assertEquals(java.util.Arrays.asList("doc1", "doc2", "doc3"), names);
        assertEquals(2, searches.get(1).get("size").asInt());
        assertEquals("name", searches.get(1).get("_source").get("includes").get(0).asText());
        assertEquals("pit-1", searches.get(1).get("pit").get("id").asText());
        assertTrue(searches.get(1).get("pit").has("keep_alive"));
        assertEquals("pit-3", closedPit);
    }

    @Test(timeout = 5000)
    public void jsonBlockerSerializesSourcesAndRetainsPartialBlock() throws Exception {
        try (JsonBlocker blocker = new JsonBlocker(store(), null, "test", 2, 0, 0)) {
            assertEquals(Integer.valueOf(3), blocker.call());
            List<String> first = blocker.getNext();
            assertEquals("doc1", mapper.readTree(first.get(0)).get("name").asText());
            assertEquals("doc2", mapper.readTree(first.get(1)).get("name").asText());
            assertEquals("doc3", mapper.readTree(blocker.getNext().get(0)).get("name").asText());
            assertNull(blocker.getNext());
        }
        assertEquals("pit-3", closedPit);
    }

    @Test(timeout = 5000)
    public void nativeSearchDeserializesActualDocumentSourcesAndRetainsMetadata() {
        co.elastic.clients.elasticsearch.core.SearchResponse<Document> response =
                new com.nosqlrevolution.service.QueryService(client).executeSearch(request(), Document.class);
        assertEquals("doc1", response.hits().hits().get(0).source().name);
        assertEquals("1", response.hits().hits().get(0).id());
        assertEquals("pit-1", response.pitId());
        assertEquals(3, response.hits().total().value());
        assertNull("Native search leaves PIT ownership with caller", closedPit);
    }

    @Test(timeout = 5000)
    public void modelExportPreservesMetadataIdAndOriginalJson() throws Exception {
        try (ModelBlocker blocker = new ModelBlocker(store(), null, "test", 2, 1, 0)) {
            assertEquals(Integer.valueOf(1), blocker.call());
            JsonNode envelope = mapper.readTree(blocker.getNext().get(0));
            assertEquals("1", envelope.get("id").asText());
            assertEquals("doc1", mapper.readTree(envelope.get("json").asText()).get("name").asText());
        }
        assertEquals("pit-1", closedPit);
    }

    @Test(timeout = 5000)
    public void transferWritesJsonObjectsIncludingSingletonFinalBlock() throws Exception {
        JsonBlocker blocker = new JsonBlocker(store(), null, "test", 2, 0, 0);
        AbstractPoolRunner runner = new AbstractPoolRunner() {
            public void run(String[] args) throws Exception { run(blocker, 2); }
            protected java.util.concurrent.Callable<CountsAndBytes> getNextCallable(List<String> block) throws Exception {
                return new JsonImporter(store(), "test", block, null);
            }
        };
        runner.run(new String[0]);
        assertEquals(3, bulkSources.size());
        List<String> names = new ArrayList<>();
        for (JsonNode document : bulkSources) {
            assertTrue("Bulk source must be a JSON object", document.isObject());
            names.add(document.get("name").asText());
        }
        java.util.Collections.sort(names);
        assertEquals(java.util.Arrays.asList("doc1", "doc2", "doc3"), names);
        assertEquals("pit-3", closedPit);
    }

    @Test(timeout = 5000)
    public void successfulHttpResponseWithPartialLaterPageFailsExportPipeline() throws Exception {
        partialLaterPage = true;
        JsonBlocker blocker = new JsonBlocker(store(), null, "test", 2, 0, 0);
        java.io.BufferedOutputStream output = new java.io.BufferedOutputStream(new java.io.ByteArrayOutputStream());
        AbstractPoolRunner runner = new AbstractPoolRunner() {
            public void run(String[] args) throws Exception { run(blocker, 2); }
            protected java.util.concurrent.Callable<CountsAndBytes> getNextCallable(List<String> block) {
                return new com.nosqlrevolution.apps.JsonExporter(output, block);
            }
        };
        try { runner.run(new String[0]); fail("Expected incomplete export failure"); }
        catch (Exception error) {
            Throwable cause = error;
            while (cause.getCause() != null) cause = cause.getCause();
            assertTrue(cause.getMessage(), cause.getMessage().contains("timed out"));
        } finally { output.close(); }
        assertEquals("pit-2", closedPit);
    }

    @Test(timeout = 5000)
    public void bulkRejectionFailsWorker() throws Exception {
        failBulk = true;
        JsonImporter importer = new JsonImporter(store(), "test",
                java.util.Collections.singletonList("{\"name\":\"doc1\"}"), null);
        try { importer.call(); fail("Expected bulk rejection"); }
        catch (IllegalStateException ex) { assertTrue(ex.getMessage().contains("bulk write failed")); }
    }

    @Test(timeout = 5000)
    public void malformedImportRecordFailsWithoutReportingSuccess() throws Exception {
        JsonImporter importer = new JsonImporter(store(), "test",
                java.util.Collections.singletonList("not JSON"), null);
        try { importer.call(); fail("Expected malformed record failure"); }
        catch (IOException expected) { }
        assertTrue(bulkSources.isEmpty());
    }

    @Test(timeout = 5000)
    public void trailingDocumentInLaterRecordRejectsWholeBlock() throws Exception {
        JsonImporter importer = new JsonImporter(store(), "test", java.util.Arrays.asList(
                "{\"id\":\"valid\"}", "{\"id\":\"bad\"} {}"), null);
        try { importer.call(); fail("Expected trailing document rejection"); }
        catch (IOException expected) { }
        assertTrue(bulkSources.isEmpty());
    }

    @Test(timeout = 5000)
    public void closingStoreClosesHttpTransport() throws Exception {
        ElasticStore store = new ElasticStore().withClient(client, true);
        store.close();
        try { client.search(request(), JsonData.class); fail("Expected closed transport"); }
        catch (RuntimeException expected) {
            // The closed transport must reject before making any HTTP request.
        }
        assertTrue(searches.isEmpty());
    }

    @Test(timeout = 5000)
    public void initialSearchFailureReleasesOpenedPit() throws Exception {
        failSearch = true;
        try {
            new ScrollCursor<>(JsonData.class, request(), client);
            fail("Expected search failure");
        } catch (RuntimeException expected) { }
        assertEquals("pit-0", closedPit);
    }
    @Test(timeout = 5000)
    public void nativeOptionsSurviveEveryPage() throws Exception {
        SearchRequest nativeRequest = new SearchRequest.Builder().withJson(new java.io.StringReader(
                "{\"size\":2,\"pit\":{\"id\":\"pit-0\"},\"post_filter\":{\"term\":{\"name\":\"doc1\"}},"
                + "\"min_score\":0.5,\"runtime_mappings\":{\"derived\":{\"type\":\"keyword\"}},\"track_scores\":true}"))
                .q("name:doc*").requestCache(false).build();
        try (Cursor<Document> cursor = new ScrollCursor<>(Document.class, nativeRequest, client)) {
            int count = 0;
            for (Document ignored : cursor) count++;
            assertEquals(3, count);
        }
        for (int i = 1; i < searches.size(); i++) {
            for (String field : new String[] {"post_filter", "min_score", "runtime_mappings", "track_scores"}) {
                assertEquals(field, searches.get(0).get(field), searches.get(i).get(field));
            }
            assertTrue(searchParameters.get(i).contains("q=name%3Adoc*"));
            assertTrue(searchParameters.get(i).contains("request_cache=false"));
            assertEquals(1, searches.get(i).get("search_after").size());
        }
    }
    @Test public void rejectedPitCleanupIsReported() throws Exception {
        Cursor<Document> cursor = new ScrollCursor<>(Document.class, request(), client);
        failClose = true;
        try { cursor.close(); fail("Ignored PIT cleanup failure"); }
        catch (IOException expected) { assertTrue(expected.getMessage().contains("point in time")); }
    }

    @Test public void missingSourcesCloseThePitAndDoNotPermitContinuation() throws Exception {
        missingSource = true;
        try (Cursor<Document> cursor = new ScrollCursor<>(Document.class, request(), client)) {
            java.util.Iterator<Document> iterator = cursor.iterator();
            IllegalStateException original;
            try { iterator.next(); throw new AssertionError("Accepted missing source"); }
            catch (IllegalStateException expected) { original = expected; }
            assertEquals("pit-1", closedPit);
            try { iterator.hasNext(); fail("Continued after invalid document"); }
            catch (IllegalStateException expected) { assertSame(original, expected); }
        }
    }
}
