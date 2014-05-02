package fi.vm.sade.security;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import fi.vm.sade.authentication.cas.CasFriendlyCache;

public class CasFriendlyCacheTest {

	CasFriendlyCache cache = null;
	
    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    	// Default TTL
    	cache = new CasFriendlyCache();
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testMain() throws Exception {
        Assert.assertTrue(true);
    }

    @Test
    public void testAddToCache() {
    	String callerService = "any";
    	String targetUrl = "https://localhost:8443/organisaatio-ui";
    	String userName = "test";
    	String id = "testId";
    	this.cache.setSessionId(callerService, targetUrl, userName, id);
    	String tId = this.cache.getSessionId(callerService, targetUrl, userName);
    	Assert.assertEquals(id, tId);    	
    }
    
    @Test
    public void testRemoveFromCache() {
    	String callerService = "any";
    	String targetUrl = "https://localhost:8443/organisaatio-ui";
    	String userName = "test";
    	String id = "testId";
    	this.cache.setSessionId(callerService, targetUrl, userName, id);
    	String tId = this.cache.getSessionId(callerService, targetUrl, userName);
    	Assert.assertEquals(id, tId);
    	this.cache.removeSessionId(callerService, targetUrl, userName);
    	tId = this.cache.getSessionId(callerService, targetUrl, userName);
    	Assert.assertNull(tId);
    }
    
    @Test
    public void testExpiration() {
    	CasFriendlyCache shortCache = new CasFriendlyCache(1);
    	String callerService = "any";
    	String targetUrl = "https://localhost:8443/organisaatio-ui";
    	String userName = "test";
    	String id = "testId";
    	shortCache.setSessionId(callerService, targetUrl, userName, id);
    	try { 
    		Thread.sleep(2000);
    	} catch(Exception ex) {
    		ex.printStackTrace();
    	}
    	String tId = shortCache.getSessionId(callerService, targetUrl, userName);
    	Assert.assertNull(tId);
    }
    
}
