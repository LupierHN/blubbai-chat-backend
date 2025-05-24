package chat.dvai.backend.web;

import chat.dvai.backend.model.PhoneNumber;
import chat.dvai.backend.model.Token;
import chat.dvai.backend.service.PhoneNumberService;
import chat.dvai.backend.utils.TokenUtility;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.util.Base64;

@Controller
@AllArgsConstructor
@RequestMapping("/tools")
public class ToolsController {
    private final DataSource dataSource;
    private final PhoneNumberService phoneNumberService;

    @GetMapping("/health")
    public ResponseEntity<String> getStatus() {
        return new ResponseEntity<>("OK", HttpStatus.OK);
    }

    @GetMapping("/key")
    public ResponseEntity<String> getKey() {
        String secretKey = Base64.getEncoder().encodeToString(Keys.secretKeyFor(SignatureAlgorithm.HS512).getEncoded());
        return new ResponseEntity<>(secretKey, HttpStatus.OK);
    }

//    @PostMapping("/test")
//    public ResponseEntity<String> getTest(@RequestBody PhoneNumber number) {
//        PhoneNumber createdNumber = phoneNumberService.createPhoneNumber(number);
//        if (createdNumber != null) {
//            return new ResponseEntity<>("Phone number created with ID: " + createdNumber.getPnId(), HttpStatus.CREATED);
//        } else {
//            return new ResponseEntity<>("Failed to create phone number", HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }

    @GetMapping("/token")
    public ResponseEntity<Token> getToken() {
        return new ResponseEntity<>(TokenUtility.createTestToken(), HttpStatus.OK);
    }

    @GetMapping("/bearer")
    public ResponseEntity<String> getBearerToken(@RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            return new ResponseEntity<>(token, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Invalid Authorization header", HttpStatus.BAD_REQUEST);
        }
    }

}
