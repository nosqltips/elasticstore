package com.nosqlrevolution.cursor;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.transport.ElasticsearchTransport;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import static org.junit.Assert.*;

public class PagingFailureTest {
    private ElasticsearchClient client(boolean initialFailure, boolean empty) {
        AtomicInteger requests = new AtomicInteger();
        ElasticsearchTransport transport = (ElasticsearchTransport) Proxy.newProxyInstance(
                ElasticsearchTransport.class.getClassLoader(),
                new Class<?>[] { ElasticsearchTransport.class }, (proxy, method, args) -> {
                    if (method.getName().equals("jsonpMapper")) return new co.elastic.clients.json.jackson.JacksonJsonpMapper();
                    if (method.getName().equals("performRequest")) {
                        if (initialFailure || requests.getAndIncrement() > 0) {
                            throw new IOException("page unavailable");
                        }
                        return SearchResponse.of(r -> r.took(1).timedOut(false)
                                .shards(s -> s.total(1).successful(1).failed(0))
                                .pitId("pit")
                                .hits(h -> h.hits(empty ? Collections.emptyList() :
                                        Collections.singletonList(Hit.of(hit -> hit.index("test")
                                                .id("1").sort("1"))))));
                    }
                    return null;
                });
        return new ElasticsearchClient(transport);
    }

    @Test
    public void initialIoFailureKeepsOriginalCause() {
        try {
            new HitScrollCursorIterator(SearchRequest.of(r -> r.size(1)), client(true, false));
            fail("Expected IO failure");
        } catch (UncheckedIOException ex) { assertEquals("page unavailable", ex.getCause().getMessage()); }
    }

    @Test
    public void nextPageIoFailureIsNotNullOrEndOfStream() {
        HitScrollCursorIterator cursor = new HitScrollCursorIterator(
                SearchRequest.of(r -> r.size(1)), client(false, false));
        assertEquals("1", cursor.next().id());
        try { cursor.hasNext(); fail("Expected IO failure"); }
        catch (UncheckedIOException ex) { assertEquals("page unavailable", ex.getCause().getMessage()); }
    }

    @Test
    public void emptyInitialPageIsValidEndOfStream() {
        HitScrollCursorIterator cursor = new HitScrollCursorIterator(
                SearchRequest.of(r -> r.size(1)), client(false, true));
        assertFalse(cursor.hasNext());
        try { cursor.next(); fail("Expected exhausted iterator"); }
        catch (NoSuchElementException expected) { }
    }
}
