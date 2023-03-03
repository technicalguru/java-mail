/**
 * 
 */
package rs.mail.templates.impl;

import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.io.output.StringBuilderWriter;

import freemarker.cache.NullCacheStorage;
import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import rs.mail.templates.BuilderException;
import rs.mail.templates.ContentType;
import rs.mail.templates.MessageBuilder;
import rs.mail.templates.TemplateContext;

/**
 * Default implementation with Freemarker.
 * <p>It needs to be noted that caching is handled at {@link MessageBuilder} level
 *    not at FreeMarker level. That's why the caching for Freemarker is disabled.</p>
 *    
 * @author ralph
 *
 */
public class FreemarkerMessageBuilder<T> extends AbstractMessageBuilder<T> {

	private Configuration            configuration;
	private FreemarkerTemplateLoader templateLoader;
	
	/**
	 * Constructor.
	 */
	public FreemarkerMessageBuilder(MessageCreator<T> messageCreator) {
		super(messageCreator);
		this.configuration  = null;
		this.templateLoader = null;
	}

	/**
	 * Returns or creates the configuration for Freemarker.
	 * 
	 * @return the freemarker configuration
	 */
	protected Configuration getFreemarkerConfiguration() {
		if (configuration == null) {
			configuration = createFreemarkerConfiguration();
		}
		return configuration;
	}
	
	/**
	 * Creates a configuration based on current builder status.
	 * @return a fresh configuration
	 */
	protected Configuration createFreemarkerConfiguration() {
		// Configuration
		Configuration cfg = new Configuration(Configuration.VERSION_2_3_31);
		cfg.setTemplateLoader(getFreemarkerTemplateLoader());
		cfg.setDefaultEncoding("UTF-8");
		cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
		cfg.setLogTemplateExceptions(false);
		cfg.setWrapUncheckedExceptions(true);
		cfg.setFallbackOnNullLoopVariable(false);
		cfg.setSQLDateAndTimeTimeZone(TimeZone.getDefault());
		cfg.setCacheStorage(new NullCacheStorage());
		return cfg;
	}
	
	/**
	 * Returns or creates the template loader for Freemarker.
	 * 
	 * @return the template loader
	 */
	protected FreemarkerTemplateLoader getFreemarkerTemplateLoader() {
		if (templateLoader == null) {
			templateLoader = new FreemarkerTemplateLoader(this);
		}
		return templateLoader;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String build(String templateName, ContentType contentType) throws BuilderException {
		TemplateContext          context = getContext();
		Configuration            config  = getFreemarkerConfiguration();
		FreemarkerTemplateLoader loader  = getFreemarkerTemplateLoader();
		
		// Always recreate the data model (basically it's the context)
		Map<String, Object> root = new HashMap<>(context.getValues());
		
		try {
			loader.setContentType(contentType);
			freemarker.template.Template temp = config.getTemplate(templateName);
			Writer out = new StringBuilderWriter();
			temp.process(root, out);
			return out.toString();
		} catch (Throwable t) {
			throw new BuilderException(this, "Cannot process template", t);
		}
	}

}
