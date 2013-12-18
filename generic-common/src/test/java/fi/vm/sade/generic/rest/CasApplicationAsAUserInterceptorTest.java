package fi.vm.sade.generic.rest;

import fi.vm.sade.authentication.cas.CasApplicationAsAUserInterceptor;
import fi.vm.sade.authentication.cas.DefaultTicketCachePolicy;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.cxf.jaxrs.client.WebClient;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Antti Salonen
 */
public class CasApplicationAsAUserInterceptorTest extends RestWithCasTestSupport {

    @Test
    public void testCasApplicationAsAUserInterceptor() throws Exception {
        // prepare & mock stuff
        CasApplicationAsAUserInterceptor appAsUserInterceptor = new CasApplicationAsAUserInterceptor();
        appAsUserInterceptor.setAppClientUsername("user");
        appAsUserInterceptor.setAppClientPassword("pass");
        appAsUserInterceptor.setTargetService("target");
        appAsUserInterceptor.setWebCasUrl(getUrl("/mock_cas/cas"));
        WebClient c = WebClient.create(getUrl("/httptest/testMethod"));
        WebClient.getConfig(c).getOutInterceptors().add(appAsUserInterceptor);

        // kutsutaan resurssia
        Assert.assertEquals(HttpStatus.SC_OK, c.get().getStatus());

        // assertoidaan: ticket haettu kerran
        assertCas(0, 1, 1, 1, 1);

        // kutsutaan resurssia
        Assert.assertEquals(HttpStatus.SC_OK, c.get().getStatus());

        // assertoidaan: ticket haettu kerran (ei autentikoida uudestaan vaan ticket cachetettu), mutta validoitu kaksi kertaa
        assertCas(0, 1, 1, 2, 2);

        // simuloidaan: cas restart, server ticket cache tyhjäys -> ticket ei enää validi
        TestParams.instance.failNextBackendAuthentication = true;

        // kutsutaan resurssia -> virhe
        Assert.assertEquals(HttpStatus.SC_UNAUTHORIZED, c.get().getStatus());
        assertCas(0, 1, 1, 3, 2); // autentikointi kutsuttiin kerran mutta epäonnistuneesti

        // simuloidaan: käyttäjä joutuu kirjautumaan uudelleen sisään, jonka jälkeen resurssi taas toimii
        DefaultTicketCachePolicy.ticketThreadLocal.remove(); // oikeassa ympäristössä ticket kakutettu käyttäjän http sessioon
        Assert.assertEquals(HttpStatus.SC_OK, c.get().getStatus());

        // assertoidaan: ticket haettu ja validoitu nyt uusiksi
        assertCas(0, 2, 2, 4, 3);
    }

}
