/**
 * 
 */
package rs.mail.templates.resolver;

import rs.mail.templates.Resolver;
import rs.mail.templates.ResolverException;
import rs.mail.templates.TemplateContext;
import rs.mail.templates.cache.Cache;
import rs.mail.templates.cache.CacheFactory;
import rs.mail.templates.cache.CacheFactory.CacheBuilder;
import rs.mail.templates.cache.CacheStrategy;
import rs.mail.templates.impl.I18nId;

/**
 * Abstract implementation of resolvers that caches any result and delivers
 * when subsequent requests arrive.
 * 
 * @param <K> the ID class
 * @param <X> the resolved object class
 * 
 * @author ralph
 *
 */
public abstract class AbstractResolver<K,X> implements Resolver<X> {

	private Cache<K,X> cache;

	/**
	 * Default constructor (which uses a LRU cache).
	 * 
	 * @param idClass the class of the ID
	 * @param objectClass the class of the resolved object
	 */
	public AbstractResolver(Class<K> idClass, Class<X> objectClass) {
		this(true, idClass, objectClass);
	}
	
	/**
	 * Constructor to enable or disable the default LRU cache.
	 * 
	 * @param enableCache whether to enable the Cache
	 * @param idClass the class of the ID
	 * @param objectClass the class of the resolved object
	 */
	public AbstractResolver(boolean enableCache, Class<K> idClass, Class<X> objectClass) {
		this(enableCache ? CacheFactory.newBuilder(idClass, objectClass).with(CacheStrategy.LRU).build() : null);
	}
	
	/**
	 * Constructor to use a specific cache strategy.
	 * 
	 * @param cacheStrategy the cache strategy to be used
	 * @param idClass the class of the ID
	 * @param objectClass the class of the resolved object
	 */
	public AbstractResolver(CacheStrategy cacheStrategy, Class<K> idClass, Class<X> objectClass) {
		this(CacheFactory.newBuilder(idClass, objectClass).with(cacheStrategy));
	}
	
	/**
	 * Constructor to use a custom {@link CacheBuilder}.
	 * 
	 * @param cacheBuilder the cache builder
	 */
	public AbstractResolver(CacheBuilder<K,X> cacheBuilder) {
		this(cacheBuilder.build());
	}
	
	/**
	 * Constructor to use a custom cache.
	 * 
	 * @param cache the cache to be used (can be null)
	 */
	public AbstractResolver(Cache<K, X> cache) {
		this.cache = cache;
	}

	/**
	 * Returns whether caching is enabled.
	 * 
	 * @return {@code true} when a cache is present
	 */
	public boolean isCacheEnabled() {
		return cache != null;
	}
	
	/**
	 * Returns the cache.
	 * 
	 * @return the cache or {@code null} if no cache is present.
	 */
	protected Cache<K, X> getCache() {
		return cache;
	}
	
	/**
	 * Resolves the name in the given context.
	 * 
	 * @param name - name of object to be resolved
	 * @param context - the context of the template
	 * @return the resolved object (from cache or freshly loaded)
	 * @throws ResolverException when resolving fails
	 */
	public X resolve(String name, TemplateContext context) throws ResolverException {
		X rc = null;
		K id = getId(name, context);
		if (cache != null) {
			rc   = cache.get(id);
			if (rc == null) {
				rc = resolve(id, name, context);
				if (rc != null) {
					cache.put(id, rc);
				}
			}
		} else {
			rc = resolve(id, name, context);
		}
		return rc;
	}
	
	/** 
	 * Construct a unique ID object.
	 * <p>The default implementation returns a simple ID object that holds the name of the translations.</p>.
	 * 
	 * @param name - name of translations
	 * @param context - context for usage
	 * @return - ID object
	 * @see I18nId
	 */
	protected abstract K getId(String name, TemplateContext context);
	
	/**
	 * Actually tries to resolve the template using the given meta information.
	 * 
	 * @param id - the template ID object
	 * @param name - the name of the object
	 * @param context - the context
	 * @return the template resolved or {@code null} if not available from this resolver.
	 * @throws ResolverException when resolving the template fails
	 */
	protected abstract X resolve(K id, String name, TemplateContext context) throws ResolverException;
}
