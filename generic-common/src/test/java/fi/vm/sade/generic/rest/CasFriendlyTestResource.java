package fi.vm.sade.generic.rest;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;

import fi.vm.sade.security.CasFriendlyCxfInterceptorTest;

/**
 * @author Jouni Stam
 */
@Path("/casfriendly")
public class CasFriendlyTestResource {

    private static Map<String, Integer> testCaseCounts = new HashMap<String, Integer>();
    
    @Path("/protected")
    @GET
    @Produces("text/plain")
    public Response protectedGet(@Context HttpServletRequest request, 
            @HeaderParam (value="Testcase-Id") String testCaseId,
            @QueryParam (value="ticket") String ticket) {
        try {
            HttpSession session = request.getSession(false);
            if(session == null && ticket == null)
                return Response.status(301).location(new URI(CasFriendlyCxfInterceptorTest.getUrl("/cas/login"))).build();
            else {
                session = request.getSession(true);
                return Response
                    .ok("ok " + getAndIncreaseTestCaseCount(request.getRequestURI() + testCaseId))
                    .build();
            }
        } catch (URISyntaxException e) {
            return Response.serverError().build();
        }
    }

    @Path("/protected")
    @POST
    @Produces("text/plain")
    public Response protectedPost(@Context HttpServletRequest request,
            @FormParam (value="TESTNAME") String name,
            @HeaderParam (value="Testcase-Id") String testCaseId,
            @QueryParam (value="ticket") String ticket) {
        try {
            if(!"TESTVALUE".equals(name)) {
                System.err.println("BODY MISSING!");
                throw new Exception("Post body missing.");
            }
            HttpSession session = request.getSession(false);
            if(session == null && ticket == null)
                return Response.status(301).location(new URI(CasFriendlyCxfInterceptorTest.getUrl("/cas/login"))).build();
            else {
                session = request.getSession(true);
                return Response
                    .ok("ok " + getAndIncreaseTestCaseCount(request.getRequestURI() + testCaseId))
                    .build();
            }
        } catch (Exception e) {
            return Response.serverError().build();
        }
    }
    
    @Path("/unprotected")
    @GET
    @Produces("text/plain")
    public Response unprotectedGet(@Context HttpServletRequest request,
            @HeaderParam (value="Testcase-Id") String testCaseId) {
        return Response
                .ok("ok " + getAndIncreaseTestCaseCount(request.getRequestURI() + testCaseId))
                .build();
    }

    @Path("/j_spring_cas_security_check")
    @GET
    @Produces("text/plain")
    public Response casCheckGet(@Context HttpServletRequest request, 
            @HeaderParam (value="Testcase-Id") String testCaseId,
            @QueryParam (value="ticket") String ticket) {
        if(ticket == null)
            return Response.status(401).build();
        else {
            HttpSession session = request.getSession(true);
            return Response
                .ok("spring authenticated")
                .build();
        }
    }
    
    private static int getAndIncreaseTestCaseCount(String testCaseId) {
        Integer value = testCaseCounts.get(testCaseId);
        if(value == null)
            value = new Integer(0);
        value++;
        testCaseCounts.put(testCaseId, value);
        return value;
    }
}
