package chat.dvai.backend.web;

import chat.dvai.backend.model.ErrorResponse;
import chat.dvai.backend.model.Token;
import chat.dvai.backend.model.User;
import chat.dvai.backend.service.UserService;
import chat.dvai.backend.utils.TokenUtility;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
    private static final ErrorResponse ERROR_1005 = new ErrorResponse(1005, "User not found");
    private static final ErrorResponse ERROR_4002 = new ErrorResponse(4002, "Wrong Password");
    private static final ErrorResponse ERROR_4004 = new ErrorResponse(4004, "User not found");


    /**
     * constants
     */
    private static final String SECRET_METHOD_2FA = "2fa";

    /**
     * Retrieves the currently authenticated user's profile information.
     *
     * @param authHeader The Authorization header containing the user's access token.
     * @return The user object if authentication is successful, or an error response otherwise.
     */
    @GetMapping
    public ResponseEntity<?> getUser(@RequestHeader("Authorization") String authHeader){
        if (!TokenUtility.validateAuthHeader(authHeader)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        User user = TokenUtility.getUserFromHeader(authHeader, userService);
        if (user == null) return new ResponseEntity<>(ERROR_1005, HttpStatus.INTERNAL_SERVER_ERROR);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    /**
     * Registers a new user account with the provided user details.
     *
     * @param user The user object containing registration information (username, email, etc.).
     * @return A list of authentication tokens upon successful registration, or an error response.
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody final User user) {
        List<Token> tokens = new ArrayList<>();
        try {
            if (userService.findUser(user.getUsername())) return new ResponseEntity<>(HttpStatus.CONFLICT);
            if (userService.validateMail(user.getEmail())) return new ResponseEntity<>(ERROR_1002,HttpStatus.BAD_REQUEST);
            if (userService.validatePhone(user.getPhoneNumber())) return new ResponseEntity<>(ERROR_1004,HttpStatus.BAD_REQUEST);
            final User created = userService.registerUser(user);
            tokens.add(TokenUtility.generateAccessToken(created));
            tokens.add(TokenUtility.generateRefreshToken(created));
            return new ResponseEntity<>(tokens, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Authenticates a user with the provided credentials.
     *
     * @param user The user object containing login credentials (username and password).
     * @return A list of authentication tokens if login is successful, or an error response.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody final User user) {
        List<Token> tokens = new ArrayList<>();
        try {
            if (!userService.findUser(user.getUsername())) return new ResponseEntity<>(ERROR_4004,HttpStatus.UNAUTHORIZED);
            if (!userService.validatePassword(user.getUsername(), user.getPassword())) return new ResponseEntity<>(ERROR_4002,HttpStatus.UNAUTHORIZED);
            final User loggedIn = userService.getUserByUsername(user.getUsername());
            tokens.add(TokenUtility.generateAccessToken(loggedIn));
            tokens.add(TokenUtility.generateRefreshToken(loggedIn));
            return new ResponseEntity<>(tokens, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Updates the authenticated user's profile information.
     *
     * @param authHeader The Authorization header containing the user's access token.
     * @param user The user object with updated profile data.
     * @param oldPassword The user's current password for verification.
     * @return The updated user object if successful, or an error response.
     */
    @PutMapping("/update")
    public ResponseEntity<?> updateUser(@RequestHeader("Authorization") String authHeader,
                                           @Valid @RequestBody final User user,
                                           @RequestParam(value = "oldPassword", required = true) String oldPassword) {
        if (!TokenUtility.validateAuthHeader(authHeader)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        User loggedIn = TokenUtility.getUserFromHeader(authHeader, userService);
        if (loggedIn == null) return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        if (!userService.validatePassword(loggedIn.getUsername(), oldPassword)) return new ResponseEntity<>(ERROR_4002, HttpStatus.UNAUTHORIZED);
        try {
            if (userService.validateMail(user.getEmail())) return new ResponseEntity<>(ERROR_1002,HttpStatus.BAD_REQUEST);
            if (userService.validatePhone(user.getPhoneNumber())) return new ResponseEntity<>(ERROR_1004,HttpStatus.BAD_REQUEST);
            loggedIn.setEmail(user.getEmail());
            loggedIn.setPhoneNumber(user.getPhoneNumber());
            loggedIn.setPassword(user.getPassword());
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
     * Deletes the currently authenticated user.
     * @param authHeader JWT token
     * @return 204 if deleted, 404 if not found
     */
    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteUser(@RequestHeader("Authorization") String authHeader) {
        if (!TokenUtility.validateAuthHeader(authHeader)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        User loggedIn = TokenUtility.getUserFromHeader(authHeader, userService);
        final boolean removed = userService.deleteUser(loggedIn);
        if (removed) return ResponseEntity.noContent().build();
        else return ResponseEntity.notFound().build();
    }


    /**
     * Initiates or manages two-factor authentication (2FA) for the authenticated user.
     * Depending on the method, either generates a QR code for TOTP setup or sends a 2FA code.
     *
     * @param authHeader The Authorization header containing the user's access token.
     * @param method The 2FA method to use (e.g., "2fa" for TOTP).
     * @return A QR code string for TOTP setup or a status response.
     */
    @GetMapping("/2fa")
    public ResponseEntity<?> get2faCode(@RequestHeader("Authorization") String authHeader,
                                             @RequestParam(value = "method", required = false) String method) {
        if (!TokenUtility.validateAuthHeader(authHeader)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        User user = TokenUtility.getUserFromHeader(authHeader, userService);
        if (user == null) return new ResponseEntity<>(ERROR_1005, HttpStatus.INTERNAL_SERVER_ERROR);
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
     * Verifies the submitted 2FA code for the authenticated user.
     *
     * @param authHeader The Authorization header containing the user's access token.
     * @param code The 2FA code to verify.
     * @return HTTP 200 if verification is successful, or HTTP 401 if the code is invalid.
     */
    @PostMapping("/2fa")
    public ResponseEntity<?> verify2fa(@RequestHeader("Authorization") String authHeader,
                                             @RequestParam(value = "code", required = true) String code) {
        if (!TokenUtility.validateAuthHeader(authHeader)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        User user = TokenUtility.getUserFromHeader(authHeader, userService);
        if (user == null) return new ResponseEntity<>(ERROR_1005, HttpStatus.INTERNAL_SERVER_ERROR);
        if (user.getSecretMethod() == null) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        if (userService.verify2faCode(user, code)) {
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * Validates a given token.
     * @param token Token object
     * @return true if valid, false otherwise
     */
    @PostMapping("/validateToken")
    public ResponseEntity<Boolean> validateToken(@RequestBody final Token token) {
        final boolean valid = TokenUtility.validateToken(token);
        return new ResponseEntity<>(valid, HttpStatus.OK);
    }


    /**
     * Renews a token if valid.
     * @param token Token to renew
     * @param authHeader JWT token
     * @return New token or 401 if invalid
     */
    @PostMapping("/renewToken")
    public ResponseEntity<Token> renewToken(@RequestBody final Token token, @RequestHeader("Authorization") String authHeader) {
        if (TokenUtility.getUser(token, userService) == null) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        final Token newToken = TokenUtility.renewToken(token, TokenUtility.getTokenFromHeader(authHeader));
        if (newToken == null) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        return new ResponseEntity<>(newToken, HttpStatus.OK);
    }
}
