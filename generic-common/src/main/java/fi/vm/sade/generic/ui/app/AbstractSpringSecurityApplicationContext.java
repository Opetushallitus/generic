package fi.vm.sade.generic.ui.app;

import com.vaadin.terminal.gwt.server.HttpServletRequestListener;
import fi.vm.sade.generic.ui.portlet.security.User;
import org.dellroad.stuff.vaadin.SpringContextApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.ConfigurableWebApplicationContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 
 * @author Jussi Jartamo
 * 
 *         DellRoadin Vaadin-Spring konteksti joka abstrahoi autentikoinnin
 *         palveluiden taakse. Sekä Portlet että Servlet autentikoinnin
 *         tarjoajan voi toteuttaa erillisinä palveluina.
 * 
 */
public abstract class AbstractSpringSecurityApplicationContext extends SpringContextApplication implements
        HttpServletRequestListener {

    @Autowired
    private HttpServletRequestAuthenticationHandler servletRequestAuthenticationHandler;

    @Override
    protected final void initSpringApplication(ConfigurableWebApplicationContext context) {
        initialize();
    }

    protected abstract void initialize();

    @Override
    public User getUser() {
        if ((SecurityContextHolder.getContext()).getAuthentication() == null) {
            return null;
        }
        return (User) (SecurityContextHolder.getContext()).getAuthentication().getPrincipal();
    }

    @Override
    protected void doOnRequestStart(HttpServletRequest request, HttpServletResponse response) {
        servletRequestAuthenticationHandler.onRequestStart(request, response);
        setCurrentUserLocale();
    }

    @Override
    protected void doOnRequestEnd(HttpServletRequest request, HttpServletResponse response) {
        servletRequestAuthenticationHandler.onRequestEnd(request, response);
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

    private void setCurrentUserLocale() {
        User user = getUser();
        if (user != null) {
            setLocale(user.getLang());
        }
    }

    private static final long serialVersionUID = -7135972410972272962L;

}
