package fi.vm.sade.generic.ui;

import fi.vm.sade.authentication.cas.CasClient;
import fi.vm.sade.generic.common.EnhancedProperties;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.jasig.cas.client.validation.Assertion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.cas.authentication.CasAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;

/**
 * @author Antti Salonen
 */
public class LoadHtmlTemplateServlet extends HttpServlet {

    private static final String CACHEKEY = "LoadHtmlTemplateServlet_htmltemplate";

    private String targetService;
    private String targetResource = "/group/virkailijan-tyopoyta";
    private String casUrl;

    private static final Logger logger = LoggerFactory.getLogger(LoadHtmlTemplateServlet.class);

    @Override
    public void init() throws ServletException {
        try {
            Properties props = new EnhancedProperties();
            props.load(new FileInputStream(new File(System.getProperty("user.home"), "oph-configuration/common.properties")));
            targetService = "http://"+props.getProperty("host.virkailija");
            if (!"80".equals(props.getProperty("port.liferay"))) {
                 targetService += props.getProperty("port.liferay");
            }
            casUrl = props.getProperty("web.url.cas");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        // call cms/navigation/liferay -service to obtain header/navigation/footer html
        String html = loadTemplateHtml(request);

        // write response
        response.setContentType("text/html; charset=utf-8");
        PrintWriter out = response.getWriter();
        out.print(html);
        out.flush();
        out.close();
    }

    private String loadTemplateHtml(HttpServletRequest request) throws IOException {

        // TODO: syö ticket poikkeuksen jollei olla kirjauduttu sisään? lopulta tämmönen kun testaa http://itest-virkailija.oph.ware.fi:7004/organisaatio-app/htmltemplate
        /*
        javax.servlet.ServletException: org.jasig.cas.client.validation.TicketValidationException:
        ticket 'ST-48-lu092afC4AsSHCbBgzgY-cas01.example.org' does not match supplied service.  The original service was 'http://itest-virkailija.oph.ware.fi:8180' and the supplied service was 'http://itest-virkailija.oph.ware.fi/group/virkailijan-tyopoyta'.
        */

        try {

            // if cached, return it
            String cached = (String) request.getSession().getAttribute(CACHEKEY);
            if (cached != null) {
                return cached;
            }

            // obtain cas authentication object
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String ticketTargetService = targetService + targetResource;
            String ticket;
            /* TODO: proxy autentikointi kuntoon jotta tämä toimii omalla käyttäjällä
            if (authentication instanceof CasAuthenticationToken) {
                Assertion assertion = ((CasAuthenticationToken) authentication).getAssertion();

                // obtain proxyticket for target service
                System.out.println("loadTemplateHtml, get ticket for service: "+targetService+targetResource);
                ticket = assertion.getPrincipal().getProxyTicketFor(ticketTargetService);
                if (ticket == null) {
                    throw new NullPointerException("could not obtain ticket");
                }
            }

            // in dev env use root user's navi - TODO: ei tuotantoon, tämä konffattavaksi jonkun dev-vivun taakse?
            else {
            */
                System.err.println("WARNING - temp using root user's navi, authentication object was: "+authentication+", casurl: "+casUrl+", targetservice: "+ticketTargetService);
                ticket = CasClient.getTicket(casUrl + "/v1/tickets", "ophadmin", "ilonkautta", ticketTargetService);
            /*
            }
            */

            // call rest resource with proxyticket as parameter
            String htmlResourceUrl = targetService + targetResource + "?ticket=" + ticket;
            logger.debug("loadTemplateHtml, get html from url: {}", htmlResourceUrl);
            String response = doHttpCall(htmlResourceUrl);

            // cache the html
            request.getSession().setAttribute(CACHEKEY, response);

            return response;

        } catch (Exception e) {
            logger.error("FAILED to load htmltemplate, targetService: {}, targetResource: {}, casUrl: {}, exception: {}",
                    new Object[] {targetService, targetResource, casUrl, e});
            throw new RuntimeException(e);
        }

    }

    private String doHttpCall(String urlString) throws IOException {
        HttpClient client = new HttpClient();
        GetMethod get = new GetMethod(urlString);
        client.executeMethod(get);
        return get.getResponseBodyAsString();
    }

}
