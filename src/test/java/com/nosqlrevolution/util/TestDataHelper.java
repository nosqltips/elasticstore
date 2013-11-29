package com.nosqlrevolution.util;

import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.client.Client;
import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

/**
 *
 * @author Craig
 */
public class TestDataHelper {
    public static void indexCursorTestData(Client client, String index, String type) {
        try {
        BulkRequestBuilder bulkRequest = client.prepareBulk();
        bulkRequest.add(client.prepareIndex(index, type, "1")
                .setSource(
                    jsonBuilder().startObject().field("id", "1").field("name", "Homer Simpson").field("username", "hsimpson").endObject()
                ));
        bulkRequest.add(client.prepareIndex(index, type, "2")
                .setSource(
                    jsonBuilder().startObject().field("id", "2").field("name", "C Montgomery Burns").field("username", "cburns").endObject()
                ));
        bulkRequest.add(client.prepareIndex(index, type, "3")
                .setSource(
                    jsonBuilder().startObject().field("id", "3").field("name", "Carl Carlson").field("username", "ccarlson").endObject()
                ));
        bulkRequest.add(client.prepareIndex(index, type, "4")
                .setSource(
                    jsonBuilder().startObject().field("id", "4").field("name", "Clancy Wigum").field("username", "cwigum").endObject()
                ));
        bulkRequest.add(client.prepareIndex(index, type, "5")
                .setSource(
                    jsonBuilder().startObject().field("id", "5").field("name", "Lenny Leonard").field("username", "lleonard").endObject()
                ));
        
        bulkRequest.execute().actionGet();
        client.admin().indices().refresh(new RefreshRequest(index)).actionGet();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
