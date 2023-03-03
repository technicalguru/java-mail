/**
 * 
 */
package rs.mail.templates;

import rs.mail.templates.impl.FreemarkerMessageBuilder;
import rs.mail.templates.impl.MessageCreator;

/**
 * Entry class for message generation.
 * 
 * Use this class to create your message builder.
 * 
 * @author ralph
 *
 */
public class MessageBuilderFactory {

	/**
	 * Creates a new {@link MessageBuilder}.
	 * @return the new message builder
	 */
	public static <T> MessageBuilder<T> newBuilder(MessageCreator<T> messageCreator) {
		return new FreemarkerMessageBuilder<>(messageCreator);
	}
	
	
}
