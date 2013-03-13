package fi.vm.sade.auth.service.business.it;

import fi.vm.sade.security.ThreadLocalAuthorizer;
import fi.vm.sade.generic.common.auth.Role;
import fi.vm.sade.generic.service.exception.NotAuthorizedException;
import fi.vm.sade.security.OrganisationHierarchyAuthorizer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import java.util.Arrays;

@RunWith(BlockJUnit4ClassRunner.class)
public class ThreadLocalAuthorizerTest {

    @Test
    public void testSuccessfulAuthorization() {

        // organisaatiorakenne: foobar on parent, keijo ja jorma lapsia
        ThreadLocalAuthorizer authorizer = createAuthorizerWithOrgHierarchy("foobar", new String[]{"keijo", "jorma"});
        // oikeudet: READ_UPDATE oikeus annettu userille ja kohdistettu parent organisaatioon
        createUserAndRoles("foobar", Role.READ_UPDATE.toString());

        // pitäisi olla oikeus parenttiin
        authorizer.checkOrganisationAccess("foobar", Role.READ_UPDATE, Role.CRUD);

        // pitäisi olla oikeus lapseen
        authorizer.checkOrganisationAccess("keijo", Role.READ_UPDATE, Role.CRUD);

    }

    @Test(expected = NotAuthorizedException.class)
    public void testUnAuthorized() {

        // organisaatiorakenne: foobar on parent, keijo ja jorma lapsia
        ThreadLocalAuthorizer authorizer = createAuthorizerWithOrgHierarchy("foobar", new String[]{"keijo", "jorma"});
        // oikeudet: READ oikeus annettu userille ja kohdistettu parent organisaatioon
        createUserAndRoles("foobar", Role.READ.toString());

        // ei pitäisi olla oikeus parenttiin
        authorizer.checkOrganisationAccess("foobar", Role.READ_UPDATE, Role.CRUD);
    }

    @Test(expected = NotAuthorizedException.class)
    public void testNullOrganisation() {

        // organisaatiorakenne: foobar on parent, keijo ja jorma lapsia
        ThreadLocalAuthorizer authorizer = createAuthorizerWithOrgHierarchy("foobar", new String[]{"keijo", "jorma"});
        // oikeudet: READ_UPDATE oikeus annettu userille ja kohdistettu ihan toiseen organisaatioon
        createUserAndRoles("notexistingorg", Role.READ.toString());

        // ei pitäisi olla oikeus parenttiin
        authorizer.checkOrganisationAccess("foobar", Role.READ_UPDATE, Role.CRUD);
    }

    private void createUserAndRoles(String oid, String role) {
        SecurityContextHolder.getContext().setAuthentication(new PreAuthenticatedAuthenticationToken("test", "test", Arrays.asList(
                new SimpleGrantedAuthority("ROLE_"+role+"_"+oid)
        )));
    }

    private ThreadLocalAuthorizer createAuthorizerWithOrgHierarchy(final String parentOrg, final String[] childOrgs) {
        ThreadLocalAuthorizer authorizer = new ThreadLocalAuthorizer();
        authorizer.setAuthorizer(OrganisationHierarchyAuthorizer.createMockAuthorizer(parentOrg, childOrgs));
        return authorizer;
    }

}
