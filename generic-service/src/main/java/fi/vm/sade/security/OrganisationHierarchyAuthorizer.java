package fi.vm.sade.security;

import fi.vm.sade.generic.service.exception.NotAuthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Arrays;
import java.util.List;

/**
 * @author Antti Salonen
 */
// TODO: cas todo cas/author luokat oikeisiin moduuleihin..
public class OrganisationHierarchyAuthorizer {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrganisationHierarchyAuthorizer.class);

    /**
     * Check if current user has at least one of given roles to target organisation or it's parents.
     *
     * @param targetOrganisationOid
     * @param roles
     * @throws NotAuthorizedException
     */
    public static void checkOrganisationAccess(Authentication currentUser, List<String> targetOrganisationAndParentsOids, String... roles) throws NotAuthorizedException {

        // do assertions

        if (currentUser == null) {
            throw new NotAuthorizedException("checkOrganisationAccess failed, currentUser is null");
        }
        if (targetOrganisationAndParentsOids == null || targetOrganisationAndParentsOids.size() == 0) {
            throw new NotAuthorizedException("checkOrganisationAccess failed, no targetOrganisationAndParentsOids null");
        }
        if (roles == null || roles.length == 0) {
            throw new NotAuthorizedException("checkOrganisationAccess failed, no roles given");
        }

        // do the checks

        // sen sijaan että tarkastettaisiin käyttäjän roolipuussa alaspäin, tarkastetaan kohde-puussa ylöspäin
        // jos käyttäjällä on rooli organisaatioon, tai johonkin sen parenttiin, pääsy sallitaan
        for (String role : roles) {
            for (String oid : targetOrganisationAndParentsOids) {
                for (GrantedAuthority authority : currentUser.getAuthorities()) {
                    if (roleMatchesToAuthority(role, authority) && authorityIsTargetedToOrganisation(authority, oid)) {
                        return;
                    }
                }
            }
        }
        LOGGER.error("Not authorized! currentUser: "+currentUser+", targetOrganisationAndParentsOids: "+targetOrganisationAndParentsOids+", roles: "+ Arrays.asList(roles));
        throw new NotAuthorizedException("User is not authorized for Koodisto");
    }

    private static boolean roleMatchesToAuthority(String role, GrantedAuthority authority) {
        return authority.getAuthority().contains(role);
    }

    private static boolean authorityIsTargetedToOrganisation(GrantedAuthority authority, String oid) {
        return authority.getAuthority().endsWith(oid);
    }

}
