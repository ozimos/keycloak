package org.keycloak.models.rama;

import com.rpl.rama.test.InProcessCluster;
import com.rpl.rama.test.LaunchConfig;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.connections.rama.RamaConnectionProvider;
import org.keycloak.module.rama.KeycloakRamaModule;
import org.keycloak.connections.rama.TestRamaConnectionProvider;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.*;

public abstract class BaseRamaTest {

    protected static InProcessCluster ipc;
    protected static KeycloakRamaModule module;
    protected static String moduleName;

    @Mock
    protected static KeycloakSession session;

    @Mock
    protected static RealmModel realm;

    @BeforeClass
    public static void initializeBase() throws Exception {
        // Initialize mocks
        session = mock(KeycloakSession.class);
        realm = mock(RealmModel.class);

        // Create test connection provider
        TestRamaConnectionProvider connectionProvider = new TestRamaConnectionProvider(session);
        lenient().when(session.getProvider(RamaConnectionProvider.class)).thenReturn(connectionProvider);
        ipc = connectionProvider.getCluster();

        // Launch module
        module = new KeycloakRamaModule();
        moduleName = module.getClass().getName();
        ipc.launchModule(module, new LaunchConfig(4, 2));
    }

    @AfterClass
    public static void cleanup() throws Exception {
        if (ipc != null) {
            // Get the connection provider and close it
            TestRamaConnectionProvider connectionProvider = (TestRamaConnectionProvider) session.getProvider(RamaConnectionProvider.class);
            if (connectionProvider != null) {
                connectionProvider.close();
            }
            ipc = null;
        }
    }

    protected static void waitForDepotProcessing() throws Exception {
        // Wait for depot processing to complete
        Thread.sleep(100);
    }
} 