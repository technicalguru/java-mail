package rs.mail.templates.impl;

import java.util.Locale;
import java.util.Objects;

/**
 * A i18n ID holds valuable information that 
 * is required to find translations when resolving. However, the
 * main information is the ID itself which is used for caching purposes.
 * 
 * @author ralph
 *
 */
public class I18nId {
	
	private String id;
	private Locale locale;
	
	/**
	 * Constructor.
	 * @param id - the ID of the translations.
	 */
	public I18nId(String id) {
		this(id, null);
	}
	
	/**
	 * Constructor.
	 * @param id - the ID of the translations.
	 * @param locale - the locale of the translations
	 */
	public I18nId(String id, Locale locale) {
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
		if (!(obj instanceof I18nId)) {
			return false;
		}
		I18nId other = (I18nId) obj;
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