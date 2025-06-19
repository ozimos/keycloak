package org.keycloak.models.rama;

import org.keycloak.models.*;
import java.util.*;
import java.util.stream.*;
import org.keycloak.credential.UserCredentialManager;
import org.keycloak.models.SubjectCredentialManager;

public class UserAdapter implements UserModel {
    private final KeycloakSession session;
    private final RealmModel realm;
    private final Map<String, Object> userData;
    private final String id;
    private final String username;
    private final String email;
    private final String firstName;
    private final String lastName;
    private final boolean enabled;
    private final boolean emailVerified;
    private final Long createdTimestamp;

    public UserAdapter(KeycloakSession session, RealmModel realm, Map<String, Object> userData) {
        this.session = session;
        this.realm = realm;
        this.userData = userData;
        this.id = (String) userData.get("id");
        this.username = (String) userData.get("username");
        this.email = (String) userData.get("email");
        this.firstName = (String) userData.get("firstName");
        this.lastName = (String) userData.get("lastName");
        this.enabled = (Boolean) userData.getOrDefault("enabled", true);
        this.emailVerified = (Boolean) userData.getOrDefault("emailVerified", false);
        this.createdTimestamp = (Long) userData.get("createdTimestamp");
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public void setUsername(String username) {
        userData.put("username", username);
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        userData.put("enabled", enabled);
    }

    @Override
    public void setSingleAttribute(String name, String value) {
        @SuppressWarnings("unchecked")
        Map<String, List<String>> attributes = (Map<String, List<String>>) userData.getOrDefault("attributes", new HashMap<>());
        List<String> values = new ArrayList<>();
        values.add(value);
        attributes.put(name, values);
        userData.put("attributes", attributes);
    }

    @Override
    public void setAttribute(String name, List<String> values) {
        @SuppressWarnings("unchecked")
        Map<String, List<String>> attributes = (Map<String, List<String>>) userData.get("attributes");
        if (attributes == null) {
            attributes = new HashMap<>();
            userData.put("attributes", attributes);
        }
        attributes.put(name, values);
    }

    @Override
    public void removeAttribute(String name) {
        @SuppressWarnings("unchecked")
        Map<String, List<String>> attributes = (Map<String, List<String>>) userData.get("attributes");
        if (attributes != null) {
            attributes.remove(name);
        }
    }

    @Override
    public String getFirstAttribute(String name) {
        @SuppressWarnings("unchecked")
        Map<String, List<String>> attributes = (Map<String, List<String>>) userData.get("attributes");
        if (attributes != null && attributes.containsKey(name)) {
            List<String> values = attributes.get(name);
            return values != null && !values.isEmpty() ? values.get(0) : null;
        }
        return null;
    }

    public List<String> getAttribute(String name) {
        @SuppressWarnings("unchecked")
        Map<String, List<String>> attributes = (Map<String, List<String>>) userData.get("attributes");
        if (attributes != null && attributes.containsKey(name)) {
            List<String> values = attributes.get(name);
            return values != null ? values : Collections.emptyList();
        }
        return Collections.emptyList();
    }

    @Override
    public Map<String, List<String>> getAttributes() {
        @SuppressWarnings("unchecked")
        Map<String, List<String>> attributes = (Map<String, List<String>>) userData.get("attributes");
        return attributes != null ? attributes : new HashMap<>();
    }

    @Override
    public Stream<String> getRequiredActionsStream() {
        @SuppressWarnings("unchecked")
        List<String> requiredActions = (List<String>) userData.getOrDefault("requiredActions", new ArrayList<>());
        return requiredActions.stream();
    }

    @Override
    public void addRequiredAction(String action) {
        @SuppressWarnings("unchecked")
        List<String> requiredActions = (List<String>) userData.getOrDefault("requiredActions", new ArrayList<>());
        if (!requiredActions.contains(action)) {
            requiredActions.add(action);
            userData.put("requiredActions", requiredActions);
        }
    }

    @Override
    public void removeRequiredAction(String action) {
        @SuppressWarnings("unchecked")
        List<String> requiredActions = (List<String>) userData.getOrDefault("requiredActions", new ArrayList<>());
        requiredActions.remove(action);
        userData.put("requiredActions", requiredActions);
    }

    @Override
    public void addRequiredAction(RequiredAction action) {
        addRequiredAction(action.name());
    }

    @Override
    public void removeRequiredAction(RequiredAction action) {
        removeRequiredAction(action.name());
    }

    @Override
    public String getFirstName() {
        return firstName;
    }

    @Override
    public void setFirstName(String firstName) {
        userData.put("firstName", firstName);
    }

    @Override
    public String getLastName() {
        return lastName;
    }

    @Override
    public void setLastName(String lastName) {
        userData.put("lastName", lastName);
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public void setEmail(String email) {
        userData.put("email", email);
    }

    @Override
    public boolean isEmailVerified() {
        return emailVerified;
    }

    @Override
    public void setEmailVerified(boolean verified) {
        userData.put("emailVerified", verified);
    }

    @Override
    public Stream<GroupModel> getGroupsStream() {
        @SuppressWarnings("unchecked")
        List<String> groupIds = (List<String>) userData.getOrDefault("groups", new ArrayList<>());
        return groupIds.stream()
            .map(groupId -> session.groups().getGroupById(realm, groupId))
            .filter(Objects::nonNull);
    }

    @Override
    public void joinGroup(GroupModel group) {
        @SuppressWarnings("unchecked")
        List<String> groupIds = (List<String>) userData.getOrDefault("groups", new ArrayList<>());
        if (!groupIds.contains(group.getId())) {
            groupIds.add(group.getId());
            userData.put("groups", groupIds);
        }
    }

    @Override
    public void leaveGroup(GroupModel group) {
        @SuppressWarnings("unchecked")
        List<String> groupIds = (List<String>) userData.getOrDefault("groups", new ArrayList<>());
        groupIds.remove(group.getId());
        userData.put("groups", groupIds);
    }

    @Override
    public boolean isMemberOf(GroupModel group) {
        @SuppressWarnings("unchecked")
        List<String> groupIds = (List<String>) userData.getOrDefault("groups", new ArrayList<>());
        return groupIds.contains(group.getId());
    }

    @Override
    public Stream<RoleModel> getRealmRoleMappingsStream() {
        @SuppressWarnings("unchecked")
        List<String> roleIds = (List<String>) userData.getOrDefault("realmRoles", new ArrayList<>());
        return roleIds.stream()
            .map(roleId -> session.roles().getRoleById(realm, roleId))
            .filter(Objects::nonNull);
    }

    @Override
    public Stream<RoleModel> getClientRoleMappingsStream(ClientModel client) {
        @SuppressWarnings("unchecked")
        Map<String, List<String>> clientRoles = (Map<String, List<String>>) userData.getOrDefault("clientRoles", new HashMap<>());
        List<String> roleIds = clientRoles.getOrDefault(client != null ? client.getId() : null, new ArrayList<>());
        return roleIds.stream()
            .map(roleId -> session.roles().getRoleById(realm, roleId))
            .filter(Objects::nonNull);
    }

    @Override
    public boolean hasRole(RoleModel role) {
        if (role.getContainer() instanceof RealmModel) {
            @SuppressWarnings("unchecked")
            List<String> roleIds = (List<String>) userData.getOrDefault("realmRoles", new ArrayList<>());
            return roleIds.contains(role.getId());
        } else {
            @SuppressWarnings("unchecked")
            Map<String, List<String>> clientRoles = (Map<String, List<String>>) userData.getOrDefault("clientRoles", new HashMap<>());
            List<String> roleIds = clientRoles.getOrDefault(((ClientModel) role.getContainer()).getId(), new ArrayList<>());
            return roleIds.contains(role.getId());
        }
    }

    @Override
    public void grantRole(RoleModel role) {
        if (role.getContainer() instanceof RealmModel) {
            @SuppressWarnings("unchecked")
            List<String> roleIds = (List<String>) userData.getOrDefault("realmRoles", new ArrayList<>());
            if (!roleIds.contains(role.getId())) {
                roleIds.add(role.getId());
                userData.put("realmRoles", roleIds);
            }
        } else {
            @SuppressWarnings("unchecked")
            Map<String, List<String>> clientRoles = (Map<String, List<String>>) userData.getOrDefault("clientRoles", new HashMap<>());
            String clientId = ((ClientModel) role.getContainer()).getId();
            List<String> roleIds = clientRoles.getOrDefault(clientId, new ArrayList<>());
            if (!roleIds.contains(role.getId())) {
                roleIds.add(role.getId());
                clientRoles.put(clientId, roleIds);
                userData.put("clientRoles", clientRoles);
            }
        }
    }

    @Override
    public Stream<RoleModel> getRoleMappingsStream() {
        return Stream.concat(getRealmRoleMappingsStream(), getClientRoleMappingsStream(null));
    }

    @Override
    public void deleteRoleMapping(RoleModel role) {
        if (role.getContainer() instanceof RealmModel) {
            @SuppressWarnings("unchecked")
            List<String> roleIds = (List<String>) userData.getOrDefault("realmRoles", new ArrayList<>());
            roleIds.remove(role.getId());
            userData.put("realmRoles", roleIds);
        } else {
            @SuppressWarnings("unchecked")
            Map<String, List<String>> clientRoles = (Map<String, List<String>>) userData.getOrDefault("clientRoles", new HashMap<>());
            String clientId = ((ClientModel) role.getContainer()).getId();
            List<String> roleIds = clientRoles.getOrDefault(clientId, new ArrayList<>());
            roleIds.remove(role.getId());
            clientRoles.put(clientId, roleIds);
            userData.put("clientRoles", clientRoles);
        }
    }

    @Override
    public Long getCreatedTimestamp() {
        return createdTimestamp;
    }

    @Override
    public void setCreatedTimestamp(Long timestamp) {
        userData.put("createdTimestamp", timestamp);
    }

    public void setServiceAccountClientLink(ClientModel client) {
        userData.put("serviceAccountClientId", client.getId());
    }

    @Override
    public void setServiceAccountClientLink(String clientId) {
        userData.put("serviceAccountClientId", clientId);
    }

    @Override
    public String getServiceAccountClientLink() {
        return (String) userData.get("serviceAccountClientId");
    }

    @Override
    public void setFederationLink(String link) {
        userData.put("federationLink", link);
    }

    @Override
    public String getFederationLink() {
        return (String) userData.get("federationLink");
    }

    @Override
    public SubjectCredentialManager credentialManager() {
        return new UserCredentialManager(session, realm, this);
    }

    @Override
    public Stream<String> getAttributeStream(String name) {
        @SuppressWarnings("unchecked")
        Map<String, List<String>> attributes = (Map<String, List<String>>) userData.get("attributes");
        if (attributes == null || !attributes.containsKey(name)) {
            return Stream.empty();
        }
        return attributes.get(name).stream();
    }
} 