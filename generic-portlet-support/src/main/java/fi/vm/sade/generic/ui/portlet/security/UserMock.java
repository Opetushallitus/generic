package fi.vm.sade.generic.ui.portlet.security;

import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.Collections;
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

	private final List<String> userRoles = new ArrayList<String>();
	private String oid = "SAMPLEOID";
	private List<AccessRight> rawAccessRights = new ArrayList<AccessRight>();
	private Locale lang = new Locale("fi");
	private String ticket = "ticket";
	private final Set<String> organisations = new HashSet<String>();
	private final Set<String> organizationHierarchy = new HashSet<String>();
    private Authentication authentication;

    public UserMock() {
		organisations.add("1.2.2004.3");
		organisations.add("1.2.2004.4");
		organisations.add("1.2.2004.9");

		organizationHierarchy.add("1.2.2004.6");
		organizationHierarchy.add("1.2.2004.7");
		organizationHierarchy.add("1.2.2004.8");
	}
	
    @Override
    public boolean isUserInRole(String role) {
        return userRoles.contains(role);
    }
    
    public void addUserRole(String role) {
    	userRoles.add(role);
    }
    
    public void removeUserRole(String role) {
    	userRoles.remove(role);
    }

    @Override
    public String getOid() {
        return oid;
    }
    
    public void setOid(String oid) {
		this.oid = oid;
	}

    @Override
    public List<AccessRight> getRawAccessRights() {
        return Collections.unmodifiableList(rawAccessRights);
    }
    
    public void addRawAccessRight(AccessRight accessRight) {
    	rawAccessRights.add(accessRight);
    }
    
    public void removeRawAccessRight(AccessRight accessRight) {
    	rawAccessRights.remove(accessRight);
    }

    @Override
    public Locale getLang() {
        return lang;
    }
    
    public void setLang(Locale lang) {
		this.lang = lang;
	}

    @Override
    public String getTicket() {
        return ticket;
    }

    public void setTicket(String ticket) {
		this.ticket = ticket;
	}
    
    @Override
    public Set<String> getOrganisations() {
        return Collections.unmodifiableSet(organisations);
    }
    
    public void addOrganization(String organizationOid) {
    	organisations.add(organizationOid);
    }
    
    public void removeOrganization(String organizationOid) {
    	organisations.remove(organizationOid);
    }

    @Override
    public Set<String> getOrganisationsHierarchy() {
        return Collections.unmodifiableSet(organizationHierarchy);
    }
    
    public void addOrganisationsHierarchy(String organizationOid) {
    	organizationHierarchy.add(organizationOid);
    }

    public void removeOrganisationsHierarchy(String organizationOid) {
    	organizationHierarchy.remove(organizationOid);
    }

    @Override
    public Authentication getAuthentication() {
        // todo: cas todo, eroon tästä, käytä spring securityä
        if (authentication == null) {
            List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
            for (AccessRight ar : getRawAccessRights()) {
                authorities.add(new SimpleGrantedAuthority(ar.getApplication() + "_" + ar.getRole() + "_" + ar.getOrganizatioOid()));
            }
            authentication = new TestingAuthenticationToken("USEROID", "USEROID", authorities);
        }
        return authentication;
    }
}
