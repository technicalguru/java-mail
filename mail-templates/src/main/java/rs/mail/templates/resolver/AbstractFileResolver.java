package rs.mail.templates.resolver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import rs.mail.templates.ResolverException;
import rs.mail.templates.TemplateContext;
import rs.mail.templates.cache.Cache;
import rs.mail.templates.cache.CacheFactory;
import rs.mail.templates.cache.CacheFactory.CacheBuilder;
import rs.mail.templates.cache.CacheStrategy;

/**
 * The default resolver searches a specific directory (non-recursive)
 * versions of a file name. The order of lookups will be provided through 
 * method ...
 * 
 * @author ralph
 *
 */
public abstract class AbstractFileResolver<K,X> extends AbstractResolver<K,X> {

	private File directory;
	private Charset charset;
	
	/**
	 * Constructor (which uses a LRU cache).
	 * 
	 * @param directory - the directory to search templates (non-recursive)
	 * @throws IOException - when the directory is not accessible
	 */
	public AbstractFileResolver(File directory, Class<K> idClass, Class<X> objectClass) throws IOException {
		this(directory, true, idClass, objectClass);
	}
	
	/**
	 * Constructor to enable or disable the default LRU cache.
	 * 
	 * @param directory - the directory to search templates (non-recursive)
	 * @param enableCache whether to enable the Cache
	 * @throws IOException - when the directory is not accessible
	 */
	public AbstractFileResolver(File directory, boolean enableCache, Class<K> idClass, Class<X> objectClass) throws IOException {
		this(directory, enableCache? CacheFactory.newBuilder(idClass, objectClass).with(CacheStrategy.LRU).build() : null);
	}
	
	/**
	 * Constructor to use a specific cache strategy.
	 * 
	 * @param directory - the directory to search templates (non-recursive)
	 * @param cacheStrategy the cahce strategy to be used
	 * @throws IOException - when the directory is not accessible
	 */
	public AbstractFileResolver(File directory, CacheStrategy cacheStrategy, Class<K> idClass, Class<X> objectClass) throws IOException {
		this(directory, CacheFactory.newBuilder(idClass, objectClass).with(cacheStrategy));
	}
	
	/**
	 * Constructor to use a custom {@link CacheBuilder}.
	 * 
	 * @param directory - the directory to search templates (non-recursive)
	 * @param cacheBuilder the cache builder
	 * @throws IOException - when the directory is not accessible
	 */
	public AbstractFileResolver(File directory, CacheBuilder<K,X> cacheBuilder) throws IOException {
		this(directory, cacheBuilder.build());
	}
	
	/**
	 * Constructor to use a custom cache.
	 * 
	 * @param directory - the directory to search templates (non-recursive)
	 * @param cache the cache to be used (can be null)
	 * @throws IOException - when the directory is not accessible
	 */
	public AbstractFileResolver(File directory, Cache<K, X> cache) throws IOException {
		super(cache);
		this.directory = directory;
		this.charset   = null;
		if (!directory.exists())      throw new FileNotFoundException("Not found: "+directory.getCanonicalPath());
		if (!directory.isDirectory()) throw new IOException("Not a directory: "+directory.getCanonicalPath());
		if (!directory.canRead())     throw new IOException("Cannot read: "+directory.getCanonicalPath());
	}

	/**
	 * Returns the directory.
	 * @return the directory
	 */
	public File getDirectory() {
		return directory;
	}

	/**
	 * Returns the charset to be used.
	 * @return charset to be used
	 */
	public Charset getCharset() {
		return getCharset(false);
	}
	
	/**
	 * Sets the charset.
	 * @param charset the charset to set
	 */
	public void setCharset(Charset charset) {
		this.charset = charset;
	}

	/**
	 * Returns the charset to be used or - if required - a default one
	 * @param resolveWithDefault whether a default charset shall be returned when no charset was set before
	 * @return a charset
	 */
	protected Charset getCharset(boolean resolveWithDefault) {
		if (charset == null) {
			if (resolveWithDefault) return Charset.defaultCharset();
		}
		return charset;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected X resolve(K id, String name, TemplateContext context) throws ResolverException {
		X rc = null;
		
		// Find in cache
		boolean cacheHit = false;
		Cache<K,X> cache = getCache();
		if (cache != null) {
			rc = cache.get(id);
		}
		if (rc == null) {
			rc = create(id, name, context);
		} else {
			cacheHit = true;
		}
		
		if ((cache != null) && (rc != null) && !cacheHit) {
			cache.put(id, rc);
		}
		return rc;
	}

	protected abstract X create(K id, String name, TemplateContext context) throws ResolverException;
	
	/**
	 * Returns the files to be checked in order of priority.
	 * @param name - the base name of the file
	 * @param context - the context
	 * @param suffix - the suffix of the file
	 * @return the files to be checked
	 * @see #getFileVariants(TemplateContext)
	 */
	protected List<File> getPriorityPaths(String name, TemplateContext context, String suffix) {
		String variants[]  = getFileVariants(context); 
		List<File> files   = new ArrayList<>();
		for (int i=0; i<variants.length; i++) {
			files.add(new File(directory, name+"."+variants[i]+suffix));
		}
		files.add(new File(directory, name+suffix));
		return files;
	}
	
	/**
	 * Returns the variants of the file in order of precendence.
	 * <p>Default implementation uses the locale in the context and e.g. returns ["de-DE", "de" ]</p>
	 * @param context - the context holding information about the message to be built
	 * @return the list of variants to be checked for files
	 */
	protected String[] getFileVariants(TemplateContext context) {
		Locale locale = context.getLocale();
		return new String[] {
				locale.toLanguageTag(),
				locale.getLanguage()
		};
	}
	
	/**
	 * Check files in order of priorities to find the first readable.
	 * @param priorities - list of files in order of priority
	 * @return - the file that can be used or {@code null} if none is available
	 */
	protected File findFile(List<File> priorities) {
		for (File file : priorities) {
			if (file.exists() && file.isFile() && file.canRead()) {
				return file;
			}
		}
		return null;
	}
}
