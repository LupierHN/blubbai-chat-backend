package chat.blubbai.backend.service;

import chat.blubbai.backend.model.PhoneNumber;
import chat.blubbai.backend.model.User;
import chat.blubbai.backend.persistence.UserRepository;
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

}
