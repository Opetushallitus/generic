package fi.vm.sade.authentication.cas;

import org.apache.cxf.message.Message;
import org.springframework.security.core.Authentication;

/**
 * @author Antti Salonen
 */
public interface TicketCachePolicy {
    String getTicketFromCache(Message message, String targetService, Authentication auth);
    void putTicketToCache(Message message, String targetService, Authentication auth, String ticket);
}
