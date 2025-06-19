package org.keycloak.connections.rama;

import com.rpl.rama.test.InProcessCluster;
import org.keycloak.models.KeycloakSession;
import java.io.IOException;

public class TestRamaConnectionProvider implements RamaConnectionProvider {
    private final InProcessCluster ipc;

    public TestRamaConnectionProvider(KeycloakSession session) {
        this.ipc = InProcessCluster.create();
    }

    @Override
    public InProcessCluster getCluster() {
        return ipc;
    }

    @Override
    public void close() {
        /*
        Thread.sleep(300);
        if (ipc != null) {
            try {
                ipc.close();
            } catch (IOException e) {
                // Log the error but don't throw it since this is a cleanup method
                System.err.println("Error closing Rama cluster: " + e.getMessage());
            }
        }
        */
    }
} 