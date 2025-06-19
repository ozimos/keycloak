package org.keycloak.models.rama;

import com.rpl.rama.*;
import com.rpl.rama.ops.*;
import com.rpl.rama.test.LaunchConfig;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.keycloak.models.*;
import org.mockito.junit.MockitoJUnitRunner;
import org.keycloak.models.rama.data.UserRegistration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Arrays;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import org.mockito.MockitoAnnotations;

public class RamaUserProviderTest extends BaseRamaTest {

    private static RamaUserProvider provider;
    private static Depot userDepot;

    @BeforeClass
    public static void setup() throws Exception {
        // Initialize base test components
        BaseRamaTest.initializeBase();
        
        // Ensure realm mock is properly configured
        when(realm.getId()).thenReturn("test-realm-id");
        
        // Create specific clients for different operations
        userDepot = ipc.clusterDepot(moduleName, "*userDepot");
        provider = new RamaUserProvider(session, realm);
        System.out.println("Test setup completed - initialized RamaUserProvider and connections");
    }

    public UserRegistration register(Depot userDepot) throws Exception {
        // Generate unique test data
        String id = "test-user-" + UUID.randomUUID();
        String username = "testuser-" + UUID.randomUUID();
        String email = "test-" + UUID.randomUUID() + "@example.com";
        String realmId = "test-realm-id";
        
        System.out.println("Registering test user - ID: " + id + ", Username: " + username + ", Email: " + email);
        
        // Prepare test data
        UserRegistration userData = new UserRegistration(
            id,
            username,
            email,
            realmId,
            true,
            false
        );

        // Add user to depot
        Map<String, Object> ackReturns = userDepot.append(Arrays.asList(id, realmId, userData));
        String userId = (String) ackReturns.get("profiles");
        if(userId != null) {
            System.out.println("Successfully registered user with ID: " + userId);
            // Wait for depot processing to complete
            Thread.sleep(500);
            return userData;
        } else {
            System.out.println("Failed to register user - username already exists");
            throw new RuntimeException("Username already registered");
        }
    }

    @Test
    public void testGetUserById_UserExists() throws Exception {
        System.out.println("Starting testGetUserById_UserExists");
        
        // Register a test user
        UserRegistration userData = register(userDepot);
        System.out.println("Looking up user by ID: " + userData.id);

        // Execute test
        UserModel user = provider.getUserById(realm, userData.id);
        System.out.println("Retrieved user: " + (user != null ? user.getUsername() : "null"));

        // Verify results
        assertNotNull("User should not be null", user);
        assertEquals("User ID should match", userData.id, user.getId());
        assertEquals("Username should match", userData.username, user.getUsername());
        assertEquals("Email should match", userData.email, user.getEmail());
        assertEquals("Enabled status should match", userData.enabled, user.isEnabled());
        assertEquals("Email verified status should match", userData.emailVerified, user.isEmailVerified());
        System.out.println("testGetUserById_UserExists completed successfully");
    }

    @Test
    public void testGetUserById_UserDoesNotExist() {
        System.out.println("Starting testGetUserById_UserDoesNotExist");
        // Use a unique non-existent ID
        String nonExistentId = "non-existent-" + UUID.randomUUID();
        System.out.println("Looking up non-existent user with ID: " + nonExistentId);
        
        // Execute test
        UserModel user = provider.getUserById(realm, nonExistentId);
        System.out.println("Retrieved user: " + (user != null ? user.getUsername() : "null"));

        // Verify results
        assertNull("User should be null", user);
        System.out.println("testGetUserById_UserDoesNotExist completed successfully");
    }

    @Test
    public void testGetUserByEmail() throws Exception {
        System.out.println("Starting testGetUserByEmail");
        // Register a test user
        UserRegistration userData = register(userDepot);
        System.out.println("Looking up user by email: " + userData.email);

        // Execute test
        UserModel user = provider.getUserByEmail(realm, userData.email);
        System.out.println("Retrieved user: " + (user != null ? user.getUsername() : "null"));

        // Verify results
        assertNotNull("User should not be null", user);
        assertEquals("Email should match", userData.email, user.getEmail());
        assertEquals("User ID should match", userData.id, user.getId());
        assertEquals("Username should match", userData.username, user.getUsername());
        System.out.println("testGetUserByEmail completed successfully");
    }

    @Test
    public void testGetUserByUsername() throws Exception {
        System.out.println("Starting testGetUserByUsername");
        // Register a test user
        UserRegistration userData = register(userDepot);
        System.out.println("Looking up user by username: " + userData.username);

        // Execute test
        UserModel user = provider.getUserByUsername(realm, userData.username);
        System.out.println("Retrieved user: " + (user != null ? user.getUsername() : "null"));

        // Verify results
        assertNotNull("User should not be null", user);
        assertEquals("Username should match", userData.username, user.getUsername());
        assertEquals("User ID should match", userData.id, user.getId());
        assertEquals("Email should match", userData.email, user.getEmail());
        System.out.println("testGetUserByUsername completed successfully");
    }

    
    @Test
    public void testSearchForUserStream() throws Exception {
        System.out.println("Starting testSearchForUserStream");
        // Register a test user
        UserRegistration userData = register(userDepot);
        String searchPrefix = userData.username.substring(0, 8);
        System.out.println("Searching for users with prefix: " + searchPrefix);

        // Execute test - search by username prefix
        var users = provider.searchForUserStream(realm, searchPrefix, 0, 10).toList();
        System.out.println("Found " + users.size() + " users matching search criteria");

        // Verify results
        assertFalse("User list should not be empty", users.isEmpty());
        assertEquals("Should find one user", 1, users.size());
        assertEquals("Username should match", userData.username, users.get(0).getUsername());
        assertEquals("User ID should match", userData.id, users.get(0).getId());
        assertEquals("Email should match", userData.email, users.get(0).getEmail());
        System.out.println("testSearchForUserStream completed successfully");
    }
/*
    @Test
    public void testGetUsersCount() throws Exception {
        System.out.println("Starting testGetUsersCount");
        // Register a test user
        UserRegistration userData = register(userDepot);
        System.out.println("Counting total users in realm");

        // Execute test
        int count = provider.getUsersCount(realm);
        System.out.println("Total user count: " + count);

        // Verify results
        assertEquals("User count should be 1", 1, count);
        System.out.println("testGetUsersCount completed successfully");
    }

    @Test
    public void testAddUser() throws Exception {
        System.out.println("Starting testAddUser");
        // Generate unique username
        String username = "newuser-" + UUID.randomUUID();
        System.out.println("Adding new user with username: " + username);
        
        // Create user registration data
        UserRegistration userData = new UserRegistration(
            "test-user-" + UUID.randomUUID(),
            username,
            "test-" + UUID.randomUUID() + "@example.com",
            "test-realm-id",
            true,
            false
        );
        
        // Add user to depot first
        userDepot.append(userData);
        Thread.sleep(1000); // Wait for depot processing
        
        // Execute test
        UserModel user = provider.addUser(realm, username);
        System.out.println("Created user: " + (user != null ? user.getUsername() : "null"));

        // Verify results
        assertNotNull("User should not be null", user);
        assertEquals("Username should match", username, user.getUsername());
        System.out.println("testAddUser completed successfully");
    }

    @Test
    public void testRemoveUser() throws Exception {
        System.out.println("Starting testRemoveUser");
        // Register a test user
        UserRegistration userData = register(userDepot);
        System.out.println("Removing user with ID: " + userData.id);

        // Get the user
        UserModel user = provider.getUserById(realm, userData.id);
        assertNotNull("User should exist before removal", user);

        // Execute test
        provider.removeUser(realm, user);
        System.out.println("User removed, verifying deletion");

        // Verify results
        UserModel removedUser = provider.getUserById(realm, userData.id);
        assertNull("User should be removed", removedUser);
        System.out.println("testRemoveUser completed successfully");
    }
    */
} 