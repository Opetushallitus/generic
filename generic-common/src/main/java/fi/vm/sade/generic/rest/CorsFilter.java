package fi.vm.sade.generic.rest;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;

/**
 * User: tommiha
 * Date: 6/13/13
 * Time: 2:35 PM
 */
public class CorsFilter implements ContainerResponseFilter {
    @Override
    public ContainerResponse filter(ContainerRequest containerRequest, ContainerResponse containerResponse) {
        if ( containerRequest.getRequestHeaders().containsKey("access-control-request-method") ) {
            for ( String value : containerRequest.getRequestHeaders().get("access-control-request-method") ) {
                containerResponse.getHttpHeaders().add("Access-Control-Allow-Methods", value );
            }
        }
        if ( containerRequest.getRequestHeaders().containsKey("access-control-request-headers") ) {
            for ( String value : containerRequest.getRequestHeaders().get("access-control-request-headers") ) {
                containerResponse.getHttpHeaders().add("Access-Control-Allow-Headers", value );
            }
        }
        
        String headerOrigin = containerRequest.getRequestHeader("Origin")!=null && containerRequest.getRequestHeader("Origin").size()>0?containerRequest.getRequestHeader("Origin").get(0):null;

        if (headerOrigin != null) {
            containerResponse.getHttpHeaders().add("Access-Control-Allow-Origin", headerOrigin);
            containerResponse.getHttpHeaders().add("Access-Control-Allow-Credentials", "true");

        } else {
            //never happens?
            containerResponse.getHttpHeaders().add("Access-Control-Allow-Origin", "*");
        }
        

        
        return containerResponse;
    }
}
