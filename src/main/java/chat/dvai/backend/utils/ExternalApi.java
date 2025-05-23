package chat.dvai.backend.utils;

import chat.dvai.backend.model.PhoneNumber;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONException;

public class ExternalApi {

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
