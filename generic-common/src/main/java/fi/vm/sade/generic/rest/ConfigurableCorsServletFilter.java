package fi.vm.sade.generic.rest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;

/**
 * javax.servlet.Filter implementation of CorsFilter
 * 
 * Tested by configuring Spring-Security.
 * Spring-Security was configured with following parameters:
 * 
 * <http use-expressions="true>
 *  ...
 *  <custom-filter ref="corsHandler" after="PRE_AUTH_FILTER"/>
 * </http>
 * <beans:bean id="corsHandler" class="fi.vm.sade.koodisto.service.filter.ConfigurableCorsServletFilter" />
 * 
 * Calling JavaScript should set withCredentials flag to true when calling REST-methods with @PreAuthorize annotation.
 * Otherwise requests from Chrome (36.0.1985.125) and Mozilla Firefox (31.0) will fail with status 401.
 * 
 * Note that this filter is provided as an alternative wider implementation of CorsFilter. 
 * 
 * @see fi.vm.sade.koodisto.service.filter.ConfigurableCorsFilter should be preferred for REST-interfaces using
 * Jersey.
 * 
 * @see fi.vm.sade.koodisto.service.filter.CorsFilter for configuration parameters
 * 
 * @author Risto Salama
 *
 */
@Component
public class ConfigurableCorsServletFilter extends CorsFilter implements Filter {
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        CorsFiller<HttpServletResponse, HttpServletRequest> filler = new HttpServletCorsFiller(mode, allowedDomains);
        filler.setHeadersToResponse(req, res);
        filler.setAllowOrigin(res, req);
        chain.doFilter(request, res);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void destroy() {
    }
      
    private class HttpServletCorsFiller extends CorsFiller<HttpServletResponse, HttpServletRequest> {

        protected HttpServletCorsFiller(CorsFilterMode mode, String allowedDomains) {
            super(mode, allowedDomains);
        }

        @Override
        protected void setHeader(String key, String value, HttpServletResponse response) {
            response.addHeader(key, value);
        }

        @Override
        protected List<String> getHeaders(String key, HttpServletRequest request) {
            List<String> headerValues = new ArrayList<String>();
            Enumeration<String> headers = request.getHeaders(key);
            if(headers != null) {
                while (headers.hasMoreElements()) {
                    headerValues.add(headers.nextElement());
                }
            };
            return headerValues;
        }

    }
}
