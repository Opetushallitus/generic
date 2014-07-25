package fi.vm.sade.generic.rest;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;

/**
 * com.sun.jersey.spi.container.ContainerResponseFilter implementation of CorsFilter
 * 
 * Simply add this filter into your web.xml Jersey configuration:
 * 
 * <init-param>
 *     <param-name>com.sun.jersey.spi.container.ContainerResponseFilters</param-name>
 *     <param-value>fi.vm.sade.koodisto.service.filter.ConfigurableCorsFilter, {other filters]</param-value>
 * </init-param>
 *  
 * Calling JavaScript should set withCredentials flag to true when calling REST-methods with @PreAuthorize annotation.
 * Otherwise requests from Chrome (36.0.1985.125) and Mozilla Firefox (31.0) will fail with status 401.
 * 
 * @see fi.vm.sade.koodisto.service.filter.CorsFilter for configuration parameters
 * 
 * @author Risto Salama
 *
 */

@Component
public class ConfigurableCorsFilter extends CorsFilter implements ContainerResponseFilter {

    @Override
    public ContainerResponse filter(ContainerRequest containerRequest, ContainerResponse containerResponse) {
        CorsFiller<ContainerResponse, ContainerRequest> filler = new JerseyCorsFiller(mode, allowedDomains);
        filler.setHeadersToResponse(containerRequest, containerResponse);
        filler.setAllowOrigin(containerResponse, containerRequest);
        return containerResponse;
    }

    private class JerseyCorsFiller extends CorsFiller<ContainerResponse, ContainerRequest> {
        
        private JerseyCorsFiller(CorsFilterMode mode, String allowedDomains) {
            super(mode, allowedDomains);
        }

        @Override
        protected void setHeader(String key, String value, ContainerResponse response) {
            response.getHttpHeaders().add(key, value);
        }

        @Override
        protected List<String> getHeaders(String key, ContainerRequest request) {
            List<String> headers = request.getRequestHeader(key);
            return headers != null ? headers : new ArrayList<String>();
        }

    }

}
