package com.nosqlrevolution;

import org.elasticsearch.client.RestHighLevelClient;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author cbrown
 */
public class ElasticStoreTest {

     @Test
     public void testLocal() throws Exception {
         ElasticStore es = new ElasticStore().asLocal().execute();
         assertNotNull(es);

         RestHighLevelClient client = es.getRestClient();
         assertNotNull(client);
         
         es.close();
     }

     @Test
     public void testNode() throws Exception {
         ElasticStore es = new ElasticStore().asLocal().execute();
         assertNotNull(es);

         ElasticStore esNode = new ElasticStore().asLocal().execute();
         RestHighLevelClient client = es.getRestClient();
         assertNotNull(client);
         
         es.close();
     }

     @Test
     public void testTransport() throws Exception {
         ElasticStore es = new ElasticStore().asLocal().execute();
         assertNotNull(es);

         ElasticStore esTransport = new ElasticStore().asElastic().withUnicast("127.0.0.1").execute();
         RestHighLevelClient client = es.getRestClient();
         assertNotNull(client);
         
         es.close();
     }

     @Test
     public void testMemory() throws Exception {
         ElasticStore es = new ElasticStore().asMemoryOnly().execute();
         assertNotNull(es);

         ElasticStore esNode = new ElasticStore().asMemoryOnly().execute();
         RestHighLevelClient client = es.getRestClient();
         assertNotNull(client);
         
         es.close();
     }
}
