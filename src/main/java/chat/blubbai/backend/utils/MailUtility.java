package chat.blubbai.backend.utils;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;

public class MailUtility {
    Session session;

    public MailUtility() {
        Properties prop = new Properties();
        prop.put("mail.smtp.auth", true);
        prop.put("mail.smtp.starttls.enable", "true");
        prop.put("mail.smtp.host", EnvProvider.getEnv("MAILTRAP_SMTP_HOST"));
        prop.put("mail.smtp.port", EnvProvider.getEnv("MAILTRAP_SMTP_PORT"));
        prop.put("mail.smtp.ssl.trust", EnvProvider.getEnv("MAILTRAP_SMTP_HOST"));

        this.session = Session.getInstance(prop, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EnvProvider.getEnv("MAILTRAP_USERNAME"), EnvProvider.getEnv("MAILTRAP_PASSWORD"));
            }
        });
    }

    public void sendEmail(String to, String from, String subject, String body) throws MessagingException {
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject);

        MimeBodyPart mimeBodyPart = new MimeBodyPart();
        mimeBodyPart.setContent(body, "text/html; charset=utf-8");

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(mimeBodyPart);

        message.setContent(multipart);

        Transport.send(message);
    }



}
