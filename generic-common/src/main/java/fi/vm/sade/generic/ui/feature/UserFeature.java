package fi.vm.sade.generic.ui.feature;

import fi.vm.sade.generic.ui.portlet.security.User;

/**
 * User: tommiha Date: 11/13/12 Time: 12:24 PM
 * 
 * @Deprecated Mieluummin Spring Security tavalla,
 *             SecurityContextHolder.getContext().getAuthentication()...
 */
@Deprecated
public class UserFeature {

    private static ThreadLocal<User> userThreadLocal = new ThreadLocal<User>();

    public static void set(User user) {
        userThreadLocal.set(user);
    }

    public static void remove() {
        userThreadLocal.remove();
    }

    public static User get() {
        if (userThreadLocal.get() == null) {
            throw new RuntimeException("User was not set on thread local.");
        }
        return userThreadLocal.get();
    }
}
