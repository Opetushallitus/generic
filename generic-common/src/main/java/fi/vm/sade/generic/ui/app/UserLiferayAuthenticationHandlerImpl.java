package fi.vm.sade.generic.ui.app;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import fi.vm.sade.generic.ui.feature.UserFeature;
import fi.vm.sade.generic.ui.portlet.security.User;

/**
 * 
 * @author Jussi Jartamo
 * 
 */
public class UserLiferayAuthenticationHandlerImpl implements HttpServletRequestAuthenticationHandler,
        PortletRequestAuthenticationHandler {

    @Override
    public void onRequestStart(PortletRequest request, PortletResponse response) {
        User user = new UserLiferayImpl(request);
        UserFeature.set(user);
    }

    @Override
    public void onRequestEnd(PortletRequest request, PortletResponse response) {
        UserFeature.remove();
    }

    @Override
    public void onRequestStart(HttpServletRequest request, HttpServletResponse response) {
        User user = new UserLiferayImpl(request);
        UserFeature.set(user);
    }

    @Override
    public void onRequestEnd(HttpServletRequest request, HttpServletResponse response) {
        UserFeature.remove();
    }

    private static final long serialVersionUID = -2133932841223903587L;

}
