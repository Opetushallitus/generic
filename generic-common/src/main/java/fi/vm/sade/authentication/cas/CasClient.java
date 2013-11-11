package fi.vm.sade.authentication.cas;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;

import java.io.IOException;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An example Java client to authenticate against CAS using REST services.
 * Please ensure you have followed the necessary setup found on the <a
 * href="http://www.ja-sig.org/wiki/display/CASUM/RESTful+API">wiki</a>.
 *
 * @author Antti Salonen
 * @author <a href="mailto:jieryn@gmail.com">jesse lauren farinacci</a>
 * @since 3.4.2
 */
public final class CasClient {
    private static final Logger LOG = LoggerFactory.getLogger(CasClient.class.getName());

    private CasClient() {
        // static-only access
    }

    /** get cas service ticket, throws runtime exception if fails */
    public static String getTicket(final String server, final String username, final String password, final String service) {
        notNull(server, "server must not be null");
        notNull(username, "username must not be null");
        notNull(password, "password must not be null");
        notNull(service, "service must not be null");
        return getServiceTicket(server, getTicketGrantingTicket(server, username, password), service);
    }

    private static String getServiceTicket(final String server, final String ticketGrantingTicket, final String service) {
        final HttpClient client = new HttpClient();

        LOG.debug("getServiceTicket: {} / {}", server , ticketGrantingTicket);

        final PostMethod post = new PostMethod(server + "/" + ticketGrantingTicket);
        post.setRequestBody(new NameValuePair[]{new NameValuePair("service", service)});

        try {
            client.executeMethod(post);
            final String response = post.getResponseBodyAsString();
            switch (post.getStatusCode()) {
                case HttpStatus.SC_OK:
                    LOG.info("serviceTicket found");
                    return response;
                default:
                    LOG.warn("Invalid response code ({}) from CAS server!", post.getStatusCode());
                    LOG.info("Response (1k): " + response.substring(0, Math.min(1024, response.length())));
                    throw new RuntimeException("failed to get CAS service ticket, response code: "+post.getStatusCode()+", server: "+server+", tgt: "+ticketGrantingTicket+", service: "+service);
            }
        } catch (final IOException e) {
            throw new RuntimeException("failed to get CAS service ticket, server: "+server+", tgt: "+ticketGrantingTicket+", service: "+service+", cause: "+e, e);
        } finally {
            post.releaseConnection();
        }
    }

    public static String getTicketGrantingTicket(final String server, final String username, final String password) {
        final HttpClient client = new HttpClient();
        final PostMethod post = new PostMethod(server);
        post.setRequestBody(new NameValuePair[]{
                new NameValuePair("username", username),
                new NameValuePair("password", password)});

        try {
            client.executeMethod(post);
            final String response = post.getResponseBodyAsString();
            switch (post.getStatusCode()) {
                case 201: {
                    final Matcher matcher = Pattern.compile(".*action=\".*/(.*?)\".*").matcher(response);
                    if (matcher.matches()) {
                        return matcher.group(1);
                    }
                    throw new RuntimeException("Successful ticket granting request, but no ticket found! server: "+server+", user: "+username+", response: "+response.substring(0, Math.min(1024, response.length())));
                }
                default:
                    throw new RuntimeException("Invalid response code from CAS server: "+post.getStatusCode()+", server: "+server+", user: "+username+", response: "+response.substring(0, Math.min(1024, response.length())));
            }
        } catch (final IOException e) {
            throw new RuntimeException("error getting TGT, server: "+server+", user: "+username+", exception: "+e, e);
        } finally {
            post.releaseConnection();
        }
    }

    private static void notNull(final Object object, final String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }

}