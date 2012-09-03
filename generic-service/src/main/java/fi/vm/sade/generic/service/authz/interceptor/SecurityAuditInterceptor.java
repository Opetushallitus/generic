package fi.vm.sade.generic.service.authz.interceptor;

import fi.vm.sade.generic.common.JAXBUtils;
import fi.vm.sade.generic.common.auth.xml.AuthzDataHolder;
import fi.vm.sade.generic.common.auth.xml.ElementNames;
import fi.vm.sade.generic.service.authz.aspect.AuthzData;
import fi.vm.sade.generic.service.authz.aspect.AuthzDataThreadLocal;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.binding.soap.interceptor.SoapActionInInterceptor;
import org.apache.cxf.headers.Header;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSSecurityEngineResult;
import org.apache.ws.security.handler.WSHandlerConstants;
import org.apache.ws.security.handler.WSHandlerResult;
import org.apache.ws.security.message.token.UsernameToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import java.util.*;

/**
 * @author Eetu Blomqvist
 */
public class SecurityAuditInterceptor extends AbstractSoapInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityAuditInterceptor.class);
    private static final Set<QName> HEADERS = new HashSet<QName>();
    private static ThreadLocal<Map<String, Set<String>>> foo;

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

        LOGGER.info(" -- Security Audit handler called. -- ");

        // first, look for authorization data from SOAP header and set it in thread local variable.
        List<Header> headers = soapMessage.getHeaders();

        Header header = null;
        for (Header h : headers) {
            if (h.getName().getLocalPart().equals(ElementNames.AUTHZ_DATA)) {
                header = h;
                break;
            }
        }

        if (header == null) {
            throw new Fault(new org.apache.cxf.common.i18n.Message("SOAP header for authorization data is null",
                    (ResourceBundle) null, null));
        }

        Element elem = (Element) header.getObject();
        AuthzDataHolder holder = null;

        try {

            holder = JAXBUtils.unmarshal(elem, AuthzDataHolder.class);

        } catch (JAXBException e) {
            throw new Fault(new org.apache.cxf.common.i18n.Message("Can't read authz data.",
                    (ResourceBundle) null, null));
        }

        if (holder != null) {

            LOGGER.info("Got authz data: " + holder.organisations.toString());

            AuthzData ad = new AuthzData(holder.organisations);
            AuthzDataThreadLocal.set(ad);
        } else {
            // if this interceptor is configured, it assumes the data will be found.
            org.apache.cxf.common.i18n.Message msg =
                    new org.apache.cxf.common.i18n.Message("Authorization data missing",
                            (ResourceBundle) null, null);
            throw new Fault(msg);
        }


        // do audit logging by mining some data out of the request

        String operation = (String) soapMessage.get(Message.WSDL_OPERATION);
        String iface = (String) soapMessage.get(Message.WSDL_INTERFACE);
        String user = "";
        List<Object> results = (List<Object>) soapMessage.get(WSHandlerConstants.RECV_RESULTS);

        if (results != null) {

            for (Object result : results) {
                WSHandlerResult hr = (WSHandlerResult) result;
                if (hr == null || hr.getResults() == null) {
                    break;
                }
                for (WSSecurityEngineResult engineResult : hr.getResults()) {
                    if (engineResult != null &&
                            engineResult.get(WSSecurityEngineResult.TAG_USERNAME_TOKEN) instanceof UsernameToken) {
                        UsernameToken usernameToken =
                                (UsernameToken) engineResult.get(WSSecurityEngineResult.TAG_USERNAME_TOKEN);
                        user = usernameToken.getName();
                        break;
                    }
                }
                if (user != null) {
                    break;
                }
            }
            // TODO audit logging to sade log.
        } else {
            // TODO check ticket to get user.
        }
        LOGGER.info("User '" + user + "' called operation " + operation + " in " + iface);
    }
}
