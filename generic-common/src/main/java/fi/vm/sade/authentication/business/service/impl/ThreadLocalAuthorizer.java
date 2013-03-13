package fi.vm.sade.authentication.business.service.impl;

import fi.vm.sade.authentication.business.service.Authorizer;
import fi.vm.sade.generic.common.auth.Role;
import fi.vm.sade.generic.service.exception.NotAuthorizedException;
import fi.vm.sade.security.OrganisationHierarchyAuthorizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class ThreadLocalAuthorizer implements Authorizer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThreadLocalAuthorizer.class);

//    @Autowired
//    private OidProvider oidProvider;
    @Autowired
    private OrganisationHierarchyAuthorizer authorizer;

    @Override
    public void checkOrganisationAccess(String targetOrganisationOid, Role... roles) throws NotAuthorizedException {
        /*OrganisationHierarchyAuthorizer*/authorizer.checkAccess(
                SecurityContextHolder.getContext().getAuthentication(),
                /*oidProvider.getSelfAndParentOids(targetOrganisationOid),*/
                targetOrganisationOid,
                rolesAsStrings(roles)); // TODO: cas todo, onko oikeet roolinimet, eli ROLE_KOODISTO_CRUD, eikä esim pelkkä CRUD ???
    }

    private String[] rolesAsStrings(Role[] roles) {
        String[] roleStrings = new String[roles.length];
        for (int i = 0; i < roles.length; i++) {
            roleStrings[i] = roles[i].name();
        }
        return roleStrings;
    }

    @Override
    public void checkUserIsNotSame(String userOid) throws NotAuthorizedException {
        LOGGER.info("Authorizing with thread local data.");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            LOGGER.error("Not authorized! User is null.");
            throw new NotAuthorizedException("User is not authorized for Authentication");
        }
        String user = authentication.getName();
        if (user == null) {
            LOGGER.error("Not authorized! User has no id.");
            throw new NotAuthorizedException("User is not authorized for Authentication");
        } else if (user.equals(userOid)) {
            LOGGER.error("Not authorized! User can't edit his/her own data");
            throw new NotAuthorizedException("User is not authorized for Authentication");
        }

        LOGGER.info("Authorized!");
    }

    public void setAuthorizer(OrganisationHierarchyAuthorizer authorizer) {
        this.authorizer = authorizer;
    }
}
