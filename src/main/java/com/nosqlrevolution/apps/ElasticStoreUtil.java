package com.nosqlrevolution.apps;

import com.nosqlrevolution.ElasticStore;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author cbrown
 */
public class ElasticStoreUtil {
    private static final int DEFAULT_PORT = 9300;
    
    // Create new ElasticStore
    public static ElasticStore createElasticStore(String hostname, String clustername, String index, String type, boolean asNode) {
        if (! asNode) {
            return new ElasticStore().asTransport().withClusterName(clustername).withUnicast(getAddresses(hostname)).execute();
        } else {
            return new ElasticStore().asNode().withClusterName(clustername).withUnicast(getHosts(hostname)).execute();
        }
    }
    
    public static InetSocketAddress[] getAddresses(String hostnames) {
        String[] hosts = hostnames.split(",");
        List<InetSocketAddress> addresses = new ArrayList<>();
        for (String host: hosts) {
            if (host.contains(":")) {
                String parts[] = host.split(":");
                String hostPart = parts[0];
                int portPart = DEFAULT_PORT;
                try {
                    portPart = Integer.parseInt(parts[1]);
                } catch (Exception e) {}
                System.out.println("Adding new address " + host + " port " + portPart);
                addresses.add(new InetSocketAddress(hostPart, portPart));
            } else {
                System.out.println("Adding new address " + host + " port " + DEFAULT_PORT);
                addresses.add(new InetSocketAddress(host, DEFAULT_PORT));
            }
        }
        
        return addresses.toArray(new InetSocketAddress[addresses.size()]);
    }
    
    public static String[] getHosts(String hostnames) {
        String[] hosts = hostnames.split(",");
        List<String> allHosts = new ArrayList<>();
        for (String host: hosts) {
            if (host.contains(":")) {
                String parts[] = host.split(":");
                System.out.println("Adding new host " + hosts[0]);
                allHosts.add(hosts[0]);
            } else {
                System.out.println("Adding new host " + host);
                allHosts.add(host);
            }
        }
        
        return allHosts.toArray(new String[allHosts.size()]);
    }
}
