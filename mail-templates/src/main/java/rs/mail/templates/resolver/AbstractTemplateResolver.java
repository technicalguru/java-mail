/**
 * 
 */
package rs.mail.templates.resolver;

import rs.mail.templates.ResolverException;
import rs.mail.templates.Template;
import rs.mail.templates.TemplateContext;
import rs.mail.templates.TemplateResolver;
import rs.mail.templates.cache.Cache;
import rs.mail.templates.cache.CacheFactory;
import rs.mail.templates.cache.CacheFactory.CacheBuilder;
import rs.mail.templates.impl.TemplateId;
import rs.mail.templates.cache.CacheStrategy;

/**
 * Abstract implementation of a {@link TemplateResolver} that caches any result and delivers
 * when subsequent requests arrive.
 * 
 * @author ralph
 *
 */
public abstract class AbstractTemplateResolver implements TemplateResolver {

	private Cache<TemplateId,Template> cache;

	/**
	 * Default constructor (which uses a LRU cache).
	 */
	public AbstractTemplateResolver() {
		this(true);
	}
	
	/**
	 * Constructor to enable or disable the default LRU cache.
	 * 
	 * @param enableCache whether to enable the Cache
	 */
	public AbstractTemplateResolver(boolean enableCache) {
		this(enableCache? CacheFactory.newBuilder(TemplateId.class, Template.class).with(CacheStrategy.LRU).build() : null);
	}
	
	/**
	 * Constructor to use a specific cache strategy.
	 * 
	 * @param cacheStrategy the cahce strategy to be used
	 */
	public AbstractTemplateResolver(CacheStrategy cacheStrategy) {
		this(CacheFactory.newBuilder(TemplateId.class, Template.class).with(cacheStrategy));
	}
	
	/**
	 * Constructor to use a custom {@link CacheBuilder}.
	 * 
	 * @param cacheBuilder the cache builder
	 */
	public AbstractTemplateResolver(CacheBuilder<TemplateId,Template> cacheBuilder) {
		this(cacheBuilder.build());
	}
	
	/**
	 * Constructor to use a custom cache.
	 * 
	 * @param cache the cache to be used (can be null)
	 */
	public AbstractTemplateResolver(Cache<TemplateId, Template> cache) {
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
	protected Cache<TemplateId, Template> getCache() {
		return cache;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Template getTemplate(String name, TemplateContext context) throws ResolverException {
		Template   rc = null;
		TemplateId id = getTemplateId(name, context);
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
	 * <p>The default implementation returns a simple ID object that holds the name of the template</p>.
	 * 
	 * @param name - name of template
	 * @param context - context for usage
	 * @return - ID object
	 * @see TemplateId
	 */
	protected TemplateId getTemplateId(String name, TemplateContext context) {
		return new TemplateId(name, context.getLocale());
	}
	
	/**
	 * Actually tries to resolve the template using the given meta information.
	 * 
	 * @param id - the template ID object
	 * @param name - the name of the object
	 * @param context - the context
	 * @return the template resolved or {@code null} if not available from this resolver.
	 */
	protected abstract Template resolve(TemplateId id, String name, TemplateContext context) throws ResolverException;
}
