package org.keycloak.models.rama.data;

import com.rpl.rama.RamaSerializable;

public class UserRegistration implements RamaSerializable {

    public String id;
    public String username;
    public String email;
    public String realmId;
    public boolean enabled;
    public boolean emailVerified;

    public UserRegistration(String id, String username, String email, String realmId, boolean enabled, boolean emailVerified) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.realmId = realmId;
        this.enabled = enabled;
        this.emailVerified = emailVerified;
    }
}
