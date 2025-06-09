package chat.blubbai.backend.web;

import chat.blubbai.backend.model.enums.ErrorResponse;
import chat.blubbai.backend.model.AccessTokenDTO;
import chat.blubbai.backend.model.User;
import chat.blubbai.backend.model.enums.Method2FA;
import chat.blubbai.backend.service.AuthService;
import chat.blubbai.backend.service.UserService;
import chat.blubbai.backend.utils.TokenUtility;
import chat.blubbai.backend.utils.ExternalApi;
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
    private final AuthService authService;

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
        User user = userService.getUserByUsername((String) SecurityContextHolder.getContext().getAuthentication().getPrincipal()); // get the username from the security context
        if (user == null) return new ResponseEntity<>(ErrorResponse.USER_NOT_FOUND,HttpStatus.NOT_FOUND); // if user is not found, return 404
        return new ResponseEntity<>(user, HttpStatus.OK); // return the user object with 200 OK
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
    // TODO: 2FA übergabe verbessern -> wie STRING in ENUM umwandeln
    @PutMapping("/update")
    public ResponseEntity<?> updateUser(@RequestHeader("Authorization") String authHeader,
                                           @Valid @RequestBody final User user,
                                           @RequestParam(value = "oldPassword", required = true) String oldPassword) {
        User loggedIn = userService.getUserByUsername((String) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        if (!authService.validatePassword(loggedIn.getUsername(), oldPassword)) return new ResponseEntity<>(ErrorResponse.INVALID_PASSWORD, HttpStatus.UNAUTHORIZED);
        try {
            if (!ExternalApi.validateMail(user.getEmail())) return new ResponseEntity<>(ErrorResponse.BAD_EMAIL,HttpStatus.BAD_REQUEST);
            if (!ExternalApi.validatePhone(user.getPhoneNumber())) return new ResponseEntity<>(ErrorResponse.BAD_PHONE,HttpStatus.BAD_REQUEST);
            loggedIn.setEmail(user.getEmail());
            loggedIn.setPhoneNumber(user.getPhoneNumber());
            if (user.getPassword() != null) userService.updatePassword(loggedIn, user.getPassword());
            if (!Objects.equals(user.getSecretMethod(), Method2FA.AUTHENTICATOR)) {
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
}
