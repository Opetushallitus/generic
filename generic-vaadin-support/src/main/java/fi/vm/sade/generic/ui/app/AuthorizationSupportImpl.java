package fi.vm.sade.generic.ui.app;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import fi.vm.sade.generic.ui.portlet.security.AccessRight;

/**
 * 
 * @author kkammone
 * 
 */
@Component
public class AuthorizationSupportImpl implements AuthorizationSupport {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * Pulls Liferay dependencies, enable if needed
     * 
     * @return
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<AccessRight> getRawAccessRights() {
        // HttpServletRequest httpServletRequest =
        // PortalUtil.getHttpServletRequest(threadLocalPortletRequest.get());
        // Object o =
        // httpServletRequest.getSession().getAttribute(SecuritySessionAttributes.AUTHENTICATION_DATA);
        //
        List<AccessRight> list = new ArrayList<AccessRight>();
        // if (o != null && o instanceof List) {
        // try {
        // list = (List<AccessRight>) o;
        // return list;
        // } catch (ClassCastException e) {
        // log.warn("Failed to get "
        // + SecuritySessionAttributes.AUTHENTICATION_DATA
        // +
        // " Attribute from session. Session contained something else than expected. Expected List<AccessRight> got: ["
        // + o + "]");
        // }
        // }
        return list;
    }

    @Override
    public boolean isUserInRole(String role) {
        return ThreadLocalPortletRequestSupport.getPortletRequest().isUserInRole(role);
    }

    @Override
    public String getUserOID() {
        return ThreadLocalPortletRequestSupport.getPortletRequest().getUserPrincipal().getName();
    }

}
