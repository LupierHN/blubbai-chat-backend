package chat.blubbai.backend.web;

import chat.blubbai.backend.model.AccessTokenDTO;
import chat.blubbai.backend.model.User;
import chat.blubbai.backend.model.enums.ErrorResponse;
import chat.blubbai.backend.model.enums.Method2FA;
import chat.blubbai.backend.service.AuthService;
import chat.blubbai.backend.service.UserService;
import chat.blubbai.backend.utils.ExternalApi;
import chat.blubbai.backend.utils.TokenUtility;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Controller
@AllArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {
    
    private final AuthService authService;
    private final UserService userService;
    /**
     * POST /api/v1/user/noa/register
     * <p>
     * Registers a new user account with the provided user details.
     * <p>
     * <b>Request:</b> JSON body with user details (username, email, phone, password, etc.)<br>
     * <b>Response:</b>
     * <ul>
     *     <li>201 Created: List of authentication tokens (access, refresh)</li>
     *     <li>400 Bad Request: Invalid email or phone</li>
     *     <li>409 Conflict: Username already exists</li>
     *     <li>500 Internal Server Error: On unexpected error</li>
     * </ul>
     */
    @PostMapping("/noa/register")
    public ResponseEntity<?> register(@Valid @RequestBody final User user) {
        List<AccessTokenDTO> tokens = new ArrayList<>();
        try {
            if (userService.getUserByUsername(user.getUsername()) != null) return new ResponseEntity<>(ErrorResponse.BAD_USERNAME,HttpStatus.CONFLICT);
            if (!ExternalApi.validateMail(user.getEmail())) return new ResponseEntity<>(ErrorResponse.BAD_EMAIL,HttpStatus.BAD_REQUEST);
            if (!ExternalApi.validatePhone(user.getPhoneNumber())) return new ResponseEntity<>(ErrorResponse.BAD_PHONE,HttpStatus.BAD_REQUEST);
            final User created = authService.registerUser(user);
            tokens.add(TokenUtility.generateAccessToken(created, false));
            tokens.add(TokenUtility.generateRefreshToken(created));
            return new ResponseEntity<>(tokens, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * POST /api/v1/user/noa/login
     * <p>
     * Authenticates a user with the provided credentials.
     * <p>
     * <b>Request:</b> JSON body with username and password.<br>
     * <b>Response:</b>
     * <ul>
     *     <li>200 OK: List of authentication tokens (access, refresh)</li>
     *     <li>401 Unauthorized: Invalid credentials</li>
     *     <li>500 Internal Server Error: On unexpected error</li>
     * </ul>
     */
    @PostMapping("/noa/login")
    public ResponseEntity<?> login(@Valid @RequestBody final User user) {
        List<AccessTokenDTO> tokens = new ArrayList<>();
        try {
            if (userService.getUserByUsername(user.getUsername()) != null) return new ResponseEntity<>(ErrorResponse.BAD_USERNAME,HttpStatus.UNAUTHORIZED);
            if (!authService.validatePassword(user.getUsername(), user.getPassword())) return new ResponseEntity<>(ErrorResponse.INVALID_PASSWORD,HttpStatus.UNAUTHORIZED);
            final User loggedIn = userService.getUserByUsername(user.getUsername());
            tokens.add(TokenUtility.generateAccessToken(loggedIn, false));
            tokens.add(TokenUtility.generateRefreshToken(loggedIn));
            return new ResponseEntity<>(tokens, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * GET /api/v1/user/no2fa/2fa
     * <p>
     * Initiates or manages two-factor authentication (2FA) for the authenticated user.
     * Depending on the method, either generates a QR code for TOTP setup or sends a 2FA code.
     * <p>
     * <b>Request:</b>
     * <ul>
     *     <li>Authorization header with valid JWT</li>
     *     <li>Query param: method (optional, e.g., "2fa")</li>
     * </ul>
     * <b>Response:</b>
     * <ul>
     *     <li>200 OK: QR code string or status</li>
     *     <li>400 Bad Request: If method is missing</li>
     *     <li>404 Not Found: User not found</li>
     *     <li>401 Unauthorized: If JWT is missing or invalid (handled by filter)</li>
     * </ul>
     */
    @GetMapping("/no2fa/2fa")
    public ResponseEntity<?> get2faCode(@RequestHeader("Authorization") String authHeader,
                                        @RequestParam(value = "method", required = false) Method2FA method) {
        User user = userService.getUserByUsername((String) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        if (user == null) return new ResponseEntity<>(ErrorResponse.USER_NOT_FOUND, HttpStatus.NOT_FOUND);
        if (method == null) {
            method = user.getSecretMethod();
            if (method == null) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (user.getSecretMethod() == null && Objects.equals(method, Method2FA.AUTHENTICATOR)) {
            userService.setSecretMethod(user, method);
            String qrCode = authService.generateAuthQRCode(user);
            return new ResponseEntity<>(qrCode, HttpStatus.OK);
        } else {
            if (user.getSecretMethod() == null) {
                authService.setSecretMethod(user, method);
            }
            if (Objects.equals(user.getSecretMethod(), Method2FA.AUTHENTICATOR)) {
                return new ResponseEntity<>(HttpStatus.OK);
            }
            authService.send2faCode(user, method);
            return new ResponseEntity<>(HttpStatus.OK);
        }
    }

    /**
     * POST /api/v1/user/no2fa/2fa
     * <p>
     * Verifies the submitted 2FA code for the authenticated user.
     * <p>
     * <b>Request:</b>
     * <ul>
     *     <li>Authorization header with valid JWT</li>
     *     <li>Query param: code (required)</li>
     * </ul>
     * <b>Response:</b>
     * <ul>
     *     <li>200 OK: If verification is successful (returns new access token)</li>
     *     <li>401 Unauthorized: If code is invalid or 2FA not set up</li>
     *     <li>404 Not Found: User not found</li>
     * </ul>
     */
    @PostMapping("/no2fa/2fa")
    public ResponseEntity<?> verify2fa(@RequestHeader("Authorization") String authHeader,
                                       @RequestParam(value = "code", required = true) String code) {
        User user = authService.getUserByUsername((String) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        if (user == null) return new ResponseEntity<>(ErrorResponse.USER_NOT_FOUND,HttpStatus.NOT_FOUND);
        if (user.getSecretMethod() == null) return new ResponseEntity<>(ErrorResponse.METHOD_NOT_SET,HttpStatus.UNAUTHORIZED);
        if (authService.verify2faCode(user, code)) {
            AccessTokenDTO token = TokenUtility.generateAccessToken(user, true);
            return new ResponseEntity<>(token, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(ErrorResponse.INVALID_2FA,HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * POST /api/v1/user/noa/validateToken
     * <p>
     * Validates a given token.
     * <p>
     * <b>Request:</b> JSON body with token.<br>
     * <b>Response:</b>
     * <ul>
     *     <li>200 OK: true if valid, false otherwise</li>
     * </ul>
     */
    @PostMapping("/noa/validateToken")
    public ResponseEntity<Boolean> validateToken(@RequestBody final AccessTokenDTO token) {
        final boolean valid = TokenUtility.validateToken(token);
        return new ResponseEntity<>(valid, HttpStatus.OK);
    }

    /**
     * POST /api/v1/user/noa/renewToken
     * <p>
     * Renews a token if valid.
     * <p>
     * <b>Request:</b>
     * <ul>
     *     <li>JSON body with token</li>
     *     <li>Authorization header with valid JWT</li>
     * </ul>
     * <b>Response:</b>
     * <ul>
     *     <li>200 OK: New token</li>
     *     <li>401 Unauthorized: If token is invalid or user not found</li>
     * </ul>
     */
    @PostMapping("/noa/renewToken")
    public ResponseEntity<AccessTokenDTO> renewToken(@RequestBody final AccessTokenDTO token, @RequestHeader("Authorization") String authHeader) {
        if (TokenUtility.getUser(token, authService) == null) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        final AccessTokenDTO newToken = TokenUtility.renewToken(token, TokenUtility.getTokenFromHeader(authHeader));
        if (newToken == null) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        return new ResponseEntity<>(newToken, HttpStatus.OK);
    }
}
