/**
 * 
 */
package rs.mail.templates.cache.impl;

import java.util.Comparator;

/**
 * @author ralph
 *
 */
public class LfuCacheManager extends AbstractCacheManager {

	public LfuCacheManager() {
		super();
	}

	public LfuCacheManager(int cleanupThreshold, int minSizeThreshold, long minCleanupLapse) {
		super(cleanupThreshold, minSizeThreshold, minCleanupLapse);
	}

	protected <K> Comparator<CacheEntryMeta<K>> getMetaComparator() {
		return new Comparator<CacheEntryMeta<K>>() {
			@Override
			public int compare(CacheEntryMeta<K> o1, CacheEntryMeta<K> o2) {
				if (o1.getUseCounter() < o2.getUseCounter()) return -1;
				if (o1.getUseCounter() > o2.getUseCounter()) return 1;
				return 0;
			}
			
		};
	}
}
