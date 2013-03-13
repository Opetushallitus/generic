package fi.vm.sade.authentication.cas;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.phase.Phase;

import java.net.HttpURLConnection;

/**
 * Interceptor for outgoing SOAP calls that uses "application-as-a-user" pattern: authenticates against CAS REST API to get a service ticket.
 *
 * @author Antti Salonen
 */
public class CasApplicationAsAUserInterceptor extends AbstractSoapInterceptor {

    private String webCasUrl;
    private String targetService;
    private String appClientUsername;
    private String appClientPassword;

    public CasApplicationAsAUserInterceptor() {
        super(Phase.PRE_PROTOCOL);
    }

    @Override
    public void handleMessage(SoapMessage message) throws Fault {
        // authenticate against CAS REST API, and get a service ticket
        String serviceTicket = CasClient.getTicket(webCasUrl + "/v1/tickets", appClientUsername, appClientPassword, targetService);

        // put service ticket to SOAP message as a http header 'CasSecurityTicket'
        ((HttpURLConnection)message.get("http.connection")).setRequestProperty("CasSecurityTicket", serviceTicket);

        System.out.println("CasApplicationAsAUserInterceptor.handleMessage added CasSecurityTicket="+serviceTicket+" -header");
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
}
