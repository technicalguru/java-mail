/**
 * 
 */
package rs.mail.templates.util;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;

/**
 * Provide easy methods to use messages.
 * 
 * @author ralph
 *
 */
public class MailUtils {

	public static Multipart addMultipart(Message message)  throws MessagingException {
		Multipart rc = new MimeMultipart();
		message.setContent(rc);
		return rc;
	}
	
	public static MimeBodyPart addMimePart(Multipart multipart, String content, String contentType) throws MessagingException {
		MimeBodyPart mimePart = new MimeBodyPart();
		mimePart.setContent(content, contentType);
		multipart.addBodyPart(mimePart);
		return mimePart;
	}
	
	// TODO Attach files and images
	// https://cloud.google.com/appengine/docs/standard/java/mail/mail-with-headers-attachments?hl=de#mail_with_attachments
}
