package fi.vm.sade.generic.ui.app;

import java.util.ArrayList;
import java.util.List;

import javax.portlet.PortletRequest;
import javax.servlet.http.HttpServletRequest;

import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.theme.ThemeDisplay;

import fi.vm.sade.generic.ui.portlet.security.AccessRight;
import fi.vm.sade.generic.ui.portlet.security.User;

/**
 * Consider writing this impl in different way that does not stash
 * {@link PortletRequest}
 * 
 * @author kkammone
 * 
 */
public class UserImpl implements User {

    private PortletRequest portletRequest;

    private HttpServletRequest servletRequest;

    public UserImpl(PortletRequest request) {
        this.portletRequest = request;
    }

    public UserImpl(HttpServletRequest request) {
        this.servletRequest = request;
    }

    @Override
    public boolean isUserInRole(String role) {
        if (portletRequest != null) {
            return portletRequest.isUserInRole(role);
        } else if (servletRequest != null) {
            return servletRequest.isUserInRole(role);
        }
        return false;
    }

    @Override
    public String getOid() {
        if (portletRequest != null) {
            ThemeDisplay themeDisplay = (ThemeDisplay) portletRequest.getAttribute(WebKeys.THEME_DISPLAY);
            com.liferay.portal.model.User liferayUser = themeDisplay.getUser();
            String attribute = (String) liferayUser.getExpandoBridge().getAttribute("oid_henkilo");
            return attribute;
        } else if (servletRequest != null) {
            return "implement me";
        }
        return null;
    }

    @Override
    public List<AccessRight> getRawAccessRights() {
        return new ArrayList<AccessRight>();
    }
}
