package fi.vm.sade.generic.ui.app;

import com.vaadin.terminal.Terminal.ErrorListener;
import com.vaadin.terminal.gwt.server.HttpServletRequestListener;
import com.vaadin.ui.Window;
import fi.vm.sade.generic.ui.feature.UserFeature;
import fi.vm.sade.generic.ui.portlet.security.User;
import org.dellroad.stuff.vaadin.SpringContextApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.ConfigurableWebApplicationContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashSet;
import java.util.Set;

/**
 * User: tommiha Date: 11/13/12 Time: 12:20 PM
 * 
 * @Deprecated Liitoksissa UserLiferayImpl-toteutuksen kanssa.
 */
// @Deprecated
public abstract class AbstractSpringContextApplication extends SpringContextApplication implements HttpServletRequestListener {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    // Toteuttaa Vaadinin Applicationin ErrorListenerin logiikan jos palvelu
    // tarjotaan.
    // Tarkoitus on mahdollistaa kaikille Vaadin sovelluksille yhteinen tapa
    // toteuttaa omat virhesivut.
    @Autowired(required = false)
    private GenericExceptionInterceptor exceptionInterceptor;

    @Override
    protected final void initSpringApplication(ConfigurableWebApplicationContext context) {
        optionalExceptionHandler();
        initialize();
    }

    private void optionalExceptionHandler() {
        if (exceptionInterceptor != null) {
            setErrorHandler(new ErrorListener() {
                private static final long serialVersionUID = 1L;

                @Override
                public void terminalError(com.vaadin.terminal.Terminal.ErrorEvent event) {

                    Window mainWindow = AbstractSpringContextApplication.this.getMainWindow();
                    if (mainWindow != null) {

                        if (exceptionInterceptor.intercept(event.getThrowable())) {
                            removeWindow(mainWindow);
                            setMainWindow(exceptionInterceptor.getErrorWindow(AbstractSpringContextApplication.this));
                            // mainWindow.open(new
                            // ExternalResource(exceptionInterceptor.redirect(event.getThrowable())));
                        }
                    }

                }
            });
        }
    }

    /*
     * @Override public static SystemMessages getSystemMessages() { return null;
     * }
     */

    protected abstract void initialize();

    @Override
    public User getUser() {
        return UserFeature.get();
    }

    @Override
    protected void doOnRequestStart(HttpServletRequest request, HttpServletResponse response) {
        User user = new UserLiferayImpl(request);
        setLocale(user.getLang());
        UserFeature.set(user);
    }

    @Override
    protected void doOnRequestEnd(HttpServletRequest request, HttpServletResponse response) {
        UserFeature.remove();
    }

    public boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            if (authority.getAuthority().equals(role)) {
                return true;
            }
        }
        return false;
    }

    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        return authentication.isAuthenticated();
    }

    public Authentication getCurrentUser() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    public boolean hasOrganization(String oid) {
        Authentication user = getCurrentUser();
        if(user == null || oid == null || oid.isEmpty()) return false;

        for(GrantedAuthority authority : user.getAuthorities()) {
            String roleName = authority.getAuthority();
            if(roleName.contains(oid)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Palauttaa kaikki organisaatiot, joihin käyttäjä on liitetty annetussa sovelluksessa.
     * Esim. getOrganizationForApplication("KOODISTO") palauttaa ROLE_APP_KOODISTO_* -oidit.
     * @param applicationKey
     * @return
     */
    public Set<String> getOrganizationForApplication(String applicationKey) {
        Authentication user = getCurrentUser();
        final Set<String> orgSet = new HashSet<String>();

        if(user == null || applicationKey == null || applicationKey.isEmpty()) return orgSet;

        final String prefix = "ROLE_APP_" + applicationKey + "_";

        for(GrantedAuthority authority : user.getAuthorities()) {
            String roleName = authority.getAuthority();
            if(roleName.startsWith(prefix)) {
                orgSet.add(roleName.substring(prefix.length() - 1));
            }
        }
        return orgSet;
    }
}
