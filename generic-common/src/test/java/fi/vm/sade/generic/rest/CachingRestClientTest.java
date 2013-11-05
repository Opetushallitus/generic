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

    CachingRestClient client = new CachingRestClient(){
        @Override
        protected String obtainNewCasTicket() throws IOException {
            return new CachingRestClient().get(getUrl("/httptest/cas/v1/tickets"), String.class);
        }
    };

    @Test
    public void testXmlGregorianCalendarParsing() throws Exception {
        Calendar now = new GregorianCalendar();
        assertDay(now, client.get(getUrl("/httptest/xmlgregoriancalendar1"), XMLGregorianCalendar.class));
        assertDay(now, client.get(getUrl("/httptest/xmlgregoriancalendar2"), XMLGregorianCalendar.class));
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

    @Test(expected = JsonObjectException.class)
    public void testErrorStatus() throws IOException {
        get("/httptest/status500");
    }

    @Test
    public void testAuthenticationWithRedirect() throws Exception {
        // lue suojattu resurssi joka redirectaa "casiin" - client hoitaa autentikoinnin sisäisesti
        initClientAuthentication();
        Assert.assertEquals("pong 1", get("/httptest/pingSecuredRedirect"));
        Assert.assertEquals(1, HttpTestResource.authenticationCount);

        // invalidoi tiketti tai restarttaa cas tai kohdepalvelu välissä, ja yritä uudestaan - huom oikesti ei tartte tehdä koska tässä ei ole sessioita mutta restclientilla on kyllä tila
        Assert.assertEquals("pong 2", get("/httptest/pingSecuredRedirect"));
        Assert.assertEquals(2, HttpTestResource.authenticationCount);
    }

    @Test
    public void testAuthenticationWithRedirectAndPost() throws Exception {
        // lue suojattu resurssi joka redirectaa "casiin" - client hoitaa autentikoinnin sisäisesti
        initClientAuthentication();
        Assert.assertEquals("pong 1", IOUtils.toString(client.post(getUrl("/httptest/pingSecuredRedirect"), "application/json", "post content").getEntity().getContent()));
        Assert.assertEquals(1, HttpTestResource.authenticationCount);

        // invalidoi tiketti tai restarttaa cas tai kohdepalvelu välissä, ja yritä uudestaan - huom oikesti ei tartte tehdä koska tässä ei ole sessioita mutta restclientilla on kyllä tila
        Assert.assertEquals("pong 2", IOUtils.toString(client.post(getUrl("/httptest/pingSecuredRedirect"), "application/json", "post content").getEntity().getContent()));
        Assert.assertEquals(2, HttpTestResource.authenticationCount);
    }

    @Test
    public void testAuthenticationWith401Unauthorized() throws Exception {
        // lue suojattu resurssi joka palauttaa ensin 401 unauthorized - client hoitaa autentikoinnin sisäisesti
        initClientAuthentication();
        Assert.assertEquals("pong 1", get("/httptest/pingSecured401Unauthorized"));
        Assert.assertEquals(1, HttpTestResource.authenticationCount);

        // invalidoi tiketti tai restarttaa cas tai kohdepalvelu välissä, ja yritä uudestaan - huom oikesti ei tartte tehdä koska tässä ei ole sessioita mutta restclientilla on kyllä tila
        Assert.assertEquals("pong 2", get("/httptest/pingSecured401Unauthorized"));
        Assert.assertEquals(2, HttpTestResource.authenticationCount);
    }

    @Before
    public void start() throws Exception {
        JettyJersey.startServer("fi.vm.sade.generic.rest", null);
        HttpTestResource.counter = 1;
        HttpTestResource.someResource = "original value";
        HttpTestResource.authenticationCount = 0;
    }

    @After
    public void stop() throws Exception {
        JettyJersey.stopServer();
    }

    private void initClientAuthentication() {
        client.setCasService(getUrl("/httptest"));
        client.setWebCasUrl(getUrl("/httptest/cas"));
        client.setUsername("test");
        client.setPassword("test");
    }

//    private void invalidateTicket() {
//        client.ticket = "invalid_"+client.ticket;
//    }

    private String get(String url) throws IOException {
        return IOUtils.toString(client.get(getUrl(url)));
    }

    private String getUrl(String url) {
        return "http://localhost:"+ JettyJersey.getPort()+url;
    }

}
