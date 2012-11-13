package fi.vm.sade.generic.ui.feature;

import com.vaadin.Application;
import com.vaadin.service.ApplicationContext;
import com.vaadin.terminal.gwt.server.HttpServletRequestListener;
import com.vaadin.terminal.gwt.server.PortletRequestListener;
import fi.vm.sade.generic.ui.app.UserLiferayImpl;
import fi.vm.sade.generic.ui.portlet.security.User;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * User: tommiha
 * Date: 11/13/12
 * Time: 12:24 PM
 */
public class UserFeature {

    private static ThreadLocal<UserRequestListener> userThreadLocal = new ThreadLocal<UserRequestListener>();

    public static void setUser(User user) {
        userThreadLocal.set(new UserRequestListener(user));
    }

    public static User getUser() {
        if(userThreadLocal.get() == null) {
            throw new RuntimeException("User was not set on thread local.");
        }
        return userThreadLocal.get().getUser();
    }

    private static class UserRequestListener {
        private User user;

        public UserRequestListener(User user) {
            this.user = user;
        }

        public User getUser() {
            return user;
        }

        public void setUser(User user) {
            this.user = user;
        }
    }
}
