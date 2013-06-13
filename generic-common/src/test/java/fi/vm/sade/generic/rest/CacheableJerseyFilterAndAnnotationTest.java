package fi.vm.sade.generic.rest;

import junit.framework.Assert;
import org.apache.cxf.helpers.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

/**
 * @author Antti Salonen
 */
public class CacheableJerseyFilterAndAnnotationTest {

    CachingRestClient client = new CachingRestClient();
    private int port = 6789;

    @Test
    public void testCacheableJerseyFilterAndAnnotation() throws Exception {
        // lue resurssi, jossa maxAge 2 sek (huomaa että max-agella, age on aluksi 1 sec)
        Assert.assertEquals("cacheable 1", get("/httptest/cacheableAnnotatedResource"));

        // lue resurssi uudestaan, assertoi että tuli cachesta, eikä serveriltä asti
        Assert.assertEquals("cacheable 1", get("/httptest/cacheableAnnotatedResource"));

        // odota 1 sek
        Thread.sleep(2000);

        // lue resurssi uudestaan, assertoi että haettiin serveriltä koska cache vanheni
        Assert.assertEquals("cacheable 2", get("/httptest/cacheableAnnotatedResource"));
    }

    @Before
    public void start() throws Exception {
        JettyJersey.startServer(port, "fi.vm.sade.generic.rest", "fi.vm.sade.generic.rest.CacheableJerseyFilter");
        HttpTestResource.counter = 1;
        HttpTestResource.someResource = "original value";
    }

    @After
    public void stop() throws Exception {
        JettyJersey.stopServer();
    }

    private String get(String url) throws IOException {
        return IOUtils.toString(client.get("http://localhost:"+port+url));
    }

}
