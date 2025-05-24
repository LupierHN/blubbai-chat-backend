package chat.dvai.backend.service;

import chat.dvai.backend.model.PhoneNumber;
import chat.dvai.backend.model.User;
import chat.dvai.backend.persistence.UserRepository;
import org.jboss.aerogear.security.otp.Totp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PhoneNumberService phoneNumberService;

    /**
     * Get all users
     *
     * @return Iterable<User>
     */
    public Iterable<User> getUsers() {
        return this.userRepository.findAll();
    }

    /**
     * Get a user by id
     *
     * @param uId User id
     * @return User
     */
    public User getUser(Integer uId) {
        return this.userRepository.findById(uId).orElse(null);
    }

    /**
     * Get a user by username
     *
     * @param username Username of the user
     * @return User
     */
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Verify the 2FA code for a user
     *
     * @param user User object
     * @param code The 2FA code to verify
     * @return true if the code is valid, false otherwise
     */
    public boolean verify2faCode(User user, String code) {
        Totp totp = new Totp(getSecret(user));
        return totp.verify(code);
    }

    /**
     * Get the secret of a user
     *
     * @param user User object
     * @return String secret
     */
    private String getSecret(User user) {
        User u = getUser(user.getUId());
        if (u != null) {
            return userRepository.getSecretByUId(user.getUId());
        }
        return null;
    }

    /**
     * Get the password of a user
     *
     * @param user User object
     * @return String password
     */
    private String getPassword(User user) {
        return userRepository.getPasswordByUId(user.getUId());
    }

    /**
     * Send a 2FA code to the user via the specified method (e.g., SMS, email).
     *
     * @param user   User object
     * @param method The method to send the code (e.g., "SMS", "email")
     */
    public void send2faCode(User user, String method) {
        // This method is a placeholder for sending the 2FA code via the specified method.
        // Implementation would depend on the method (e.g., SMS, email) and is not provided here.
        // You can use a service like Twilio for SMS or JavaMailSender for email.
        // Following the Code is printed to the console for demonstration purposes.
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
     * Set the secret method for a user
     *
     * @param user   User object
     * @param method The secret method to set (e.g., "SMS", "email")
     */
    public void setSecretMethod(User user, String method) {
        User existingUser = getUser(user.getUId());
        if (existingUser != null) {
            existingUser.setSecretMethod(method);
            userRepository.save(existingUser);
        }
    }

    /**
     * Generate a QR code for the user to set up 2FA
     *
     * @param user User object
     * @return String URI for the QR code
     */
    public String generateAuthQRCode(User user) {
        String secret = getSecret(user);
        String name = user.getUsername()+"@BlubbAI";
        Totp totp = new Totp(secret);
        return totp.uri(name);
    }

    /**
     * Update user information
     *
     * @param loggedIn User object containing updated information
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
     * Update the password of a user
     *
     * @param user User object
     * @param password The new password to set
     */
    public void updatePassword(User user, String password) {
        User existingUser = getUser(user.getUId());
        if (existingUser != null) {
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            existingUser.setPassword(passwordEncoder.encode(password));
            userRepository.save(existingUser);
        }
    }

    /**
     * Validate the password of a user
     *
     * @param username The username of the user
     * @param password The password to validate
     * @return true if the password is valid, false otherwise
     */
    public boolean validatePassword(String username, String password) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return false;
        }
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder.matches(password, getPassword(user));
    }


    /**
     * Register a new user
     *
     * @param user User object containing the new user's information
     * @return User object if registration is successful, null if no phone number is provided
     */
    public User registerUser(User user) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        user.setPassword(passwordEncoder.encode(getPassword(user)));
        if (user.getPhoneNumber() != null) {
            PhoneNumber phoneNumber = phoneNumberService.createPhoneNumber(user.getPhoneNumber());
            if (phoneNumber != null) {
                user.setPhoneNumber(phoneNumber);
            }
        } else {
            // Return null if no phone number is provided (DB constraint)
            return null;
        }
        return userRepository.save(user);
    }

    /**
     * Delete a user
     *
     * @param user User object to be deleted
     * @return true if the user was deleted successfully, false otherwise
     */
    public boolean deleteUser(User user) {
        User existingUser = getUser(user.getUId());
        if (existingUser != null) {
            userRepository.delete(existingUser);
            return true;
        }
        return false;
    }
}
