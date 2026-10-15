package com.nosqlrevolution.apps;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nosqlrevolution.DocumentOptions;
import com.nosqlrevolution.App;
import com.nosqlrevolution.ElasticStore;
import com.nosqlrevolution.service.QueryService;
import java.nio.file.*;
import java.util.*;
import org.junit.*;
import static org.junit.Assert.*;

/** Opt-in metadata round trips through the real CLI and Elasticsearch. */
public class LiveMetadataTransferTest {
    private final ObjectMapper mapper = new ObjectMapper();
    private final List<String> cleanup = new ArrayList<>();
    private ElasticStore store;
    private ElasticsearchClient client;
    private QueryService service;
    private String endpoint;

    @Before public void connect() throws Exception {
        endpoint = System.getProperty("elasticstore.test.url");
        Assume.assumeNotNull(endpoint);
        store = new ElasticStore().withEndpoints(endpoint).execute();
        client = store.getClient();
        service = new QueryService(client);
    }
    @After public void close() throws Exception {
        try { if (client != null) for (String name : cleanup) client.indices().delete(r -> r.index(name)); }
        finally { if (store != null) store.close(); }
    }
    private String index(boolean routing) throws Exception {
        String name = "elasticstore-metadata-" + UUID.randomUUID();
        client.indices().create(r -> r.index(name).settings(s -> s.numberOfShards("3").numberOfReplicas("0"))
                .mappings(m -> m.routing(route -> route.required(routing))));
        cleanup.add(name);
        return name;
    }
    private void verify(String index, Map<String, String> sources) throws Exception {
        service.refresh(index);
        assertEquals(sources.size(), store.getIndex(String.class, index).count());
        for (Map.Entry<String, String> entry : sources.entrySet()) {
            String routing = "tenant-" + entry.getKey();
            assertEquals(mapper.readTree(entry.getValue()), mapper.readTree(service.realTimeGet(index, entry.getKey(), routing)));
        }
    }

    @Test(timeout=60000) public void cliModelRoundTripAndTransferPreserveIdsRoutingAndSourceAcrossPages() throws Exception {
        String source = index(true), restored = index(true), transferred = index(true);
        Map<String, String> expected = new LinkedHashMap<>();
        for (int i = 0; i < 5; i++) {
            String id = "metadata-" + i;
            // Source identity and routing deliberately disagree with Elasticsearch metadata.
            String identity = i == 0 ? "" : "\"id\":\"business-" + i + "\",";
            String json = "{" + identity + "\"routing\":\"business-routing\",\"value\":\"é " + i + "\",\"nested\":{\"id\":\"nested\"}}";
            expected.put(id, json);
            service.index(source, json, id, DocumentOptions.DEFAULT.routing("tenant-" + id));
        }
        service.refresh(source);
        Path directory = Files.createTempDirectory("elasticstore-model");
        Path file = directory.resolve("export.json.gz");
        try {
            App.main(new String[] { "export", "-h", endpoint, "-i", source, "-f", file.toString(), "-m", "-g", "-b", "2" });
            App.main(new String[] { "import", "-h", endpoint, "-i", restored, "-f", file.toString(), "-m", "-b", "2", "-th", "3" });
            verify(restored, expected);
            // Importing again must update the same IDs instead of generating duplicates.
            App.main(new String[] { "import", "-h", endpoint, "-i", restored, "-f", file.toString(), "-m", "-b", "2" });
            verify(restored, expected);
            for (int attempt = 0; attempt < 2; attempt++) {
                App.main(new String[] { "transfer", "-sh", endpoint, "-dh", endpoint,
                        "-si", source, "-di", transferred, "-b", "2", "-th", "3" });
                verify(transferred, expected);
            }
        } finally { Files.deleteIfExists(file); Files.deleteIfExists(directory); }
    }

    @Test(timeout=30000) public void legacyModelsRestoreAndRawLookalikesRemainDocuments() throws Exception {
        String destination = index(false);
        String body = "{\"value\":\"legacy\"}";
        String envelope = mapper.writeValueAsString(new ExportModel().setId("old-id").setJson(body));
        new JsonImporter(store, destination, Collections.singletonList(envelope), null, true).call();
        assertEquals(mapper.readTree(body), mapper.readTree(service.realTimeGet(destination, "old-id")));
        String raw = "{\"id\":\"raw-id\",\"json\":\"business string\",\"routing\":\"business field\"}";
        new JsonImporter(store, destination, Collections.singletonList(raw), null).call();
        assertEquals(mapper.readTree(raw), mapper.readTree(service.realTimeGet(destination, "raw-id")));
    }

    @Test(timeout=30000) public void invalidModelBlockWritesNothing() throws Exception {
        String destination = index(false);
        String valid = mapper.writeValueAsString(new ExportModel().setId("valid").setJson("{\"value\":1}"));
        String invalid = "{\"id\":\"bad\",\"json\":\"[]\"}";
        try {
            new JsonImporter(store, destination, Arrays.asList(valid, invalid), null, true).call();
            fail("Expected invalid model source");
        } catch (IllegalArgumentException expected) { }
        service.refresh(destination);
        assertEquals(0, store.getIndex(String.class, destination).count());
    }
}
