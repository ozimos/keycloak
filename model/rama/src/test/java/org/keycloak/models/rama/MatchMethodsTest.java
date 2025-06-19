package org.keycloak.models.rama;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.keycloak.module.rama.KeycloakRamaModule.*;

import java.util.HashMap;
import java.util.Map;

public class MatchMethodsTest {

    @Test
    public void testIsExactMatch() {
        Map<String, Object> userObject = new HashMap<>();
        Map<String, Object> params = new HashMap<>();
        
        // Test null paramValue returns true
        assertTrue("Null param value should match any user value", 
            isExactMatch("test", userObject, params));
        params.put("test", null);
        assertTrue("Null param value should match any user value", 
            isExactMatch("test", userObject, params));
        
        // Test null userValue matches false boolean paramValue
        params.clear();
        params.put("enabled", false);
        assertTrue("Null user value should match false boolean param value", 
            isExactMatch("enabled", userObject, params));
        
        // Test exact string matches
        userObject.put("username", "testUser");
        params.put("username", "testUser");
        assertTrue("Exact string match should return true", 
            isExactMatch("username", userObject, params));
        params.put("username", "TestUser");
        assertFalse("Case-sensitive string match should return false", 
            isExactMatch("username", userObject, params));
        
        // Test boolean matches
        userObject.put("enabled", true);
        params.put("enabled", true);
        assertTrue("Exact boolean match should return true", 
            isExactMatch("enabled", userObject, params));
        params.put("enabled", false);
        assertFalse("Different boolean values should not match", 
            isExactMatch("enabled", userObject, params));
    }

    @Test
    public void testIsMatch() {
        Map<String, Object> userObject = new HashMap<>();
        Map<String, Object> params = new HashMap<>();
        
        // Test null paramValue returns true
        assertTrue("Null param value should match any user value", 
            isMatch("test", userObject, params));
        
        // Test null userValue matches false boolean paramValue
        params.put("enabled", false);
        assertTrue("Null user value should match false boolean param value", 
            isMatch("enabled", userObject, params));
        
        // Test case-insensitive string matches
        userObject.put("username", "testUser");
        params.put("username", "test");
        assertTrue("Partial string match should return true", 
            isMatch("username", userObject, params));
        params.put("username", "TEST");
        assertTrue("Case-insensitive match should return true", 
            isMatch("username", userObject, params));
        params.put("username", "xyz");
        assertFalse("Non-matching string should return false", 
            isMatch("username", userObject, params));
        
        // Test exact mode
        params.put("exact", "true");
        params.put("username", "testUser");
        assertTrue("Exact match with exact=true should return true", 
            isMatch("username", userObject, params));
        params.put("username", "test");
        assertFalse("Partial match with exact=true should return false", 
            isMatch("username", userObject, params));
        
        // Test boolean matches in non-exact mode
        userObject.put("enabled", true);
        params.put("enabled", true);
        assertTrue("Boolean match should work in non-exact mode", 
            isMatch("enabled", userObject, params));
        params.put("enabled", false);
        assertFalse("Different boolean values should not match in non-exact mode", 
            isMatch("enabled", userObject, params));
    }
} 