package org.keycloak.connections.rama;

import org.keycloak.provider.Spi;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.provider.Provider;

public class RamaConnectionSpi implements Spi {
    public static final String PROVIDER_ID = "rama-connection";
    public static final String MODULE_NAME_CONFIG = "moduleName";
    public static final String CLUSTER_CONFIG = "cluster";
    public static final String HOSTS_CONFIG = "hosts";
    public static final String PORT_CONFIG = "port";

    @Override
    public boolean isInternal() {
        return false;
    }

    @Override
    public String getName() {
        return "Rama Connection";
    }

    @Override
    public Class<? extends ProviderFactory> getProviderFactoryClass() {
        return RamaConnectionProviderFactory.class;
    }

    @Override
    public Class<? extends Provider> getProviderClass() {
        return RamaConnectionProvider.class;
    }
}