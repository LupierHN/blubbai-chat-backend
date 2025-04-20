package chat.dvai.backend.web;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.sql.DataSource;
import java.util.Base64;

@Controller
@AllArgsConstructor
@RequestMapping("/tools")
public class HealthController {
    private final DataSource dataSource;

    @GetMapping("/health")
    public ResponseEntity<String> getStatus() {
        return new ResponseEntity<>("OK", HttpStatus.OK);
    }

    @GetMapping("/key")
    public ResponseEntity<String> getKey() {
        String secretKey = Base64.getEncoder().encodeToString(Keys.secretKeyFor(SignatureAlgorithm.HS512).getEncoded());
        return new ResponseEntity<>(secretKey, HttpStatus.OK);
    }
}
