package fi.vm.sade.generic.ui.app;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.portlet.PortletRequest;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portal.util.PortalUtil;

import fi.vm.sade.generic.ui.portlet.security.AccessRight;
import fi.vm.sade.generic.ui.portlet.security.SecuritySessionAttributes;
import fi.vm.sade.generic.ui.portlet.security.User;

/**
 * Consider writing this impl in different way that does not stash
 * {@link PortletRequest}
 * 
 * @author kkammone
 * 
 */
public class UserLiferayImpl implements User {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    private PortletRequest portletRequest;

    private HttpServletRequest servletRequest;

    public UserLiferayImpl(PortletRequest request) {
        this.portletRequest = request;
    }

    public UserLiferayImpl(HttpServletRequest request) {
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
            return "oidhenkilo8";
        }
        return null;
    }

    @Override
    public List<AccessRight> getRawAccessRights() {

        List<AccessRight> list = new ArrayList<AccessRight>();
        HttpServletRequest s = null;

        if (portletRequest != null) {
            s = PortalUtil.getHttpServletRequest(portletRequest);
        } else {
            s = this.servletRequest;
        }
        if (s != null) {
            Object o = s.getSession().getAttribute(SecuritySessionAttributes.AUTHENTICATION_DATA);

            if (o != null && o instanceof List) {
                try {
                    list = (List<AccessRight>) o;
                    return list;
                } catch (ClassCastException e) {
                    log.warn("Failed to get "
                            + SecuritySessionAttributes.AUTHENTICATION_DATA
                            + " Attribute from session. Session contained something else than expected. Expected List<AccessRight> got: ["
                            + o + "]");
                }
            }
        }
        return list;
    }

    @Override
    public Locale getLang() {
        if (portletRequest != null) {
            ThemeDisplay themeDisplay = (ThemeDisplay) portletRequest.getAttribute(WebKeys.THEME_DISPLAY);
            com.liferay.portal.model.User liferayUser = themeDisplay.getUser();
            return liferayUser.getLocale();
        } else if (servletRequest != null) {
            return servletRequest.getLocale();
        }
        return null;
    }
}
