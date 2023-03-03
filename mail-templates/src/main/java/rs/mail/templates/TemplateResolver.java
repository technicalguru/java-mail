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
public interface TemplateResolver {

	/**
	 * Provides the template object of the given name to be used in the given context.
	 * @param name - name of template
	 * @param context - context for usage
	 * @return the template instance suitable for the context or {@code null} when no such template can be found
	 * @throws ResolverException when resolving fails (e.g. loading the template)
	 */
	public Template getTemplate(String name, TemplateContext context) throws ResolverException;
}
