package fi.vm.sade.generic.ui.portlet.security;

import fi.vm.sade.security.SimpleCache;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.binding.soap.interceptor.SoapPreProtocolOutInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
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
 * Interceptor for adding a security ticket SOAP header into outbound SOAP message.
 * Should be used by ticket-aware clients of services.
 *
 * @author Eetu Blomqvist
 */
public class SecurityTicketOutInterceptor extends AbstractSoapInterceptor {

    private final static Logger log = LoggerFactory.getLogger(SecurityTicketOutInterceptor.class);

    public static final int MAX_TICKET_CACHE_SIZE = 10000;

    // simple in-memory cache is sufficient when we use user user auth as part of cache key
    private static Map<String, String> ticketCache = SimpleCache.<String, String>buildCache(MAX_TICKET_CACHE_SIZE);

    @Value("${auth.mode:cas}")
    private String authMode;

    public SecurityTicketOutInterceptor() {
        super(Phase.PRE_PROTOCOL);
        getAfter().add(SoapPreProtocolOutInterceptor.class.getName());
    }

    @Override
    public void handleMessage(SoapMessage message) throws Fault {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && "dev".equals(authMode)) {
            ((HttpURLConnection) message.get("http.connection")).setRequestProperty("CasSecurityTicket", "oldDeprecatedSecurity_REMOVE");
            String user = authentication.getName();
            String authorities = toString(authentication.getAuthorities());
            ((HttpURLConnection) message.get("http.connection")).setRequestProperty("oldDeprecatedSecurity_REMOVE_username", user);
            ((HttpURLConnection) message.get("http.connection")).setRequestProperty("oldDeprecatedSecurity_REMOVE_authorities", authorities);
            log.info("DEV Proxy ticket! user: "+ user + ", authorities: "+authorities);
            return;
        }

        else if(authentication instanceof CasAuthenticationToken) {
            String casTargetService = getCasTargetService((String) message.get(Message.ENDPOINT_ADDRESS));
            CasAuthenticationToken casAuthenticationToken = (CasAuthenticationToken) authentication;
            String proxyTicket = getCachedProxyTicket(casTargetService, casAuthenticationToken);
            ((HttpURLConnection) message.get("http.connection")).setRequestProperty("CasSecurityTicket", proxyTicket);
            return;
        }

        //((HttpURLConnection) message.get("http.connection")).setRequestProperty("oldDeprecatedSecurity_REMOVE_username", authentication.getName());
        //((HttpURLConnection) message.get("http.connection")).setRequestProperty("oldDeprecatedSecurity_REMOVE_authorities", ticketHeader.ticket);
        log.warn("Could not attach security ticket to SOAP message, authMode: "+authMode+", authentication: " + authentication);
    }

    private String getCachedProxyTicket(String casTargetService, CasAuthenticationToken casAuthenticationToken) {
        String cacheKey = casAuthenticationToken.hashCode()+"_"+casTargetService;
        String proxyTicket = ticketCache.get(cacheKey);
        boolean cached = proxyTicket != null;
        if (!cached) {
            proxyTicket = casAuthenticationToken.getAssertion().getPrincipal().getProxyTicketFor(casTargetService);
            ticketCache.put(cacheKey, proxyTicket);
        }
        log.info("CAS Proxy ticket, key: "+cacheKey+", cached: "+cached+", ticket: "+proxyTicket);
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
     *
     * https://asd.asd.asd:8080/backend-service/asd/qwe/qwe2.foo?bar=asd
     * --->
     * https://asd.asd.asd:8080/backend-service
     */
    private static String getCasTargetService(String url) {
        return url.replaceAll("(.*?//.*?/.*?)/.*", "$1") + "/j_spring_cas_security_check";
    }

}
