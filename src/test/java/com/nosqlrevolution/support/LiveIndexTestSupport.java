package com.nosqlrevolution.support;

import com.nosqlrevolution.ElasticStore;
import java.util.UUID;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;

/** Explicit live tests own a uniquely named index and their client, never a shared index. */
public abstract class LiveIndexTestSupport {
    protected ElasticStore elasticStore;
    protected String indexName;
    private boolean created;
    @Before public void connect() throws Exception {
        String endpoint = System.getProperty("elasticstore.test.url");
        Assume.assumeNotNull(endpoint);
        elasticStore = new ElasticStore().withEndpoints(endpoint).execute();
        indexName = "elasticstore-legacy-" + UUID.randomUUID();
        elasticStore.getClient().indices().create(r -> r.index(indexName)
                .settings(s -> s.numberOfShards("1").numberOfReplicas("0")));
        created = true;
    }
    @After public void cleanup() throws Exception {
        try { if (created) elasticStore.getClient().indices().delete(r -> r.index(indexName)); }
        finally { if (elasticStore != null) elasticStore.close(); }
    }
}
