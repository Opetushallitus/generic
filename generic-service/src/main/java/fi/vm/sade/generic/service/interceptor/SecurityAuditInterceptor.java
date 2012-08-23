package fi.vm.sade.generic.service.interceptor;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.binding.soap.interceptor.SoapActionInInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.ws.security.WSConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.namespace.QName;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Eetu Blomqvist
 */
public class SecurityAuditInterceptor extends AbstractSoapInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityAuditInterceptor.class);
    private static final Set<QName> HEADERS = new HashSet<QName>();

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

        // do nothing

        // this interceptor simply returns all WS-Security headers in
        // its getUnderstoodHeaders()
        // method, so that CXF does not complain that they have not been
        // "processed"
        // this is useful if you only need to look at the non-encrypted XML

        // TODO: check that the headers exist and log them with logging client.

    }
}
