package org.keycloak.models.rama;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.keycloak.module.rama.KeycloakRamaModule.*;

import java.util.HashMap;
import java.util.Map;

public class MatchesSearchParamsTest {
    private final MatchesSearchParams matcher = new MatchesSearchParams();

    private Map<String, Object> createTestUser() {
        Map<String, Object> user = new HashMap<>();
        user.put("username", "testUser");
        user.put("email", "test@example.com");
        user.put("firstName", "Test");
        user.put("lastName", "User");
        user.put("enabled", true);
        user.put("emailVerified", false);
        return user;
    }

    @Test
    public void testNullAndEmptyParams() {
        Map<String, Object> user = createTestUser();
        assertTrue("Null params should match", matcher.invoke(user, null));
        assertTrue("Empty params should match", matcher.invoke(user, new HashMap<>()));
    }

    @Test
    public void testSearchParameter() {
        Map<String, Object> user = createTestUser();
        Map<String, Object> params = new HashMap<>();

        // Test matching against different fields
        params.put("search", "test");
        assertTrue("Should match username", matcher.invoke(user, params));
        
        params.put("search", "example.com");
        assertTrue("Should match email", matcher.invoke(user, params));
        
        params.put("search", "Test");
        assertTrue("Should match firstName", matcher.invoke(user, params));
        
        params.put("search", "user");
        assertTrue("Should match lastName", matcher.invoke(user, params));

        // Test case insensitivity
        params.put("search", "TEST");
        assertTrue("Should match case-insensitive", matcher.invoke(user, params));

        // Test multi-word search where words match different fields
        params.put("search", "test user");
        assertTrue("Should match when words match different fields (username and lastName)", matcher.invoke(user, params));

        // Test multi-word search where all words match same field
        user.put("firstName", "Test Example");
        params.put("search", "test example");
        assertTrue("Should match when multiple words match same field", matcher.invoke(user, params));

        // Test multi-word search where only some words match
        params.put("search", "test nonexistent");
        assertTrue("Should match when at least one word matches", matcher.invoke(user, params));

        // Test multi-word search where no words match
        params.put("search", "nonexistent unknown");
        assertFalse("Should not match when no words match", matcher.invoke(user, params));

        // Test with null fields
        user = new HashMap<>();
        params.put("search", "nonexistent");
        assertFalse("Should not match when fields are null", matcher.invoke(user, params));
    }

    @Test
    public void testExactParameter() {
        Map<String, Object> user = createTestUser();
        Map<String, Object> params = new HashMap<>();
        
        // Test exact matching
        params.put("exact", true);
        params.put("username", "testUser");
        assertTrue("Should match exact username", matcher.invoke(user, params));
        
        params.put("username", "TestUser");
        assertFalse("Should not match different case in exact mode", matcher.invoke(user, params));
        
        // Test non-exact matching
        params.put("exact", false);
        params.put("username", "test");
        assertTrue("Should match partial username in non-exact mode", matcher.invoke(user, params));
    }

    @Test
    public void testBooleanParameters() {
        Map<String, Object> user = createTestUser();
        Map<String, Object> params = new HashMap<>();
        
        // Test enabled=true
        params.put("enabled", true);
        assertTrue("Should match enabled=true", matcher.invoke(user, params));
        
        // Test emailVerified=false
        params.put("emailVerified", false);
        assertTrue("Should match emailVerified=false", matcher.invoke(user, params));
        
        // Test non-matching boolean
        params.put("enabled", false);
        assertFalse("Should not match enabled=false", matcher.invoke(user, params));
    }

    @Test
    public void testMultipleParameters() {
        Map<String, Object> user = createTestUser();
        Map<String, Object> params = new HashMap<>();
        
        // Test multiple matching parameters
        params.put("username", "test");
        params.put("email", "example");
        params.put("enabled", true);
        assertTrue("Should match all parameters", matcher.invoke(user, params));
        
        // Test with one non-matching parameter
        params.put("emailVerified", true);
        assertFalse("Should not match when one parameter fails", matcher.invoke(user, params));
    }

    @Test
    public void testNullValues() {
        Map<String, Object> user = createTestUser();
        Map<String, Object> params = new HashMap<>();
        
        // Test null parameter value
        params.put("nonexistent", null);
        assertTrue("Null parameter value should match", matcher.invoke(user, params));
        
        // Test null user value with false boolean parameter
        user.put("emailVerified", null);
        params.put("emailVerified", false);
        assertTrue("Null user value should match false boolean parameter", matcher.invoke(user, params));
        
        params.put("emailVerified", true);
        assertFalse("Null user value should not match true boolean parameter", matcher.invoke(user, params));
    }

    @Test
    public void testAllParamsSet() {
        Map<String, Object> user = createTestUser();
        Map<String, Object> params = new HashMap<>();
        
        // Set all possible parameters with partial username
        params.put("search", "test");
        params.put("username", "test"); // partial match for "testUser"
        params.put("email", "test@example.com");
        params.put("firstName", "Test");
        params.put("lastName", "User");
        params.put("enabled", true);
        params.put("emailVerified", false);
        params.put("exact", false);
        
        // Should match because exact=false allows partial matches
        assertTrue("Should match when exact=false and username is partial", matcher.invoke(user, params));
        
        // Now set exact to true, should not match because username is partial
        params.put("exact", true);
        assertFalse("Should not match when exact=true and username is partial", matcher.invoke(user, params));
        
        // Fix username to exact match, should now match
        params.put("username", "testUser");
        assertTrue("Should match when exact=true and username matches exactly", matcher.invoke(user, params));
        
        // Change one parameter to not match
        params.put("email", "different@example.com");
        assertFalse("Should not match when one parameter doesn't match", matcher.invoke(user, params));
    }
} 