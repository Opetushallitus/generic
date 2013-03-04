package fi.vm.sade.security;

import fi.vm.sade.generic.service.exception.NotAuthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Antti Salonen
 */
@Component
public class OrganisationHierarchyAuthorizer { // TODO: cas todo rename?

    private static final Logger LOGGER = LoggerFactory.getLogger(OrganisationHierarchyAuthorizer.class);
    private static final int MAX_CACHE_SIZE = 10000;
    public static final String ANY_ROLE = "*";

    @Autowired
    private OidProvider oidProvider;

    // poor man's cache, use auth object as part of key so objects will last only one authenticated session
    private Map<String,List<String>> cache = new ConcurrentHashMap<String, List<String>>();

    public OrganisationHierarchyAuthorizer() {
    }

    public OrganisationHierarchyAuthorizer(OidProvider oidProvider) {
        this.oidProvider = oidProvider;
    }

    /**
     * Check if current user has at least one of given roles to target organisation or it's parents.
     *
     * @param targetOrganisationOid
     * @param roles
     * @throws NotAuthorizedException
     */
    public void checkAccess(Authentication currentUser, String targetOrganisationOid, String... roles) throws NotAuthorizedException {

        // do assertions
        if (currentUser == null) {
            throw new NotAuthorizedException("checkAccess failed, currentUser is null");
        }

        List<String> targetOrganisationAndParentsOids = getSelfAndParentOidsCached(currentUser, targetOrganisationOid);
        if (targetOrganisationAndParentsOids == null || targetOrganisationAndParentsOids.size() == 0) {
            throw new NotAuthorizedException("checkAccess failed, no targetOrganisationAndParentsOids null");
        }
        if (roles == null || roles.length == 0) {
            throw new NotAuthorizedException("checkAccess failed, no roles given");
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
        // todo: cas todo logitus täältä pois
        LOGGER.error("Not authorized! currentUser: "+currentUser+", targetOrganisationAndParentsOids: "+targetOrganisationAndParentsOids+", roles: "+ Arrays.asList(roles));
        throw new NotAuthorizedException("User is not authorized for Koodisto");
    }

    private List<String> getSelfAndParentOidsCached(Authentication currentUser, String targetOrganisationOid) {
        String cacheKey = currentUser.hashCode()+"_"+targetOrganisationOid; // user hash mukana keyssä jotta resultit eläisi vain autentikoidun session
        List<String> cacheResult = cache.get(cacheKey);
        if (cacheResult == null) {
            cacheResult = oidProvider.getSelfAndParentOids(targetOrganisationOid);
            if (cache.size() > MAX_CACHE_SIZE) {
                LOGGER.info("cleaning getSelfAndParentOids -cache");
                cache.clear();
            }
            cache.put(cacheKey, cacheResult);
        }
        return cacheResult;
    }

    private static boolean roleMatchesToAuthority(String role, GrantedAuthority authority) {
        if (ANY_ROLE.equals(role)) {
            return true;
        }
        role = stripRolePrefix(role);
        return authority.getAuthority().contains(role);
    }

    private static String stripRolePrefix(String role) {
        return role.replace("APP_", "").replace("ROLE_", "");
    }

    private static boolean authorityIsTargetedToOrganisation(GrantedAuthority authority, String oid) {
        return authority.getAuthority().endsWith(oid);
    }

    public static OrganisationHierarchyAuthorizer createMockAuthorizer(final String parentOrg, final String[] childOrgs) {
        return new OrganisationHierarchyAuthorizer(new OidProvider(){
            @Override
            public List<String> getSelfAndParentOids(String organisaatioOid) {
                if (parentOrg.equals(organisaatioOid)) {
                    return Arrays.asList(organisaatioOid);
                }
                if (Arrays.asList(childOrgs).contains(organisaatioOid)) {
                    return Arrays.asList(organisaatioOid, parentOrg);
                }
                return new ArrayList<String>();
            }
        });
    }

}
