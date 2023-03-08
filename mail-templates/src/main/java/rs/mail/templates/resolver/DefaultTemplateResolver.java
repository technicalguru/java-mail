package rs.mail.templates.resolver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.io.IOUtils;

import rs.mail.templates.ContentType;
import rs.mail.templates.ResolverException;
import rs.mail.templates.Template;
import rs.mail.templates.TemplateContext;
import rs.mail.templates.cache.Cache;
import rs.mail.templates.cache.CacheFactory;
import rs.mail.templates.cache.CacheFactory.CacheBuilder;
import rs.mail.templates.cache.CacheStrategy;
import rs.mail.templates.impl.DefaultTemplate;
import rs.mail.templates.impl.TemplateId;

/**
 * The default template resolver searches a specific directory (non-recursive)
 * for the locale-specific versions of a template:
 * <ol>
 * <li>&lt;directory&gt;/&lt;template-name&gt;.&lt;context.locale.language&gt;-&lt;context.locale.country&gt;.&lt;content-type-suffix&gt;</li>
 * <li>&lt;directory&gt;/&lt;template-name&gt;.&lt;context.locale.language&gt;.&lt;content-type-suffix&gt;</li>
 * <li>&lt;directory&gt;/&lt;template-name&gt;.&lt;content-type-suffix&gt;</li>
 * </ol>
 * <p>where {@code content-type-suffix} is either {@code html} or {@code txt}. So for template {@code my-template} in 
 * locale {@code de-DE} the following files are searched in order of priority</p>
 *
 * <ol>
 * <li>&lt;directory&gt;/my-template.de-DE.html</li>
 * <li>&lt;directory&gt;/my-template.de-DE.txt</li>
 * <li>&lt;directory&gt;/my-template.de.html</li>
 * <li>&lt;directory&gt;/my-template.de.txt</li>
 * <li>&lt;directory&gt;/my-template.html</li>
 * <li>&lt;directory&gt;/my-template.txt</li>
 * </ol>
 * <p>The prioritized list of paths can be changed by overrriding {@link #getPriorityPaths(String, TemplateContext, ContentType)}.</p>
 * 
 * <p>Please notice that an additiona file {@code <directory>/my-template.ftl} will be returned when the name was {@code my-template.ftl}
 *    (ends with {@code .ftl}). This allows an easier inclusion of general Freemarker libraries.</p>
 * @author ralph
 *
 */
public class DefaultTemplateResolver extends AbstractTemplateResolver {

	private File directory;
	private Charset charset;
	
	/**
	 * Constructor (which uses a LRU cache).
	 * 
	 * @param directory - the directory to search templates (non-recursive)
	 * @throws IOException - when the directory is not accessible
	 */
	public DefaultTemplateResolver(File directory) throws IOException {
		this(directory, true);
	}
	
	/**
	 * Constructor to enable or disable the default LRU cache.
	 * 
	 * @param directory - the directory to search templates (non-recursive)
	 * @param enableCache whether to enable the Cache
	 * @throws IOException - when the directory is not accessible
	 */
	public DefaultTemplateResolver(File directory, boolean enableCache) throws IOException {
		this(directory, enableCache? CacheFactory.newBuilder(TemplateId.class, Template.class).with(CacheStrategy.LRU).build() : null);
	}
	
	/**
	 * Constructor to use a specific cache strategy.
	 * 
	 * @param directory - the directory to search templates (non-recursive)
	 * @param cacheStrategy the cahce strategy to be used
	 * @throws IOException - when the directory is not accessible
	 */
	public DefaultTemplateResolver(File directory, CacheStrategy cacheStrategy) throws IOException {
		this(directory, CacheFactory.newBuilder(TemplateId.class, Template.class).with(cacheStrategy));
	}
	
	/**
	 * Constructor to use a custom {@link CacheBuilder}.
	 * 
	 * @param directory - the directory to search templates (non-recursive)
	 * @param cacheBuilder the cache builder
	 * @throws IOException - when the directory is not accessible
	 */
	public DefaultTemplateResolver(File directory, CacheBuilder<TemplateId,Template> cacheBuilder) throws IOException {
		this(directory, cacheBuilder.build());
	}
	
	/**
	 * Constructor to use a custom cache.
	 * 
	 * @param directory - the directory to search templates (non-recursive)
	 * @param cache the cache to be used (can be null)
	 * @throws IOException - when the directory is not accessible
	 */
	public DefaultTemplateResolver(File directory, Cache<TemplateId, Template> cache) throws IOException {
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
	protected Template resolve(TemplateId id, String name, TemplateContext context) throws ResolverException {
		Template       rc = null;
		
		// Find in cache
		boolean cacheHit = false;
		Cache<TemplateId,Template> cache = getCache();
		if (cache != null) {
			rc = cache.get(id);
		}
		if (rc == null) {
			File htmlPriorities[] = getPriorityPaths(name, context, ContentType.HTML);
			File htmlFile         = findFile(htmlPriorities);
			File textPriorities[] = getPriorityPaths(name, context, ContentType.TEXT);
			File textFile         = findFile(textPriorities);
			if ((htmlFile != null) || (textFile != null)) try {
				String htmlContent = htmlFile != null ? IOUtils.toString(new FileInputStream(htmlFile), getCharset(true)) : null;
				String textContent = textFile != null ? IOUtils.toString(new FileInputStream(textFile), getCharset(true)) : null;
				rc = createTemplate(id, htmlContent, textContent);
			} catch (IOException e) {
				throw new ResolverException(this, "Cannot load template", e);
			}
		} else {
			cacheHit = true;
		}
		
		if ((cache != null) && (rc != null) && !cacheHit) {
			cache.put(id, rc);
		}
		return rc;
	}

	/**
	 * Creates the template using the ID and the content.
	 * @param id - ID of template
	 * @param htmlContent - HTML content
	 * @param textContent - TEXT content
	 * @return the created template
	 */
	protected Template createTemplate(TemplateId id, String htmlContent, String textContent) {
		return new DefaultTemplate(id, htmlContent, textContent);
	}
	
	/**
	 * Returns the files to be checked in order of priority.
	 * @param name - the name of the template
	 * @param context - the context
	 * @param contentType - the content type of the template
	 * @return the files to be checked
	 * @see #getFileVariants(TemplateContext)
	 */
	protected File[] getPriorityPaths(String name, TemplateContext context, ContentType contentType) {
		String suffix      = ContentType.HTML.equals(contentType) ? ".html" : ".txt";
		boolean ftlLibrary = name.endsWith(".ftl");
		String variants[]  = getFileVariants(context); 
		List<File> files   = new ArrayList<>();
		for (int i=0; i<variants.length; i++) {
			files.add(new File(directory, name+"."+variants[i]+suffix));
		}
		files.add(new File(directory, name+suffix));
		if (ftlLibrary) files.add(new File(directory, name));
		return files.toArray(new File[files.size()]);
	}
	
	/**
	 * Returns the variants of the template in order of precendence.
	 * <p>Default implementation uses the locale in the context and e.g. returns ["de-DE", "de" ]</p>
	 * @param context - the context holding information about the message to be built
	 * @return the list of variants to be checked for template files
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
	protected File findFile(File priorities[]) {
		for (File file : priorities) {
			if (file.exists() && file.isFile() && file.canRead()) {
				return file;
			}
		}
		return null;
	}
}
