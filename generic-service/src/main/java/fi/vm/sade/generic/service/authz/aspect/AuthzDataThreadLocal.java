package fi.vm.sade.generic.service.authz.aspect;

/**
 * Thread local for accessing authorization data.
 *
 * @author Eetu Blomqvist
 */
public class AuthzDataThreadLocal {

    public static final ThreadLocal<AuthzData> authzThreadLocal = new ThreadLocal<AuthzData>();

    public static void set(AuthzData authzData) {
        authzThreadLocal.set(authzData);
    }

    public static AuthzData get() {
        return authzThreadLocal.get();
    }

    public static void remove() {
        authzThreadLocal.remove();
    }
}
