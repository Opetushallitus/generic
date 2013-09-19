package fi.vm.sade.generic.ui.portlet.security;

import fi.vm.sade.security.SimpleCache;
import org.apache.cxf.binding.soap.interceptor.SoapPreProtocolOutInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.cas.authentication.CasAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.net.HttpURLConnection;
import java.util.Collection;
import java.util.Map;

/**
 * User: wuoti
 * Date: 3.9.2013
 * Time: 14.00
 */
public abstract class AbstractSecurityTicketOutInterceptor<T extends Message> extends AbstractPhaseInterceptor<T> {

    private final static Logger log = LoggerFactory.getLogger(AbstractSecurityTicketOutInterceptor.class);

    public static final int MAX_TICKET_CACHE_SIZE = 10000;

    // simple in-memory cache is sufficient when we use user user auth as part of cache key
    private static Map<String, String> ticketCache = SimpleCache.<String, String>buildCache(MAX_TICKET_CACHE_SIZE);

    @Value("${auth.mode:cas}")
    private String authMode;

    public AbstractSecurityTicketOutInterceptor() {
        super(Phase.PRE_PROTOCOL);
    }

    @Override
    public void handleMessage(T message) throws Fault {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if(authentication != null && authentication instanceof CasAuthenticationToken) {
            String casTargetService = getCasTargetService((String) message.get(Message.ENDPOINT_ADDRESS));
            CasAuthenticationToken casAuthenticationToken = (CasAuthenticationToken) authentication;
            String proxyTicket = getCachedProxyTicket(casTargetService, casAuthenticationToken, true);
            ((HttpURLConnection) message.get("http.connection")).setRequestProperty("CasSecurityTicket", proxyTicket);
            return;
        } else if (authentication != null && "dev".equals(authMode)) {
            ((HttpURLConnection) message.get("http.connection")).setRequestProperty("CasSecurityTicket", "oldDeprecatedSecurity_REMOVE");
            String user = authentication.getName();
            String authorities = toString(authentication.getAuthorities());
            ((HttpURLConnection) message.get("http.connection")).setRequestProperty("oldDeprecatedSecurity_REMOVE_username", user);
            ((HttpURLConnection) message.get("http.connection")).setRequestProperty("oldDeprecatedSecurity_REMOVE_authorities", authorities);
            log.info("DEV Proxy ticket! user: "+ user + ", authorities: "+authorities);
            return;
        }

        //((HttpURLConnection) message.get("http.connection")).setRequestProperty("oldDeprecatedSecurity_REMOVE_username", authentication.getName());
        //((HttpURLConnection) message.get("http.connection")).setRequestProperty("oldDeprecatedSecurity_REMOVE_authorities", ticketHeader.ticket);
        log.warn("Could not attach security ticket to SOAP message, authMode: "+authMode+", authentication: " + authentication);
    }

    @Override
    public void handleFault(Message message) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof CasAuthenticationToken) {
            String casTargetService = getCasTargetService((String) message.get(Message.ENDPOINT_ADDRESS));
            String cachedProxyTicket = getCachedProxyTicket(casTargetService, (CasAuthenticationToken) authentication, false);
            String msgProxyTicket = ((HttpURLConnection) message.get("http.connection")).getRequestProperty("CasSecurityTicket");
            log.error("FAULT in soap call, authentication: " + authentication + ", msgProxyTicket: " + msgProxyTicket + ", cachedProxyTicket: " + cachedProxyTicket);
        }
    }

    private String getCachedProxyTicket(String casTargetService, CasAuthenticationToken casAuthenticationToken, boolean createIfNotCached) {
        String cacheKey = casAuthenticationToken.hashCode() + "_" + casTargetService;
        String proxyTicket = ticketCache.get(cacheKey);
        boolean cached = proxyTicket != null;
        if (!cached && createIfNotCached) {
            proxyTicket = casAuthenticationToken.getAssertion().getPrincipal().getProxyTicketFor(casTargetService);
            ticketCache.put(cacheKey, proxyTicket);
        }
        log.info("CAS Proxy ticket, key: " + cacheKey + ", cached: " + cached + ", ticket: " + proxyTicket);
        return proxyTicket;
    }

    private String toString(Collection<? extends GrantedAuthority> authorities) {
        StringBuffer sb = new StringBuffer();
        for (GrantedAuthority authority : authorities) {
            sb.append(authority.getAuthority()).append(",");
        }
        return sb.toString();
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
}
