/**
 * 
 */
package rs.mail.templates.cache.impl;

import java.util.Comparator;

/**
 * @author ralph
 *
 */
public class FifoCacheManager extends AbstractCacheManager {

	
	public FifoCacheManager() {
		super();
	}

	public FifoCacheManager(int cleanupThreshold, int minSizeThreshold, long minCleanupLapse) {
		super(cleanupThreshold, minSizeThreshold, minCleanupLapse);
	}

	protected <K> Comparator<CacheEntryMeta<K>> getMetaComparator() {
		return new Comparator<CacheEntryMeta<K>>() {
			@Override
			public int compare(CacheEntryMeta<K> o1, CacheEntryMeta<K> o2) {
				if (o1.getCreationTime() < o2.getCreationTime()) return -1;
				if (o1.getCreationTime() > o2.getCreationTime()) return 1;
				return 0;
			}
			
		};
	}
}
