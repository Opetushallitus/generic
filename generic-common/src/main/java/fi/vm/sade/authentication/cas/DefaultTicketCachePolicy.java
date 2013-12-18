package fi.vm.sade.authentication.cas;

import org.apache.cxf.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

/**
 * Ticket cache policy that keeps cached ticket in user's http session context (if using from spring webapp), otherwise in threadlocal.
 *
 * @author Antti Salonen
 */
public class DefaultTicketCachePolicy implements TicketCachePolicy {

    private static final Logger log = LoggerFactory.getLogger(DefaultTicketCachePolicy.class);
    public static ThreadLocal<String> ticketThreadLocal = new ThreadLocal<String>();

    public String getTicketFromCache(Message message, String targetService, Authentication auth) {
        String user = auth.getName();
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        String cachedTicket;
        if (requestAttributes != null) {
            cachedTicket = (String) requestAttributes.getAttribute(ticketKey(targetService, user), RequestAttributes.SCOPE_SESSION);
        } else {
            cachedTicket = ticketThreadLocal.get();
        }
        return cachedTicket;
    }

    public void putTicketToCache(Message message, String targetService, Authentication auth, String ticket) {
        String user = auth.getName();
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null) {
            // note! normaali keissi, mutta warn jotta nähdään jos näitä tehdään liikaa
            log.warn("cache ticket to httpsession, service: "+targetService+", user: "+user+", ticket: "+ticket);
            requestAttributes.setAttribute(ticketKey(targetService, user), ticket, RequestAttributes.SCOPE_SESSION);
        } else {
            // note! normaali keissi, mutta warn jotta nähdään jos näitä tehdään liikaa
            log.warn("cache ticket to threadlocal, service: "+targetService+", user: "+user+", ticket: "+ticket);
            ticketThreadLocal.set(ticket);
        }
    }

    private String ticketKey(String targetService, String user) {
        return "cachedTicket_" + targetService + "_"+user;
    }


}
