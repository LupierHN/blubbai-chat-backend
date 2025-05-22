package chat.dvai.backend.web;

import chat.dvai.backend.model.ErrorResponse;
import chat.dvai.backend.model.Token;
import chat.dvai.backend.model.User;
import chat.dvai.backend.service.UserService;
import chat.dvai.backend.utils.TokenUtility;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
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
     * Get the user
     *
     * @return Iterable<User>
     */
    @GetMapping
    public ResponseEntity<?> getUser(@RequestHeader("Authorization") String authHeader){
        if (!TokenUtility.validateAuthHeader(authHeader)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        User user = TokenUtility.getUserFromHeader(authHeader, userService);
        if (user == null) return new ResponseEntity<>(ERROR_1005, HttpStatus.INTERNAL_SERVER_ERROR);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    /**
     * Register a new user
     *
     * @param user User
     * @return List<Token> Tokens
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
     * Login a user
     *
     * @param user User
     * @return List<Token> Tokens
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
     * Refresh the access token
     *
     * @param authHeader Authorization Header
     * @return Token
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
            if (!Objects.equals(user.getSecretMethod(), "2fa")) {
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


    @GetMapping("/2fa")
    public ResponseEntity<?> get2faCode(@RequestHeader("Authorization") String authHeader,
                                             @RequestParam(value = "method", required = false) String method) {
        if (!TokenUtility.validateAuthHeader(authHeader)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        User user = TokenUtility.getUserFromHeader(authHeader, userService);
        if (user == null) return new ResponseEntity<>(ERROR_1005, HttpStatus.INTERNAL_SERVER_ERROR);
        if (user.getSecretMethod() == null && Objects.equals(method, "2fa")) {
            userService.setSecretMethod(user, method);
            String qrCode = userService.generateAuthQRCode(user);
            return new ResponseEntity<>(qrCode, HttpStatus.OK);
        } else {
            if (user.getSecretMethod() == null) {
                userService.setSecretMethod(user, method);
            }
            userService.send2faCode(user, method);
            return new ResponseEntity<>(HttpStatus.OK);
        }
    }

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
}
