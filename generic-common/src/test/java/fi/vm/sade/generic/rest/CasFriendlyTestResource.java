package fi.vm.sade.generic.rest;

import org.apache.commons.codec.binary.Hex;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author Jouni Stam
 */
@Path("/casfriendly")
public class CasFriendlyTestResource {

    @Path("/protected")
    @GET
    @Produces("text/plain")
    public Response protectedGet() {
        return Response
                .ok("ok")
                .build();
    }

    @Path("/unprotected")
    @GET
    @Produces("text/plain")
    public Response unprotectedGet(@Context javax.ws.rs.core.Request request) {
        return Response
                .ok("ok")
                .build();
    }
}
