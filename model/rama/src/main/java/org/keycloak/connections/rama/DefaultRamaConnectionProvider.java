package org.keycloak.connections.rama;

import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import com.rpl.rama.RamaClusterManager;
import java.io.IOException;

public class DefaultRamaConnectionProvider implements RamaConnectionProvider {
    private final KeycloakSession session;
    private final String moduleName;
    private String clusterName;
    private String hosts;
    private int port;
    private RamaClusterManager cluster;

    public DefaultRamaConnectionProvider(KeycloakSession session, String moduleName) {
        this.session = session;
        this.moduleName = moduleName;
    }

    public void init(String moduleName, String clusterName, String hosts, int port) {
        this.clusterName = clusterName;
        this.hosts = hosts;
        this.port = port;
    }

    @Override
    public void close() {
        if (cluster != null) {
            try {
                cluster.close();
            } catch (IOException e) {
                // Log the error but don't throw it since this is a cleanup method
                System.err.println("Error closing Rama cluster: " + e.getMessage());
            }
        }
    }

    public String getModuleName() {
        return moduleName;
    }

    @Override
    public RamaClusterManager getCluster() {
        if (cluster == null) {
            Map<String, Object> config = new HashMap<>();
            config.put("conductor.host", hosts);
            config.put("conductor.port", port);
            config.put("cluster.name", clusterName);
            
            cluster = RamaClusterManager.open(config);
        }
        return cluster;
    }
} 