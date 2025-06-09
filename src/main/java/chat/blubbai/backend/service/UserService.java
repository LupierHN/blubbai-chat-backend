package chat.blubbai.backend.service;

// ...existing imports...

import chat.blubbai.backend.model.User;
import chat.blubbai.backend.persistence.UserRepository;
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
        return this.userRepository.findByUUID(uId);
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
        User existingUser = getUser(loggedIn.getUUID());
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
        User existingUser = getUser(user.getUUID());
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
        User existingUser = getUser(user.getUUID());
        if (existingUser != null) {
            existingUser.setPassword(passwordEncoder.encode(password));
            userRepository.save(existingUser);
        }
    }

    public void setMailVerified(User user) {
        user.setMailVerified(true);
        userRepository.save(user);
    }
}
