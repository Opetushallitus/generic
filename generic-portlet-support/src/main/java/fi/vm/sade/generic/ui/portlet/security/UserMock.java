package fi.vm.sade.generic.ui.portlet.security;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * 
 * @author kkammone
 * 
 */
public class UserMock implements User {

    @Override
    public boolean isUserInRole(String role) {
        return true;
    }

    @Override
    public String getOid() {
        return "SAMPLEOID";
    }

    @Override
    public List<AccessRight> getRawAccessRights() {
        return new ArrayList<AccessRight>();
    }

    @Override
    public Locale getLang() {
        return new Locale("fi");
    }

    @Override
    public String getTicket() {
        return "ticket";
    }

    @Override
    public Set<String> getOrganisations() {
        Set<String> oids = new HashSet<String>();
        oids.add("1.2.2004.3");
        oids.add("1.2.2004.4");
        oids.add("1.2.2004.9");

        return oids;
    }

    @Override
    public Set<String> getOrganisationsHierarchy() {
        Set<String> oids = getOrganisations();
        oids.add("1.2.2004.6");
        oids.add("1.2.2004.7");
        oids.add("1.2.2004.8");
        return oids;
    }

}
