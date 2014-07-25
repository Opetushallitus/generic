package fi.vm.sade.security;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.message.Message;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.mortbay.log.Log;

import fi.vm.sade.authentication.cas.CasFriendlyCache;
import fi.vm.sade.authentication.cas.CasFriendlyCxfInterceptor;
import fi.vm.sade.authentication.cas.CasFriendlyHttpClient;

/**
 * JUST FOR TEMPORARY LOCAL TESTING!!
 */
public class CasFriendlyHttpClientTest {

    private String serverPrefix = "https://itest-virkailija.oph.ware.fi";
    //	private String serverPrefix = "http://localhost:8080";

    private String casUrl = serverPrefix + "/cas/v1/tickets";
    private String login = System.getProperty("login");
    private String password = System.getProperty("password");

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    //    @Test
    public void testGetRequestWithCAS() {
        CasFriendlyHttpClient client = new CasFriendlyHttpClient();
        HttpGet request = new HttpGet("https://localhost:8443/organisaatio-ui/test.jsp?test1=test1value&test2=test2value");
        try {
            HttpResponse response = client.execute(request);
            System.out.println("FINAL: " + response.getStatusLine());
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //    @Test
    public void testPostRequestWithCAS() {
        CasFriendlyHttpClient client = new CasFriendlyHttpClient();
        HttpPost request = new HttpPost("https://localhost:8443/organisaatio-ui/test.jsp");
        try {
            ArrayList<BasicNameValuePair> postParameters = new ArrayList<BasicNameValuePair>();
            postParameters.add(new BasicNameValuePair("test1", "test1value"));
            postParameters.add(new BasicNameValuePair("test2", "test2value"));
            request.setEntity(new UrlEncodedFormEntity(postParameters));
            HttpResponse response = client.execute(request);
            System.out.println("FINAL: " + response.getStatusLine());
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //    @Test
    public void testSessionRequestWithCAS() {
        CasFriendlyHttpClient client = new CasFriendlyHttpClient();
        HttpPost request1 = new HttpPost("https://localhost:8443/organisaatio-ui/test.jsp");
        //    	String tgt = "TGT-12-ekc97qVr2w5T9Irzf9EaqaO4io62BTV0fXyvBTgUDEWuA45Uha-cas.oph.fi";

        CasFriendlyCache tempCache = new CasFriendlyCache();

        HttpContext context = client.createHttpContext(login, password, tempCache);

        //		context.setAttribute(CasRedirectStrategy.ATTRIBUTE_CAS_TGT, tgt);

        //    	context.setAttribute(CasRedirectStrategy.ATTRIBUTE_PROXYGRANTINGTICKET, proxyGrantingTicket);
        HttpGet request2 = new HttpGet("https://localhost:8443/organisaatio-ui/configuration/configuration.js");
        try {
            ArrayList<BasicNameValuePair> postParameters = new ArrayList<BasicNameValuePair>();
            postParameters.add(new BasicNameValuePair("test1", "test1value"));
            postParameters.add(new BasicNameValuePair("test2", "test2value"));
            request1.setEntity(new UrlEncodedFormEntity(postParameters));
            //    		request2.setEntity(new UrlEncodedFormEntity(postParameters));
            // Execute once with context (context has PGT)
            HttpResponse response = client.execute(request1, context);
            System.out.println("FINAL: " + response.getStatusLine());
            request1.releaseConnection();

            for(Cookie one:client.getCookieStore().getCookies())
                Log.debug(one.getName() + ": " + one.getValue());

            // Do twice
            HttpResponse response2 = client.execute(request2);
            System.out.println("FINAL: " + response2.getStatusLine());
            request2.releaseConnection();

            for(Cookie one:client.getCookieStore().getCookies())
                Log.debug(one.getName() + ": " + one.getValue());

        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    //    @Test
    public void testCasFriendlyCxfMessage() {

        // Temporary cache for testing
        CasFriendlyCache cache = new CasFriendlyCache(10, "temp");
        //        cache.setSessionId("any", "https://itest-virkailija.oph.ware.fi/authentication-service/", login, "FD8C93D0767E4CDA1F061998C8974C1F");

        WebClient client = createClient(cache, "https://itest-virkailija.oph.ware.fi/authentication-service/resources/omattiedot", login, password);

        // Call
        String body = client.get(String.class);

        System.out.println("BODY: " + body);

        int status = client.getResponse().getStatus();
        System.out.println("STATUS: " + client.getResponse().getStatus());

        System.out.println(client.getResponse().getMetadata());

        Assert.assertEquals(HttpStatus.SC_OK, status);

        // AGAIN

        WebClient client2 = createClient(cache, "https://itest-virkailija.oph.ware.fi/authentication-service/resources/omattiedot", login, password);

        // Call
        body = client2.get(String.class);

        System.out.println("BODY: " + body);

        status = client.getResponse().getStatus();
        System.out.println("STATUS: " + client.getResponse().getStatus());

        System.out.println(client.getResponse().getMetadata());

        Assert.assertEquals(HttpStatus.SC_OK, status);

    }

    private WebClient createClient(CasFriendlyCache cache, String url, String login, String password) {
        //    	url = JettyJersey.getUrl(url);
        WebClient c = WebClient.create(url)
                .accept("*/*");
        //        		.accept(MediaType.TEXT_PLAIN, MediaType.TEXT_HTML, MediaType.APPLICATION_JSON)
        //        		.cookie(new javax.ws.rs.core.Cookie("CLIENTCOOKIE", "asdasd"));
        //        SessionBasedCxfAuthInterceptor authInterceptor = new SessionBasedCxfAuthInterceptor(blockingAuthCookieCache, "user1", "pass1");
        CasFriendlyCxfInterceptor<Message> authInterceptor = new CasFriendlyCxfInterceptor<Message>();
        authInterceptor.setLogin(login);
        authInterceptor.setPassword(password);
        authInterceptor.setCache(cache);
        WebClient.getConfig(c).getOutInterceptors().add(authInterceptor);
        WebClient.getConfig(c).getInInterceptors().add(authInterceptor);
        return c;
    }

    //    @Test
    //    public void testJersey() {
    //    	ClientConfig config = new DefaultClientConfig();
    //        Client client = Client.create(config);
    //        String testUrl = "https://localhost:8443/cas/me";
    //        WebResource service = client.resource(testUrl);
    //        System.out.println(service.path("rest").accept(MediaType.APPLICATION_JSON).get(String.class));
    //    }
}
