package org.keycloak.storage.configuration.rama;

import com.rpl.rama.*;
import com.rpl.rama.ops.*;
import org.keycloak.Config;
import org.keycloak.models.*;
import org.keycloak.component.ComponentModel;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;
import org.keycloak.storage.configuration.rama.RamaConfigStorageProvider;
import org.keycloak.storage.configuration.ServerConfigStorageProviderFactory;
import java.util.List;

public class RamaConfigStorageProviderFactory implements ServerConfigStorageProviderFactory {
    private static final String PROVIDER_ID = "rama";
    private static final String MODULE_NAME = "keycloak-rama";
    private static final String CLUSTER_NAME = "keycloak-rama-cluster";
    private static final String HOSTS = "localhost";
    private static final int PORT = 8888;

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    public String getHelpText() {
        return "Rama Server Config Storage Provider";
    }

    public List<ProviderConfigProperty> getConfigProperties() {
        return ProviderConfigurationBuilder.create()
            .property()
                .name("moduleName")
                .label("Module Name")
                .helpText("Name of the Rama module to use")
                .type(ProviderConfigProperty.STRING_TYPE)
                .defaultValue("keycloak-config")
                .add()
            .property()
                .name("cluster")
                .label("Cluster Name")
                .helpText("Name of the Rama cluster")
                .type(ProviderConfigProperty.STRING_TYPE)
                .defaultValue("keycloak")
                .add()
            .property()
                .name("hosts")
                .label("Hosts")
                .helpText("Comma-separated list of Rama hosts")
                .type(ProviderConfigProperty.STRING_TYPE)
                .defaultValue("localhost")
                .add()
            .property()
                .name("port")
                .label("Port")
                .helpText("Port number for Rama hosts")
                .type(ProviderConfigProperty.STRING_TYPE)
                .defaultValue("8888")
                .add()
            .build();
    }

    @Override
    public void init(Config.Scope config) {
        // No initialization needed
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        // No post-initialization needed
    }

    @Override
    public void close() {
        // No cleanup needed for now
    }

    @Override
    public RamaConfigStorageProvider create(KeycloakSession session) {
        try {
            return new RamaConfigStorageProvider(MODULE_NAME, session, session.getContext().getRealm());
        } catch (Exception e) {
            throw new RuntimeException("Failed to create RamaConfigStorageProvider", e);
        }
    }

    public RamaConfigStorageProvider create(KeycloakSession session, ComponentModel model) {
        try {
            String moduleName = model.getConfig().getFirst("moduleName");
            if (moduleName == null) {
                moduleName = MODULE_NAME;
            }

            return new RamaConfigStorageProvider(moduleName, session, session.getContext().getRealm());
        } catch (Exception e) {
            throw new RuntimeException("Failed to create RamaConfigStorageProvider", e);
        }
    }
} 