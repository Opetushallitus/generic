package fi.vm.sade.generic.service.authz.interceptor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.binding.soap.interceptor.SoapActionInInterceptor;
import org.apache.cxf.headers.Header;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.phase.Phase;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.util.WSSecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import fi.vm.sade.generic.common.JAXBUtils;
import fi.vm.sade.generic.common.auth.xml.AuthzDataHolder;
import fi.vm.sade.generic.common.auth.xml.ElementNames;
import fi.vm.sade.generic.common.auth.xml.Organisation;
import fi.vm.sade.generic.common.auth.xml.TicketHeader;
import fi.vm.sade.generic.service.authz.aspect.AuthzData;
import fi.vm.sade.generic.service.authz.aspect.AuthzDataThreadLocal;

/**
 * @author Eetu Blomqvist
 */
public class SecurityAuditInterceptor extends AbstractSoapInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityAuditInterceptor.class);
    private static final Set<QName> HEADERS = new HashSet<QName>();
    private static ThreadLocal<Map<String, Set<String>>> foo;
    private boolean ignoreMissing = false;

    static {
        HEADERS.add(new QName(WSConstants.WSSE_NS, WSConstants.WSSE_LN));
        HEADERS.add(new QName(WSConstants.WSSE11_NS, WSConstants.WSSE_LN));
        HEADERS.add(new QName(WSConstants.ENC_NS, WSConstants.ENC_DATA_LN));
    }

    public SecurityAuditInterceptor() {
        super(Phase.PRE_PROTOCOL);
        getAfter().add(SoapActionInInterceptor.class.getName());
    }

    public SecurityAuditInterceptor(String phase) {
        super(phase);
    }

    @Override
    public Set<QName> getUnderstoodHeaders() {
        return HEADERS;
    }

    public void handleMessage(SoapMessage soapMessage) throws Fault {

        LOGGER.info(" -- Authorization data handler -- ");

        // first, look for authorization data from SOAP header and set it in
        // thread local variable.
        List<Header> headers = soapMessage.getHeaders();

        Header header = null;
        for (Header h : headers) {

            if (h.getName().getLocalPart().equals(ElementNames.AUTHZ_DATA)) {
                header = h;
                break;
            }
        }

        if (header == null) {
            if (!ignoreMissing) {
                throw new Fault(new org.apache.cxf.common.i18n.Message("SOAP header for authorization data is null",
                        (ResourceBundle) null, null));
            } else {
                // missing header can be ignored. Nothing to do then, return...
                return;
            }
        }

        Element elem = (Element) header.getObject();
        AuthzDataHolder holder = null;

        try {

            holder = JAXBUtils.unmarshal(elem, AuthzDataHolder.class);

        } catch (JAXBException e) {
            throw new Fault(new org.apache.cxf.common.i18n.Message("Can't read authz data.", (ResourceBundle) null,
                    null));
        }

        if (holder != null) {
            Set<Organisation> organisations = holder.organisations;
            Map<String, AuthzData.Organisation> map = new HashMap<String, AuthzData.Organisation>();

            for (Organisation organisation : organisations) {
                map.put(organisation.oid, new AuthzData.Organisation(organisation.children, organisation.roles));
            }

            AuthzData ad = new AuthzData(map);

            String user = null;

            // find user for authz data
            Header h = soapMessage.getHeader(ElementNames.SECURITY_HEADER_QN);
            if (h != null) {

                // TODO: korvata unmarshallilla
                Element username = WSSecurityUtil.findElement((Element) h.getObject(), ElementNames.USERNAME,
                        ElementNames.WSS);
                if (username != null) {
                    user = username.getTextContent();
                } else {
                    org.apache.cxf.common.i18n.Message msg = new org.apache.cxf.common.i18n.Message(
                            "Username not found", (ResourceBundle) null, null);
                    throw new Fault(msg, ElementNames.FAULT_Q_NAME);
                }

                // SecurityHeader th =
                // JAXBUtils.unmarshal(item,SecurityHeader.class);

            } else {
                h = soapMessage.getHeader(ElementNames.SECURITY_TICKET_QNAME);
                if (h != null) {
                    try {
                        TicketHeader th = JAXBUtils.unmarshal((Element) h.getObject(), TicketHeader.class);
                        user = th.username;

                    } catch (JAXBException e) {
                        throw new Fault(e, ElementNames.FAULT_Q_NAME);
                    }
                }
            }

            LOGGER.info("User: " + user);
            ad.setUser(user);
            AuthzDataThreadLocal.set(ad);

            LOGGER.info(" -- Security data transformed for thread. -- ");

        } else {
            // if this interceptor is configured, it assumes the data will be
            // found.
            org.apache.cxf.common.i18n.Message msg = new org.apache.cxf.common.i18n.Message(
                    "Authorization data missing", (ResourceBundle) null, null);
            throw new Fault(msg);
        }
    }

    public void setIgnoreMissing(boolean ignoreMissing) {
        this.ignoreMissing = ignoreMissing;
    }
}
