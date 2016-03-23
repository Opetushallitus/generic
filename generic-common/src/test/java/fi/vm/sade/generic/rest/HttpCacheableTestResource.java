package fi.vm.sade.generic.rest;

import fi.vm.sade.jetty.HttpTestResource;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.Date;

@Path("/httptest")
public class HttpCacheableTestResource extends HttpTestResource {

    @Path("/cacheableAnnotatedResource")
    @GET
    @Produces("text/plain")
    @Cacheable(maxAgeSeconds = 2)
    public Response cacheableAnnotatedResource() {
        System.out.println("HttpTest.cacheableAnnotatedResource, counter: " + counter + ", now: " + new Date(System.currentTimeMillis()));

        return Response
                .ok("cacheable " + (counter++))
                .build();
    }
}
