package fi.vm.sade.generic.service.authz.interceptor;

import fi.vm.sade.generic.service.authz.aspect.AuthzData;
import fi.vm.sade.generic.service.authz.aspect.AuthzDataThreadLocal;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.binding.soap.interceptor.SoapActionInInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.ws.security.WSConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

    public void handleMessage(SoapMessage soapMessage) {

        LOGGER.info("Security Audit handler called - not yet implemented.");

        SOAPMessage message = soapMessage.getContent(SOAPMessage.class);
        try {
            SOAPHeader header = message.getSOAPPart().getEnvelope().getHeader();
            NodeList authzData = header.getElementsByTagName("AuthzData");
            if (authzData.getLength() > 0) {
                Node item = authzData.item(0);
                AuthzDataThreadLocal.set(new AuthzData(AuthzDataUtil.convertToMap(item)));
            }

            // TODO audit logging!

        } catch (SOAPException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}
