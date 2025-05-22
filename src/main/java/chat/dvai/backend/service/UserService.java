package chat.dvai.backend.service;

import chat.dvai.backend.model.PhoneNumber;
import chat.dvai.backend.model.User;
import chat.dvai.backend.persistence.UserRepository;
import jakarta.validation.Valid;
import org.jboss.aerogear.security.otp.Totp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

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

    public boolean validatePhone(PhoneNumber phoneNumber) {
        return false;
    }

    public boolean validateMail(String email) {
        return false;
    }

    public boolean validatePassword(String username, String oldPassword) {
        return false;
    }

    public boolean findUser(String username) {
        return false;
    }

    public User registerUser(@Valid User user) {
        return user;
    }

    public boolean deleteUser(User user) {
        return false;
    }
}
