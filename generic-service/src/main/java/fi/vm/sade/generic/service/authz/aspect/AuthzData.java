package fi.vm.sade.generic.service.authz.aspect;

import java.util.Map;
import java.util.Set;

/**
 * @author Eetu Blomqvist
 */
public class AuthzData {

    private Map<String, Set<String>> dataMap;

    public AuthzData(Map<String, Set<String>> dataMap) {
        this.dataMap = dataMap;
    }

    public Map<String, Set<String>> getDataMap() {
        return dataMap;
    }

    public void setDataMap(Map<String, Set<String>> dataMap) {
        this.dataMap = dataMap;
    }
}
