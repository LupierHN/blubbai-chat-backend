package chat.dvai.backend.service;

import chat.dvai.backend.model.PhoneNumber;
import chat.dvai.backend.model.User;
import chat.dvai.backend.persistence.PhoneNumberRepository;
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

    public User getUserByUsername(String username) {
        return this.userRepository.findByUsername(username);
    }

    public boolean verify2faCode(User user, String code) {
        Totp totp = new Totp(getSecret(user));
        return totp.verify(code);
    }

    private String getSecret(User user) {
        return "";
    }

    public void send2faCode(User user, String method) {
    }

    public void setSecretMethod(User user, String method) {
        User existingUser = userRepository.findByUsername(user.getUsername());
        if (existingUser != null) {
            existingUser.setSecretMethod(method);
            userRepository.save(existingUser);
        }
    }

    public String generateAuthQRCode(User user) {
        String secret = getSecret(user);
        String name = user.getUsername()+"@BlubbAI";
        Totp totp = new Totp(secret);
        return totp.uri(name);
    }

    public void updateUser(User loggedIn) {
    }

    public boolean validatePassword(String username, String oldPassword) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return false;
        }
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder.matches(oldPassword, user.getPassword());
    }

    public boolean findUser(String username) {
        return false;
    }

    public User registerUser(User user) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        user.setPassword(passwordEncoder.encode(user.getPassword()));
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

    public boolean deleteUser(User user) {
        return false;
    }
}
