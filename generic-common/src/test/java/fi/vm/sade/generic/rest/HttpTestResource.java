package fi.vm.sade.generic.rest;

import org.apache.commons.codec.binary.Hex;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

/**
 * @author Antti Salonen
 */
@Path("/httptest")
public class HttpTestResource {

    public static int counter = 1;
    public static String someResource = "original value";
    public static int authenticationCount = -999;

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

    @Path("/xmlgregoriancalendar1")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String xmlgregoriancalendar1() throws InterruptedException {
        return ""+new Date().getTime();
    }

    @Path("/xmlgregoriancalendar2")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String xmlgregoriancalendar2() throws InterruptedException {
        return new SimpleDateFormat("yyyy-MM-dd").format(new Date());
    }

    private Date date(int dSeconds) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, dSeconds); // 24h
        return calendar.getTime();
    }

    @Path("/status500")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response status500() {
        return Response.status(500).build();
    }

    @Path("/pingSecuredRedirect")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response pingSecuredRedirect(@Context HttpServletRequest request) throws URISyntaxException {
        if (hasValidCasTicket(request)) {
            return Response.ok("pong " + (counter++)).build();
        }
        return Response.temporaryRedirect(new URI("/httptest/cas")).build();
    }

    @Path("/pingSecuredRedirect")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response pingSecuredRedirectPost(@Context HttpServletRequest request) throws URISyntaxException {
        return pingSecuredRedirect(request);
    }

    @Path("/pingSecured401Unauthorized")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response pingSecured401Unauthorized(@Context HttpServletRequest request) throws URISyntaxException {
        if (hasValidCasTicket(request)) {
            return Response.ok("pong " + (counter++)).build();
        }
        return Response.status(401).build();
    }

    private boolean hasValidCasTicket(HttpServletRequest request) {
        String ticket = request.getParameter("ticket");
        System.out.println("hasValidCasTicket, request: "+request.getRequestURL()+", ticket: "+ticket);
        return ticket != null && !ticket.startsWith("invalid");
    }

    @Path("/cas")
    @GET
    public Response cas(@Context HttpServletRequest request) throws URISyntaxException {
        String service = request.getParameter("service");
        if (service != null) {
            System.out.println("HttpTestResource.cas, service: "+service+", temp auth OK, sending back");
            return Response.temporaryRedirect(new URI(service+"?ticket=TEMP_TGT")).build();
        }
        return Response.ok("this is cas").build();
    }

    @Path("/cas/v1/tickets")
    @GET
    public Response casTickets(@Context HttpServletRequest request) throws URISyntaxException {
        System.out.println("cas tickets, params: "+ request.getParameterMap());
        authenticationCount++;
        return Response.ok("TEMP_CAS_TICKET_"+System.currentTimeMillis()).build();
    }

}
