package fi.vm.sade.security;

import org.springframework.security.cas.web.CasAuthenticationFilter;

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
    	String casTicketHeader = request.getHeader(CAS_SECURITY_TICKET);
        if (casTicketHeader != null) {
            return casTicketHeader;
        }

        // getParameter -kutsu saattaa hajottaa tietyt post-requestit,
        // siksi ticket-paremeter validointi skipataan, jos a) post-request, ja c) headerissa ei tikettiä
        if ("POST".equals(request.getMethod())) {
            logger.info("skipping cas obtainArtifact because post and already authenticated");
            return null;
        }

        return super.obtainArtifact(request);
    }

}