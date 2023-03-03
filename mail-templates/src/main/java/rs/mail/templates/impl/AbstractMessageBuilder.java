/**
 * 
 */
package rs.mail.templates.impl;

import org.jsoup.Jsoup;

import rs.mail.templates.BuilderException;
import rs.mail.templates.BuilderResult;
import rs.mail.templates.ContentType;
import rs.mail.templates.MessageBuilder;
import rs.mail.templates.ResolverException;
import rs.mail.templates.Template;
import rs.mail.templates.TemplateContext;
import rs.mail.templates.TemplateResolver;

/**
 * Abstract base class for message builders.
 * @author ralph
 *
 */
public abstract class AbstractMessageBuilder<T> implements MessageBuilder<T> {

	private TemplateContext   context = new TemplateContext();
	private MessageCreator<T> messageCreator;
	
	/**
	 * Creates the builder using the given creator.
	 * @param messageCreator - the message creator
	 */
	protected AbstractMessageBuilder(MessageCreator<T> messageCreator) {
		this.messageCreator = messageCreator;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public MessageBuilder<T> withContext(TemplateContext context, boolean replace) {
		if (replace) this.context = new TemplateContext();
		this.context.add(context);
		return this;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public MessageBuilder<T> withBodyTemplate(String templateName) {
		context.setBodyTemplate(templateName);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public MessageBuilder<T> withBodyTemplate(Template template) {
		context.setBodyTemplate(template);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public MessageBuilder<T> withSubjectTemplate(String templateName) {
		context.setSubjectTemplate(templateName);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public MessageBuilder<T> withSubjectTemplate(Template template) {
		context.setSubjectTemplate(template);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public MessageBuilder<T> withValue(String name, Object o) {
		context.setValue(name, o);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public MessageBuilder<T> withResolver(TemplateResolver... templateResolvers) {
		context.addResolver(templateResolvers);
		return this;
	}

	/**
	 * Returns the context.
	 * @return the context
	 */
	public TemplateContext getContext() {
		return context;
	}

	
	@Override
	public T build() throws BuilderException {
		try {
			BuilderResult result = buildContent();
			return messageCreator.create(result);
		} catch (BuilderException e) {
			throw e;
		} catch (Throwable t) {
			throw new BuilderException(this, "Cannot construct MIME message", t);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public BuilderResult buildContent() throws BuilderException {
		// It is important to lock the context
		context.lock();
		
		String subject = buildSubject();
		String html    = buildBody(ContentType.HTML);
		String text    = buildBody(ContentType.TEXT);

		if ((text == null) && (html != null)) {
			text = toText(html); 
		}
		if ((text != null) && (html == null)) {
			html = toHtml(text);
		}
		
		if ((text != null) || (html != null)) {
			return new BuilderResult(subject, text, html);
		}
		throw new BuilderException(this, "Cannot build message");
	}
	
	/**
	 * Builds the template with given name and content type.
	 * <p>Implementors can override this method or {@link #build(Template, ContentType)} depending
	 *    on whether this abstract implementation class shall perform the resolving step. This can
	 *    be unnecessary when implementors have its own resolution process, e.g. Freemarker.</p>
	 * <p>The default implementation calls {@link #build(Template, ContentType)} to actually
	 *    build the template.</p>
	 * @param templateName - the name of the template
	 * @param type         - the content type
	 * @return the template filled with translations and context values
	 * @throws BuilderException when the build or resolving fails.
	 */
	protected String build(String templateName, ContentType type) throws BuilderException {
		Template template = null;
		try {
			template = resolve(templateName);
		} catch (ResolverException e) {
			throw new BuilderException(this, e.getMessage(), e);
		}
		if (template != null) {
			return build(template, type);
		}
			
		return null;
	}
	
	/**
	 * Fill the given template using the current context.
	 * <p>Implementors can override this method or {@link #build(String, ContentType)} depending
	 *    on whether this abstract implementation class shall perform the resolving step. This can
	 *    be unnecessary when implementors have its own resolution process, e.g. Freemarker.</p>
	 * <p>The default implementation returns the content of the template.</p>
	 * @param template - the template object
	 * @param contentType - the content type to build
	 * @return the template filled with translations and context values
	 */
	protected String build(Template template, ContentType contentType) {
		return template.getTemplate(contentType);
	}
	
	/**
	 * Build the subject of the message
	 * @return the subject or {@code null} if no template available
	 */
	protected String buildSubject() throws BuilderException {
		Template template = context.getSubjectTemplate();
		if (template == null) return build(context.getSubjectTemplateName(), ContentType.TEXT);
		return build(template, ContentType.TEXT);
	}
	
	/**
	 * Build the subject of the message
	 * @return the subject or {@code null} if no template available
	 */
	protected String buildBody(ContentType contentType) throws BuilderException {
		Template template = context.getBodyTemplate();
		if (template == null) return build(context.getBodyTemplateName(), contentType);
		return build(template, contentType);
	}
	
	/**
	 * Resolves the name of the template.
	 * @param name - name of template to find
	 * @return the template or {@code null} if not found.
	 */
	protected Template resolve(String name) throws ResolverException {
		if (name != null) {
			for (TemplateResolver resolver : context.getResolvers()) {
				Template rc = resolver.getTemplate(name, context);
				if (rc != null) return rc;
			}
		}
		return null;
	}
	
	/**
	 * Converts HTML to TEXT content by stripping off all tags using {@link Jsoup}.
	 * 
	 * @param html - the HTML content
	 * @return the text content
	 */
	protected String toText(String html) {
		return Jsoup.parse(html).wholeText();
	}
	
	/**
	 * Converts TEXT to HTML content by replacing all newlines with &lt;br&gt; tags.
	 * 
	 * @param text - the TEXT content
	 * @return the HTML content
	 */
	protected String toHtml(String text) {
		return text.replaceAll("\\n", "<br>");
	}
}
