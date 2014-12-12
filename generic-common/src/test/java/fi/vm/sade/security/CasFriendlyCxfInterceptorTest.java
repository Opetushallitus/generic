package fi.vm.sade.security;

import java.io.InputStream;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.cxf.helpers.IOUtils;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.jaxrs.ext.form.Form;
import org.apache.cxf.message.Message;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.security.core.context.SecurityContextHolder;

import fi.vm.sade.authentication.cas.CasFriendlyCache;
import fi.vm.sade.authentication.cas.CasFriendlyCxfInterceptor;
import fi.vm.sade.generic.rest.HttpTestResource;
import fi.vm.sade.generic.rest.JettyJersey;
import fi.vm.sade.generic.rest.TestParams;

public class CasFriendlyCxfInterceptorTest {

    String unprotectedTargetUrl = "/casfriendly/unprotected";
    String protectedTargetUrl = "/casfriendly/protected";
    String login = "whatever";
    String password = "whatever";
    String wrongLogin = "deny";
    static CasFriendlyCache cache = null;

    @BeforeClass
    public static void setUpClass() {
        cache = new CasFriendlyCache(3600, "CasFriendlyCxfInterceptorTest");
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() throws Exception {
        JettyJersey.startServer("fi.vm.sade.generic.rest", null);
        TestParams.instance = new TestParams();
        HttpTestResource.counter = 1;
        HttpTestResource.someResource = "original value";
        SecurityContextHolder.clearContext();
        cache.clearAll();
    }

    @After
    public void tearDown() {
    }

    /**
     * PALVELUTUNNUKSELLA
     *  CASE:
     *  - ei olemassa olevaa sessiota, 
     *  - sessionRequired,
     *  - vaatii kirjautumista
     */
    @Test
    public void testProtectedWithLoginNoSessionRequestGet() {
        try {
            CasFriendlyCxfInterceptor<Message> interceptor = this.createInterceptor(
                    login, password, true, true, false);
            WebClient cxfClient = createClient(protectedTargetUrl, interceptor);
            String response = IOUtils.toString((InputStream) cxfClient.get().getEntity());
            Assert.assertTrue("Response should be: ok 1, but is: " + response, response.equals("ok 1"));
            Assert.assertTrue("Session count should be 1, but is: " + interceptor.getCache().getSize(), interceptor.getCache().getSize() == 1);
        } catch(Exception ex) {
            ex.printStackTrace();
            Assert.assertTrue(false);
        }
    }
    
    /**
     * PALVELUTUNNUKSELLA POST
     *  CASE:
     *  - ei olemassa olevaa sessiota, 
     *  - sessionRequired,
     *  - vaatii kirjautumista
     */
    @Test
    public void testProtectedWithLoginNoSessionRequestPost() {
        try {
            CasFriendlyCxfInterceptor<Message> interceptor = this.createInterceptor(
                    login, password, true, true, false);
            WebClient cxfClient = createClient(protectedTargetUrl, interceptor);
            Form form = new Form();
            form.set("TESTNAME", "TESTVALUE");
            Response resp = cxfClient.form(form);
            String response = IOUtils.toString((InputStream) resp.getEntity());
            Assert.assertTrue("Response should be: ok 1, but is: " + response, response.equals("ok 1"));
            Assert.assertTrue("Session count should be 1, but is: " + interceptor.getCache().getSize(), interceptor.getCache().getSize() == 1);
        } catch(Exception ex) {
            ex.printStackTrace();
            Assert.assertTrue(false);
        }
    }

    /**
     * PALVELUTUNNUKSELLA
     *  CASE:
     *  - ei olemassa olevaa sessiota, 
     *  - sessionRequired,
     *  - vaatii kirjautumista,
     *  - väärä tunnus/salasana
     */
    @Test
    public void testProtectedWithIncorrectLoginNoSessionRequestGet() {
        try {
            CasFriendlyCxfInterceptor<Message> interceptor = this.createInterceptor(
                    wrongLogin, password, true, true, false);
            WebClient cxfClient = createClient(protectedTargetUrl, interceptor);
            cxfClient.get();
            Assert.assertTrue("Response status must be <> 200, got: " + cxfClient.getResponse().getStatus(), cxfClient.getResponse().getStatus() != 200);
            Assert.assertTrue("Session count should be 0, but is: " + interceptor.getCache().getSize(), interceptor.getCache().getSize() == 0);
        } catch(Exception ex) {
            ex.printStackTrace();
            Assert.assertTrue(false);
        }
    }
    
    /**
     * PALVELUTUNNUKSELLA
     *  CASE:
     *  - ei olemassa olevaa sessiota, 
     *  - sessionRequired,
     *  - ei vaadi kirjautumista
     */
    @Test
    public void testUnprotectedWithSessionRequiredRequestGet() {
        try {
            CasFriendlyCxfInterceptor<Message> interceptor = this.createInterceptor(
                    login, password, true, true, false);
            WebClient cxfClient = createClient(unprotectedTargetUrl, interceptor);
            String response = IOUtils.toString((InputStream) cxfClient.get().getEntity());
            Assert.assertTrue("Response should be: ok 1, but is: " + response, response.equals("ok 1"));
            Assert.assertTrue("Session count should be 1, but is: " + interceptor.getCache().getSize(), interceptor.getCache().getSize() == 1);
        } catch(Exception ex) {
            ex.printStackTrace();
            Assert.assertTrue(false);
        }
    }

    /**
     * EI PALVELUTUNNUSTA
     *  CASE:
     *  - ei olemassa olevaa sessiota, 
     *  - sessionRequired,
     *  - vaatii kirjautumista
     */
    @Test
    public void testProtectedWithoutLoginSessionRequiredRequestGet() {
        try {
            CasFriendlyCxfInterceptor<Message> interceptor = this.createInterceptor(
                    null, null, true, true, false);
            WebClient cxfClient = createClient(protectedTargetUrl, interceptor);
            cxfClient.get();
            Assert.assertTrue("Response status must be <> 200, got: " + cxfClient.getResponse().getStatus(), cxfClient.getResponse().getStatus() != 200);
            Assert.assertTrue("Session count should be 0, but is: " + interceptor.getCache().getSize(), interceptor.getCache().getSize() == 0);
        } catch(Exception ex) {
            ex.printStackTrace();
            Assert.assertTrue(false);
        }
    }
    
    /**
     * EI PALVELUTUNNUSTA
     *  CASE:
     *  - ei olemassa olevaa sessiota, 
     *  - sessionRequired,
     *  - ei vaadi kirjautumista
     */
    @Test
    public void testUnprotectedWithoutLoginSessionRequiredRequestGet() {
        try {
            CasFriendlyCxfInterceptor<Message> interceptor = this.createInterceptor(
                    null, null, true, true, false);
            WebClient cxfClient = createClient(unprotectedTargetUrl, interceptor);
            cxfClient.get();
            Assert.assertTrue("Response status must be 200, got: " + cxfClient.getResponse().getStatus(), cxfClient.getResponse().getStatus() == 200);
            Assert.assertTrue("Session count should be 0, but is: " + interceptor.getCache().getSize(), interceptor.getCache().getSize() == 0);
        } catch(Exception ex) {
            ex.printStackTrace();
            Assert.assertTrue(false);
        }
    }

    /**
     * PALVELUTUNNUKSELLA
     *  CASE:
     *  - ei olemassa olevaa sessiota, 
     *  - no sessionRequired,
     *  - ei vaadi kirjautumista
     */
    @Test
    public void testUnprotectedWithNoSessionRequiredRequestGet() {
        try {
            CasFriendlyCxfInterceptor<Message> interceptor = this.createInterceptor(
                    login, password, false, true, false);
            WebClient cxfClient = createClient(unprotectedTargetUrl, interceptor);
            String response = IOUtils.toString((InputStream) cxfClient.get().getEntity());
            Assert.assertTrue("Response should be: ok 1, but is: " + response, response.equals("ok 1"));
            Assert.assertTrue("Session count should be 0, but is: " + interceptor.getCache().getSize(), interceptor.getCache().getSize() == 0);
        } catch(Exception ex) {
            ex.printStackTrace();
            Assert.assertTrue(false);
        }
    }

    /**
     * PALVELUTUNNUKSELLA POST
     *  CASE:
     *  - ei olemassa olevaa sessiota, 
     *  - no sessionRequired,
     *  - vaatii kirjautumista
     */
    @Test
    public void testProtectedWithNoSessionRequiredRequestPost() {
        try {
            CasFriendlyCxfInterceptor<Message> interceptor = this.createInterceptor(
                    login, password, false, true, false);
            WebClient cxfClient = createClient(protectedTargetUrl, interceptor);
            Form form = new Form();
            form.set("TESTNAME", "TESTVALUE");
            Response resp = cxfClient.form(form);
            String response = IOUtils.toString((InputStream) resp.getEntity());
            Assert.assertTrue("Response should be: ok 1, but is: " + response, response.equals("ok 1"));
            Assert.assertTrue("Session count should be 1, but is: " + interceptor.getCache().getSize(), interceptor.getCache().getSize() == 1);
        } catch(Exception ex) {
            ex.printStackTrace();
            Assert.assertTrue(false);
        }
    }

    private WebClient createClient(String url, CasFriendlyCxfInterceptor<Message> interceptor) {
        String testCaseId = interceptor.toString();
        WebClient c = WebClient.create(getUrl(url)).accept(MediaType.TEXT_PLAIN, MediaType.TEXT_HTML, MediaType.APPLICATION_JSON).header("Testcase-Id", testCaseId);
        WebClient.getConfig(c).getOutInterceptors().add(interceptor);
        WebClient.getConfig(c).getInInterceptors().add(interceptor);
        return c;
    }
    
    private CasFriendlyCxfInterceptor<Message> createInterceptor(
            String login, String password, boolean sessionRequired, 
            boolean useSessionPerUser, boolean useBlockingConcurrent) {
        CasFriendlyCxfInterceptor<Message> interceptor = new CasFriendlyCxfInterceptor<Message>();
        interceptor.setCache(cache);
        interceptor.setAppClientUsername(login);
        interceptor.setAppClientPassword(password);
        interceptor.setSessionRequired(sessionRequired);
        interceptor.setCallerService("CasFriendlyCxfInterceptorTest");
        interceptor.setUseSessionPerUser(useSessionPerUser);
        interceptor.setUseBlockingConcurrent(useBlockingConcurrent);

        return interceptor;
    }
    
    public static String getUrl(String url) {
        return JettyJersey.getUrl(url);
    }

}