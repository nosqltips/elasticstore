package com.nosqlrevolution.cursor;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import com.nosqlrevolution.service.QueryService;
import com.nosqlrevolution.util.ClientCompatibility;
import java.io.*;
import java.lang.reflect.Proxy;
import java.util.*;
import org.junit.*;
import static org.junit.Assert.*;

/** Partial responses are failures, even when transport execution succeeds. */
public class ReadIntegrityTest {
    private static class Fixture {
        final Queue<Object> pages = new ArrayDeque<>();
        final List<SearchRequest> requests = new ArrayList<>();
        final List<String> closed = new ArrayList<>();
        IOException cleanupFailure;
        int countFailures;
        OpenPointInTimeResponse opened;
        final ElasticsearchClient client = new ElasticsearchClient((ElasticsearchTransport) Proxy.newProxyInstance(
                ElasticsearchTransport.class.getClassLoader(), new Class<?>[] {ElasticsearchTransport.class}, (proxy, method, args) -> {
                    if (method.getName().equals("jsonpMapper")) return new JacksonJsonpMapper();
                    if (!method.getName().equals("performRequest")) return null;
                    Object request = args[0];
                    if (request instanceof SearchRequest) {
                        requests.add((SearchRequest) request);
                        Object reply = pages.remove();
                        if (reply instanceof IOException) throw (IOException) reply;
                        return reply;
                    }
                    if (request instanceof ClosePointInTimeRequest) {
                        closed.add(((ClosePointInTimeRequest) request).id());
                        if (cleanupFailure != null) throw cleanupFailure;
                        return ClosePointInTimeResponse.of(r -> r.succeeded(true).numFreed(1));
                    }
                    if (request instanceof OpenPointInTimeRequest) return opened;
                    if (request instanceof CountRequest) return CountResponse.of(r -> r.count(42)
                            .shards(s -> s.total(2).successful(2 - countFailures).failed(countFailures)));
                    throw new AssertionError("Unexpected request " + request.getClass());
                }));
    }

    private SearchResponse<JsonData> page(String pit, String fault, boolean empty) {
        return SearchResponse.of(r -> r.took(1).timedOut("timeout".equals(fault))
                .terminatedEarly("early".equals(fault)).pitId(pit)
                .shards(s -> s.total(2).successful("shards".equals(fault) ? 1 : 2).failed("shards".equals(fault) ? 1 : 0))
                .hits(h -> h.hits(empty ? Collections.emptyList() : Collections.singletonList(
                        co.elastic.clients.elasticsearch.core.search.Hit.of(hit -> hit.index("test").id("1").sort("1")
                                .source(JsonData.of(Collections.singletonMap("name", "document"))))))));
    }

    private SearchRequest request() {
        return SearchRequest.of(r -> r.size(1).timeout("2s").pit(p -> p.id("pit-open")));
    }

    private static RuntimeException failure(Runnable action) {
        try { action.run(); throw new AssertionError("Expected incomplete read failure"); }
        catch (RuntimeException expected) { return expected; }
    }

    @Test public void incompleteInitialPagesAreNotEmptyResultsAndCloseRotatedPit() {
        for (String fault : Arrays.asList("timeout", "shards", "early")) {
            Fixture fixture = new Fixture();
            fixture.pages.add(page("pit-initial", fault, true));
            RuntimeException error = failure(() -> new HitScrollCursorIterator(request(), fixture.client));
            assertTrue(error.getMessage(), error.getMessage().contains("incomplete"));
            assertEquals(Collections.singletonList("pit-initial"), fixture.closed);
            assertEquals(1, fixture.requests.size());
        }
    }

    @Test public void incompleteLaterPagesRemainFailedWithoutRetryAndCloseLatestPit() throws Exception {
        for (String fault : Arrays.asList("timeout", "shards", "early")) {
            Fixture fixture = new Fixture();
            fixture.pages.add(page("pit-first", null, false));
            fixture.pages.add(page("pit-failed", fault, false));
            HitScrollCursorIterator cursor = new HitScrollCursorIterator(request(), fixture.client);
            assertEquals("1", cursor.next().id());
            RuntimeException error = failure(cursor::hasNext);
            assertSame(error, failure(cursor::hasNext));
            assertSame(error, failure(cursor::next));
            cursor.close();
            assertEquals(Collections.singletonList("pit-failed"), fixture.closed);
            assertEquals(2, fixture.requests.size());
            assertEquals(Boolean.FALSE, fixture.requests.get(1).allowPartialSearchResults());
            assertEquals("2s", fixture.requests.get(1).timeout());
        }
    }

    @Test public void ioFailureAlsoClosesAndCannotBecomeNormalExhaustion() {
        Fixture fixture = new Fixture();
        fixture.pages.add(page("pit-first", null, false));
        IOException unavailable = new IOException("page unavailable");
        fixture.pages.add(unavailable);
        HitScrollCursorIterator cursor = new HitScrollCursorIterator(request(), fixture.client);
        cursor.next();
        RuntimeException error = failure(cursor::hasNext);
        assertTrue(error instanceof UncheckedIOException);
        assertSame(unavailable, error.getCause());
        assertSame(error, failure(cursor::hasNext));
        assertEquals(Collections.singletonList("pit-first"), fixture.closed);
    }

    @Test public void cleanupFailureIsSuppressedWithoutMaskingIncompleteRead() {
        Fixture fixture = new Fixture();
        fixture.pages.add(page("pit-failed", "timeout", false));
        fixture.cleanupFailure = new IOException("cleanup failed");
        RuntimeException error = failure(() -> new HitScrollCursorIterator(request(), fixture.client));
        assertTrue(error.getMessage().contains("timed out"));
        assertArrayEquals(new Throwable[] {fixture.cleanupFailure}, error.getSuppressed());
        assertEquals(Collections.singletonList("pit-failed"), fixture.closed);
    }

    @Test public void singleSearchRejectsIncompleteResponsesAndRequestsStrictResults() {
        for (String fault : Arrays.asList("timeout", "shards", "early")) {
            Fixture fixture = new Fixture();
            fixture.pages.add(page("unused", fault, false));
            RuntimeException error = failure(() -> new QueryService(fixture.client).getSingle("test"));
            assertTrue(error.getMessage().contains("incomplete"));
            assertEquals(Boolean.FALSE, fixture.requests.get(0).allowPartialSearchResults());
        }
    }

    @Test public void bothCountOverloadsRejectPartialShardCounts() {
        Fixture fixture = new Fixture();
        fixture.countFailures = 1;
        QueryService service = new QueryService(fixture.client);
        assertTrue(failure(() -> service.count("test")).getMessage().contains("shard failures"));
        assertTrue(failure(() -> service.count(null, new String[] {"first", "second"})).getMessage().contains("shard failures"));
        fixture.countFailures = 0;
        assertEquals(42, service.count("test"));
    }

    @Test public void typedNativeSearchRetainsSourcesAndResponseMetadata() {
        Fixture fixture = new Fixture();
        SearchResponse<JsonData> response = page("pit-rotated", null, false);
        fixture.pages.add(response);
        SearchResponse<JsonData> actual = new QueryService(fixture.client).executeSearch(request(), JsonData.class);
        assertSame(response, actual);
        assertEquals("pit-rotated", actual.pitId());
        assertEquals("document", actual.hits().hits().get(0).source().toJson(new JacksonJsonpMapper()).asJsonObject().getString("name"));
        assertTrue("Native-search PITs remain caller-owned", fixture.closed.isEmpty());
    }

    @Test public void nativeAndLegacySearchHelpersNeverSwallowReadFailures() {
        for (String fault : Arrays.asList("timeout", "shards", "early")) {
            Fixture fixture = new Fixture();
            fixture.pages.add(page("pit-failed", fault, false));
            fixture.pages.add(page("pit-failed", fault, false));
            QueryService service = new QueryService(fixture.client);
            RuntimeException error = failure(() -> service.executeSearch(request(), JsonData.class));
            assertTrue(error.getMessage().contains("incomplete"));
            assertEquals("pit-failed", ((com.nosqlrevolution.IncompleteSearchException) error).getResponse().pitId());
            assertTrue(failure(() -> service.executeBuilder(request())).getMessage().contains("incomplete"));
        }
        Fixture fixture = new Fixture();
        IOException unavailable = new IOException("search unavailable");
        fixture.pages.add(unavailable);
        fixture.pages.add(unavailable);
        QueryService service = new QueryService(fixture.client);
        assertSame(unavailable, failure(() -> service.executeSearch(request(), JsonData.class)).getCause());
        assertSame(unavailable, failure(() -> service.executeBuilder(request())).getCause());
    }

    @Test public void partialPitCreationClosesContextWhenClientExposesShardMetadata() {
        // withJson uses a strict mapper: check capability before parsing the newer response shape.
        Assume.assumeTrue(Arrays.stream(OpenPointInTimeResponse.class.getMethods())
                .anyMatch(method -> method.getName().equals("shards")));
        Fixture fixture = new Fixture();
        fixture.opened = new OpenPointInTimeResponse.Builder().withJson(new StringReader(
                "{\"id\":\"pit-partial\",\"_shards\":{\"total\":2,\"successful\":1,\"failed\":1}}")).build();
        // Older clients discard this field; Elasticsearch 8.19 still defaults PIT creation to strict.
        assertNotNull(ClientCompatibility.pitShards(fixture.opened));
        RuntimeException error = failure(() -> new QueryService(fixture.client).findAllScroll("test"));
        assertTrue(error.getMessage().contains("shard failures"));
        assertEquals(Collections.singletonList("pit-partial"), fixture.closed);
        assertTrue(fixture.requests.isEmpty());
    }
}
