package com.nosqlrevolution.util;

import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.settings.Settings;
import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

/**
 *
 * @author Craig
 */
public class TestDataHelper {
    public static RestHighLevelClient createTestClient() {
        Settings.Builder builder = Settings.builder()
            .put("es.index.storage.type", "memory")
            .put("index.number_of_shards", "1")
            .put("index.number_of_replicas", "0");
            
//        Client client = nodeBuilder()
//                .settings(builder.build())
//                .local(true)
//                .data(true)
//                .node()
//                .client();
//
//        client.admin()
//                .cluster()
//                .prepareHealth()
//                .setWaitForYellowStatus()
//                .execute()
//                .actionGet();

        return null;
    }
    
//    public static void indexCursorTestData(RestHighLevelClient restClient, String index, String type) {
//        try {
//            BulkRequestBuilder bulkRequest = restClient.prepareBulk();
//            bulkRequest.add(restClient.prepareIndex(index, type, "1")
//                .setSource(
//                    jsonBuilder().startObject().field("id", "1").field("name", "Homer Simpson").field("username", "hsimpson").endObject()
//                ));
//            bulkRequest.add(restClient.prepareIndex(index, type, "2")
//                .setSource(
//                    jsonBuilder().startObject().field("id", "2").field("name", "C Montgomery Burns").field("username", "cburns").endObject()
//                ));
//            bulkRequest.add(restClient.prepareIndex(index, type, "3")
//                .setSource(
//                    jsonBuilder().startObject().field("id", "3").field("name", "Carl Carlson").field("username", "ccarlson").endObject()
//                ));
//            bulkRequest.add(restClient.prepareIndex(index, type, "4")
//                .setSource(
//                    jsonBuilder().startObject().field("id", "4").field("name", "Clancy Wigum").field("username", "cwigum").endObject()
//                ));
//            bulkRequest.add(restClient.prepareIndex(index, type, "5")
//                .setSource(
//                    jsonBuilder().startObject().field("id", "5").field("name", "Lenny Leonard").field("username", "lleonard").endObject()
//                ));
//        
//            BulkResponse response = bulkRequest.execute().actionGet();
//            if (response.hasFailures()) {
//                System.out.println(response.buildFailureMessage());
//            }
//        restClient.admin().indices().refresh(new RefreshRequest(index)).actionGet();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}
