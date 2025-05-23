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
    private PhoneNumberRepository phoneNumberRepository;

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
        return false;
    }

    public boolean findUser(String username) {
        return false;
    }

    public User registerUser(User user) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        // Save phone number first if not null
        if (user.getPhoneNumber() != null && user.getPhoneNumber().getPnID() == null) {
            PhoneNumber savedNumber = phoneNumberRepository.save(user.getPhoneNumber());
            user.setPhoneNumber(savedNumber);
        }
        return userRepository.save(user);
    }

    public boolean deleteUser(User user) {
        return false;
    }
}
