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
	
	/**
	 * Sets sessionId for caching.
	 * @param serviceUrl
	 * @param userName
	 * @param sessionId
	 */
	public void setSessionId(String serviceUrl, String userName, String sessionId) {
		if(StringUtils.isEmpty(serviceUrl) || StringUtils.isEmpty(userName) || StringUtils.isEmpty(sessionId))
			return;
		Element element = new Element(createKey(serviceUrl, userName), sessionId);
		this.getCache().put(element);
	}
	
	/**
	 * Gets sessionId from cache.
	 * @param serviceUrl
	 * @param userName
	 * @return
	 */
	public String getSessionId(String serviceUrl, String userName) {
		if(StringUtils.isEmpty(serviceUrl) || StringUtils.isEmpty(userName))
			return null;
		Element element = this.getCache().get(createKey(serviceUrl, userName));
		if(element != null)
			return (String)element.getObjectValue();
		else
			return null;
	}
	
	/**
	 * Creates a key for caching.
	 * @param serviceUrl
	 * @param userName
	 * @return
	 */
	private static String createKey(String serviceUrl, String userName) {
		return userName + "@" + serviceUrl;
	}

	/**
	 * Gets cache manager. Creates if not available.
	 * @return
	 */
	private Cache getCache() {
		if(cacheManager == null) {
			cacheManager = CacheManager.create();
			// This is where cache is configured, currently memory only, can be configured from xml as well with minor changes
			Cache memoryOnlyCache = new Cache(CACHE_NAME, 5000, false, false, 5, 2);
			
			cacheManager.addCacheIfAbsent(memoryOnlyCache);
		}
		return cacheManager.getCache(CACHE_NAME);
	}
	
}


