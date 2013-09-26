package fi.vm.sade.authentication.cas;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;

import java.io.IOException;
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

    public static String getTicket(final String server, final String username, final String password, final String service) {
        notNull(server, "server must not be null");
        notNull(username, "username must not be null");
        notNull(password, "password must not be null");
        notNull(service, "service must not be null");
        return getServiceTicket(server, getTicketGrantingTicket(server, username, password), service);
    }

    private static String getServiceTicket(final String server, final String ticketGrantingTicket, final String service) {
        if (ticketGrantingTicket == null)
            return null;

        final HttpClient client = new HttpClient();

        LOG.info("getServiceTicket: {} / {}", server , ticketGrantingTicket);

        final PostMethod post = new PostMethod(server + "/" + ticketGrantingTicket);
        post.setRequestBody(new NameValuePair[]{new NameValuePair("service", service)});

        try {
            client.executeMethod(post);
            final String response = post.getResponseBodyAsString();
            switch (post.getStatusCode()) {
                case 200:
                    LOG.info("serviceTicket found");
                    return response;
                default:
                    LOG.warn("Invalid response code ({}) from CAS server!", post.getStatusCode());
                    LOG.info("Response (1k): " + response.substring(0, Math.min(1024, response.length())));
                    break;
            }
        } catch (final IOException e) {
            LOG.warn(e.getMessage());
        } finally {
            post.releaseConnection();
        }

        return null;
    }

    public static String getTicketGrantingTicket(final String server, final String username, final String password) {
        final HttpClient client = new HttpClient();
        final PostMethod post = new PostMethod(server);
        post.setRequestBody(new NameValuePair[]{
                new NameValuePair("username", username),
                new NameValuePair("password", password)});

        LOG.info("getTicketGrantingTicket: {}", server);

        try {
            client.executeMethod(post);
            final String response = post.getResponseBodyAsString();
            switch (post.getStatusCode()) {
                case 201: {
                    final Matcher matcher = Pattern.compile(".*action=\".*/(.*?)\".*").matcher(response);
                    if (matcher.matches()) {
                        LOG.info("ticketGrantingTicket found");
                        return matcher.group(1);
                    }
                    LOG.warn("Successful ticket granting request, but no ticket found!");
                    LOG.info("Response (1k): {}", response.substring(0, Math.min(1024, response.length())));
                    break;
                }

                default:
                    LOG.warn("Invalid response code ({}) from CAS server!", post.getStatusCode());
                    LOG.info("Response (1k): {}", response.substring(0, Math.min(1024, response.length())));
                    break;
            }
        } catch (final IOException e) {
            LOG.warn(e.getMessage());
        } finally {
            post.releaseConnection();
        }

        return null;
    }

    private static void notNull(final Object object, final String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }

}