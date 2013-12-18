package fi.vm.sade.authentication.cas;

import fi.vm.sade.security.SimpleCache;
import org.apache.cxf.message.Message;
import org.springframework.security.core.Authentication;

import java.util.Map;

/**
 * @author Antti Salonen
 */
@Deprecated // ei pitäisi käyttää, staattinen ticket cache ei hjuva
public class SimpleTicketCachePolicy implements TicketCachePolicy {

    public static final int MAX_TICKET_CACHE_SIZE = 10000;
    // simple in-memory cache is sufficient when we use user user auth as part of cache key
    private static Map<String, String> ticketCache = SimpleCache.<String, String>buildCache(MAX_TICKET_CACHE_SIZE);

    @Override
    public String getTicketFromCache(Message message, String targetService, Authentication auth) {
        return ticketCache.get(cacheKey(auth, targetService));
    }

    @Override
    public void putTicketToCache(Message message, String targetService, Authentication auth, String ticket) {
        ticketCache.put(cacheKey(auth, targetService), ticket);
    }

    private String cacheKey(Authentication auth, String targetService) {
        return auth.hashCode() + "_" + targetService;
    }

}
