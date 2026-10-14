package com.nosqlrevolution;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
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

         ElasticsearchClient client = es.getClient();
         assertNotNull(client);
         
         es.close();
     }

     @Test
     public void testNode() throws Exception {
         ElasticStore es = new ElasticStore().asLocal().execute();
         assertNotNull(es);

         ElasticStore esNode = new ElasticStore().asLocal().execute();
         ElasticsearchClient client = es.getClient();
         assertNotNull(client);
         
         es.close();
     }

     @Test
     public void testTransport() throws Exception {
         ElasticStore es = new ElasticStore().asLocal().execute();
         assertNotNull(es);

         ElasticStore esTransport = new ElasticStore().asElastic().withUnicast("127.0.0.1").execute();
         ElasticsearchClient client = es.getClient();
         assertNotNull(client);
         
         es.close();
     }

     @Test
     public void testMemory() throws Exception {
         ElasticStore es = new ElasticStore().asMemoryOnly().execute();
         assertNotNull(es);

         ElasticStore esNode = new ElasticStore().asMemoryOnly().execute();
         ElasticsearchClient client = es.getClient();
         assertNotNull(client);
         
         es.close();
     }
}
