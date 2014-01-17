package fi.vm.sade.security;

import org.springframework.security.cas.web.CasAuthenticationFilter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.http.HttpServletRequest;

/**
 * Extends spring CasAuthenticationFilter so that it can obtain ticket also from 'CasSecurityTicket' -http header in addition to http parameter
 *
 * @author Antti Salonen
 * @author Riku Karjalainen
 */
public class CustomCasAuthenticationFilter extends CasAuthenticationFilter {

    public static final String CAS_SECURITY_TICKET = "CasSecurityTicket";

    @Override
    protected String obtainArtifact(HttpServletRequest request) {

        // ticket-parametrin lisäksi autentikoidaan myös CasSecurityTicket-headerissa oleva ticket
    	String casTicketHeader = request.getHeader(CAS_SECURITY_TICKET);
        if (casTicketHeader != null) {

            // jos ko tiketillä ollaan jo autentikoiduttu sessio, ei tehdä sitä enää
            if (casTicketHeader.equals(getSessionTicket())) {
                logger.info("ticket already authenticated in session: "+casTicketHeader);
                return null;
            } else {
                return casTicketHeader;
            }
        }

        // getParameter -kutsu saattaa hajottaa tietyt post-requestit,
        // siksi ticket-paremeter validointi skipataan, jos a) post-request, ja c) headerissa ei tikettiä
        if ("POST".equals(request.getMethod())) {
            logger.info("skipping cas obtainArtifact because post and already authenticated");
            return null;
        }

        return super.obtainArtifact(request);
    }

    private Object getSessionTicket() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            return auth.getCredentials(); // cas ticket is saved as authentication.credentials, if credentials is something else than ticketstring, it doesn't matter
        } else {
            return null;
        }
    }

}