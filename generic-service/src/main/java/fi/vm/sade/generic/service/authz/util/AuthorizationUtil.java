package fi.vm.sade.generic.service.authz.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Helper utility to check user's privileges.
 * User: tommiha
 * Date: 10/22/12
 * Time: 9:46 PM
 */
public class AuthorizationUtil { // todo: cas todo, security luokat samaan paikkaan

    public static String getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        return authentication.getName();
    }

}
