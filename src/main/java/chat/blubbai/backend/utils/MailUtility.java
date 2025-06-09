package chat.blubbai.backend.utils;

// Looking to send emails in production? Check out our Email API/SMTP product!
import io.mailtrap.client.MailtrapClient;
import io.mailtrap.config.MailtrapConfig;
import io.mailtrap.factory.MailtrapClientFactory;
import io.mailtrap.model.request.emails.Address;
import io.mailtrap.model.request.emails.MailtrapMail;

import java.util.List;
import java.util.Map;

public class MailUtility {

    /**
     * Sends an email to the user with a verification link.
     *
     * @param to          The recipient's email address.
     * @param subjectName The name of the recipient, used in the email template.
     * @param link        The verification link to be included in the email.
     */
    static public void sendEmailVerificationEmail(String to, String subjectName, String link) {
        final MailtrapConfig config = new MailtrapConfig.Builder()
                .sandbox(true)
                .inboxId(3784628L)
                .token(EnvProvider.getEnv("MAILTRAP_API_KEY_SANDBOX"))
                .build();

        final MailtrapClient client = MailtrapClientFactory.createMailtrapClient(config);

        final MailtrapMail mail = MailtrapMail.builder()
                .from(new Address(EnvProvider.getEnv("MAIL_FROM"), EnvProvider.getEnv("PLATFORM_NAME")))
                .to(List.of(new Address(to)))
                .templateUuid("689c35ce-5a33-43c7-889d-1279ca0b56f8")
                .templateVariables(Map.of(
                        "company_info_name", EnvProvider.getEnv("PLATFORM_NAME"),
                        "name", subjectName,
                        "url", link
                ))
                .build();
        try {
            System.out.println(client.send(mail));
        } catch (Exception e) {
            System.out.println("Caught exception : " + e);
        }
    }

    static public void send2FACodeEmail(String to, String code) {
        final MailtrapConfig config = new MailtrapConfig.Builder()
                .sandbox(true)
                .inboxId(3784628L)
                .token(EnvProvider.getEnv("MAILTRAP_API_KEY_SANDBOX"))
                .build();

        final MailtrapClient client = MailtrapClientFactory.createMailtrapClient(config);

        final MailtrapMail mail = MailtrapMail.builder()
                .from(new Address(EnvProvider.getEnv("MAIL_FROM"), EnvProvider.getEnv("PLATFORM_NAME")))
                .to(List.of(new Address(to)))
                .templateUuid("cc8a52be-7e05-49cf-a42f-9601528c6b67")
                .templateVariables(Map.of(
                        "company_info_name", EnvProvider.getEnv("PLATFORM_NAME"),
                        "code", code
                ))
                .build();

        try {
            System.out.println(client.send(mail));
        } catch (Exception e) {
            System.out.println("Caught exception : " + e);
        }
    }

}
