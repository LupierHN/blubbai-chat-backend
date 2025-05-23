package chat.dvai.backend.utilsTests;

import chat.dvai.backend.model.PhoneNumber;
import chat.dvai.backend.utils.ExternalApi;
import chat.dvai.backend.utils.EnvProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class ExternalApiExtTests {

    @Test
    @DisplayName("Integration test: Validates a real, valid phone number")
    void testValidatePhone_valid() throws Exception {
        try (MockedStatic<EnvProvider> envMock = Mockito.mockStatic(EnvProvider.class, Mockito.CALLS_REAL_METHODS)) {
            envMock.when(() -> EnvProvider.getEnv("PHONE_VALIDATION_API_KEY")).thenReturn("3c2924e50a5d4b01aa409f493627082f");
            PhoneNumber phoneNumber = new PhoneNumber();
            phoneNumber.setNumber("1608735841"); // Example number, adjust if necessary
            phoneNumber.setCountry("DE");
            boolean result = ExternalApi.validatePhone(phoneNumber);
            assertTrue(result); // Adjust as needed!
        }
    }

    @Test
    @DisplayName("Integration test: Validates a real, valid email address")
    void testValidateMail_valid() throws Exception {
        try (MockedStatic<EnvProvider> envMock = Mockito.mockStatic(EnvProvider.class, Mockito.CALLS_REAL_METHODS)) {
            envMock.when(() -> EnvProvider.getEnv("MAIL_VALIDATION_API_KEY")).thenReturn("4f8cef00dd214483beca83a7a2935525");
            String email = "test@l.pierchalla.de";
            boolean result = ExternalApi.validateMail(email);
            assertTrue(result); // Adjust as needed!
        }
    }

    @Test
    @DisplayName("Integration test: Validates a real, invalid phone number")
    void testValidatePhone_notValid() throws Exception {
        try (MockedStatic<EnvProvider> envMock = Mockito.mockStatic(EnvProvider.class, Mockito.CALLS_REAL_METHODS)) {
            envMock.when(() -> EnvProvider.getEnv("PHONE_VALIDATION_API_KEY")).thenReturn("3c2924e50a5d4b01aa409f493627082f");
            PhoneNumber phoneNumber = new PhoneNumber();
            phoneNumber.setNumber("135181335"); // Example number, adjust if necessary
            phoneNumber.setCountry("DE");
            boolean result = ExternalApi.validatePhone(phoneNumber);
            assertFalse(result); // Adjust as needed!
        }
    }

    @Test
    @DisplayName("Integration test: Validates a real, invalid email address")
    void testValidateMail_notValid() throws Exception {
        try (MockedStatic<EnvProvider> envMock = Mockito.mockStatic(EnvProvider.class, Mockito.CALLS_REAL_METHODS)) {
            envMock.when(() -> EnvProvider.getEnv("MAIL_VALIDATION_API_KEY")).thenReturn("4f8cef00dd214483beca83a7a2935525");
            String email = "test@l.hanasd.e";
            boolean result = ExternalApi.validateMail(email);
            assertFalse(result); // Adjust as needed!
        }
    }
}

