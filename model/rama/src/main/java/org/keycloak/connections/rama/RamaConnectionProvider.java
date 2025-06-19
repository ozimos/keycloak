package org.keycloak.connections.rama;

import com.rpl.rama.*;
import com.rpl.rama.cluster.ClusterManagerBase;
import org.keycloak.provider.Provider;

public interface RamaConnectionProvider extends Provider {
    /**
     * Gets the RamaCluster instance.
     *
     * @return The RamaCluster instance
     */
    ClusterManagerBase getCluster();
} 