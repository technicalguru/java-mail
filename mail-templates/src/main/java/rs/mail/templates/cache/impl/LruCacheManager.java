/**
 * 
 */
package rs.mail.templates.cache.impl;

import java.util.Comparator;

/**
 * @author ralph
 *
 */
public class LruCacheManager extends AbstractCacheManager {

	public LruCacheManager() {
		super();
	}

	public LruCacheManager(int cleanupThreshold, int minSizeThreshold, long minCleanupLapse) {
		super(cleanupThreshold, minSizeThreshold, minCleanupLapse);
	}

	protected <K> Comparator<CacheEntryMeta<K>> getMetaComparator() {
		return new Comparator<CacheEntryMeta<K>>() {
			@Override
			public int compare(CacheEntryMeta<K> o1, CacheEntryMeta<K> o2) {
				if (o1.getLastUseTime() < o2.getLastUseTime()) return -1;
				if (o1.getLastUseTime() > o2.getLastUseTime()) return 1;
				return 0;
			}
			
		};
	}
	
}
