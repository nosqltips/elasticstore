package com.nosqlrevolution;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nosqlrevolution.annotation.*;
import com.nosqlrevolution.util.AnnotationHelper;
import java.io.StringReader;
import java.lang.reflect.Proxy;
import java.util.*;
import org.junit.*;
import static org.junit.Assert.*;

/** Invalid metadata must never become an auto-ID or an unconstrained write. */
public class MetadataIntegrityTest {
    private final List<Object> requests = new ArrayList<>();
    private Index<Object> index;

    @Before public void setup() throws Exception {
        ElasticsearchTransport transport = (ElasticsearchTransport) Proxy.newProxyInstance(
                ElasticsearchTransport.class.getClassLoader(), new Class<?>[] {ElasticsearchTransport.class}, (proxy, method, args) -> {
                    if (method.getName().equals("jsonpMapper")) return new JacksonJsonpMapper();
                    if (!method.getName().equals("performRequest")) return null;
                    requests.add(args[0]);
                    if (args[0] instanceof IndexRequest) {
                        IndexRequest<?> request = (IndexRequest<?>) args[0];
                        String id = request.id() == null ? "server-generated" : request.id();
                        return new IndexResponse.Builder().withJson(new StringReader("{\"_index\":\"test\",\"_id\":\"" + id
                                + "\",\"_version\":1,\"result\":\"created\",\"_seq_no\":0,\"_primary_term\":1,"
                                + "\"_shards\":{\"total\":1,\"successful\":1,\"failed\":0}}")).build();
                    }
                    if (args[0] instanceof BulkRequest) {
                        List<String> items = new ArrayList<>();
                        for (BulkOperation operation : ((BulkRequest) args[0]).operations()) {
                            String verb = operation.isDelete() ? "delete" : "index";
                            String id = operation.isDelete() ? operation.delete().id() : operation.index().id();
                            items.add("{\"" + verb + "\":{\"_index\":\"test\",\"_id\":\"" + id + "\",\"status\":200,\"result\":\""
                                    + (operation.isDelete() ? "deleted" : "updated") + "\"}}");
                        }
                        return new BulkResponse.Builder().withJson(new StringReader("{\"took\":1,\"errors\":false,\"items\":["
                                + String.join(",", items) + "]}")).build();
                    }
                    throw new AssertionError("Unexpected Elasticsearch request");
                });
        index = new ElasticStore().withClient(new ElasticsearchClient(transport)).getIndex(Object.class, "test");
    }

    public static class Body { public String value = "payload"; }
    public static class Good extends Body { @DocumentId public String key = "stable"; }
    public static class ThrowingId extends Body {
        final IllegalStateException problem = new IllegalStateException("identity unavailable");
        public boolean fail = true;
        @DocumentId @JsonIgnore public String getKey() { if (fail) throw problem; return "recovered"; }
    }
    public static class ThrowingRouting extends Good {
        final IllegalStateException problem = new IllegalStateException("routing unavailable");
        @Routing @JsonIgnore public String getTenant() { throw problem; }
    }
    public static class ThrowingVersion extends Good {
        final IllegalStateException problem = new IllegalStateException("version unavailable");
        @Version @JsonIgnore public Long getRevision() { throw problem; }
    }
    public static class Overridden extends ThrowingRouting {
        @Version @JsonIgnore public Long getRevision() { throw new AssertionError("Overridden version getter called"); }
    }
    public static class UnsupportedId extends Body { @DocumentId public Double key; }
    public static class UnsupportedRouting extends Good { @Routing public Boolean tenant; }
    public static class UnsupportedVersion extends Good { @Version public Date revision; }
    public static class ParameterGetter extends Body {
        @DocumentId public String key(String suffix) { throw new AssertionError("Invalid getter invoked"); }
    }
    public static class VoidGetter extends Body {
        @DocumentId public void key() { throw new AssertionError("Invalid getter invoked"); }
    }
    public static class UnsupportedGetter extends Body { @DocumentId public Double key() { return null; } }
    public static class BoxedVoidGetter extends Body { @DocumentId public Void key() { throw new AssertionError("Invalid getter invoked"); } }
    public static class BlankId extends Body { @DocumentId public String key = " "; }
    public static class NullTextId extends Body { @DocumentId public Object key = new Object() {
        @Override public String toString() { return null; }
    }; }
    public static class BrokenTextId extends Body {
        final IllegalStateException problem = new IllegalStateException("conversion failed");
        @DocumentId public Object key = new Object() { @Override public String toString() { throw problem; } };
    }
    public static class FatalId extends Body {
        final AssertionError problem = new AssertionError("fatal getter failure");
        @DocumentId public String key() { throw problem; }
    }
    public static class NullMetadata extends Body {
        @DocumentId public String key;
        @Routing public String tenant;
        @Version public Long revision;
    }
    public static class GeneratedBase extends Body { @DocumentId @UUIDProvider public String key; }
    public static class Generated extends GeneratedBase { }
    public static class InvalidProvider extends Body { @DocumentId @UUIDProvider public Long key; }
    public static class FinalProvider extends Body { @DocumentId @UUIDProvider public final String key = null; }
    public static class StaticProvider extends Body { @DocumentId @UUIDProvider public static String key = "unchanged"; }
    public static class InvalidProviderBase extends Body { @UUIDProvider public Long other; }
    public static class MixedProviders extends InvalidProviderBase { @DocumentId @UUIDProvider public String key; }

    private RuntimeException rejected(Runnable action) {
        int before = requests.size();
        try { action.run(); throw new AssertionError("Expected metadata rejection"); }
        catch (RuntimeException expected) { assertEquals("Invalid metadata reached Elasticsearch", before, requests.size()); return expected; }
    }

    @Test public void throwingIdentityRetainsCauseAndNeverGeneratesAnId() {
        ThrowingId document = new ThrowingId();
        RuntimeException error = rejected(() -> index.write(document));
        assertSame(document.problem, error.getCause());
        assertTrue(error.getMessage(), error.getMessage().contains("#getKey"));
        rejected(() -> index.write(Arrays.asList(new Good(), document)));
        rejected(() -> index.remove(document));
    }

    @Test public void routingAndVersionGetterFailuresRejectWholeWriteBeforeRequests() {
        ThrowingRouting routing = new ThrowingRouting();
        ThrowingVersion version = new ThrowingVersion();
        assertSame(routing.problem, rejected(() -> index.write(routing)).getCause());
        assertSame(version.problem, rejected(() -> index.write(version)).getCause());
        rejected(() -> index.write(Arrays.asList(new Good(), routing)));
        rejected(() -> index.write(Arrays.asList(new Good(), version)));
        rejected(() -> index.remove(new Good(), routing));
    }

    @Test public void unsupportedTypesAndMalformedGettersDoNotBecomeMissingMetadata() {
        for (Object document : Arrays.asList(new UnsupportedId(), new UnsupportedRouting(), new UnsupportedVersion(),
                new ParameterGetter(), new VoidGetter(), new UnsupportedGetter(), new BoxedVoidGetter())) {
            RuntimeException error = rejected(() -> index.write(document));
            assertTrue(error.getMessage(), error instanceof IllegalArgumentException);
        }
    }

    @Test public void blankAndBrokenConversionsAreErrorsRatherThanAbsentIdentity() {
        rejected(() -> index.write(new BlankId()));
        rejected(() -> index.write(new NullTextId()));
        BrokenTextId document = new BrokenTextId();
        assertSame(document.problem, rejected(() -> index.write(document)).getCause());
    }

    @Test public void fatalGetterErrorsAreNotConvertedToMissingMetadata() {
        FatalId document = new FatalId();
        try { index.write(document); fail("Expected getter error"); }
        catch (AssertionError error) { assertSame(document.problem, error); }
        assertTrue(requests.isEmpty());
    }

    @Test public void invalidProviderTargetsFailEvenWhenPopulatedAndBeforeAnyGeneration() {
        InvalidProvider populated = new InvalidProvider(); populated.key = 7L;
        MixedProviders mixed = new MixedProviders();
        for (Object document : Arrays.asList(new InvalidProvider(), populated, new FinalProvider(), new StaticProvider(), mixed)) {
            rejected(() -> index.write(document));
        }
        assertNull("No provider should run before all targets are valid", mixed.key);
        assertEquals("unchanged", StaticProvider.key);
    }

    @Test public void inheritedGeneratedIdentityRemainsStableAndIsSerialized() {
        Generated document = new Generated();
        index.write(document);
        String id = document.key;
        assertNotNull(id);
        assertEquals(id, UUID.fromString(id).toString());
        index.write(document);
        for (Object item : requests) {
            IndexRequest<?> request = (IndexRequest<?>) item;
            assertEquals(id, request.id());
            assertEquals(id, ((JsonData) request.document()).toJson().asJsonObject().getString("key"));
        }
        Generated missing = new Generated();
        rejected(() -> index.remove(missing));
        assertNull("Deletes must not generate identity", missing.key);
    }

    @Test public void genuineNullMetadataRetainsServerGeneratedIdBehavior() {
        OperationStatus result = index.write(new NullMetadata());
        IndexRequest<?> request = (IndexRequest<?>) requests.get(0);
        assertNull(request.id()); assertNull(request.routing()); assertNull(request.version());
        assertEquals("server-generated", result.getItems().get(0).getId());
    }

    @Test public void explicitRoutingAndVersionBypassOverriddenModelGetters() {
        index.write(DocumentOptions.DEFAULT.routing("explicit").externalVersion(5), new Overridden());
        IndexRequest<?> request = (IndexRequest<?>) requests.get(0);
        assertEquals("explicit", request.routing());
        assertEquals(Long.valueOf(5), request.version());
    }

    @Test public void deleteDoesNotReadWriteOnlyVersionMetadata() {
        index.remove(new ThrowingVersion());
        assertEquals("stable", ((BulkRequest) requests.get(0)).operations().get(0).delete().id());
    }

    @Test public void failedBufferedValidationKeepsDocumentsForRetry() {
        ThrowingId document = new ThrowingId();
        index.setAutoBulkFlush(false);
        index.addBulk(new Good()); index.addBulk(document);
        rejected(() -> index.flushBulk());
        document.fail = false;
        assertTrue(index.flushBulk());
        assertEquals(2, ((BulkRequest) requests.get(0)).operations().size());
        assertTrue(index.flushBulk());
        assertEquals(1, requests.size());
    }

    @Test public void conventionalIdentityMatchingDoesNotDependOnLocale() {
        class Conventional extends Body { public String ID = "locale-stable"; }
        Locale previous = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            assertEquals("locale-stable", AnnotationHelper.findDocumentId(new Conventional(), null));
        } finally { Locale.setDefault(previous); }
    }
}
