package fi.vm.sade.generic.common.auth.xml;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * XML adapter for adapting the authorization map data into XML.
 *
 * @author Eetu Blomqvist
 */
public class AuthzDataXmlAdapter extends XmlAdapter<Organisation, Map<String, Set<String>>> {

    @Override
    public Map<String, Set<String>> unmarshal(Organisation v) throws Exception {

        Map<String, Set<String>> result = new HashMap<String, Set<String>>();

//        for (OrganisationRoles orgRole : v.orgRoles) {
//            Set<String> roles = new HashSet<String>();
//            for (String role : orgRole.roles) {
//                roles.add(role);
//            }
//
//            result.put(orgRole.oid, roles);
//        }

        return result;
    }

    @Override
    public Organisation marshal(Map<String, Set<String>> v) throws Exception {

//        Organisation o = new Organisation();
        return null;
    }
}
