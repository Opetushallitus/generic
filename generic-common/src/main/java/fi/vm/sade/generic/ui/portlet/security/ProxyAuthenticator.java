package fi.vm.sade.generic.ui.portlet.security;

import fi.vm.sade.security.SimpleCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.cas.authentication.CasAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collection;
import java.util.Map;

/**
 * @author Antti Salonen
 */
public class ProxyAuthenticator {

    public static final int MAX_TICKET_CACHE_SIZE = 10000;
    private static final Logger log = LoggerFactory.getLogger(ProxyAuthenticator.class);
    // simple in-memory cache is sufficient when we use user user auth as part of cache key
    private static Map<String, String> ticketCache = SimpleCache.<String, String>buildCache(MAX_TICKET_CACHE_SIZE);

    public void proxyAuthenticate(String casTargetService, String authMode, Callback callback) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && "dev".equals(authMode)) {
            proxyAuthenticateDev(callback, authentication);
            return;
        }

        else if(authentication instanceof CasAuthenticationToken) {
            proxyAuthenticateCas(casTargetService, callback, authentication);
            return;
        }

        //((HttpURLConnection) message.get("http.connection")).setRequestProperty("oldDeprecatedSecurity_REMOVE_username", authentication.getName());
        //((HttpURLConnection) message.get("http.connection")).setRequestProperty("oldDeprecatedSecurity_REMOVE_authorities", ticketHeader.ticket);
        log.warn("Could not attach security ticket to SOAP message, authMode: "+ authMode +", authentication: " + authentication);
    }

    protected void proxyAuthenticateCas(String casTargetService, Callback callback, Authentication authentication) {
        CasAuthenticationToken casAuthenticationToken = (CasAuthenticationToken) authentication;
        String proxyTicket = getCachedProxyTicket(casTargetService, casAuthenticationToken, true);
        if (proxyTicket == null) {
            log.warn("got null proxyticket, cannot attach to request, casTargetService: "+casTargetService+", authentication: "+authentication);
        } else {
            callback.setRequestHeader("CasSecurityTicket", proxyTicket);
            log.debug("attached proxyticket to request! user: "+ authentication.getName() + ", ticket: "+proxyTicket);
        }
    }

    protected void proxyAuthenticateDev(Callback callback, Authentication authentication) {
        callback.setRequestHeader("CasSecurityTicket", "oldDeprecatedSecurity_REMOVE");
        String user = authentication.getName();
        String authorities = toString(authentication.getAuthorities());
        callback.setRequestHeader("oldDeprecatedSecurity_REMOVE_username", user);
        callback.setRequestHeader("oldDeprecatedSecurity_REMOVE_authorities", authorities);
        log.warn("DEV Proxy ticket! user: "+ user + ", authorities: "+authorities);
    }

    public String getCachedProxyTicket(String casTargetService, CasAuthenticationToken casAuthenticationToken, boolean createIfNotCached) {
        String cacheKey = casAuthenticationToken.hashCode() + "_" + casTargetService;
        String proxyTicket = ticketCache.get(cacheKey);
        boolean cached = proxyTicket != null;
        if (!cached && createIfNotCached) {
            proxyTicket = obtainNewCasProxyTicket(casTargetService, casAuthenticationToken);
            ticketCache.put(cacheKey, proxyTicket);
        }
        log.info("CAS Proxy ticket, key: " + cacheKey + ", cached: " + cached + ", ticket: " + proxyTicket);
        return proxyTicket;
    }

    public void clearTicket(String casTargetService) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof CasAuthenticationToken) {
            String cacheKey = authentication.hashCode() + "_" + casTargetService;
            String prevValue = ticketCache.remove(cacheKey);
            if (prevValue == null) {
                log.warn("clearTicket done, but there was no ticket! auth: "+authentication);
            }
        }
    }

    protected String obtainNewCasProxyTicket(String casTargetService, CasAuthenticationToken casAuthenticationToken) {
        return casAuthenticationToken.getAssertion().getPrincipal().getProxyTicketFor(casTargetService);
    }

    private String toString(Collection<? extends GrantedAuthority> authorities) {
        StringBuffer sb = new StringBuffer();
        for (GrantedAuthority authority : authorities) {
            sb.append(authority.getAuthority()).append(",");
        }
        return sb.toString();
    }

    public static interface Callback {
        void setRequestHeader(String key, String value);
    }
}
