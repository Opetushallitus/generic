package fi.vm.sade.generic.rest;

import fi.vm.sade.generic.ui.portlet.security.ProxyAuthenticator;
import junit.framework.Assert;
import org.apache.cxf.helpers.IOUtils;
import org.apache.http.client.cache.CacheResponseStatus;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.xml.datatype.XMLGregorianCalendar;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * @author Antti Salonen
 */
public class CachingRestClientTest extends RestWithCasTestSupport {

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

    @Test(expected = IOException.class)
    public void testErrorStatus() throws IOException {
        get("/httptest/status500");
    }

    @Test
    public void testAuthenticationWithGetRedirect() throws Exception {
        initClientAuthentication();

        // alustava pyyntö -> CachingRestClient hankkii tiketin kutsua ennen, kutsu menee ok:sti
        Assert.assertEquals("pong 1", get("/httptest/pingSecuredRedirect/asd1")); // asd? tarvitaan koska muuten apache http saattaa tulkita circular redirectiksi..
        assertCas(0, 1, 1, 1, 1);

        // autentikoiduttu casiin, mutta ei kohdepalveluun vielä, joten kutsun suojattuun resurssiin pitäisi redirectoitua casiin
        TestParams.instance.userIsAlreadyAuthenticatedToCas = "asdsad";
        TestParams.instance.failNextBackendAuthentication = true;

        // lue suojattu resurssi -> välillä käydään cassilla, joka ohjaa takaisin ticketin kanssa (koska ollaan jo casissa sisällä)
        Assert.assertEquals("pong 2", get("/httptest/pingSecuredRedirect/asd2")); // asd? tarvitaan koska muuten apache http saattaa tulkita circular redirectiksi..
        assertCas(1, 1, 1, 3, 2);

        // kutsu uudestaan -> ei redirectiä koska nyt serviceenkin ollaan autentikoiduttu, ainoastaan request autentikoidaan backendissä
        Assert.assertEquals("pong 3", get("/httptest/pingSecuredRedirect/asd3"));
        assertCas(1, 1, 1, 4, 2);

        // invalidoi tiketti, cas sessio edelleen ok (simuloi ticket cachen tyhjäytymistä serverillä) -> redirectit resource->cas->resource tapahtuu uusiksi
        TestParams.instance.failNextBackendAuthentication = true;
        Assert.assertEquals("pong 4", get("/httptest/pingSecuredRedirect/asd4"));
        assertCas(2, 1, 1, 6, 3);

        // invalidoi tiketti ja cas sessio (simuloi cas/backend restarttia)
        // -> resurssi redirectoi cassille, mutta cas ei ohjaa takaisin koska ei olla sisällä casissa
        // -> CachingRestClient havaitsee puuttuvan authin, ja osaa hakea uuden tiketin, ja tehdä pyynnön uusiksi
        // -> redirectejä ei tämän jälkeen tapahdu, mutta tgt+ticket luodaan casiin, ja validoidaan backend resurssilla
        TestParams.instance.failNextBackendAuthentication = true;
        TestParams.instance.userIsAlreadyAuthenticatedToCas = null;
        Assert.assertEquals("pong 5", get("/httptest/pingSecuredRedirect/asd5"));
        assertCas(2, 2, 2, 8, 4);
    }

    @Test
    @Ignore // ei oikeastaan halutakaan tukea postien cas redirectointia, aina ennen postia pitää tehdä get!
    public void testAuthenticationWithPostRedirect() throws Exception {
        initClientAuthentication();

        // alustava pyyntö -> CachingRestClient hankkii tiketin kutsua ennen, kutsu menee ok:sti
        Assert.assertEquals("pong 1", post("/httptest/pingSecuredRedirect/asd1", "post content")); // asd? tarvitaan koska muuten apache http saattaa tulkita circular redirectiksi..
        assertCas(0, 1, 1, 1, 1);

        // autentikoiduttu casiin, mutta ei kohdepalveluun vielä, joten kutsun suojattuun resurssiin pitäisi redirectoitua casiin
        TestParams.instance.userIsAlreadyAuthenticatedToCas = "asdsad";
        TestParams.instance.failNextBackendAuthentication = true;

        // lue suojattu resurssi -> välillä käydään cassilla, joka ohjaa takaisin ticketin kanssa (koska ollaan jo casissa sisällä)
        Assert.assertEquals("pong 2", post("/httptest/pingSecuredRedirect/asd2", "post content")); // asd? tarvitaan koska muuten apache http saattaa tulkita circular redirectiksi..
        //assertCas(1, 1, 1, 3, 2); - note! ei tapahdu redirectiä, ei oikeastaan halutakaan tukea postien cas redirectointia, aina ennen postia pitää tehdä get!
    }

    @Test
    public void testAuthenticationWith401Unauthorized() throws Exception {
        initClientAuthentication();

        // lue suojattu resurssi joka palauttaisi 401 unauthorized, mikäli ei oltaisi autentikoiduttu -> client kuitenkin on yllä konffattu käyttämään palvelutunnuksia
        Assert.assertEquals("pong 1", get("/httptest/pingSecured401Unauthorized"));
        assertCas(0,1,1,1,1);

        // invalidoi serveripään tiketti -> seur kutsussa resurssi palauttaa 401, jonka jälkeen restclient osaa hakea uuden tiketin ja koittaa pyyntöä uusiksi
        TestParams.instance.failNextBackendAuthentication = true;
        Assert.assertEquals("pong 2", get("/httptest/pingSecured401Unauthorized"));
        assertCas(0,2,2,3,2);
    }

    @Test
    public void testProxyAuthentication() throws Exception {
        // prepare & mock stuff
        final String user = "uiasdhjsadhu";
        final int[] proxyTicketCounter = {0};
        List<GrantedAuthority> roles = Arrays.asList((GrantedAuthority)new SimpleGrantedAuthority("testrole"));
        TestingAuthenticationToken clientAuth = new TestingAuthenticationToken(user, user, roles);
        SecurityContextHolder.getContext().setAuthentication(clientAuth);
        client.setCasService(getUrl("/mock_cas/cas"));
        client.setUseProxyAuthentication(true);
        client.setProxyAuthenticator(new ProxyAuthenticator() {
            @Override
            protected String obtainNewCasProxyTicket(String casTargetService, Authentication casAuthenticationToken) {
                return user + "_" + (++proxyTicketCounter[0]);
            }
        });

        // lue suojattu resurssi joka palauttaisi muuten 401 unauthorized, mutta client hoitaa autentikoinnin sisäisesti ja kutsuu clientuserina
        Assert.assertEquals("pong 1", get("/httptest/pingSecured401Unauthorized"));
        Assert.assertEquals(1, proxyTicketCounter[0]);
        assertCas(0,0,0,1,1); // redir ei tehtä, tikettejä ei luoda koska client laittaa mukaan proxytiketin, tiketin validointi tehty serverillä kerran ok

        // invalidoi tiketti serveripäässä (esim restarttaa cas tai kohdepalvelu välissä), ja yritä uudestaan -> client pitäisi hankkia uuusi proxy ticket
        TestParams.instance.failNextBackendAuthentication = true;
        Assert.assertEquals("pong 2", get("/httptest/pingSecured401Unauthorized"));
        Assert.assertEquals(2, proxyTicketCounter[0]);
        assertCas(0,0,0,3,2);

        // invalidoi tiketti clientilla -> client pitäisi hankkia uuusi proxy ticket
        client.getProxyAuthenticator().clearTicket(getUrl("/httptest"));
        Assert.assertEquals("pong 3", get("/httptest/pingSecured401Unauthorized"));
        Assert.assertEquals(3, proxyTicketCounter[0]);
        assertCas(0,0,0,4,2); // todo: wtf onnistuneita validointeja serverillä pitäisi olla +1 ???
    }

    private void initClientAuthentication() {
        client.setCasService(getUrl("/httptest"));
        client.setWebCasUrl(getUrl("/mock_cas/cas"));
        client.setUsername("test");
        client.setPassword("test");
    }

    private String get(String url) throws IOException {
        return IOUtils.toString(client.get(getUrl(url)));
    }

    private String post(String url, String postContent) throws IOException {
        return IOUtils.toString(client.post(getUrl(url), "application/json", postContent).getEntity().getContent());
    }

}
