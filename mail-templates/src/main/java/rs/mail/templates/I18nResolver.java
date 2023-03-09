/**
 * 
 */
package rs.mail.templates;

/**
 * Interface for a template provider.
 * 
 * Selects the template to be used for a specific context.
 * 
 * @author ralph
 *
 */
public interface I18nResolver {

	/**
	 * Provides the translations for the given name to be used in the given context.
	 * @param name - name of translations (usually equals to a template name)
	 * @param context - context for usage
	 * @return the translations instance suitable for the context. Must not be {@code null}.
	 * @throws ResolverException when resolving fails (e.g. loading the properties files)
	 */
	public I18n getTranslations(String name, TemplateContext context) throws ResolverException;
}
