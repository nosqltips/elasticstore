package com.nosqlrevolution;

import com.nosqlrevolution.util.AnnotationHelper;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.settings.Settings;

/**
 *
 * @author cbrown
 */
public class ElasticStore {
    private static final int DEFAULT_PORT = 9243;
    private static final String DEFAULT_HOST = "127.0.0.1";
    private static final String DEFAULT_TIMEOUT = "10s";
    private static final String DEFAULT_CLUSTER_NAME = "elasticsearch";
    private static boolean multicast = true;
    private static boolean local = false;
    private static boolean memory = false;
    
    private RestHighLevelClient restClient = null;
    private String[] hosts = null;
    private InetSocketAddress[] addresses = null;
    private String clusterName = DEFAULT_CLUSTER_NAME;
    private int port = DEFAULT_PORT;
    private String timeout = DEFAULT_TIMEOUT;
    
    // for Elastic service
    private String username = null;
    private String password = null;
    private String clusterId = null;
    private String region = null;
    private boolean enableSsl = false;

    /**
     * Run a local instance of ElasticSearch.
     * This is useful for unit tests or running ElasticSearch as part of your application.
     * 
     * @return 
     */
    public ElasticStore asLocal() {
        local = true;
        return this;
    }

    /**
     * Run a local instance of ElasticSearch as an in=memory store.
     * This is useful for unit tests or running ElasticSearch as part of your application.
     * 
     * @return 
     */
    public ElasticStore asMemoryOnly() {
        local = true;
        memory = true;
        return this;
    }

    /**
     * Connect to the Elastic.co service. Requires usernamen and password.
     * 
     * @return 
     */
    public ElasticStore asElastic() {
        enableSsl = true;
        multicast = false;
        return this;
    }

    /**
     * Specify multicast connection to a localhost cluster.
     * 
     * @return 
     */
    public ElasticStore withMultiCast() {
        multicast = true;
        return this;
    }

    /**
     * Specify unicast connection to a localhost cluster.
     * 
     * @return 
     */
    public ElasticStore withUnicast() {
        multicast = false;
        return this;
    }

    /**
     * Specify unicast connection with a list of nodes in the ElasticSearch cluster.
     * 
     * @param hosts
     * @return 
     */
    public ElasticStore withUnicast(String... hosts) {
        multicast = false;
        this.hosts = hosts;
        return this;
    }

    /**
     * Specify unicast connection with a list of nodes in the ElasticSearch cluster.
     * 
     * @param addresses
     * @return 
     */
    public ElasticStore withUnicast(InetSocketAddress... addresses) {
        multicast = false;
        this.addresses = addresses;
        return this;
    }
    
    /**
     * Specify the port of the ElasticSearch cluster to connect to.
     * 
     * @param port
     * @return 
     */
    public ElasticStore withPort(int port) {
        this.port = port;
        return this;
    }

    /**
     * Specify the name of the ElasticSearch cluster to connect to.
     * 
     * @param clusterName
     * @return 
     */
    public ElasticStore withClusterName(String clusterName) {
        this.clusterName = clusterName;
        return this;
    }
    
    /**
     * Specify a timeout when connecting to the ElasticSearch cluster.
     * 
     * @param timeout
     * @return 
     */
    public ElasticStore withTimeout(String timeout) {
        this.timeout = timeout;
        return this;
    }
    
    /**
     * Specify a username to use with the Elastic.co service.
     * 
     * @param username
     * @return 
     */
    public ElasticStore withUsername(String username) {
        this.username = username;
        return this;
    }
    
    /**
     * Specify a username to use with the Elastic.co service.
     * 
     * @param password
     * @return 
     */
    public ElasticStore withPassword(String password) {
        this.password = password;
        return this;
    }
    
    /**
     * Specify a clusterId to use with the Elastic.co service.
     * 
     * @param clusterId
     * @return 
     */
    public ElasticStore withClusterId(String clusterId) {
        this.clusterId = clusterId;
        return this;
    }
    
    /**
     * Specify a username to use with the Elastic.co service.
     * 
     * @param region
     * @return 
     */
    public ElasticStore withRegion(String region) {
        this.region = region;
        return this;
    }

    /**
     * Create an ElasticStore instance.
     * 
     * @return 
     * @throws java.lang.Exception 
     */
    public ElasticStore execute() throws Exception {
        Settings.Builder builder = Settings.builder()
            .put("cluster.name", clusterName)
            .put("discovery.zen.ping.timeout", timeout);
        
        if (local || memory) {
            builder.put("es.index.storage.type", "memory");
            builder.put("index.number_of_shards", "1");
            builder.put("index.number_of_replicas", "0");
        }
        
        if (! multicast) {
            if (hosts != null) {
                builder.put("discovery.zen.ping.unicast.hosts", String.join(",", hosts));
            } else {
                builder.put("discovery.zen.ping.unicast.hosts", DEFAULT_HOST);
            }
        }
        
        HttpHost[] httpHosts = new HttpHost[addresses.length];
        for (int i=0; i < addresses.length; i++) {
            httpHosts[i] = new HttpHost(addresses[i].getHostName(), addresses[i].getPort(), "https");
            System.out.println("hosts[i]=" +addresses[i].getHostName() + " port=" + addresses[i].getPort());
        }

        // Build the settings for our client.
        if (clusterId == null && region == null && username == null && password == null) {
            throw new Exception("Elastic service not configured properly, aborting");
        }

        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));

        RestClientBuilder restClientBuilder = RestClient.builder(httpHosts)
            .setRequestConfigCallback(
                    (RequestConfig.Builder requestConfigBuilder) -> 
                            requestConfigBuilder.setConnectTimeout(20000)
                                    .setSocketTimeout(300000))
            .setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
                @Override
                public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
                    return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                }
            });

        restClient = new RestHighLevelClient(restClientBuilder);

        restClient.cluster().health(new ClusterHealthRequest(), RequestOptions.DEFAULT);
        return this;
    }
    
    /**
     * Check to see if ElasticStore has been initialized.
     * @return 
     */
    public boolean isInitialized() {
        return restClient != null;
    }

    /**
     * Access the ElasticSearch REST client.
     * 
     * @return 
     */
    public RestHighLevelClient getRestClient() {
        return restClient;
    }
    
    /**
     * Close ElasticStore
     */
    public void close() {
        try {
            if (restClient != null) {
                restClient.close();
            }
        } catch (IOException ex) {
            Logger.getLogger(ElasticStore.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Create strongly typed access to an index and type.
     * 
     * @param clazz
     * @param index
     * @param type
     * @return
     * @throws Exception 
     */
    // TODO: Need to type the exception more strongly
    public Index getIndex(Class clazz, String index, String type) throws Exception {
        if (restClient == null) {
            throw new IllegalArgumentException("ElasticStore is not executed");
        }

        if (clazz == String.class) {
            return new JsonIndex<>(this, index, type);
        }
        
        // TODO: need to catch and throw an exception here
        return new TypedIndex(clazz, this, index, type, restClient);
    }

    /**
     * Create strongly typed access to an index and type.
     * @Index and @IndexType must both be defined on the object to define the index and type.
     * 
     * @param clazz
     * @return
     * @throws Exception 
     */
    // TODO: Need to type the exception more strongly
    public Index getIndex(Class clazz) throws Exception {
        if (restClient == null) {
            throw new IllegalArgumentException("ElasticStore is not executed");
        }

        String index = AnnotationHelper.getIndexValue(clazz);
        String type = AnnotationHelper.getIndexTypeValue(clazz);
        if (index == null || type == null) {
            throw new IllegalArgumentException("Both @Index and @IndexType must be defined for object based mapping.");
        }

        // TODO: need to catch and throw an exception here
        return new TypedIndex(clazz, this, index, type, restClient);
    }

//    /**
//     * Remove an index from ElasticStore as specified by an Index.
//     * 
//     * @param index
//     * @throws Exception 
//     */
//    public void removeIndex(Index index) throws Exception {
//        client.admin()
//                .indices()
//                .delete(new DeleteIndexRequest(index.getIndex()))
//                .actionGet();
//    }
//
//    /**
//     * Remove an index from ElasticStore as specified by an index name..
//     * 
//     * @param index
//     * @throws Exception 
//     */
//    public void removeIndex(String index) throws Exception {
//        client.admin()
//                .indices()
//                .delete(new DeleteIndexRequest(index))
//                .actionGet();
//    }
//
//    /**
//     * Refresh the index,
//     * 
//     * @param index
//     * @throws Exception 
//     */
//    public void refreshIndex(Index index) throws Exception {
//        client.admin()
//                .indices()
//                .refresh(new RefreshRequest(index.getIndex()))
//                .actionGet();
//    }

    /**
     * Apply type mapping to one or more indexes.
     * If ignoreConflicts is false, then the mapping will be rejected if there are conflicts.
     * If ignoreConflits is true, then conflicts will be ignored.
     * 
     * @param mapping
     * @param indexes
     * @param type
     */
    protected void applyMapping(String mapping, String type, String... indexes) {
        // Check to see if indexes exist and create if missing.
        for (String index: indexes) {
//            if (! client.admin().indices().exists(new IndicesExistsRequest(index)).actionGet().isExists()) {
//                client.admin().indices().create(new CreateIndexRequest(index)).actionGet();
//            }
        }
        
//        // Apply the new mapping to the index
//        client.admin()
//                .indices()
//                .putMapping(new PutMappingRequest(indexes)
//                    .source(mapping)
//                    .type(type)
//                ).actionGet();
    }
}
