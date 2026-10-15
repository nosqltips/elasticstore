package com.nosqlrevolution.apps;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.nosqlrevolution.ElasticStore;
import java.util.Collections;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.junit.Test;
import static org.junit.Assert.*;

public class ModelImportTest {
    @Test public void invalidEnvelopesFailBeforeTransport() throws Exception {
        // This client has no server: validation must fail before any request is attempted.
        try (ElasticStore store = new ElasticStore().withClient(new ElasticsearchClient(new RestClientTransport(
                RestClient.builder(new HttpHost("127.0.0.1", 1)).build(), new JacksonJsonpMapper())), true)) {
            String[] invalid = { "null", "[]", "{}", "{\"id\":\"\",\"json\":\"{}\"}",
                "{\"id\":7,\"json\":\"{}\"}", "{\"id\":\"id\",\"json\":{}}",
                "{\"id\":\"id\",\"json\":\"null\"}", "{\"id\":\"id\",\"json\":\"{} {}\"}",
                "{\"id\":\"id\",\"json\":\"{}\",\"routing\":7}",
                "{\"id\":\"id\",\"json\":\"{}\",\"routing\":\" \"}" };
            for (String record : invalid) {
                try { new JsonImporter(store, "index", Collections.singletonList(record), null, true).call(); fail(record); }
                catch (IllegalArgumentException | com.fasterxml.jackson.core.JsonProcessingException expected) { }
            }
            try { new JsonImporter(store, "index", Collections.emptyList(), "id", true); fail(); }
            catch (IllegalArgumentException expected) { }
        }
    }
}
