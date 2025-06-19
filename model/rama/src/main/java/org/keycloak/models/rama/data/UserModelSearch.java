package org.keycloak.models.rama.data;

import com.rpl.rama.RamaSerializable;
import java.util.List;
import java.util.Map;

public class UserModelSearch implements RamaSerializable {

    // Search fields in order as specified in UserQueryMethodsProvider
    public String search;
    public String firstName;
    public String lastName;
    public String email;
    public String username;
    public Boolean exact;
    public Boolean emailVerified;
    public Boolean enabled;
    public String idpAlias;
    public String idpUserId;

    // Core user fields
    public String id;

    // Default constructor for Rama serialization
    public UserModelSearch() {
    }

    public UserModelSearch(String id, String search, String firstName, String lastName, String email, 
                          String username, Boolean exact, Boolean emailVerified, Boolean enabled, 
                          String idpAlias, String idpUserId) {
        this.id = id;
        this.search = search;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.username = username;
        this.exact = exact;
        this.emailVerified = emailVerified;
        this.enabled = enabled;
        this.idpAlias = idpAlias;
        this.idpUserId = idpUserId;
    }

    // Convenience constructor with minimal required fields
    public UserModelSearch(String id, String username, String email, Boolean enabled, Boolean emailVerified) {
        this(id, null, null, null, email, username, null, emailVerified, enabled, null, null);
    }

} 