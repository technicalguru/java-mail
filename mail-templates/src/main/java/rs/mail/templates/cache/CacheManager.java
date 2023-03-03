/**
 * 
 */
package rs.mail.templates.cache;

import java.util.Map;

import rs.mail.templates.cache.impl.CacheEntryMeta;

/**
 * A cache manager is responsible to cleanup a cache to prevent from consuming to much memory.
 * 
 * @author ralph
 *
 */
public interface CacheManager {

	public int getCleanupThreshold();
	public long getMinCleanupLapse();
	public <K,V> void cleanup(Cache<K,V> cache, Map<K,CacheEntryMeta<K>> meta);
	
}
