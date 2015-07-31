package fi.vm.sade.security.service.authz.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Helper utility to check user's privileges.
 * User: tommiha
 * Date: 10/22/12
 * Time: 9:46 PM
 */
public class AuthorizationUtil { // todo: cas todo, security luokat samaan paikkaan, tämän voisi poistaa kokonaan?

    public static String getCurrentUser() {
        SecurityContext ctx = SecurityContextHolder.getContext();
        if (ctx == null) {
            return null;
        }
        Authentication authentication = ctx.getAuthentication();
        if (authentication == null) {
            return null;
        }
        return authentication.getName();
    }

}
