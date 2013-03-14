/*
 *
 * Copyright (c) 2012 The Finnish Board of Education - Opetushallitus
 *
 * This program is free software:  Licensed under the EUPL, Version 1.1 or - as
 * soon as they will be approved by the European Commission - subsequent versions
 * of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at: http://www.osor.eu/eupl/
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * European Union Public Licence for more details.
 */
package fi.vm.sade.generic.ui.app;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.model.UserGroup;
import com.liferay.portal.util.PortalUtil;
import fi.vm.sade.generic.auth.LiferayCustomAttributes;
import fi.vm.sade.generic.ui.portlet.security.AccessRight;
import fi.vm.sade.generic.ui.portlet.security.SecuritySessionAttributes;
import fi.vm.sade.generic.ui.portlet.security.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * Consider writing this impl in different way that does not stash
 * {@link PortletRequest}
 *
 * @author kkammone
 *
 */
public class UserLiferayImpl implements User {

    private static final long serialVersionUID = 1L;

    protected final Logger log = LoggerFactory.getLogger(getClass());

    private PortletRequest portletRequest;

    private HttpServletRequest servletRequest;

    private List<AccessRight> rawAccessRights = new ArrayList<AccessRight>();
    private Set<String> organisations = new HashSet<String>();
    private Authentication authentication;

    public UserLiferayImpl(PortletRequest request) {
        this.portletRequest = request;
        // build spring authentication-object out of liferay user + roles // todo: cas todo, eroon authentication-objektin käsin tekemisestä, käytä spring securityä / hae ldapista
        Set<GrantedAuthority> authorities = new HashSet<GrantedAuthority>();
        try {
            for (UserGroup group : getLiferayUser().getUserGroups()) {
                String name = group.getName();
                if (name.matches(".*_.*_.*_.*")) { // app_koodisto_crud_1.2.3 TAI app_koodisto_read_update_1.2.3
                    String[] parts = name.split("_");
                    AccessRight right;
                    if (parts.length == 4) {
                        right = new AccessRight(parts[3], parts[2].toUpperCase(), parts[1].toUpperCase());
                    } else if (parts.length == 5) {
                        right = new AccessRight(parts[4], (parts[2]+"_"+parts[3]).toUpperCase(), parts[1].toUpperCase());
                    } else {
                        throw new RuntimeException("cannot parse usergroup to accessright: "+name);
                    }
                    GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_APP_"+right.getApplication()+"_"+right.getRole()); // sama rooli ilman oidia
                    GrantedAuthority authorityOid = new SimpleGrantedAuthority("ROLE_APP_"+right.getApplication()+"_"+right.getRole()+"_"+right.getOrganizatioOid());
                    authorities.add(authority);
                    authorities.add(authorityOid);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e); // todo: errors
        }
        authentication = new TestingAuthenticationToken(getLiferayUser().getEmailAddress(), getLiferayUser().getEmailAddress(), new ArrayList<GrantedAuthority>(authorities));
        initSupportForOldAuthzFromSpringAuthentication();
    }

    public UserLiferayImpl(HttpServletRequest request) {
        this.servletRequest = request;
        // build mock user - TODO: cas todo, aina admin@oph.fi, jos tulee järjestelmään uusia rooleja, pitää tännekin lisätä, ei hyvä
        Set<GrantedAuthority> authorities = buildMockAuthorities();
        //String mockUser = "admin@oph.fi";
        String mockUser = "1.2.246.562.24.00000000001";
        authentication = new TestingAuthenticationToken(mockUser, mockUser, new ArrayList<GrantedAuthority>(authorities));
        initSupportForOldAuthzFromSpringAuthentication();
    }

    public static Set<GrantedAuthority> buildMockAuthorities() {
        Set<GrantedAuthority> authorities = new HashSet<GrantedAuthority>();
        //String org = "1.2.246.562.10.10108401950"; // espoon kaupunki
        String org = "1.2.246.562.10.00000000001"; // root
        String apps[] = new String[]{"ANOMUSTENHALLINTA", "ORGANISAATIOHALLINTA", "HENKILONHALLINTA", "KOODISTO", "KOOSTEROOLIENHALLINTA", "OID", "OMATTIEDOT", "ORGANISAATIOHALLINTA", "TARJONTA"};
        String roles[] = new String[]{"READ", "READ_UPDATE", "CRUD"};
        for (String app : apps) {
            for (String role : roles) {
                GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_APP_"+app+"_"+role); // sama rooli ilman oidia
                GrantedAuthority authorityOid = new SimpleGrantedAuthority("ROLE_APP_"+app+"_"+role+"_"+org);
                authorities.add(authority);
                authorities.add(authorityOid);
            }
        }
        return authorities;
    }

    private void initSupportForOldAuthzFromSpringAuthentication() {
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            String name = authority.getAuthority(); // ROLE_APP_KOODISTO_[READ|READ_UPDATE_|CRUD]_1.2.3.4.5
            String[] parts = name.split("_");
            if (parts.length == 5 || parts.length == 6) {
                AccessRight right;
                if (parts.length == 5) {
                    right = new AccessRight(parts[4], parts[3].toUpperCase(), parts[2].toUpperCase());
                } else if (parts.length == 6) {
                    right = new AccessRight(parts[5], (parts[3]+"_"+parts[4]).toUpperCase(), parts[2].toUpperCase());
                } else {
                    throw new RuntimeException("cannot parse usergroup to accessright: "+name);
                }
                if (!"UPDATE".equalsIgnoreCase(right.getOrganizatioOid())) {
                    rawAccessRights.add(right);
                    organisations.add(right.getOrganizatioOid());
                }
                //System.out.println(name + "->" + right.getOrganizatioOid());
            }
        }
    }

    @Override
    @Deprecated // TODO: cas todo ei pitäisi käyttää, vaan spring security, tai esim PermissionService/OrganisaatioHierarchyAuthorization.checkAccess
    public boolean isUserInRole(String role) {
        if (portletRequest != null) {
            return portletRequest.isUserInRole(role);
        } else if (servletRequest != null) {
            return servletRequest.isUserInRole(role);
        }
        return false;
    }

    @Override
    public String getOid() {
        String oid = null;
        if (portletRequest != null) {
            oid =  (String) getLiferayUser().getExpandoBridge()
                    .getAttribute(LiferayCustomAttributes.OID_HENKILO, false);
        } else if (servletRequest != null) {
            oid =  "oidhenkilo8"; // TODO: cas todo pois
        }
        //DEBUGSAWAY:log.debug("getOid::" + oid);
        return oid;
    }

    /* cas todo deprecated
    @SuppressWarnings("unchecked")
    @Override
    public String getTicket() {
        String ticket = null;
        if (portletRequest != null) {
            Object o =  portletRequest.getPortletSession().getAttribute(SecuritySessionAttributes.TICKET, PortletSession.APPLICATION_SCOPE);
            if (o != null && o instanceof String) {
                ticket = (String) o;
            }
//            log.info("trying to get ticket [" +SecuritySessionAttributes.TICKET + "] from application scope portlet request ticket=[" + ticket + "] object[" + o +"]");

//            if(ticket == null) {
//                log.info("no luck");
//
//                HttpServletRequest s = PortalUtil.getHttpServletRequest(portletRequest);
//
//                o = s.getSession().getAttribute(SecuritySessionAttributes.TICKET);
//                if (o != null && o instanceof String) {
//                    ticket = (String) o;
//                }
//                log.info("trying to get ticket [" +SecuritySessionAttributes.TICKET + "] from HttpServletRequest ticket=[" + ticket + "] object[" + o +"]");
//            }
//            return ticket;
        } else {
            Object o = this.servletRequest.getSession().getAttribute(SecuritySessionAttributes.TICKET);
            if (o != null && o instanceof String) {
                ticket = (String) o;
            }
        }

        log.info("getTicket: [" + ticket + "]");
        return ticket;
    }
    */

    @Override
    public List<AccessRight> getRawAccessRights() {

        /* cas todo deprecated
        if (rawAccessRights != null) {
            return rawAccessRights;
        }

        rawAccessRights = new ArrayList<AccessRight>();
        HttpServletRequest s = null;

        if (portletRequest != null) {
            s = PortalUtil.getHttpServletRequest(portletRequest);
        } else {
            s = this.servletRequest;
        }
        if (s != null) {

            // cas todo, ldap roles -> liferay groups -> oph accessrights, poista vanha?
            if ("true".equals(s.getSession().getAttribute("USER_authenticatedByCAS"))) {
                try {
                    for (UserGroup group : getLiferayUser().getUserGroups()) {
                        String name = group.getName();
                        if (name.matches(".*_.*_.*_.*")) { // app_koodisto_crud_1.2.3 TAI app_koodisto_read_update_1.2.3
                            String[] parts = name.split("_");
                            AccessRight right;
                            if (parts.length == 4) {
                                right = new AccessRight(parts[3], parts[2].toUpperCase(), parts[1].toUpperCase());
                            } else if (parts.length == 5) {
                                 right = new AccessRight(parts[4], (parts[2]+"_"+parts[3]).toUpperCase(), parts[1].toUpperCase());
                            } else {
                                throw new RuntimeException("cannot parse usergroup to accessright: "+name);
                            }
                            rawAccessRights.add(right);
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e); // todo: errors
                }

            } else {

                Object o = s.getSession().getAttribute(SecuritySessionAttributes.AUTHENTICATION_DATA);

                if (o != null && o instanceof List) {
                    try {
                        rawAccessRights = (List<AccessRight>) o;
                        return rawAccessRights;
                    } catch (ClassCastException e) {
                        log.warn("Failed to get "
                                + SecuritySessionAttributes.AUTHENTICATION_DATA
                                + " Attribute from session. Session contained something else than expected. Expected List<AccessRight> got: ["
                                + o + "]");
                    }
                }

            }

        }
        return rawAccessRights;
        */

        return rawAccessRights;
    }

    @Override
    public Locale getLang() {
        if (portletRequest != null) {
            return getLiferayUser().getLocale();
        } else if (servletRequest != null) {
            return servletRequest.getLocale();
        }
        return null;
    }

    private com.liferay.portal.model.User getLiferayUser() {
        try {
            return PortalUtil.getUser(this.portletRequest);
        } catch (PortalException e) {
            log.error("Failed to get Liferay User, PortalException", e);
            throw new RuntimeException("Failed to get Liferay User, PortalException", e);
        } catch (SystemException e) {
            log.error("Failed to get Liferay User, SystemException", e);
            throw new RuntimeException("Failed to get Liferay User, SystemException", e);
        }
    }

    @Override
    public Set<String> getOrganisations() { // todo: cachetus
        /* cas todo vanhat pois
        Set<String> organisaatioOids = new HashSet<String>();

        if (portletRequest != null) {

            try {

                // cas todo, ldap organizations -> liferay groups -> oph organisaatios, poista vanha
                if ("true".equals(portletRequest.getPortletSession().getAttribute("USER_authenticatedByCAS", PortletSession.APPLICATION_SCOPE))) {
                    for (UserGroup group : getLiferayUser().getUserGroups()) {
                        String name = group.getName();
                        if (name.matches(".*_.*_.*_.*")) {
                            String organizationOid = name.substring(name.lastIndexOf("_")+1);
                            if (!organisaatioOids.contains(organizationOid)) {
                                log.info("Adding organization oid to user: " + organizationOid + " (name: "+name+")");
                                organisaatioOids.add(organizationOid);
                            }
                        }
                    }

                } else {
                    for (com.liferay.portal.model.Organization o : getLiferayUser().getOrganizations()) {
                        String organizationOid = (String) o.getExpandoBridge().getAttribute(LiferayCustomAttributes.ORGANISAATIO_OID, false);
                        log.info("Adding organization oid to user: " + organizationOid);
                        organisaatioOids.add(organizationOid);
                    }
                }
            } catch (PortalException e) {
                log.error("Failed to get organizations for Liferay User, PortalException", e);
                throw new RuntimeException("Failed to get organizations for Liferay User, PortalException", e);
            } catch (SystemException e) {
                log.error("Failed to get organizations for Liferay User, SystemException", e);
                throw new RuntimeException("Failed to get organizations for Liferay User, SystemException", e);
            }
        } else if (servletRequest != null) {
            organisaatioOids.add("1.2.2004.3"); // todo: pois?
            organisaatioOids.add("1.2.2004.4");
            organisaatioOids.add("1.2.2004.9");
        }

        return organisaatioOids;
        */

        return organisations;
    }

    @Override
    public Set<String> getOrganisationsHierarchy() {
        // FIXME: Figure out how to get the organisation hierarchy from
        // organisaatio service
        return getOrganisations();
    }

    @Override
    public Authentication getAuthentication() {
        /* cas todo deprecated
        // todo: cas todo, eroon tästä, käytä spring securityä
        if (authentication == null) {
            List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
            for (AccessRight ar : getRawAccessRights()) {
                authorities.add(new SimpleGrantedAuthority(ar.getApplication() + "_" + ar.getRole() + "_" + ar.getOrganizatioOid()));
            }
            authentication = new TestingAuthenticationToken("USEROID", "USEROID", authorities);
        }
        return authentication;
        */
        return authentication;
    }

    public HttpServletRequest getServletRequest() {
        return servletRequest;
    }

    public PortletRequest getPortletRequest() {
        return portletRequest;
    }

    public Object getGlobalSessionAttribute(String name) {
        if (portletRequest != null) {
            return portletRequest.getPortletSession().getAttribute(name, PortletSession.APPLICATION_SCOPE);
        } else {
            return servletRequest.getSession().getAttribute(name);
        }
    }

    public Enumeration<String> getGlobalSessionAttributeNames() {
        if (portletRequest != null) {
            return portletRequest.getPortletSession().getAttributeNames(PortletSession.APPLICATION_SCOPE);
        } else {
            return servletRequest.getSession().getAttributeNames();
        }
    }

}