package fi.vm.sade.generic.ui.portlet.security;

import org.springframework.security.core.Authentication;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public interface User extends Serializable {

    public boolean isUserInRole(String role);

    public String getOid();

    public List<AccessRight> getRawAccessRights();

    public Locale getLang();

//    public String getTicket();

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
    @Deprecated // TODO: cas todo, tää ei voi toimia enää kun koko organisaatiohierarkiaa ei lasketa userin alle!
    public Set<String> getOrganisationsHierarchy();

    Authentication getAuthentication();
}
