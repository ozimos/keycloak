package org.keycloak.module.rama;

import com.rpl.rama.ops.Ops;
import com.rpl.rama.ops.RamaFunction1;
import com.rpl.rama.ops.RamaFunction2;
import com.rpl.rama.ops.RamaFunction3;
import com.rpl.rama.helpers.ModuleUniqueIdPState;
import com.rpl.rama.helpers.TopologyUtils.ExtractJavaField;
import com.rpl.rama.*;
import com.rpl.rama.module.*;
import java.util.Map;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.HashSet;
import java.io.Serializable;
import static com.rpl.rama.helpers.TopologyUtils.*;
import java.util.Optional;

public class KeycloakRamaModule implements RamaModule {

    public static class ExtractRealmId extends ExtractJavaField {
        public ExtractRealmId() { super("realmId"); }
    }

    public static class ConcatRealmToId implements RamaFunction1< List<Object>, String> {
        @Override
        public String invoke (List<Object> list) {
            String id = (String) list.get(0);
            String realmId = (String) list.get(1);
            return realmId.concat(id);
        }
    }

    public static class MatchesSearchParams implements RamaFunction2<Object, Map<String, Object>, Boolean> {
        @Override
        public Boolean invoke(Object rawUser, Map<String, Object> params) {
            if (params == null || params.isEmpty()) {
                return true;
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> user = (Map<String, Object>) rawUser;
            
            // Handle special 'search' parameter first
            Object searchObj = params.get("search");
            if (searchObj != null) {
                String[] searchTerms = searchObj.toString().toLowerCase().split("\\s+");
                String username = Optional.ofNullable(user.get("username"))
                    .map(Object::toString)
                    .map(String::toLowerCase)
                    .orElse("");
                String email = Optional.ofNullable(user.get("email"))
                    .map(Object::toString)
                    .map(String::toLowerCase)
                    .orElse("");
                String firstName = Optional.ofNullable(user.get("firstName"))
                    .map(Object::toString)
                    .map(String::toLowerCase)
                    .orElse("");
                String lastName = Optional.ofNullable(user.get("lastName"))
                    .map(Object::toString)
                    .map(String::toLowerCase)
                    .orElse("");
                
                // Check if any search term matches any field
                boolean anyTermMatches = false;
                for (String term : searchTerms) {
                    if (username.contains(term) || 
                        email.contains(term) || 
                        firstName.contains(term) || 
                        lastName.contains(term)) {
                        anyTermMatches = true;
                        break;
                    }
                }
                if (!anyTermMatches) return false;
            }

            // Handle all other parameters
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                String key = entry.getKey();
                // Skip the search and exact parameters as they are control parameters
                if ("exact".equals(key) || "search".equals(key)) continue;
                
                if (!isMatch(key, user, params)) {
                    return false;
                }
            }
            return true;
        }
    }

    public static class IsGroupMember implements RamaFunction2<Object, String, Boolean> {
        @Override
        public Boolean invoke(Object rawUser, String groupId) {
            Map<String, Object> user = (Map<String, Object>) rawUser;
            List<String> groups = (List<String>) user.get("groups");
            return groups != null && groups.contains(groupId);
        }
    }

    private static class HasAttribute implements RamaFunction3<Object, String, String, Boolean> {
        @Override
        public Boolean invoke(Object rawUser, String attrName, String attrValue) {
            @SuppressWarnings("unchecked")
            Map<String, Object> user = (Map<String, Object>) rawUser;
            @SuppressWarnings("unchecked")
            Map<String, List<String>> attributes = (Map<String, List<String>>) user.get("attributes");
            if (attributes == null) return false;

            List<String> values = attributes.get(attrName);
            if (values == null) return false;

            return values.stream()
                .anyMatch(value -> value.toLowerCase().contains(attrValue.toLowerCase()));
        }
    }

    public static boolean isExactMatch(String key, Map<String, Object> userObject, Map<String, Object> params) {
        Object userValue = userObject.get(key);
        Object paramValue = params.get(key);
        // If paramValue is null, consider it a match
        if (paramValue == null) return true;
        // Special case: if paramValue is boolean false and userValue is null, consider it a match
        if (paramValue instanceof Boolean && !(Boolean)paramValue && userValue == null) return true;
        // If userValue is null but paramValue isn't handled by cases above, not a match
        if (userValue == null) return false;
        // If both are booleans, compare as booleans
        if (userValue instanceof Boolean && paramValue instanceof Boolean) {
            return userValue.equals(paramValue);
        }
        // Otherwise, compare as strings
        return userValue.toString().equals(paramValue.toString());
    }

    public static boolean isMatch(String key, Map<String, Object> userObject, Map<String, Object> params) {
        Object exactObj = params.get("exact");
        boolean isExact = exactObj != null && Boolean.parseBoolean(exactObj.toString());
        
        if (isExact) {
            return isExactMatch(key, userObject, params);
        }

        Object userValue = userObject.get(key);
        Object paramValue = params.get(key);
        
        // If paramValue is null, consider it a match
        if (paramValue == null) return true;
        // Special case: if paramValue is boolean false and userValue is null, consider it a match
        if (paramValue instanceof Boolean && !(Boolean)paramValue && userValue == null) return true;
        // If userValue is null but paramValue isn't handled by cases above, not a match
        if (userValue == null) return false;
        
        return userValue.toString().toLowerCase().contains(paramValue.toString().toLowerCase());
    }

    @Override
    public void define(Setup setup, Topologies topologies) {
        // Define depots for storing data
        setup.declareDepot("*userDepot", Depot.hashBy(ConcatRealmToId.class));
        setup.declareDepot("*configDepot", Depot.hashBy(ExtractRealmId.class));
        StreamTopology profiles = topologies.stream("profiles");

        // Define profile topology with PStates
        profiles.pstate("$$users", PState.mapSchema(String.class, PState.mapSchema(String.class, PState.mapSchema(String.class, Object.class))));
        profiles.pstate("$$usernameToId", PState.mapSchema(String.class, PState.mapSchema(String.class, String.class)));
        profiles.pstate("$$emailToId", PState.mapSchema(String.class, PState.mapSchema(String.class, String.class)));
        profiles.pstate("$$configs", PState.mapSchema(String.class, PState.mapSchema(String.class, String.class)));

        // Process user depot updates
        profiles.source("*userDepot")
            .out("*tuple")
            .each(Ops.EXPAND, "*tuple").out("*id", "*realmId", "*userData")
            .ifTrue(new Expr(Ops.IS_NULL, "*userData"),
                Block.localTransform("$$users", Path.key("*realmId", "*id").termVoid())
                    .localSelect("$$users", Path.key("*id"))
                    .out("*oldUserData")
                    .ifTrue(new Expr(Ops.IS_NOT_NULL, "*oldUserData"),
                        Block.macro(extractJavaFields("*oldUserData", "*email", "*username"))
                        .hashPartition(new Expr(Ops.TO_STRING, "*realmId", "*username"))
                        .localTransform("$$usernameToId", Path.key("*realmId", "*username").termVoid())
                        .hashPartition(new Expr(Ops.TO_STRING, "*realmId", "*email"))
                        .localTransform("$$emailToId", Path.key("*realmId", "*email").termVoid())
                    ),
                Block.macro(extractJavaFields("*userData", "*email", "*username", "*enabled", "*emailVerified"))
                    .localTransform("$$users", Path.key("*realmId", "*id").multiPath(
                        Path.key("id").termVal("*id"),
                        Path.key("username").termVal("*username"),
                        Path.key("email").termVal("*email"),
                        Path.key("realmId").termVal("*realmId"),
                        Path.key("enabled").termVal("*enabled"),
                        Path.key("emailVerified").termVal("*emailVerified")
                    ))
                    .each(Ops.PRINTLN, "id task", "*username", new Expr(Ops.CURRENT_TASK_ID))
                    .hashPartition(new Expr(Ops.TO_STRING, "*realmId", "*username"))
                    .localTransform("$$usernameToId", Path.key("*realmId", "*username").termVal("*id"))
                    .each(Ops.PRINTLN, "username task", "*username", new Expr(Ops.CURRENT_TASK_ID))
                    .hashPartition(new Expr(Ops.TO_STRING, "*realmId", "*email"))
                    .localTransform("$$emailToId", Path.key("*realmId", "*email").termVal("*id"))
                    .each(Ops.PRINTLN, "email task", "*email", new Expr(Ops.CURRENT_TASK_ID))
            )
            .ackReturn("*id");

        // Process config depot updates
        profiles.source("*configDepot")
            .out("*tuple")
            .macro(extractJavaFields("*tuple", "*realmId", "*key", "*value"))
            .ifTrue(new Expr(Ops.IS_NULL, "*value"),
                Block.localTransform("$$configs", Path.key("*realmId", "*key").termVoid()),
                Block.localTransform("$$configs", Path.key("*realmId", "*key").termVal("*value"))
            );

        topologies.query("getUserByUsername", "*username", "*realmId")
            .out("*user")
            .hashPartition(new Expr(Ops.TO_STRING, "*realmId", "*username"))
            .each(Ops.PRINTLN, "Query task", "*username", new Expr(Ops.CURRENT_TASK_ID))
            .localSelect("$$usernameToId", Path.key("*realmId", "*username"))
            .out("*id")
            .hashPartition(new Expr(Ops.TO_STRING, "*realmId", "*id"))
            .localSelect("$$users", Path.key("*realmId", "*id"))
            .out("*user")
            .originPartition();

        topologies.query("getUserByEmail", "*email", "*realmId")
            .out("*user")
            .hashPartition(new Expr(Ops.TO_STRING, "*realmId", "*email"))
            .each(Ops.PRINTLN, "Query email task", "*email", "*realmId", new Expr(Ops.TO_STRING, "*realmId", "*email"), new Expr(Ops.CURRENT_TASK_ID))
            .localSelect("$$emailToId", Path.key("*realmId", "*email"))
            .out("*id")
            .hashPartition(new Expr(Ops.TO_STRING, "*realmId", "*id"))
            .each(Ops.PRINTLN, "Query id task", "*id", new Expr(Ops.CURRENT_TASK_ID))
            .localSelect("$$users", Path.key("*realmId", "*id"))
            .out("*user")
            .originPartition();

        topologies.query("searchUsers", "*params", "*first", "*max", "*realmId")
            .out("*users")
            .allPartition()
            .localSelect("$$users", Path.key("*realmId")
            .filterSelected(Path.view(new MatchesSearchParams(), "*params")))
            .out("*users")
            .originPartition();

        topologies.query("getGroupMembers", "*groupId", "*first", "*max", "*realmId")
            .out("*users")
            .allPartition()
            .localSelect("$$users", Path.key("*realmId").filterSelected(Path.view(new IsGroupMember(), "*groupId")))
            .out("*users")
            .originPartition();

        topologies.query("searchUsersByAttribute", "*attrName", "*attrValue", "*realmId")
            .out("*users")
            .allPartition()
            .localSelect("$$users", Path.key("*realmId").all().filterSelected(Path.view(new HasAttribute(), "*attrName", "*attrValue")))
            .out("*users")
            .originPartition();

        // Define config queries
        topologies.query("getConfig", "*realmId", "*key")
            .out("*value")
            .select("$$configs", Path.key("*realmId", "*key"))
            .out("*value")
            .originPartition();

        topologies.query("getConfigs", "*realmId")
            .out("*configs")
            .select("$$configs", Path.key("*realmId"))
            .out("*configs")
            .originPartition();
    }

}