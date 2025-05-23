package chat.dvai.backend.utilsTests;

import chat.dvai.backend.model.PhoneNumber;
import chat.dvai.backend.utils.ExternalApi;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import org.json.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ExternalApiTests {

    /**
     * Tests the validation of a valid phone number with a mocked API response.
     */
    @Test
    @DisplayName("Validation of a valid phone number with mocked API response")
    void testValidatePhone_valid() throws Exception {
        PhoneNumber phoneNumber = new PhoneNumber();
        phoneNumber.setNumber("16821234567");
        phoneNumber.setCountry("DE");

        JSONObject json = new JSONObject();
        json.put("is_valid", true);

        JsonNode jsonNode = mock(JsonNode.class);
        when(jsonNode.getObject()).thenReturn(json);

        HttpResponse<JsonNode> response = mock(HttpResponse.class);
        when(response.getStatus()).thenReturn(200);
        when(response.getBody()).thenReturn(jsonNode);

        try (MockedStatic<Unirest> unirestMock = mockStatic(Unirest.class)) {
            com.mashape.unirest.request.GetRequest getRequest = mock(com.mashape.unirest.request.GetRequest.class);
            unirestMock.when(() -> Unirest.get(anyString())).thenReturn(getRequest);
            when(getRequest.asJson()).thenReturn(response);

            boolean result = ExternalApi.validatePhone(phoneNumber);
            assertTrue(result);
        }
    }

    /**
     * Tests the validation of an invalid phone number with a mocked API response.
     */
    @Test
    @DisplayName("Validation of an invalid phone number with mocked API response")
    void testValidatePhone_invalid() throws Exception {
        PhoneNumber phoneNumber = new PhoneNumber();
        phoneNumber.setNumber("123");
        phoneNumber.setCountry("DE");

        JSONObject json = new JSONObject();
        json.put("is_valid", false);

        JsonNode jsonNode = mock(JsonNode.class);
        when(jsonNode.getObject()).thenReturn(json);

        HttpResponse<JsonNode> response = mock(HttpResponse.class);
        when(response.getStatus()).thenReturn(200);
        when(response.getBody()).thenReturn(jsonNode);

        try (MockedStatic<Unirest> unirestMock = mockStatic(Unirest.class)) {
            com.mashape.unirest.request.GetRequest getRequest = mock(com.mashape.unirest.request.GetRequest.class);
            unirestMock.when(() -> Unirest.get(anyString())).thenReturn(getRequest);
            when(getRequest.asJson()).thenReturn(response);

            boolean result = ExternalApi.validatePhone(phoneNumber);
            assertFalse(result);
        }
    }

    /**
     * Tests the validation of a valid email address with a mocked API response.
     */
    @Test
    @DisplayName("Validation of a valid email address with mocked API response")
    void testValidateMail_valid() throws Exception {
        String email = "test@example.com";

        JSONObject json = new JSONObject();
        json.put("deliverability", "DELIVERABLE");
        json.put("is_valid_format", true);

        JsonNode jsonNode = mock(JsonNode.class);
        when(jsonNode.getObject()).thenReturn(json);

        HttpResponse<JsonNode> response = mock(HttpResponse.class);
        when(response.getStatus()).thenReturn(200);
        when(response.getBody()).thenReturn(jsonNode);

        try (MockedStatic<Unirest> unirestMock = mockStatic(Unirest.class)) {
            com.mashape.unirest.request.GetRequest getRequest = mock(com.mashape.unirest.request.GetRequest.class);
            unirestMock.when(() -> Unirest.get(anyString())).thenReturn(getRequest);
            when(getRequest.asJson()).thenReturn(response);

            boolean result = ExternalApi.validateMail(email);
            assertTrue(result);
        }
    }

    /**
     * Tests the validation of an invalid email address with a mocked API response.
     */
    @Test
    @DisplayName("Validation of an invalid email address with mocked API response")
    void testValidateMail_invalid() throws Exception {
        String email = "invalid@invalid";

        JSONObject json = new JSONObject();
        json.put("deliverability", "UNDELIVERABLE");
        json.put("is_valid_format", false);

        JsonNode jsonNode = mock(JsonNode.class);
        when(jsonNode.getObject()).thenReturn(json);

        HttpResponse<JsonNode> response = mock(HttpResponse.class);
        when(response.getStatus()).thenReturn(200);
        when(response.getBody()).thenReturn(jsonNode);

        try (MockedStatic<Unirest> unirestMock = mockStatic(Unirest.class)) {
            com.mashape.unirest.request.GetRequest getRequest = mock(com.mashape.unirest.request.GetRequest.class);
            unirestMock.when(() -> Unirest.get(anyString())).thenReturn(getRequest);
            when(getRequest.asJson()).thenReturn(response);

            boolean result = ExternalApi.validateMail(email);
            assertFalse(result);
        }
    }
}

