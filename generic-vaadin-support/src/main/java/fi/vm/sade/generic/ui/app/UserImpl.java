package fi.vm.sade.generic.ui.app;

import java.util.ArrayList;
import java.util.List;

import javax.portlet.PortletRequest;

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

    private PortletRequest r;

    public UserImpl(PortletRequest portletRequest) {
        this.r = portletRequest;
    }

    @Override
    public boolean isUserInRole(String role) {
        return r.isUserInRole(role);
    }

    @Override
    public String getOid() {
        ThemeDisplay themeDisplay = (ThemeDisplay) r.getAttribute(WebKeys.THEME_DISPLAY);

        com.liferay.portal.model.User liferayUser = themeDisplay.getUser();
        String attribute = (String) liferayUser.getExpandoBridge().getAttribute("oid_henkilo");

        return attribute;
    }

    @Override
    public List<AccessRight> getRawAccessRights() {
        return new ArrayList<AccessRight>();
    }
}
