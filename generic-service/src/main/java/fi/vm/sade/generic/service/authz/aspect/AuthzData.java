package fi.vm.sade.generic.service.authz.aspect;

import java.util.Map;
import java.util.Set;

/**
 * Holder class for authorization data.
 *
 * @author Eetu Blomqvist
 */
public class AuthzData {

    private String user;
    private Map<String, Organisation> dataMap;

    public AuthzData(Map<String, Organisation> dataMap) {
        this.dataMap = dataMap;
    }

    public Map<String, Organisation> getDataMap() {
        return dataMap;
    }

    public void setDataMap(Map<String, Organisation> dataMap) {
        this.dataMap = dataMap;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "AuthzData{" +
                "user='" + user + '\'' +
                ", dataMap=" + dataMap +
                '}';
    }

    public static class Organisation {

        public Organisation(Set<String> children, Set<String> roles) {
            this.children = children;
            this.roles = roles;
        }

        public Set<String> children;
        public Set<String> roles;

        @Override
        public String toString() {
            return "Organisation{" +
                    "children=" + children +
                    ", roles=" + roles +
                    '}';
        }
    }
}
