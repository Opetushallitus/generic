package fi.vm.sade.generic.ui;

import fi.vm.sade.authentication.cas.CasClient;
import fi.vm.sade.generic.common.EnhancedProperties;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.jasig.cas.client.validation.Assertion;
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

    @Override
    public void init() throws ServletException {
        try {
            Properties props = new EnhancedProperties();
            props.load(new FileInputStream(new File(System.getProperty("user.home"), "oph-configuration/common.properties")));
            targetService = props.getProperty("cas.service.liferay");
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

        // if cached, return it
        String cached = (String) request.getSession().getAttribute(CACHEKEY);
        if (cached != null) {
            return cached;
        }

        // obtain cas authentication object
        String ticket;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof CasAuthenticationToken) {
            Assertion assertion = ((CasAuthenticationToken) authentication).getAssertion();

            // obtain proxyticket for target service
            ticket = assertion.getPrincipal().getProxyTicketFor(targetService);
            if (ticket == null) {
                throw new NullPointerException("could not obtain ticket");
            }
        }

        // in dev env use root user's navi - TODO: ei tuotantoon, tämä konffattavaksi jonkun dev-vivun taakse?
        else {
            System.err.println("WARNING - temp using root user's navi, authentication object was: "+authentication+", casurl: "+casUrl+", targetservice: "+targetService);
            ticket = CasClient.getTicket(casUrl + "/v1/tickets", "ophadmin", "ilonkautta", targetService+targetResource);
        }

        // call rest resource with proxyticket as parameter
        String response = doHttpCall(targetService + targetResource + "?ticket=" + ticket);

        // cache the html
        request.getSession().setAttribute(CACHEKEY, response);

        return response;
    }

    private String doHttpCall(String urlString) throws IOException {
        HttpClient client = new HttpClient();
        GetMethod get = new GetMethod(urlString);
        client.executeMethod(get);
        return get.getResponseBodyAsString();
    }

}
