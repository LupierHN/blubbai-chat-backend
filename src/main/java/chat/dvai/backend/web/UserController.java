package chat.dvai.backend.web;

import chat.dvai.backend.model.ErrorResponse;
import chat.dvai.backend.model.Token;
import chat.dvai.backend.model.User;
import chat.dvai.backend.service.UserService;
import chat.dvai.backend.utils.TokenUtility;
import chat.dvai.backend.utils.ExternalApi;
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

/**
 * UserController
 * <p>
 * This controller provides RESTful endpoints for user management, authentication, registration,
 * profile updates, two-factor authentication (2FA), and token operations.
 *
 * <h2>Endpoints:</h2>
 * <ul>
 *     <li><b>GET /api/v1/user</b> - Retrieve the authenticated user's profile.</li>
 *     <li><b>POST /api/v1/user/noa/register</b> - Register a new user.</li>
 *     <li><b>POST /api/v1/user/noa/login</b> - Authenticate a user and obtain tokens.</li>
 *     <li><b>PUT /api/v1/user/update</b> - Update the authenticated user's profile.</li>
 *     <li><b>DELETE /api/v1/user/delete</b> - Delete the authenticated user.</li>
 *     <li><b>GET /api/v1/user/no2fa/2fa</b> - Initiate or manage 2FA for the user.</li>
 *     <li><b>POST /api/v1/user/no2fa/2fa</b> - Verify a submitted 2FA code.</li>
 *     <li><b>POST /api/v1/user/noa/validateToken</b> - Validate a token.</li>
 *     <li><b>POST /api/v1/user/noa/renewToken</b> - Renew a token.</li>
 * </ul>
 *
 * <h2>Security & Filters:</h2>
 * <ul>
 *     <li>JWT authentication is required for most endpoints (except /noa/*).</li>
 *     <li>2FA is enforced via a filter for protected endpoints. (except /noa/* and /no2fa7*)</li>
 * </ul>
 *
 * <h2>Error Handling:</h2>
 * <ul>
 *     <li>Returns appropriate HTTP status codes and error messages for invalid input, authentication failures, and server errors.</li>
 * </ul>
 */
@Controller
@AllArgsConstructor
@RequestMapping("/api/v1/user")
public class UserController {

    private final UserService userService;

    /**
     * Error Codes
     */
    private static final ErrorResponse ERROR_1002 = new ErrorResponse(1002, "Invalid E-Mail");
    private static final ErrorResponse ERROR_1004 = new ErrorResponse(1004, "Invalid Phone");
    private static final ErrorResponse ERROR_4002 = new ErrorResponse(4002, "Wrong Password");
    private static final ErrorResponse ERROR_4003 = new ErrorResponse(4003, "2FA Code wrong or expired");


    /**
     * constants
     */
    private static final String SECRET_METHOD_2FA = "2fa";

    /**
     * GET /api/v1/user
     * <p>
     * Retrieves the currently authenticated user's profile information.
     * <p>
     * <b>Request:</b> Requires Authorization header with a valid JWT access token.<br>
     * <b>Response:</b>
     * <ul>
     *     <li>200 OK: User object</li>
     *     <li>404 Not Found: If user does not exist</li>
     *     <li>401 Unauthorized: If JWT is missing or invalid (handled by filter)</li>
     *     <li>403 Forbidden: If 2FA is required but not completed (handled by filter)</li>
     * </ul>
     */
    @GetMapping
    public ResponseEntity<?> getUser(@RequestHeader("Authorization") String authHeader){
        User user = userService.getUserByUsername((String) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        if (user == null) return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

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
        List<Token> tokens = new ArrayList<>();
        try {
            if (userService.getUserByUsername(user.getUsername()) != null) return new ResponseEntity<>(HttpStatus.CONFLICT);
            if (!ExternalApi.validateMail(user.getEmail())) return new ResponseEntity<>(ERROR_1002,HttpStatus.BAD_REQUEST);
            if (!ExternalApi.validatePhone(user.getPhoneNumber())) return new ResponseEntity<>(ERROR_1004,HttpStatus.BAD_REQUEST);
            final User created = userService.registerUser(user);
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
        List<Token> tokens = new ArrayList<>();
        try {
            if (userService.getUserByUsername(user.getUsername()) != null) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            if (!userService.validatePassword(user.getUsername(), user.getPassword())) return new ResponseEntity<>(ERROR_4002,HttpStatus.UNAUTHORIZED);
            final User loggedIn = userService.getUserByUsername(user.getUsername());
            tokens.add(TokenUtility.generateAccessToken(loggedIn, false));
            tokens.add(TokenUtility.generateRefreshToken(loggedIn));
            return new ResponseEntity<>(tokens, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * PUT /api/v1/user/update
     * <p>
     * Updates the authenticated user's profile information.
     * <p>
     * <b>Request:</b>
     * <ul>
     *     <li>Authorization header with valid JWT</li>
     *     <li>JSON body with updated user data</li>
     *     <li>Query param: oldPassword (required)</li>
     * </ul>
     * <b>Response:</b>
     * <ul>
     *     <li>200 OK: Updated user object</li>
     *     <li>400 Bad Request: Invalid email or phone</li>
     *     <li>401 Unauthorized: Wrong password or not authenticated</li>
     *     <li>500 Internal Server Error: On unexpected error</li>
     * </ul>
     */
    @PutMapping("/update")
    public ResponseEntity<?> updateUser(@RequestHeader("Authorization") String authHeader,
                                           @Valid @RequestBody final User user,
                                           @RequestParam(value = "oldPassword", required = true) String oldPassword) {
        User loggedIn = userService.getUserByUsername((String) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        if (!userService.validatePassword(loggedIn.getUsername(), oldPassword)) return new ResponseEntity<>(ERROR_4002, HttpStatus.UNAUTHORIZED);
        try {
            if (!ExternalApi.validateMail(user.getEmail())) return new ResponseEntity<>(ERROR_1002,HttpStatus.BAD_REQUEST);
            if (!ExternalApi.validatePhone(user.getPhoneNumber())) return new ResponseEntity<>(ERROR_1004,HttpStatus.BAD_REQUEST);
            loggedIn.setEmail(user.getEmail());
            loggedIn.setPhoneNumber(user.getPhoneNumber());
            if (user.getPassword() != null) userService.updatePassword(loggedIn, user.getPassword());
            if (!Objects.equals(user.getSecretMethod(), SECRET_METHOD_2FA)) {
                loggedIn.setSecretMethod(null);
            }else {
                loggedIn.setSecretMethod(user.getSecretMethod());
            }
            userService.updateUser(loggedIn);
            return new ResponseEntity<>(loggedIn, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * DELETE /api/v1/user/delete
     * <p>
     * Deletes the currently authenticated user.
     * <p>
     * <b>Request:</b> Authorization header with valid JWT.<br>
     * <b>Response:</b>
     * <ul>
     *     <li>204 No Content: User deleted</li>
     *     <li>404 Not Found: User not found</li>
     *     <li>401 Unauthorized: If JWT is missing or invalid (handled by filter)</li>
     * </ul>
     */
    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteUser(@RequestHeader("Authorization") String authHeader) {
        User loggedIn = userService.getUserByUsername((String) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        final boolean removed = userService.deleteUser(loggedIn);
        if (removed) return ResponseEntity.noContent().build();
        else return ResponseEntity.notFound().build();
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
                                             @RequestParam(value = "method", required = false) String method) {
        User user = userService.getUserByUsername((String) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        if (user == null) return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        if (method == null) {
            method = user.getSecretMethod();
            if (method == null) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (user.getSecretMethod() == null && Objects.equals(method, SECRET_METHOD_2FA)) {
            userService.setSecretMethod(user, method);
            String qrCode = userService.generateAuthQRCode(user);
            return new ResponseEntity<>(qrCode, HttpStatus.OK);
        } else {
            if (user.getSecretMethod() == null) {
                userService.setSecretMethod(user, method);
            }
            if (Objects.equals(user.getSecretMethod(), SECRET_METHOD_2FA)) {
                return new ResponseEntity<>(HttpStatus.OK);
            }
            userService.send2faCode(user, method);
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
        User user = userService.getUserByUsername((String) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        if (user == null) return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        if (user.getSecretMethod() == null) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        if (userService.verify2faCode(user, code)) {
            Token token = TokenUtility.generateAccessToken(user, true);
            return new ResponseEntity<>(token, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(ERROR_4003,HttpStatus.UNAUTHORIZED);
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
    public ResponseEntity<Boolean> validateToken(@RequestBody final Token token) {
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
    public ResponseEntity<Token> renewToken(@RequestBody final Token token, @RequestHeader("Authorization") String authHeader) {
        if (TokenUtility.getUser(token, userService) == null) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        final Token newToken = TokenUtility.renewToken(token, TokenUtility.getTokenFromHeader(authHeader));
        if (newToken == null) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        return new ResponseEntity<>(newToken, HttpStatus.OK);
    }
}
