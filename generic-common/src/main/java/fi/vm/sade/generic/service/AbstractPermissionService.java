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
package fi.vm.sade.generic.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import fi.vm.sade.generic.service.exception.NotAuthorizedException;
import fi.vm.sade.generic.ui.feature.UserFeature;
import fi.vm.sade.generic.ui.portlet.security.User;
import fi.vm.sade.security.OrganisationHierarchyAuthorizer;

/**
 * Abstract base class for UI permission checks
 * 
 * @author wuotix
 */
public abstract class AbstractPermissionService implements PermissionService {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    public static final String ANY_ROLE = OrganisationHierarchyAuthorizer.ANY_ROLE;
    public final String ROLE_CRUD;
    public final String ROLE_RU;
    public final String ROLE_R;

    @Deprecated
    @Value("${root.organisaatio.oid}")
    private String rootOrgOid;

    @Autowired(required = false)
    private OrganisationHierarchyAuthorizer authorizer;

    protected AbstractPermissionService(String application) {
        ROLE_CRUD = "APP_" + application + "_CRUD";
        ROLE_RU = "APP_" + application + "_READ_UPDATE";
        ROLE_R = "APP_" + application + "_READ";
    }

    protected final String getReadRole() {
        return ROLE_R;
    }

    protected final String getReadUpdateRole() {
        return ROLE_RU;
    }

    protected final String getCreateReadUpdateDeleteRole() {
        return ROLE_CRUD;
    }

    public final boolean checkAccess(String[] roles) {
        if (authorizer == null) {
            throw new NullPointerException(this.getClass().getSimpleName()
                    + ".authorizer -property is not wired, do it with spring or manuyally");
        }

        boolean hasAccess = false;

        try {
            authorizer.checkAccess(getUser().getAuthentication(), roles);
            hasAccess = true;
        } catch (Exception e) {
            hasAccess = false;
        }

        return hasAccess;
    }

    @Override
    public final boolean userCanRead() {
        return checkAccess(new String[] { ROLE_R, ROLE_RU, ROLE_CRUD });
    }

    @Override
    public final boolean userCanReadAndUpdate() {
        return checkAccess(new String[] { ROLE_RU, ROLE_CRUD });
    }

    @Override
    public final boolean userCanCreateReadUpdateAndDelete() {
        return checkAccess(new String[] { ROLE_CRUD });
    }

    protected final User getUser() {
        return UserFeature.get();
    }

    protected final boolean userIsMemberOfOrganisation(final String organisaatioOid) {
        return checkAccess(organisaatioOid, ANY_ROLE);
    }

    public final boolean checkAccess(String targetOrganisaatioOid, String... roles) {
        if (authorizer == null) {
            throw new NullPointerException(this.getClass().getSimpleName()
                    + ".authorizer -property is not wired, do it with spring or manuyally");
        }

        boolean hasAccess = false;
        try {
            authorizer.checkAccess(getUser().getAuthentication(), targetOrganisaatioOid, roles);
            hasAccess = true;
        } catch (Exception e) {
            if (!(e instanceof NotAuthorizedException)) {
                log.warn("checkAccess failed because exception: " + e + ", auth: " + getUser().getAuthentication());
            }
            hasAccess = false;
        }

        return hasAccess;
    }

    @Deprecated
    public String getRootOrgOid() {
        if (rootOrgOid == null) {
            throw new RuntimeException("rootOrgId is null!");
        }
        return rootOrgOid;
    }

    @Deprecated
    public boolean isOPHUser() {
        return checkAccess(getRootOrgOid(), ANY_ROLE);
    }

    public void setAuthorizer(OrganisationHierarchyAuthorizer authorizer) {
        this.authorizer = authorizer;
    }
}
