package com.nosqlrevolution.support;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.nosqlrevolution.ElasticStore;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.junit.After;
import org.junit.Before;

/** A lazy client for server-free factory tests. No requests should be sent. */
public abstract class StoreTestSupport {
    protected ElasticStore elasticStore;
    @Before public void openClient() {
        elasticStore = new ElasticStore().withClient(new ElasticsearchClient(new RestClientTransport(
                RestClient.builder(new HttpHost("127.0.0.1", 1)).build(), new JacksonJsonpMapper())), true);
    }
    @After public void closeClient() { if (elasticStore != null) elasticStore.close(); }
}
