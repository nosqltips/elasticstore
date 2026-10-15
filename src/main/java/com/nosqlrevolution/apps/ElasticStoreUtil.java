package com.nosqlrevolution.apps;

import com.nosqlrevolution.ElasticStore;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.file.Paths;

/** CLI connection compatibility helpers. Bare hosts use the REST port, 9200. */
public class ElasticStoreUtil {
    public static ElasticStore createElasticStore(String hostname, String clustername, String index,
            boolean asNode, boolean asElastic) {
        if (asNode) throw new UnsupportedOperationException("Embedded/node clients are unsupported; supply a REST endpoint");
        ElasticStore store = new ElasticStore();
        if (asElastic) store.asElastic();
        store.withUnicast(hosts(hostname));
        if (clustername != null) store.withClusterName(clustername);
        return store;
    }

    public static void configureAuthentication(ElasticStore store, String username, String password,
            String apiKey, String caCertificate, String clusterId, String region) {
        if (clusterId != null || region != null) {
            throw new IllegalArgumentException("Use the deployment HTTPS endpoint instead of legacy clusterId/region options");
        }
        if (username != null) store.withUsername(username);
        if (password != null) store.withPassword(password);
        if (apiKey != null) store.withApiKey(apiKey);
        if (caCertificate != null) store.withCaCertificate(Paths.get(caCertificate));
    }

    private static String[] hosts(String input) {
        if (input == null) throw new IllegalArgumentException("Hosts are required");
        String[] hosts = input.split(",", -1);
        for (int i = 0; i < hosts.length; i++) {
            hosts[i] = hosts[i].trim();
            if (hosts[i].isEmpty()) throw new IllegalArgumentException("Hosts must not be empty");
        }
        return hosts;
    }

    private static URI host(String value) {
        String url = value.contains("://") ? value : "http://" + value;
        new ElasticStore().withEndpoints(url); // Validate without opening a connection.
        return URI.create(url);
    }

    public static InetSocketAddress[] getAddresses(String input) {
        String[] hosts = hosts(input);
        InetSocketAddress[] addresses = new InetSocketAddress[hosts.length];
        for (int i = 0; i < hosts.length; i++) {
            URI uri = host(hosts[i]);
            int port = uri.getPort() < 0 ? (hosts[i].contains("://") ? ("https".equals(uri.getScheme()) ? 443 : 80) : 9200) : uri.getPort();
            addresses[i] = InetSocketAddress.createUnresolved(uri.getHost(), port);
        }
        return addresses;
    }

    public static String[] getHosts(String input) {
        String[] hosts = hosts(input);
        for (int i = 0; i < hosts.length; i++) hosts[i] = host(hosts[i]).getHost();
        return hosts;
    }
}
