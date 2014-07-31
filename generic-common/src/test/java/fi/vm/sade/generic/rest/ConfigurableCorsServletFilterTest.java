package fi.vm.sade.generic.rest;


import java.util.Arrays;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.savedrequest.Enumerator;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ContextConfiguration(locations = "classpath:spring/test-cors-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class ConfigurableCorsServletFilterTest {
    
    private static final String DOMAIN1 = "https://test.site.something";

    private static final String DOMAIN2 = "http://ruhtinas.nukettaja.com";

    @Autowired
    private ConfigurableCorsServletFilter filter;
    
    private HttpServletResponse response;
    private HttpServletRequest request;
    private FilterChain chain;
    
    @Before
    public void init() {
        response = Mockito.mock(HttpServletResponse.class);
        request = Mockito.mock(HttpServletRequest.class);
        chain = Mockito.mock(FilterChain.class);
    }
    
    @Test
    public void allowsAccessFromAnywhereInDevelopmentMode() throws Exception {
        String customDomain = "http://somedomainqweqweqwe.com";
        when(request.getHeaders("origin")).thenReturn(new Enumerator<String>(Arrays.asList(customDomain)));
        filter.setMode(CorsFilterMode.DEVELOPMENT.name());
        filter.doFilter(request, response, chain);
        verify(response).addHeader("Access-Control-Allow-Origin", customDomain);
    }
    
    @Test
    public void defaultModeIsProduction() {
        filter.setMode(null);
        assertEquals(CorsFilterMode.PRODUCTION, filter.mode);
    }
    
    @Test
    public void doesNotAllowAccessInProductionModeWhenRequestIsNotInDomainsAllowed() throws Exception {
        assertDomain("http://something.net", null);
    }
    
    @Test
    public void allowsAccessFromAllowedDomainsOnlyInProductionMode() throws Exception {
        assertDomain(DOMAIN1, DOMAIN1);
        assertDomain(DOMAIN2, DOMAIN2);
    }
    
    @Test
    public void copiesHeadersFromRequestToResponse() throws Exception {
        String headerValue = "value";
        when(request.getHeaders("access-control-request-headers")).thenReturn(new Enumerator<String>(Arrays.asList(headerValue)));
        filter.doFilter(request, response, chain);
        verify(response).addHeader("Access-Control-Allow-Headers", headerValue);
    }

    private void assertDomain(String domain, String expected) throws Exception {
        when(request.getHeaders("origin")).thenReturn(new Enumerator<String>(Arrays.asList(domain)));
        filter.setMode(CorsFilterMode.PRODUCTION.name());
        filter.doFilter(request, response, chain);
        if (expected == null) {
            verify(response, never()).addHeader(eq("Access-Control-Allow-Origin"), anyString());
        } else {
            verify(response).addHeader("Access-Control-Allow-Origin", expected);
        }
    }
    
}
