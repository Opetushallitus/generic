package fi.vm.sade.generic.service.authz.interceptor;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Eetu Blomqvist
 */
public class AuthzDataXmlAdapter extends XmlAdapter<Organisations, Map<String, Set<String>>> {
    @Override
    public Map<String, Set<String>> unmarshal(Organisations v) throws Exception {

        Map<String, Set<String>> result = new HashMap<String, Set<String>>();

        for (OrganisationRoles orgRole : v.orgRoles) {
            Set<String> roles = new HashSet<String>();
            for (String role : orgRole.roles) {
                roles.add(role);
            }

            result.put(orgRole.oid, roles);
        }

        return result;
    }

    @Override
    public Organisations marshal(Map<String, Set<String>> v) throws Exception {
        // TODO: implement this method.
        return null;
    }
}
