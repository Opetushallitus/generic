package fi.vm.sade.security;

import org.springframework.security.cas.web.CasAuthenticationFilter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
                logger.warn("ticket already authenticated in session: " + casTicketHeader); // note! casfiltterin pitäisi oletuksena toimia niin että validoidaan vain kerran per sessio, ainakin CasJettyTest mukaan
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

    @Override
    protected boolean requiresAuthentication(HttpServletRequest request, HttpServletResponse response) {
        // we want to re-login if ticket changed
        String requestTicket = obtainArtifact(request);
        Object sessionTicket = getSessionTicket();
        boolean ticketChanged = requestTicket != null && !requestTicket.equals(sessionTicket);
        if (ticketChanged) {
            logger.warn("clear authentication because ticket changed, requestTicket: " + requestTicket + ", sessionTicket: " + sessionTicket); // normal scenario but want to log it
            SecurityContextHolder.clearContext();
        }

        return super.requiresAuthentication(request, response);
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