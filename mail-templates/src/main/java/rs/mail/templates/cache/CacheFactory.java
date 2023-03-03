/**
 * 
 */
package rs.mail.templates.cache;

import rs.mail.templates.cache.impl.AbstractCacheManager;
import rs.mail.templates.cache.impl.DefaultCache;
import rs.mail.templates.cache.impl.FifoCacheManager;
import rs.mail.templates.cache.impl.LfuCacheManager;
import rs.mail.templates.cache.impl.LruCacheManager;

/**
 * Create caches.
 * 
 * @author ralph
 *
 */
public class CacheFactory {

	public static <K,V> CacheBuilder<K,V> newBuilder(Class<K> keyClass, Class<V> valueClass) {
		return new CacheBuilder<K,V>();
	}
	
	
	public static class CacheBuilder<K,V> {
		private CacheStrategy strategy;
		private CacheManager  manager;
		private Integer       cleanupThreshold;
		private Integer       minSizeThreshold;
		private Long          minCleanupLapseMs;
		
		public CacheBuilder() {
			this.strategy         = CacheStrategy.LRU;
			this.cleanupThreshold = 30;
		}
		
		public CacheBuilder<K,V> with(CacheStrategy strategy) {
			this.strategy = strategy;
			return this;
		}
		
		public CacheBuilder<K,V> with(CacheManager manager) {
			this.manager = manager;
			return this;
		}
		
		public CacheBuilder<K,V> withCleanupThreshold(int cleanupThreshold) {
			this.cleanupThreshold = cleanupThreshold;
			return this;
		}
		
		public CacheBuilder<K,V> withMinSizeThreshold(int minSizeThreshold) {
			this.minSizeThreshold = minSizeThreshold;
			return this;
		}
		
		public CacheBuilder<K,V> withMinCleanupLapse(long minCleanupLapseMs) {
			this.minCleanupLapseMs = minCleanupLapseMs;
			return this;
		}
		
		public Cache<K,V> build() {
			if (manager == null) {
				switch (strategy) {
				case FIFO: manager = new FifoCacheManager(); break;
				case LFU:  manager = new LfuCacheManager();  break;
				case LRU:  manager = new LruCacheManager();  break;
				}
			}
			
			if (manager instanceof AbstractCacheManager) {
				AbstractCacheManager mgr = (AbstractCacheManager)manager;
				if (cleanupThreshold  != null) mgr.setCleanupThreshold(cleanupThreshold);
				if (minSizeThreshold  != null) mgr.setMinSizeThreshold(minSizeThreshold);
				if (minCleanupLapseMs != null) mgr.setMinCleanupLapse(minCleanupLapseMs);
			}
			
			return new DefaultCache<K,V>(manager);
		}
	}
}
