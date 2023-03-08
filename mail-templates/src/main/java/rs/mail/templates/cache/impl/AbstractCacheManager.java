/**
 * 
 */
package rs.mail.templates.cache.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import rs.mail.templates.cache.Cache;
import rs.mail.templates.cache.CacheManager;

/**
 * Abstract implementation of manager properties.
 * 
 * @author ralph
 *
 */
public abstract class AbstractCacheManager implements CacheManager {

	/** Default threshold of cach entries before cleaning up */
	public static final int  DEFAULT_CLEANUP_THRESHOLD  = 30;
	/** number of cache entries after cleanup */
	public static final int  DEFAULT_MIN_SIZE_THRESHOLD = 20;
	/** Milliseconds before next cache cleanup will run */
	public static final long DEFAULT_CLEANUP_LAPSE      = 600000;
	
	private int  cleanupThreshold;
	private int  minSizeThreshold;
	private long minCleanupLapse;
	
	protected AbstractCacheManager() { 
		this(DEFAULT_CLEANUP_THRESHOLD, DEFAULT_MIN_SIZE_THRESHOLD, DEFAULT_CLEANUP_LAPSE);
	}
	
	public AbstractCacheManager(int cleanupThreshold, int minSizeThreshold, long minCleanupLapse) {
		this.cleanupThreshold = cleanupThreshold;
		this.minSizeThreshold = minSizeThreshold;
		this.minCleanupLapse = minCleanupLapse;
	}

	/**
	 * @return the cleanupThreshold
	 */
	public int getCleanupThreshold() {
		return cleanupThreshold;
	}
	
	/**
	 * @param cleanupThreshold the cleanupThreshold to set
	 */
	public void setCleanupThreshold(int cleanupThreshold) {
		this.cleanupThreshold = cleanupThreshold;
	}
	
	/**
	 * @return the minSizeThreshold
	 */
	public int getMinSizeThreshold() {
		return minSizeThreshold;
	}
	
	/**
	 * @param minSizeThreshold the minSizeThreshold to set
	 */
	public void setMinSizeThreshold(int minSizeThreshold) {
		this.minSizeThreshold = minSizeThreshold;
	}
	
	/**
	 * @return the minCleanupLapse
	 */
	public long getMinCleanupLapse() {
		return minCleanupLapse;
	}
	
	/**
	 * @param minCleanupLapse the minCleanupLapse to set
	 */
	public void setMinCleanupLapse(long minCleanupLapse) {
		this.minCleanupLapse = minCleanupLapse;
	}

	@Override
	public <K,V> void cleanup(Cache<K, V> cache, Map<K,CacheEntryMeta<K>> meta) {
		for (K key : getCleanupPriority(meta)) {
			if (cache.size() <= getMinSizeThreshold()) break;
			cache.remove(key);
		}
	}

	protected <K, V> List<K> getCleanupPriority(Map<K,CacheEntryMeta<K>> meta) {
		List<CacheEntryMeta<K>> sorted = new ArrayList<>(meta.values());
		sorted.sort(getMetaComparator());
		return sorted.stream().map(e -> e.getKey()).collect(Collectors.toList());
	}
	
	protected abstract <K> Comparator<CacheEntryMeta<K>> getMetaComparator();
}
