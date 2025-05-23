package chat.dvai.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jboss.aerogear.security.otp.api.Base32;

import java.util.List;

@Entity(name = "account")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer uId;
    private String username;
    private String email;
    private String password;
    @JsonIgnore
    private String secret = Base32.random();
    private String secretMethod;

    @ManyToOne
    @JoinColumn(name = "rId", referencedColumnName = "rId")
    private Role role;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Chat> chats;

    @OneToOne
    @JoinColumn(name = "pnId", referencedColumnName = "pnID")
    private PhoneNumber phoneNumber;
}
