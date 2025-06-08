package chat.blubbai.backend.web;

import chat.blubbai.backend.model.AccessTokenDTO;
import chat.blubbai.backend.model.RefreshToken;
import chat.blubbai.backend.model.TokenPairDTO;
import chat.blubbai.backend.model.User;
import chat.blubbai.backend.model.enums.ErrorResponse;
import chat.blubbai.backend.model.enums.Method2FA;
import chat.blubbai.backend.service.AuthService;
import chat.blubbai.backend.service.UserService;
import chat.blubbai.backend.utils.ExternalApi;
import chat.blubbai.backend.utils.TokenUtility;
import jakarta.mail.MessagingException;
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
import java.util.UUID;

@Controller
@AllArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {
    
    private final AuthService authService;
    private final UserService userService;
    /**
     * POST /api/v1/auth/noa/register
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
        User created;
        try {
            if (userService.getUserByUsername(user.getUsername()) != null) return new ResponseEntity<>(ErrorResponse.BAD_USERNAME,HttpStatus.CONFLICT); //Check if username already exists
            if (!ExternalApi.validateMail(user.getEmail())) return new ResponseEntity<>(ErrorResponse.BAD_EMAIL,HttpStatus.BAD_REQUEST); //Check if email is valid
            if (!ExternalApi.validatePhone(user.getPhoneNumber())) return new ResponseEntity<>(ErrorResponse.BAD_PHONE,HttpStatus.BAD_REQUEST); //Check if phone number is valid
            try {
                created = authService.registerUser(user); //Register the user and store in the database and send verification email
            }
            catch (MessagingException e) {
                return new ResponseEntity<>(ErrorResponse.BAD_EMAIL,HttpStatus.BAD_REQUEST); //If email sending fails, return 400 Bad Request
            }
            return new ResponseEntity<>(TokenUtility.generateAccessToken(created, false), HttpStatus.CREATED); //Generate access token for the user and return it with 201 Created status
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * POST /api/v1/auth/noa/login
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
        try {
            if (userService.getUserByUsername(user.getUsername()) == null) return new ResponseEntity<>(ErrorResponse.BAD_USERNAME,HttpStatus.UNAUTHORIZED);
            if (!authService.validatePassword(user.getUsername(), user.getPassword())) return new ResponseEntity<>(ErrorResponse.INVALID_PASSWORD,HttpStatus.UNAUTHORIZED);
            final User loggedIn = userService.getUserByUsername(user.getUsername());
            TokenPairDTO tokens = new TokenPairDTO(TokenUtility.generateAccessToken(loggedIn, false),TokenUtility.generateRefreshToken(loggedIn));
            return new ResponseEntity<>(tokens, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * GET /api/v1/auth/no2fa/2fa
     * <p>
     * Initiates or manages two-factor authentication (2FA) for the authenticated user.
     * Depending on the method, either generates a QR code for TOTP setup or sends a 2FA code.
     * The Authenticator Code is generated in the Authenticator app, while other methods (like SMS or email) send a code to the user.
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
                                        @RequestParam(value = "method", required = false) String method) {
        Method2FA method2fa;
        User user = userService.getUserByUsername((String) SecurityContextHolder.getContext().getAuthentication().getPrincipal()); // Get the authenticated user from the security context
        if (user == null) return new ResponseEntity<>(ErrorResponse.USER_NOT_FOUND, HttpStatus.NOT_FOUND); // If user is not found, return 404 Not Found
        if (method == null) { // If no method is provided, check the user's secret method in the database and use it
            method2fa = user.getSecretMethod();
            if (method2fa == null) return new ResponseEntity<>(HttpStatus.BAD_REQUEST); // If no method is set, return 400 Bad Request
        }else { // If a method is provided, convert it to the Method2FA enum
            method2fa = Method2FA.valueOf(method);
        }
        if (user.getSecretMethod() == null && Objects.equals(method2fa, Method2FA.AUTHENTICATOR)) { // If the user has no secret method set and the method is AUTHENTICATOR, generate a QR code
            authService.setSecretMethod(user, method2fa); // Set the user's secret method to AUTHENTICATOR to indicate that the registration is in progress/completed
            String qrCode = authService.generateAuthQRCodeURI(user); // Generate the QR code URI for the user
            return new ResponseEntity<>(qrCode, HttpStatus.OK);
        } else if (!Objects.equals(method2fa, Method2FA.AUTHENTICATOR)) { // If the method is not AUTHENTICATOR, send a 2FA code to the user
            if (user.getSecretMethod() == null) { // If the user has no secret method set, set it to the provided method
                authService.setSecretMethod(user, method2fa);
            }
            authService.send2faCode(user, method2fa);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * POST /api/v1/auth/no2fa/2fa
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
        User user = userService.getUserByUsername((String) SecurityContextHolder.getContext().getAuthentication().getPrincipal()); // Get the authenticated user from the security context
        if (user == null) return new ResponseEntity<>(ErrorResponse.USER_NOT_FOUND,HttpStatus.NOT_FOUND); // If user is not found, return 404 Not Found
        if (user.getSecretMethod() == null) return new ResponseEntity<>(ErrorResponse.METHOD_NOT_SET,HttpStatus.UNAUTHORIZED); // If the user has no secret method set, return 401 Unauthorized
        if (authService.verify2faCode(user, code)) { // Verify the 2FA code using the user's secret method
            AccessTokenDTO token = TokenUtility.generateAccessToken(user, true); // Generate a new access token for the user
            RefreshToken refreshToken = TokenUtility.generateRefreshToken(user); // Generate a new refresh token for the user
            return new ResponseEntity<>(new TokenPairDTO(token, refreshToken), HttpStatus.OK); // Return the new access token and refresh token with 200 OK status
        } else {
            return new ResponseEntity<>(ErrorResponse.INVALID_2FA,HttpStatus.UNAUTHORIZED); // If the code is invalid, return 401 Unauthorized with an error message
        }
    }

    /**
     * POST /api/v1/auth/noa/validateToken
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
     * POST /api/v1/auth/noa/renewToken
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
        if (TokenUtility.getUser(token, userService) == null) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        final AccessTokenDTO newToken = TokenUtility.renewToken(token, TokenUtility.getTokenFromHeader(authHeader));
        if (newToken == null) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        return new ResponseEntity<>(newToken, HttpStatus.OK);
    }

    @PatchMapping("/noa/2fa/verifyMail")
    public ResponseEntity<?> verifyMail(@RequestParam(value = "uuid", required = true) String uuid) {
        if (uuid == null || uuid.isEmpty()) return new ResponseEntity<>(ErrorResponse.BAD_USERNAME,HttpStatus.BAD_REQUEST); // If the UUID is null or empty, return 400 Bad Request
        User user = userService.getUserByUUID(uuid);
        if (user == null) return new ResponseEntity<>(ErrorResponse.USER_NOT_FOUND,HttpStatus.NOT_FOUND);
        if (user.isMailVerified()) return new ResponseEntity<>(HttpStatus.OK); // If the user is already verified, return 200 OK
        user.setMailVerified(true); // Set the user's mail as verified
        userService.saveUser(user); // Save the user to the database
        return new ResponseEntity<>(HttpStatus.OK); // Return 200 OK status
    }

}
