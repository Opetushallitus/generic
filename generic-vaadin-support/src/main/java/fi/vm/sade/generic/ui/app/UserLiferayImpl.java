package fi.vm.sade.generic.ui.app;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.util.PortalUtil;
import fi.vm.sade.generic.auth.LiferayCustomAttributes;
import fi.vm.sade.generic.ui.portlet.security.AccessRight;
import fi.vm.sade.generic.ui.portlet.security.SecuritySessionAttributes;
import fi.vm.sade.generic.ui.portlet.security.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * Consider writing this impl in different way that does not stash
 * {@link PortletRequest}
 * 
 * @author kkammone
 * 
 */
public class UserLiferayImpl implements User {

    private static final long serialVersionUID = 1L;

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
        String oid = null;
        if (portletRequest != null) {
            oid =  (String) getLiferayUser().getExpandoBridge()
                    .getAttribute(LiferayCustomAttributes.OID_HENKILO, false);
        } else if (servletRequest != null) {
            oid =  "oidhenkilo8";
        }
        log.debug("getOid::" + oid);
        return oid;
    }

    @SuppressWarnings("unchecked")
    @Override
    public String getTicket() {
        String ticket = null;
        if (portletRequest != null) {
            Object o =  portletRequest.getPortletSession().getAttribute(SecuritySessionAttributes.TICKET, PortletSession.APPLICATION_SCOPE);
            if (o != null && o instanceof String) {
                ticket = (String) o;
            }
//            log.info("trying to get ticket [" +SecuritySessionAttributes.TICKET + "] from application scope portlet request ticket=[" + ticket + "] object[" + o +"]");

//            if(ticket == null) {
//                log.info("no luck");
//
//                HttpServletRequest s = PortalUtil.getHttpServletRequest(portletRequest);
//
//                o = s.getSession().getAttribute(SecuritySessionAttributes.TICKET);
//                if (o != null && o instanceof String) {
//                    ticket = (String) o;
//                }
//                log.info("trying to get ticket [" +SecuritySessionAttributes.TICKET + "] from HttpServletRequest ticket=[" + ticket + "] object[" + o +"]");
//            }
//            return ticket;
        } else {
            Object o = this.servletRequest.getSession().getAttribute(SecuritySessionAttributes.TICKET);
            if (o != null && o instanceof String) {
                ticket = (String) o;
            }
        }
        
        log.info("getTicket: [" + ticket + "]");
        return ticket;
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
            return getLiferayUser().getLocale();
        } else if (servletRequest != null) {
            return servletRequest.getLocale();
        }
        return null;
    }

    private com.liferay.portal.model.User getLiferayUser() {
        try {
            return PortalUtil.getUser(this.portletRequest);
        } catch (PortalException e) {
            log.error("Failed to get Liferay User, PortalException", e);
            throw new RuntimeException("Failed to get Liferay User, PortalException", e);
        } catch (SystemException e) {
            log.error("Failed to get Liferay User, SystemException", e);
            throw new RuntimeException("Failed to get Liferay User, SystemException", e);
        }
    }

    @Override
    public Set<String> getOrganisations() {
        Set<String> organisaatioOids = new HashSet<String>();

        if (portletRequest != null) {
            try {
                for (com.liferay.portal.model.Organization o : getLiferayUser().getOrganizations()) {
                    String organizationOid = (String) o.getExpandoBridge().getAttribute(LiferayCustomAttributes.ORGANISAATIO_OID, false);
                    log.info("Adding organization oid to user: " + organizationOid);
                    organisaatioOids.add(organizationOid);
                }
            } catch (PortalException e) {
                log.error("Failed to get organizations for Liferay User, PortalException", e);
                throw new RuntimeException("Failed to get organizations for Liferay User, PortalException", e);
            } catch (SystemException e) {
                log.error("Failed to get organizations for Liferay User, SystemException", e);
                throw new RuntimeException("Failed to get organizations for Liferay User, SystemException", e);
            }
        } else if (servletRequest != null) {
            organisaatioOids.add("1.2.2004.3");
            organisaatioOids.add("1.2.2004.4");
            organisaatioOids.add("1.2.2004.9");
        }

        return organisaatioOids;
    }

    @Override
    public Set<String> getOrganisationsHierarchy() {
        // FIXME: Figure out how to get the organisation hierarchy from
        // organisaatio service
        return getOrganisations();
    }

}
