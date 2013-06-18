package fi.vm.sade.generic.rest;

import junit.framework.Assert;
import org.apache.cxf.helpers.IOUtils;
import org.apache.http.client.cache.CacheResponseStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.xml.datatype.XMLGregorianCalendar;
import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * @author Antti Salonen
 */
public class CachingRestClientTest {

    CachingRestClient client = new CachingRestClient();
    private int port = 6789;

    @Test
    public void testXmlGregorianCalendarParsing() throws Exception {
        Calendar now = new GregorianCalendar();
        assertDay(now, client.get("http://localhost:" + port + "/httptest/xmlgregoriancalendar1", XMLGregorianCalendar.class));
        assertDay(now, client.get("http://localhost:" + port + "/httptest/xmlgregoriancalendar2", XMLGregorianCalendar.class));
    }

    private void assertDay(Calendar now, XMLGregorianCalendar xmlGregorianCalendar) {
        System.out.println("CachingRestClientTest.assertDay, now: "+now+", xmlGregCal: "+xmlGregorianCalendar);
        Assert.assertEquals(now.get(Calendar.YEAR), xmlGregorianCalendar.toGregorianCalendar().get(Calendar.YEAR));
        Assert.assertEquals(now.get(Calendar.MONTH), xmlGregorianCalendar.toGregorianCalendar().get(Calendar.MONTH));
        Assert.assertEquals(now.get(Calendar.DAY_OF_MONTH), xmlGregorianCalendar.toGregorianCalendar().get(Calendar.DAY_OF_MONTH));
    }

    @Test
    public void testCachingWithCommonsHttpClientAndJersey() throws Exception {
        // lue resurssi, jossa cache 1 sek
        Assert.assertEquals("pong 1", get("/httptest/pingCached1sec"));

        // lue resurssi uudestaan, assertoi että tuli cachesta, eikä serveriltä asti
        Assert.assertEquals("pong 1", get("/httptest/pingCached1sec"));

        // odota 1 sek
        Thread.sleep(2000);

        // lue resurssi uudestaan, assertoi että haettiin serveriltä koska cache vanheni
        Assert.assertEquals("pong 2", get("/httptest/pingCached1sec"));
    }

    @Test
    public void testResourceMirroringUsingEtag() throws Exception {
        // luetaan resurssi
        Assert.assertEquals("original value 1", get("/httptest/someResource"));
        Assert.assertEquals(client.getCacheStatus(), CacheResponseStatus.CACHE_MISS);

        // tehdään muutos serverin resurssiin
        HttpTestResource.someResource = "changed value";

        // luetaan resurssi, assertoi että tulee cachesta vielä (koska expires)
        Assert.assertEquals("original value 1", get("/httptest/someResource"));
        Assert.assertEquals(client.getCacheStatus(), CacheResponseStatus.CACHE_HIT);

        // odotetaan että expires menee ohi
        Thread.sleep(2000);

        // luetaan resurssi, assertoi että tulee serveriltä, koska muuttunut etag JA expires aika mennyt
        Assert.assertEquals("changed value 2", get("/httptest/someResource"));
        Assert.assertEquals(client.getCacheStatus(), CacheResponseStatus.VALIDATED);

        // odotetaan että expires menee ohi
        Thread.sleep(2000);

        // luetaan resurssi, assertoi että tulee cachesta vaikka käy serverillä (serveri palauttaa unmodified, eikä nosta counteria, koska etag sama)
        Assert.assertEquals("changed value 2", get("/httptest/someResource"));
        Assert.assertEquals(client.getCacheStatus(), CacheResponseStatus.VALIDATED);

        // vielä assertoidaan että unmodified -responsen jälkeen expires toimii kuten pitää eli ei käydä serverillä vaan tulee cache_hit
        Assert.assertEquals("changed value 2", get("/httptest/someResource"));
        Assert.assertEquals(client.getCacheStatus(), CacheResponseStatus.CACHE_HIT);
    }

    // todo: spring restTemplate test

    /* todo: cxf client ei cacheta, saisko cachettamaan?
    @Test
    public void testWithCxfClient() throws Exception {
        List<Object> providers = new ArrayList<Object>();
//        providers.add( new JacksonJaxbJsonProvider() ); http://fandry.blogspot.fi/2012/06/how-to-create-simple-cxf-based-jax-rs.html
        WebClient client = WebClient.create("http://localhost:"+JettyJersey.port+"/httptest", providers).accept("text/plain").type("text/plain").path("/pingCached1sec");
        System.out.println(client.get(String.class));
        System.out.println(client.get(String.class));
    }
    */

    @Before
    public void start() throws Exception {
        JettyJersey.startServer(port, "fi.vm.sade.generic.rest", null);
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
