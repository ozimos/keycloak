package org.keycloak.connections.rama;

import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.provider.ServerInfoAwareProviderFactory;
import org.keycloak.models.KeycloakSessionFactory;
import java.util.List;
import java.util.Map;

public class RamaConnectionProviderFactory implements ProviderFactory<RamaConnectionProvider>, ServerInfoAwareProviderFactory {
    private static final String PROVIDER_ID = "rama";
    private static final String MODULE_NAME = "keycloak-rama";

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public RamaConnectionProvider create(KeycloakSession session) {
        return new DefaultRamaConnectionProvider(session, MODULE_NAME);
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
        // No cleanup needed
    }

    public List<ProviderConfigProperty> getConfigProperties() {
        return ProviderConfigurationBuilder.create()
            .property()
                .name("moduleName")
                .label("Module Name")
                .helpText("Name of the Rama module to use")
                .type(ProviderConfigProperty.STRING_TYPE)
                .defaultValue(MODULE_NAME)
                .add()
            .build();
    }

    @Override
    public Map<String, String> getOperationalInfo() {
        return Map.of(
            "provider", PROVIDER_ID,
            "module", MODULE_NAME
        );
    }
} 