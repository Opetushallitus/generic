package fi.vm.sade.security;

import java.io.InputStream;

import org.apache.cxf.helpers.IOUtils;
import org.apache.cxf.io.CachedOutputStream;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.protocol.HttpContext;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.security.core.context.SecurityContextHolder;

import fi.vm.sade.authentication.cas.CasFriendlyCache;
import fi.vm.sade.authentication.cas.CasFriendlyHttpClient;
import fi.vm.sade.generic.rest.HttpTestResource;
import fi.vm.sade.generic.rest.JettyJersey;
import fi.vm.sade.generic.rest.TestParams;

public class CasFriendlyHttpClientTest {

    String unprotectedTargetUrl = "/casfriendly/unprotected";
    String protectedTargetUrl = "/casfriendly/protected";
    String login = "whatever";
    String password = "whatever";

    @BeforeClass
    public static void setUpClass() {
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
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testUnauthenticatedNotProtectedRequestGet() {
        HttpGet request = null;
        try {
            CasFriendlyHttpClient client = new CasFriendlyHttpClient();
            request = new HttpGet(getUrl(unprotectedTargetUrl));
            HttpResponse response = client.execute(request);
            if(response.getEntity() != null) {
                InputStream is = response.getEntity().getContent();
                CachedOutputStream bos = new CachedOutputStream();
                IOUtils.copy(is,bos);
                bos.flush();
                bos.close();
                Assert.assertTrue(bos.size() > 0);
            }
            Assert.assertTrue("Response not 200 (as expected), but " + response.getStatusLine().getStatusCode(), response.getStatusLine().getStatusCode() == 200);
        } catch(Exception ex) {
            ex.printStackTrace();
            Assert.assertTrue(false);
        } finally {
            request.releaseConnection();
        }
    }

    @Test
    public void testUnauthenticatedProtectedRequestGet() {
        HttpGet request = null;
        try {
            CasFriendlyHttpClient client = new CasFriendlyHttpClient();
            request = new HttpGet(getUrl(protectedTargetUrl));
            HttpContext context = client.createHttpContext(null, null, null);
            HttpResponse response = client.execute(request, context);
            if(response.getEntity() != null) {
                InputStream is = response.getEntity().getContent();
                CachedOutputStream bos = new CachedOutputStream();
                IOUtils.copy(is,bos);
                bos.flush();
                bos.close();
                Assert.assertTrue(bos.size() > 0);
            }
            Assert.assertTrue("Response not error (as expected), but " + response.getStatusLine().getStatusCode(), response.getStatusLine().getStatusCode() != 200);
        } catch(Exception ex) {
            ex.printStackTrace();
            Assert.assertTrue(false);
        } finally {
            request.releaseConnection();
        }
    }

    @Test
    public void testAuthenticatedProtectedRequestGet() {
        HttpGet request = null;
        try {
            CasFriendlyCache cache = new CasFriendlyCache(1, "testcache");
            CasFriendlyHttpClient client = new CasFriendlyHttpClient();
            HttpContext context = client.createHttpContext(login, password, cache);
            request = new HttpGet(getUrl(protectedTargetUrl));
            HttpResponse response = client.execute(request, context);
            if(response.getEntity() != null) {
                InputStream is = response.getEntity().getContent();
                CachedOutputStream bos = new CachedOutputStream();
                IOUtils.copy(is,bos);
                bos.flush();
                bos.close();
                Assert.assertTrue(bos.size() > 0);
            } else {
                Assert.assertTrue("No response body.", false);
            }
            Assert.assertTrue("Response not 200 (OK), but " + response.getStatusLine().getStatusCode(), response.getStatusLine().getStatusCode() == 200);
        } catch(Exception ex) {
            ex.printStackTrace();
            Assert.assertTrue(false);
        } finally {
            request.releaseConnection();
        }
    }

    @Test
    public void testAuthenticatedSessionRequestGet() {
        HttpGet request = null;
        try {
            CasFriendlyCache cache = new CasFriendlyCache(1, "testcache");
            CasFriendlyHttpClient client = new CasFriendlyHttpClient();
            HttpContext context = client.createHttpContext(login, password, cache);
            request = new HttpGet(getUrl(protectedTargetUrl));
            HttpResponse response = client.execute(request, context);
            if(response.getEntity() != null) {
                InputStream is = response.getEntity().getContent();
                CachedOutputStream bos = new CachedOutputStream();
                IOUtils.copy(is,bos);
                bos.flush();
                bos.close();
                Assert.assertTrue(bos.size() > 0);
            } else {
                Assert.assertTrue("No response body.", false);
            }
            Assert.assertTrue("Response not 200 (OK), but " + response.getStatusLine().getStatusCode(), response.getStatusLine().getStatusCode() == 200);
            
            // Reconstruct client
            client = new CasFriendlyHttpClient();
            request = new HttpGet(getUrl(protectedTargetUrl));
            // Use existing context
            response = client.execute(request, context);
            if(response.getEntity() != null) {
                InputStream is = response.getEntity().getContent();
                CachedOutputStream bos = new CachedOutputStream();
                IOUtils.copy(is,bos);
                bos.flush();
                bos.close();
                Assert.assertTrue(bos.size() > 0);
            } else {
                Assert.assertTrue("No response body.", false);
            }
            Assert.assertTrue("Response not 200 (OK), but " + response.getStatusLine().getStatusCode(), response.getStatusLine().getStatusCode() == 200);
        } catch(Exception ex) {
            ex.printStackTrace();
            Assert.assertTrue(false);
        } finally {
            request.releaseConnection();
        }
    }

    protected String getUrl(String url) {
        return JettyJersey.getUrl(url);
    }

}