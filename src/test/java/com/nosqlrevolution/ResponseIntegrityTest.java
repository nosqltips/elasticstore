package com.nosqlrevolution;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.indices.RefreshResponse;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import com.nosqlrevolution.service.QueryService;
import java.io.StringReader;
import java.lang.reflect.Proxy;
import org.junit.Test;
import static org.junit.Assert.*;

/** A successful HTTP request must not hide missing results or shard failures. */
public class ResponseIntegrityTest {
    private Object response;
    private final ElasticsearchClient client = new ElasticsearchClient((ElasticsearchTransport) Proxy.newProxyInstance(
            ElasticsearchTransport.class.getClassLoader(), new Class<?>[] {ElasticsearchTransport.class}, (proxy, method, args) -> {
                if (method.getName().equals("jsonpMapper")) return new JacksonJsonpMapper();
                if (method.getName().equals("performRequest")) return response;
                return null;
            }));
    private final QueryService service = new QueryService(client);
    private static final String SHARDS = "\"_shards\":{\"total\":2,\"successful\":1,\"failed\":1}";
    private static final String ITEM = "{\"index\":{\"_index\":\"test\",\"_id\":\"a\",\"status\":201,\"result\":\"created\"%s}}";

    private void bulk(boolean errors, String items) {
        response = new BulkResponse.Builder().withJson(new StringReader(
                "{\"took\":1,\"errors\":" + errors + ",\"items\":[" + items + "]}")).build();
    }
    private OperationStatus write() { return service.bulkIndexWithIds("test", new String[] {"{}"}, new String[] {"a"}); }
    private IllegalStateException rejected(Runnable action) {
        try { action.run(); throw new AssertionError("Expected response rejection"); }
        catch (IllegalStateException expected) { return expected; }
    }

    @Test public void bulkRequiresExactlyOneResultPerRequestedDocument() {
        bulk(false, ""); rejected(() -> write());
        String item = String.format(ITEM, "");
        bulk(false, item + "," + item); rejected(() -> write());
        bulk(false, item); assertTrue(write().succeeded());
    }

    @Test public void inconsistentErrorFlagsAndUntypedErrorsAreNotSuccess() {
        bulk(true, String.format(ITEM, "")); rejected(() -> write());
        bulk(false, String.format(ITEM, ",\"error\":{\"reason\":\"failed\"}"));
        OperationException error = (OperationException) rejected(() -> write());
        assertEquals("failed", error.getStatus().getItems().get(0).getErrorReason());
        try {
            bulk(false, String.format(ITEM, ",\"error\":{}")); rejected(() -> write());
        } catch (co.elastic.clients.json.JsonpMappingException rejectedByOlderClient) {
            // Older clients require an error reason and reject this malformed response during parsing.
        }
    }

    @Test public void bulkShardFailuresRetainDocumentResults() {
        bulk(false, String.format(ITEM, "," + SHARDS));
        OperationException error = (OperationException) rejected(() -> write());
        assertEquals("a", error.getStatus().getItems().get(0).getId());
        assertEquals("shard_failure", error.getStatus().getItems().get(0).getErrorType());
        assertEquals(1, error.getStatus().getFailureCount());
    }

    @Test public void missingDeletesAreIdempotentButRealErrorsAreRetained() {
        String missing = "{\"delete\":{\"_index\":\"test\",\"_id\":\"a\",\"status\":404,\"result\":\"not_found\"%s}}";
        bulk(false, String.format(missing, "")); assertTrue(service.deleteAll("test", new String[] {"a"}).succeeded());
        bulk(true, String.format(missing, ",\"error\":{\"type\":\"index_not_found_exception\",\"reason\":\"missing index\"}"));
        rejected(() -> service.deleteAll("test", new String[] {"a"}));
        bulk(false, String.format(ITEM, "").replace("201", "404").replace("created", "not_found"));
        rejected(() -> write());
    }

    @Test public void singleWritesAndDeletesRejectShardFailures() {
        String body = "{\"_index\":\"test\",\"_id\":\"a\",\"_version\":1,\"result\":\"created\",\"_seq_no\":0,\"_primary_term\":1," + SHARDS + "}";
        response = new IndexResponse.Builder().withJson(new StringReader(body)).build();
        OperationException error = (OperationException) rejected(() -> service.index("test", "{}", "a"));
        assertEquals("a", error.getStatus().getItems().get(0).getId());
        assertEquals(Long.valueOf(0), error.getStatus().getItems().get(0).getSequenceNumber());
        response = new DeleteResponse.Builder().withJson(new StringReader(body.replace("created", "deleted"))).build();
        rejected(() -> service.delete("test", "a"));
    }

    @Test public void refreshChecksShardFailures() {
        response = new RefreshResponse.Builder().withJson(new StringReader("{" + SHARDS + "}")).build();
        rejected(() -> service.refresh("test"));
        response = new RefreshResponse.Builder().withJson(new StringReader("{" + SHARDS.replace("\"failed\":1", "\"failed\":0") + "}")).build();
        service.refresh("test");
    }

    private void mget(String docs) {
        response = new MgetResponse.Builder<JsonData>().withJson(new StringReader("{\"docs\":[" + docs + "]}")).build();
    }
    @Test public void multiGetChecksCountAndIdentityAndPreservesGenuineMissingSlots() {
        String missing = "{\"_index\":\"test\",\"_id\":\"%s\",\"found\":false}";
        mget(String.format(missing, "a")); rejected(() -> service.realTimeMultiGet("test", new String[] {"a", "b"}));
        mget(String.format(missing, "b") + "," + String.format(missing, "a"));
        rejected(() -> service.realTimeMultiGet("test", new String[] {"a", "b"}));
        mget(String.format(missing, "a") + "," + String.format(missing, "b"));
        assertArrayEquals(new String[] {null, null}, service.realTimeMultiGet("test", new String[] {"a", "b"}));
    }

    @Test public void failedBufferedWritesRemainAvailableForRetry() throws Exception {
        Index<Object> index = new ElasticStore().withClient(client).getIndex(Object.class, "test");
        index.setAutoBulkFlush(false);
        index.addBulk(new java.util.HashMap<>());
        index.addBulk(new java.util.HashMap<>());
        bulk(false, ""); rejected(() -> index.flushBulk());
        bulk(false, String.format(ITEM, "") + "," + String.format(ITEM, "")); assertTrue(index.flushBulk());
        bulk(false, ""); assertTrue(index.flushBulk());
    }

    @Test public void reasonOnlyErrorsAreFailures() {
        assertFalse(new OperationStatus.Item("test", "a", "created", 201, null, null, null, null, "failure").succeeded());
    }
}
