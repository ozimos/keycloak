package org.keycloak.models.rama;

import org.keycloak.Config.Scope;
import org.keycloak.component.ComponentModel;
import org.keycloak.component.ComponentValidationException;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;
import org.keycloak.storage.UserStorageProviderFactory;
import org.keycloak.storage.UserStorageProvider;

import java.util.List;

public class RamaUserProviderFactory implements UserStorageProviderFactory<UserStorageProvider> {
    public static final String PROVIDER_ID = "rama";
    public static final String MODULE_NAME = "module-name";

    private static final List<ProviderConfigProperty> configProperties;

    static {
        configProperties = ProviderConfigurationBuilder.create()
            .property()
                .name(MODULE_NAME)
                .label("Module Name")
                .type(ProviderConfigProperty.STRING_TYPE)
                .defaultValue("keycloak")
                .helpText("The name of the Rama module to use")
                .add()
            .build();
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String getHelpText() {
        return "Rama User Storage Provider";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return configProperties;
    }

    public void validateConfiguration(KeycloakSession session, Scope config) throws ComponentValidationException {
        String moduleName = config.get(MODULE_NAME);
        if (moduleName == null || moduleName.trim().isEmpty()) {
            throw new ComponentValidationException("Module name is required");
        }
    }

    @Override
    public void init(Scope config) {
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

    @Override
    public UserStorageProvider create(KeycloakSession session, ComponentModel model) {
        RealmModel realm = session.getContext().getRealm();
        return new RamaUserProvider(session, realm);
    }
} 