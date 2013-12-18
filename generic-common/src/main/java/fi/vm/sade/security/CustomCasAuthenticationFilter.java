package fi.vm.sade.security;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.cas.web.CasAuthenticationFilter;

/**
 * Extends spring CasAuthenticationFilter so that it can obtain ticket also from http header in addition to http parameter. Changes tagged with 'oph'.
 * TODO: tata ei tarvittaisi mikali cxf (soap) kutsuihin saisi http 'ticket' -parametrin mukaan headerin sijaan, kts. haku 'cxf querystring' tms
 * TODO: oldDeprecatedSecurity_REMOVE - lisakustomointia tarvittu etta toimii vanhan autentikoinnin kanssa yhteen, siivoa myohemmin
 *
 * @author Antti Salonen
 * @author Riku Karjalainen
 */
public class CustomCasAuthenticationFilter extends CasAuthenticationFilter {

    public static final String CAS_SECURITY_TICKET = "CasSecurityTicket";

    @Override
    protected String obtainArtifact(HttpServletRequest request) {

        System.out.println("\n\n\nobtainArtifact\n\n\n");
        
    	String casTicketHeader = request.getHeader(CAS_SECURITY_TICKET);
        if (casTicketHeader != null) {
            return casTicketHeader;
        }

        return super.obtainArtifact(request);
    }
}