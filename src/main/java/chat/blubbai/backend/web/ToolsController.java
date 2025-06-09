package chat.blubbai.backend.web;

// ...existing imports...

import chat.blubbai.backend.model.AccessTokenDTO;
import chat.blubbai.backend.utils.TokenUtility;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Base64;

/**
 * ToolsController
 *
 * Provides utility endpoints for health checks, key generation, token creation, and extracting bearer tokens.
 * These endpoints are primarily for development, testing, and operational support.
 *
 * <h2>Endpoints:</h2>
 * <ul>
 *     <li><b>GET /tools/health</b> - Health check endpoint.</li>
 *     <li><b>GET /tools/key</b> - Generate a new secret key (Base64 encoded).</li>
 *     <li><b>GET /tools/token</b> - Generate a test token (for development).</li>
 *     <li><b>GET /tools/bearer</b> - Extracts the bearer token from the Authorization header.</li>
 * </ul>
 */
@Controller
@AllArgsConstructor
@RequestMapping("/tools")
public class ToolsController {

    /**
     * GET /tools/health
     * 
     * Health check endpoint.
     * 
     * <b>Response:</b> 200 OK with body "OK".
     */
    @GetMapping("/health")
    public ResponseEntity<String> getStatus() {
        return new ResponseEntity<>("OK", HttpStatus.OK);
    }

    /**
     * GET /tools/key
     *
     * Generates a new secret key for JWT signing (Base64 encoded).
     *
     * <b>Response:</b> 200 OK with the generated key as a string.
     */
    @GetMapping("/key")
    public ResponseEntity<String> getKey() {
        String secretKey = Base64.getEncoder().encodeToString(Keys.secretKeyFor(SignatureAlgorithm.HS512).getEncoded());
        return new ResponseEntity<>(secretKey, HttpStatus.OK);
    }


    /**
     * GET /tools/token
     *
     * Generates a test token for development purposes.
     *
     * <b>Response:</b> 200 OK with a Token object.
     */
    @GetMapping("/token")
    public ResponseEntity<AccessTokenDTO> getToken() {
        return new ResponseEntity<>(TokenUtility.createTestToken(), HttpStatus.OK);
    }

    /**
     * GET /tools/bearer
     *
     * Extracts the bearer token from the Authorization header.
     *
     * <b>Request:</b> Requires Authorization header with "Bearer &lt;token&gt;".
     * <b>Response:</b>
     * <ul>
     *     <li>200 OK: Returns the token string.</li>
     *     <li>400 Bad Request: If the Authorization header is missing or invalid.</li>
     * </ul>
     */
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
