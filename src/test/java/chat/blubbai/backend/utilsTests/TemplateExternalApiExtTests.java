package chat.blubbai.backend.utilsTests;

import chat.blubbai.backend.model.PhoneNumber;
import chat.blubbai.backend.utils.EnvProvider;
import chat.blubbai.backend.utils.ExternalApi;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class TemplateExternalApiExtTests {

// Attention: After inserting real API credentials, please remove `Template` from the class name to avoid accidentally pushing this file!

    @Test
    @Disabled("DO NOT RUN! This is an integration test and requires a real API key.")
    @DisplayName("Integration test: Validates a real, valid phone number")
    void testValidatePhone_valid() throws Exception {
        try (MockedStatic<EnvProvider> envMock = Mockito.mockStatic(EnvProvider.class, Mockito.CALLS_REAL_METHODS)) {
            envMock.when(() -> EnvProvider.getEnv("PHONE_VALIDATION_API_KEY")).thenReturn("YOUR_API_KEY_HERE");
            PhoneNumber phoneNumber = new PhoneNumber();
            phoneNumber.setNumber("YOUR_PHONE_NUMBER_HERE"); // Example number, adjust if necessary
            phoneNumber.setCountry("DE");
            boolean result = ExternalApi.validatePhone(phoneNumber);
            assertTrue(result); // Example only, please adjust!
        }
    }

    @Test
    @Disabled("DO NOT RUN! This is an integration test and requires a real API key.")
    @DisplayName("Integration test: Validates a real, valid email address")
    void testValidateMail_valid() throws Exception {
        try (MockedStatic<EnvProvider> envMock = Mockito.mockStatic(EnvProvider.class, Mockito.CALLS_REAL_METHODS)) {
            envMock.when(() -> EnvProvider.getEnv("MAIL_VALIDATION_API_KEY")).thenReturn("YOUR_API_KEY_HERE");
            String email = "YOUR_EMAIL_ADDRESS_HERE"; // Example address, adjust if necessary
            boolean result = ExternalApi.validateMail(email);
            assertTrue(result); // Example only, please adjust!
        }
    }

    @Test
    @Disabled("DO NOT RUN! This is an integration test and requires a real API key.")
    @DisplayName("Integration test: Validates a real, invalid phone number")
    void testValidatePhone_notValid() throws Exception {
        try (MockedStatic<EnvProvider> envMock = Mockito.mockStatic(EnvProvider.class, Mockito.CALLS_REAL_METHODS)) {
            envMock.when(() -> EnvProvider.getEnv("PHONE_VALIDATION_API_KEY")).thenReturn("YOUR_API_KEY_HERE");
            PhoneNumber phoneNumber = new PhoneNumber();
            phoneNumber.setNumber("135181335"); // Example number, adjust if necessary
            phoneNumber.setCountry("DE");
            boolean result = ExternalApi.validatePhone(phoneNumber);
            assertFalse(result); // Example only, please adjust!
        }
    }

    @Test
    @Disabled("DO NOT RUN! This is an integration test and requires a real API key.")
    @DisplayName("Integration test: Validates a real, invalid email address")
    void testValidateMail_notValid() throws Exception {
        try (MockedStatic<EnvProvider> envMock = Mockito.mockStatic(EnvProvider.class, Mockito.CALLS_REAL_METHODS)) {
            envMock.when(() -> EnvProvider.getEnv("MAIL_VALIDATION_API_KEY")).thenReturn("YOUR_API_KEY_HERE");
            String email = "test@l.hanasd.e";
            boolean result = ExternalApi.validateMail(email);
            assertFalse(result); // Example only, please adjust!
        }
    }
}
