package fi.vm.sade.authentication.cas;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.apache.commons.lang.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * A cache implementation to keep service and user specific session ids. Currently hardcoded to use memory only for caching.
 * @author Jouni Stam
 *
 */
@Service
@Scope ("singleton")
public class CasFriendlyCache {

	public static final String CACHE_NAME = "sessionCache";
	CacheManager cacheManager;
	private int ttlInSeconds = 3600;

	public CasFriendlyCache() {
		this(3600);
	}
	
	public CasFriendlyCache(int ttlInSeconds) {
		this.ttlInSeconds = ttlInSeconds;
	}
	
	/**
	 * Sets sessionId for caching.
	 * @param serviceUrl
	 * @param userName
	 * @param sessionId
	 */
	public void setSessionId(String callerService, String targetServiceUrl, String userName, String sessionId) {
		if(StringUtils.isEmpty(callerService) || 
				StringUtils.isEmpty(targetServiceUrl) || 
				StringUtils.isEmpty(userName) || 
				StringUtils.isEmpty(sessionId))
			return;
		Element element = new Element(createKey(callerService, targetServiceUrl, userName), sessionId);
		this.getCache().put(element);
	}
	
	/**
	 * Gets sessionId from cache.
	 * @param serviceUrl
	 * @param userName
	 * @return
	 */
	public String getSessionId(String callerService, String targetServiceUrl, String userName) {
		if(StringUtils.isEmpty(callerService) || StringUtils.isEmpty(targetServiceUrl) || StringUtils.isEmpty(userName))
			return null;
		Element element = this.getCache().get(createKey(callerService, targetServiceUrl, userName));
		if(element != null)
			return (String)element.getObjectValue();
		else
			return null;
	}
	
	/**
	 * Removes from cache.
	 * @param callerService
	 * @param targetServiceUrl
	 * @param userName
	 */
	public void removeSessionId(String callerService, String targetServiceUrl, String userName) {
		this.getCache().remove(createKey(callerService, targetServiceUrl, userName));
	}
	
	/**
	 * Creates a key for caching.
	 * @param serviceUrl
	 * @param userName
	 * @return
	 */
	private static String createKey(String callerService, String targetServiceUrl, String userName) {
		// Key does not include more than folder
		return callerService + ":" + userName + "@" + StringUtils.substringBeforeLast(targetServiceUrl, "/") + "/";
	}

	/**
	 * Gets cache manager. Creates if not available.
	 * @return
	 */
	protected Cache getCache() {
		if(cacheManager == null) {
			cacheManager = CacheManager.create();
			// This is where cache is configured, currently memory only, can be configured from xml as well with minor changes
			// Cache(String name, int maxElementsInMemory, boolean overflowToDisk, boolean eternal, long timeToLiveSeconds, long timeToIdleSeconds) 
			Cache memoryOnlyCache = new Cache(CACHE_NAME, 50000, false, false, ttlInSeconds, 3600);
			
			cacheManager.addCacheIfAbsent(memoryOnlyCache);
		}
		return cacheManager.getCache(CACHE_NAME);
	}
	
}


