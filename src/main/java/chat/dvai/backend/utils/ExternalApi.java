package chat.dvai.backend.utils;

import chat.dvai.backend.model.PhoneNumber;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONException;

public class ExternalApi {

    /**
     * Validates a phone number using the Abstract API.
     * This method constructs a request to the Abstract API's phone validation endpoint
     *
     * @param phoneNumber The phone number to validate.
     * @return true if the phone number is valid, false otherwise.
     * @throws UnirestException if there is an error with the HTTP request.
     * @throws JSONException if there is an error parsing the JSON response.
     */
    public static boolean validatePhone(PhoneNumber phoneNumber) throws UnirestException, JSONException {

        String request = "https://phonevalidation.abstractapi.com/v1/?"+
                "api_key="+EnvProvider.getEnv("PHONE_VALIDATION_API_KEY")+
                "&phone="+phoneNumber.getNumber()+
                "&country="+phoneNumber.getCountry();

        HttpResponse<JsonNode> response = Unirest.get(request).asJson();
        if (response.getStatus() == 200) {
            return response.getBody().getObject().getBoolean("valid");
        }
        return false;
    }

    /**
     * Validates an email address using the Abstract API.
     * This method constructs a request to the Abstract API's email validation endpoint
     *
     * @param email The email address to validate.
     * @return true if the email address is valid, false otherwise.
     * @throws UnirestException if there is an error with the HTTP request.
     * @throws JSONException if there is an error parsing the JSON response.
     */
    public static boolean validateMail(String email) throws UnirestException, JSONException {
        String request = "https://emailvalidation.abstractapi.com/v1" +
                "?api_key=" + EnvProvider.getEnv("MAIL_VALIDATION_API_KEY") +
                "&email=" + email +
                "&auto_correct=false";

        HttpResponse<JsonNode> response = Unirest.get(request).asJson();
        if (response.getStatus() == 200) {
            String deliverability = response.getBody().getObject().getString("deliverability");
            boolean isValidFormat = response.getBody().getObject().getJSONObject("is_valid_format").getBoolean("value");
            return "DELIVERABLE".equals(deliverability) && isValidFormat;
        }

        return false;
    }
}
