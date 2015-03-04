package fi.vm.sade.generic.rest;

import java.util.Arrays;

import javax.ws.rs.core.MultivaluedMap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerResponse;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ContextConfiguration(locations = "classpath:spring/test-cors-default-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class ConfigurableCorsFilterDefaultTest {
    
    @Autowired
    private ConfigurableCorsFilter filter;
    
    private ContainerResponse response;
    private ContainerRequest request;
    private MultivaluedMap<String, Object> responseMap;
    
    @SuppressWarnings("unchecked")
    @Before
    public void init() {
        responseMap = Mockito.mock(MultivaluedMap.class);
        response = Mockito.mock(ContainerResponse.class);
        request = Mockito.mock(ContainerRequest.class);        
        when(request.getRequestHeaders()).thenReturn((MultivaluedMap<String, String>) Mockito.mock(MultivaluedMap.class));
        when(response.getHttpHeaders()).thenReturn(responseMap);
    }
    
    @Test
    public void defaultModeIsProduction() {
        assertEquals(CorsFilterMode.PRODUCTION, filter.mode);
    }
    
    @Test
    public void doesNotSetValueToAccessControlAllowOriginWhenNoDomainIsSet() {
        when(request.getRequestHeader("origin")).thenReturn(Arrays.asList("http://hack.domain.org"));
        filter.setMode(CorsFilterMode.PRODUCTION.name());
        filter.filter(request, response);
        verify(responseMap, never()).add(eq("Access-Control-Allow-Origin"), anyString());
    }
    
    @Test
    public void setsAllowMethodsToHeader() {
        filter.filter(request, response);
        verify(responseMap).add("Access-Control-Allow-Methods", "POST, GET, OPTIONS, PUT, DELETE, HEAD");
    }
    
    @Test
    public void setsSevenDaysToMaxAgeHeader() {
        filter.filter(request, response);
        verify(responseMap).add("Access-Control-Max-Age", String.valueOf(60 * 60 * 24 * 7));
    }
    
}

