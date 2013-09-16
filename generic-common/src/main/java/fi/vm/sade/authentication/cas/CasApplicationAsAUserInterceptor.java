package fi.vm.sade.authentication.cas;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.phase.Phase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Interceptor for outgoing SOAP calls that uses "application-as-a-user" pattern: authenticates against CAS REST API to get a service ticket.
 *
 * @author Antti Salonen
 */
public class CasApplicationAsAUserInterceptor extends AbstractSoapInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(CasApplicationAsAUserInterceptor.class);

    private String webCasUrl;
    private String targetService;
    private String appClientUsername;
    private String appClientPassword;

    @Value("${auth.mode:cas}")
    private String authMode;

    public CasApplicationAsAUserInterceptor() {
        super(Phase.PRE_PROTOCOL);
    }

    @Override
    public void handleMessage(SoapMessage message) throws Fault {
        if ("dev".equals(authMode)) {

            Set<GrantedAuthority> authorities = buildMockAuthorities();
            // String mockUser = "admin@oph.fi";
            String mockUser = "1.2.246.562.24.00000000001";
            logger.warn("building mock user: " + mockUser + ", authorities: " + authorities);
            Authentication authentication = new TestingAuthenticationToken(mockUser, mockUser, new ArrayList<GrantedAuthority>(
                    authorities));
            //initSupportForOldAuthzFromSpringAuthentication();

            ((HttpURLConnection) message.get("http.connection")).setRequestProperty("CasSecurityTicket", "oldDeprecatedSecurity_REMOVE");
            String user = authentication.getName();
            //String authorities = toString(authentication.getAuthorities());
            ((HttpURLConnection) message.get("http.connection")).setRequestProperty("oldDeprecatedSecurity_REMOVE_username", user);
            ((HttpURLConnection) message.get("http.connection")).setRequestProperty("oldDeprecatedSecurity_REMOVE_authorities", toString(authorities));
            logger.info("DEV Proxy ticket! user: "+ user + ", authorities: "+authorities);
            return;
        }

        // authenticate against CAS REST API, and get a service ticket
        String serviceTicket = CasClient.getTicket(webCasUrl + "/v1/tickets", appClientUsername, appClientPassword, targetService);

        // put service ticket to SOAP message as a http header 'CasSecurityTicket'
        ((HttpURLConnection)message.get("http.connection")).setRequestProperty("CasSecurityTicket", serviceTicket);

        logger.debug("CasApplicationAsAUserInterceptor.handleMessage added CasSecurityTicket={} -header", serviceTicket);
    }

    public void setWebCasUrl(String webCasUrl) {
        this.webCasUrl = webCasUrl;
    }

    public void setTargetService(String targetService) {
        this.targetService = targetService;
    }

    public void setAppClientUsername(String appClientUsername) {
        this.appClientUsername = appClientUsername;
    }

    public void setAppClientPassword(String appClientPassword) {
        this.appClientPassword = appClientPassword;
    }

    private String toString(Collection<? extends GrantedAuthority> authorities) {
        StringBuffer sb = new StringBuffer();
        for (GrantedAuthority authority : authorities) {
            sb.append(authority.getAuthority()).append(",");
        }
        return sb.toString();
    }

    public static Set<GrantedAuthority> buildMockAuthorities() {
        Set<GrantedAuthority> authorities = new HashSet<GrantedAuthority>();
        // String org = "1.2.246.562.10.10108401950"; // espoon kaupunki
        String org = "1.2.246.562.10.00000000001"; // root
        String apps[] = new String[] { "ANOMUSTENHALLINTA", "ORGANISAATIOHALLINTA", "HENKILONHALLINTA", "KOODISTO",
                "KOOSTEROOLIENHALLINTA", "OID", "OMATTIEDOT", "ORGANISAATIOHALLINTA", "TARJONTA", "SIJOITTELU", "VALINTOJENTOTEUTTAMINEN", "VALINTAPERUSTEET" };
        String roles[] = new String[] { "READ", "READ_UPDATE", "CRUD" };
        for (String app : apps) {
            for (String role : roles) {
                GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_APP_" + app + "_" + role); // sama
                // rooli
                // ilman
                // oidia
                GrantedAuthority authorityOid = new SimpleGrantedAuthority("ROLE_APP_" + app + "_" + role + "_" + org);
                authorities.add(authority);
                authorities.add(authorityOid);
            }
        }
        return authorities;
    }
}
