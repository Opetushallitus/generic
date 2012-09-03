package fi.vm.sade.generic.ui.portlet.security;

import java.util.List;
import java.util.Locale;
import java.util.Set;

public interface User {

    public boolean isUserInRole(String role);

    public String getOid();

    public List<AccessRight> getRawAccessRights();

    public Locale getLang();

    public String getTicket();

    /**
     * Returns OIDs for organisations the user is member of
     * 
     * @return organisation oids
     */
    public Set<String> getOrganisations();

    /**
     * Returns OIDs for organisations and suborganisations the user is member of
     * 
     * @return
     */
    public Set<String> getOrganisationsHierarchy();

}
