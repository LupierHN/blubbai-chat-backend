package chat.blubbai.backend.service;

import chat.blubbai.backend.model.PhoneNumber;
import chat.blubbai.backend.model.User;
import chat.blubbai.backend.model.enums.Method2FA;
import chat.blubbai.backend.persistence.UserRepository;
import org.jboss.aerogear.security.otp.Totp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PhoneNumberService phoneNumberService;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    /**
     * Register a new user.
     * Encodes the password, ensures a phone number is set, and saves the user.
     * @param user User object containing the new user's information.
     * @return User object if registration is successful, null if no phone number is provided.
     */
    public User registerUser(User user)  {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (user.getPhoneNumber() != null) {
            PhoneNumber phoneNumber = phoneNumberService.createPhoneNumber(user.getPhoneNumber());
            if (phoneNumber != null) {
                user.setPhoneNumber(phoneNumber);
            }
        } else {
            return null;
        }
        return userRepository.save(user);
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
        user.setSecretMethod(method);
        userRepository.save(user);
    }

    /**
     * Generate a QR code URI for the user to set up 2FA.
     * @param user User object.
     * @return String URI for the QR code.
     */
    public String generateAuthQRCodeURI(User user) {
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
        return user.getSecret();
    }
}
