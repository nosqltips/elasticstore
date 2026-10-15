package com.nosqlrevolution;

import com.nosqlrevolution.support.StoreTestSupport;
import org.junit.Test;
import static org.junit.Assert.*;

public class ElasticStoreTest extends StoreTestSupport {
    @Test(expected=IllegalArgumentException.class) public void unopenedStoreCannotCreateHandles() throws Exception {
        new ElasticStore().getIndex(Object.class, "documents");
    }
    @Test public void closingStoreDetachesClientAndIsIdempotent() throws Exception {
        assertTrue(elasticStore.isInitialized());
        elasticStore.close();
        assertFalse(elasticStore.isInitialized());
        assertNull(elasticStore.getClient());
        elasticStore.close();
        try { elasticStore.getIndex(Object.class, "documents"); fail("Expected closed store rejection"); }
        catch (IllegalArgumentException expected) { }
    }
    @Test public void initializedStateIsPerInstance() {
        ElasticStore unopened = new ElasticStore();
        assertFalse(unopened.isInitialized());
        assertTrue(elasticStore.isInitialized());
        unopened.close();
        assertTrue(elasticStore.isInitialized());
    }
}
