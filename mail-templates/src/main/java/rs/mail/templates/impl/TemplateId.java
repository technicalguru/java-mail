package rs.mail.templates.impl;

import java.util.Locale;
import java.util.Objects;

/**
 * A template ID can be used to hold valuable information that 
 * is required to find a template when resolving. However, the
 * main information is the ID itself which is used for caching purposes.
 * 
 * @author ralph
 *
 */
public class TemplateId {
	
	private String id;
	private Locale locale;
	
	/**
	 * Constructor.
	 * @param id - the ID of the template.
	 */
	public TemplateId(String id) {
		this(id, null);
	}
	
	/**
	 * Constructor.
	 * @param id - the ID of the template.
	 */
	public TemplateId(String id, Locale locale) {
		this.id     = id;
		this.locale = locale;
	}
		
	/**
	 * Returns the id.
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * Returns the locale.
	 * @return the locale
	 */
	public Locale getLocale() {
		return locale;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return Objects.hash(id, locale);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof TemplateId)) {
			return false;
		}
		TemplateId other = (TemplateId) obj;
		return Objects.equals(id, other.id) && Objects.equals(locale, other.locale);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return getId()+"-"+getLocale();
	}
	
	
}