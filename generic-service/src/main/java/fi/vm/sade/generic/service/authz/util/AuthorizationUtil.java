package fi.vm.sade.generic.service.authz.util;

import fi.vm.sade.generic.common.auth.Role;
import fi.vm.sade.generic.service.authz.aspect.AuthzData;
import fi.vm.sade.generic.service.authz.aspect.AuthzDataThreadLocal;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

/**
 * Helper utility to check user's privileges.
 * User: tommiha
 * Date: 10/22/12
 * Time: 9:46 PM
 */
public class AuthorizationUtil {

    public static String getCurrentUser() {
        AuthzData authzData = AuthzDataThreadLocal.get();
        if(authzData == null) {
            return null;
        }
        return authzData.getUser();
    }

    public static boolean currentUserHasAnyRole(Collection<Role> roles) {
        AuthzData authzData = AuthzDataThreadLocal.get();
        if(authzData != null && authzData.getDataMap() != null) {
            for (Role role : roles) {
                for (Map.Entry<String, AuthzData.Organisation> entry : authzData.getDataMap().entrySet()) {
                    if (entry.getValue().roles.contains(role.name())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean currentUserHasAnyRole(Role[] roles) {
        if(roles == null) {
            return false;
        }
        return currentUserHasAnyRole(Arrays.asList(roles));
    }
}
