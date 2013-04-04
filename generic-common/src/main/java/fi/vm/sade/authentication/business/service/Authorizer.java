package fi.vm.sade.authentication.business.service;

import fi.vm.sade.generic.common.auth.Role;
import fi.vm.sade.generic.service.exception.NotAuthorizedException;

public interface Authorizer {
    public void checkUserIsNotSame(String userOid) throws NotAuthorizedException;

    public void checkOrganisationAccess(String targetOrganisationOid, String... roles) throws NotAuthorizedException;
}
