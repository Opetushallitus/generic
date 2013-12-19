package fi.vm.sade.generic.ui.portlet.security;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.cas.authentication.CasAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.net.HttpURLConnection;

/**
 * CAS proxy authenticate helper, will get ticket for targetservice for current user, and attach it into http request.
 *
 * User: wuoti
 * Date: 3.9.2013
 * Time: 14.00
 */
public class AbstractSecurityTicketOutInterceptor<T extends Message> extends AbstractPhaseInterceptor<T> {

    private final static Logger log = LoggerFactory.getLogger(AbstractSecurityTicketOutInterceptor.class);

    @Value("${auth.mode:cas}")
    private String authMode;

    private ProxyAuthenticator proxyAuthenticator = new ProxyAuthenticator();

    public AbstractSecurityTicketOutInterceptor() {
        super(Phase.PRE_PROTOCOL);
    }

    @Override
    public void handleMessage(final T message) throws Fault {
        String casTargetService = getCasTargetService((String) message.get(Message.ENDPOINT_ADDRESS));
        proxyAuthenticator.proxyAuthenticate(casTargetService, authMode, new ProxyAuthenticator.Callback() {
            @Override
            public void setRequestHeader(String key, String value) {
                ((HttpURLConnection) message.get("http.connection")).setRequestProperty(key, value);
            }
            @Override
            public void gotNewTicket(Authentication authentication, String proxyTicket) {
            }
        });
    }

    @Override
    public void handleFault(Message message) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof CasAuthenticationToken) {
            String casTargetService = getCasTargetService((String) message.get(Message.ENDPOINT_ADDRESS));
            String cachedProxyTicket = proxyAuthenticator.getCachedProxyTicket(casTargetService, authentication, false, null);
            String msgProxyTicket = ((HttpURLConnection) message.get("http.connection")).getRequestProperty("CasSecurityTicket");
            log.error("FAULT in soap call, authentication: " + authentication + ", msgProxyTicket: " + msgProxyTicket + ", cachedProxyTicket: " + cachedProxyTicket);
        }
    }

    /**
     * Get cas service from url string, get string before 4th '/' char.
     * For example:
     * <p/>
     * https://asd.asd.asd:8080/backend-service/asd/qwe/qwe2.foo?bar=asd
     * --->
     * https://asd.asd.asd:8080/backend-service
     */
    private static String getCasTargetService(String url) {
        return url.replaceAll("(.*?//.*?/.*?)/.*", "$1") + "/j_spring_cas_security_check";
    }

    public void setAuthMode(String authMode) {
        this.authMode = authMode;
    }

}
