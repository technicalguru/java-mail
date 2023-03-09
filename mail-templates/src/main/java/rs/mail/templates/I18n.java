/**
 * 
 */
package rs.mail.templates;

import java.util.HashMap;

import rs.mail.templates.impl.I18nId;

/**
 * Translations of a template.
 * 
 * An instantiation of this class provides translations.
 * 
 * @author ralph
 *
 */
public class I18n extends HashMap<String, String> {

	private static final long serialVersionUID = 1L;
	private I18nId id;

	/**
	 * Constructor.
	 * 
	 * @param id
	 */
	public I18n(I18nId id) {
		this.id = id;
	}
	
	/**
	 * Returns the ID of the translations.
	 * @return the id
	 */
	public I18nId getId() {
		return id;
	}

}
