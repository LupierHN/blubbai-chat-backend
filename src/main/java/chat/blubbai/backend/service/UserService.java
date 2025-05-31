package chat.blubbai.backend.service;

// ...existing imports...

import chat.blubbai.backend.model.PhoneNumber;
import chat.blubbai.backend.model.User;
import chat.blubbai.backend.model.enums.Method2FA;
import chat.blubbai.backend.persistence.UserRepository;
import org.jboss.aerogear.security.otp.Totp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * UserService
 *
 * This service provides business logic for user management, including:
 * <ul>
 *     <li>User CRUD operations</li>
 *     <li>Password management and validation</li>
 *     <li>Two-factor authentication (2FA) setup and verification</li>
 *     <li>Phone number management</li>
 * </ul>
 *
 * <h2>Usage:</h2>
 * This service is used by controllers and filters to perform user-related operations.
 */
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PhoneNumberService phoneNumberService;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    // -------------------- User CRUD Operations --------------------

    /**
     * Retrieve all users from the database.
     * @return Iterable of all users.
     */
    public Iterable<User> getUsers() {
        return this.userRepository.findAll();
    }

    /**
     * Retrieve a user by their unique ID.
     * @param uId User ID.
     * @return User object or null if not found.
     */
    public User getUser(UUID uId) {
        return this.userRepository.findByUId(uId);
    }

    /**
     * Retrieve a user by their username.
     * @param username Username.
     * @return User object or null if not found.
     */
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Update user information (username, email, phone number).
     * @param loggedIn User object containing updated information.
     */
    public void updateUser(User loggedIn) {
        User existingUser = getUser(loggedIn.getUId());
        if (existingUser != null) {
            existingUser.setUsername(loggedIn.getUsername());
            existingUser.setEmail(loggedIn.getEmail());
            existingUser.setPhoneNumber(loggedIn.getPhoneNumber());
            userRepository.save(existingUser);
        }
    }

    /**
     * Delete a user from the database.
     * @param user User object to be deleted.
     * @return true if the user was deleted successfully, false otherwise.
     */
    public boolean deleteUser(User user) {
        User existingUser = getUser(user.getUId());
        if (existingUser != null) {
            userRepository.delete(existingUser);
            return true;
        }
        return false;
    }

    // -------------------- Password Management --------------------

    /**
     * Update the password of a user.
     * @param user User object.
     * @param password The new password to set.
     */
    public void updatePassword(User user, String password) {
        User existingUser = getUser(user.getUId());
        if (existingUser != null) {
            existingUser.setPassword(passwordEncoder.encode(password));
            userRepository.save(existingUser);
        }
    }

    /**
     * Validate the password of a user.
     * @param username The username of the user.
     * @param password The password to validate.
     * @return true if the password is valid, false otherwise.
     */
    public boolean validatePassword(String username, String password) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return false;
        }
        return passwordEncoder.matches(password, user.getPassword());
    }

    // -------------------- Two-Factor Authentication (2FA) --------------------

    /**
     * Verify the 2FA code for a user.
     * @param user User object.
     * @param code The 2FA code to verify.
     * @return true if the code is valid, false otherwise.
     */
    public boolean verify2faCode(User user, String code) {
        Totp totp = new Totp(getSecret(user));
        return totp.verify(code);
    }

    /**
     * Send a 2FA code to the user via the specified method (e.g., SMS, email).
     * This is a placeholder; actual sending logic should be implemented as needed.
     * @param user   User object.
     * @param method The method to send the code (e.g., "SMS", "email").
     */
    public void send2faCode(User user, Method2FA method) {
        // This method is a placeholder for sending the 2FA code via the specified method.
        // Implementation would depend on the method (e.g., SMS, email) and is not provided here.
        // For demonstration, the code is printed to the console.
        String secret = getSecret(user);
        if (secret != null) {
            Totp totp = new Totp(secret);
            String code = totp.now();
            System.out.println("2FA Code for user " + user.getUsername() + ": " + code);
            // Here you would send the code via the specified method (SMS, email, etc.)
        } else {
            System.out.println("No secret found for user " + user.getUsername());
        }
    }

    /**
     * Set the secret method for a user (e.g., "2fa", "SMS", "email").
     * @param user   User object.
     * @param method The secret method to set.
     */
    public void setSecretMethod(User user, Method2FA method) {
        User existingUser = getUser(user.getUId());
        if (existingUser != null) {
            existingUser.setSecretMethod(method);
            userRepository.save(existingUser);
        }
    }

    /**
     * Generate a QR code URI for the user to set up 2FA.
     * @param user User object.
     * @return String URI for the QR code.
     */
    public String generateAuthQRCode(User user) {
        String secret = getSecret(user);
        String name = user.getUsername()+"@BlubbAI";
        Totp totp = new Totp(secret);
        return totp.uri(name)+"&issuer=BlubbAI";
    }

    // -------------------- Internal Helper Methods --------------------

    /**
     * Get the secret of a user for 2FA.
     * @param user User object.
     * @return String secret or null if not found.
     */
    private String getSecret(User user) {
        return getUser(user.getUId()).getSecret();
    }
}
