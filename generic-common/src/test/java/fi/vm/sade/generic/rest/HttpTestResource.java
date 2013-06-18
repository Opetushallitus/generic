package fi.vm.sade.generic.rest;

import org.apache.commons.codec.binary.Hex;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.Response;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * @author Antti Salonen
 */
@Path("/httptest")
public class HttpTestResource {

    public static int counter = 1;
    public static String someResource = "original value";

    @Path("/pingCached1sec")
    @GET
    @Produces("text/plain")
    public Response pingCached1sec() {
        System.out.println("HttpTest.pingCached1sec, counter: " + counter + ", now: " + new Date(System.currentTimeMillis()));
        return Response
                .ok("pong " + (counter++))
                .expires(date(2))
                .build();
    }

    @Path("/someResource")
    @GET
    @Produces("text/plain")
    public Response someResource(@Context javax.ws.rs.core.Request request) {
        System.out.println("HttpTest.someResource: "+someResource+", counter: "+counter+", now: " + new Date(System.currentTimeMillis()));

        EntityTag etag = new EntityTag(Hex.encodeHexString(someResource.getBytes()));
        Response.ResponseBuilder responseBuilder = request.evaluatePreconditions(etag);

        // Etag match = if resource not changed -> do nothing and return "unmodified" -http response (note also maxage-tag)
        if (responseBuilder != null) {
            System.out.println("resource has not changed..returning unmodified response code");
            return responseBuilder
                    .expires(date(2))
                    .build();
        }

        // otherwise do actual logic and tag response with etag and maxage -headers
        return Response
                .ok(someResource+" "+(counter++))
                .tag(etag)
                .expires(date(2))
                .build();
    }

    @Path("/cacheableAnnotatedResource")
    @GET
    @Produces("text/plain")
    @Cacheable(maxAgeSeconds = 2)
    public Response cacheableAnnotatedResource() {
        System.out.println("HttpTest.cacheableAnnotatedResource, counter: "+counter+", now: " + new Date(System.currentTimeMillis()));

        return Response
                .ok("cacheable " + (counter++))
                .build();
    }

    @Path("/oneSecondResource")
    @GET
    @Produces("text/plain")
    public Response oneSecondResource() throws InterruptedException {
        Thread.sleep(1000);
        return Response.ok("OK").build();
    }

    private Date date(int dSeconds) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, dSeconds); // 24h
        return calendar.getTime();
    }

}
