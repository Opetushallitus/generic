package fi.vm.sade.generic.ui.app;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dellroad.stuff.vaadin.SpringContextApplication;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.ConfigurableWebApplicationContext;

import com.vaadin.terminal.gwt.server.HttpServletRequestListener;
import com.vaadin.terminal.gwt.server.PortletRequestListener;

import fi.vm.sade.generic.ui.feature.UserFeature;
import fi.vm.sade.generic.ui.portlet.security.User;

/**
 * User: tommiha Date: 11/13/12 Time: 12:20 PM
 * 
 * @Deprecated Liitoksissa UserLiferayImpl-toteutuksen kanssa.
 */
@Deprecated
public abstract class AbstractSpringContextApplication extends SpringContextApplication implements
        HttpServletRequestListener, PortletRequestListener {
    @Override
    protected final void initSpringApplication(ConfigurableWebApplicationContext context) {
        initialize();
    }

    protected abstract void initialize();

    @Override
    public void onRequestStart(PortletRequest request, PortletResponse response) {
        User user = new UserLiferayImpl(request);
        setLocale(user.getLang());
        UserFeature.set(user);
    }

    @Override
    public void onRequestEnd(PortletRequest request, PortletResponse response) {
        UserFeature.remove();
    }

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
}
