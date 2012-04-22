package com.nosqlrevolution;

import com.google.common.base.Joiner;
import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

/**
 *
 * @author cbrown
 */
public class ElasticStore {
    private static int DEFAULT_PORT = 9300;
    private static String DEFAULT_HOST = "127.0.0.1";
    private static String DEFAULT_TIMEOUT = "10s";
    private static String DEFAULT_CLUSTER_NAME = "elasticsearch";
    private static boolean multicast = true;
    private static boolean node = true;
    private static boolean local = false;
    
    private Client client = null;
    private String[] hosts = null;
    private InetSocketAddress[] addresses = null;
    private String clusterName = DEFAULT_CLUSTER_NAME;
    private int port = DEFAULT_PORT;
    private String timeout = DEFAULT_TIMEOUT;

    public ElasticStore asNode() {
        node = true;
        return this;
    }
    
    public ElasticStore asTransport() {
        node = false;
        return this;
    }
        
    public ElasticStore asLocal() {
        local = true;
        return this;
    }
        
    public ElasticStore withMultiCast() {
        multicast = true;
        return this;
    }
    
    public ElasticStore withUnicast() {
        multicast = false;
        return this;
    }
    
    public ElasticStore withUniCast(String... hosts) {
        multicast = false;
        this.hosts = hosts;
        return this;
    }
    
    public ElasticStore withUnicast(InetSocketAddress... addresses) {
        multicast = false;
        this.addresses = addresses;
        return this;
    }
    
    public ElasticStore withPort(int port) {
        this.port = port;
        return this;
    }

    public ElasticStore withClusterName(String clusterName) {
        this.clusterName = clusterName;
        return this;
    }
    
    public ElasticStore withTimeout(String timeout) {
        this.timeout = timeout;
        return this;
    }
        
    public ElasticStore execute() {
        if (local) {
            client = nodeBuilder()
                .local(true)
                .data(true)
                .node()
                .client();
        } else if (node) {
            ImmutableSettings.Builder builder = ImmutableSettings.settingsBuilder()
                .put("cluster.name", clusterName)
                .put("discovery.zen.ping.timeout", timeout)
                .put("discovery.zen.ping.multicast.enabled", multicast);
            
            if (! multicast) {
                if (hosts != null) {
                    builder.put("discovery.zen.ping.unicast.hosts", Joiner.on(",").join(hosts));
                } else {
                    builder.put("discovery.zen.ping.unicast.hosts", DEFAULT_HOST);
                }
            }
            
            client = nodeBuilder()
                    .settings(builder.build())
                    .client(true)
                    .data(false)
                    .node()
                    .client();
        } else {
            TransportClient transClient = new TransportClient();
            if (! multicast) {
                if (addresses != null) {
                    for (InetSocketAddress address: addresses) {
                        transClient.addTransportAddress(new InetSocketTransportAddress(address));
                    }
                }
                if (hosts != null) {
                    for (String host: hosts) {
                        transClient.addTransportAddress(new InetSocketTransportAddress(host, port));
                    }
                } else {
                    transClient.addTransportAddress(new InetSocketTransportAddress(DEFAULT_HOST, port));
                }
            }
            client = new TransportClient()
                    .addTransportAddress(new InetSocketTransportAddress("10.1.10.54", 9300));
            client = transClient;
        }
        
        // Wait for client connection.
        client.admin().cluster().prepareHealth().setWaitForGreenStatus().execute().actionGet();
        return this;
    }
    
    public boolean isInitialized() {
        return client != null;
    }
    
    public Client getClient() {
        return client;
    }
    
    public void close() {
        client.close();
    }
    
    public JsonIndex getIndex(String... indexes) throws Exception {
        if (client == null) {
            throw new Exception("ElasticStore is not executed");
        }
        
        return new JsonIndex(this, indexes);
    }
    
    public Index getIndex(Type t, String... indexes) throws Exception {
        if (client == null) {
            throw new Exception("ElasticStore is not executed");
        }
        
        return new TypedIndex(t, this, indexes);
    }
}
