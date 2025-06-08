package chat.blubbai.backend.model;

import chat.blubbai.backend.model.enums.Method2FA;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.jboss.aerogear.security.otp.api.Base32;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity(name = "account")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @Column(nullable = false, updatable = false)
    private UUID uId;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @JsonIgnore
    @Column(nullable = false, updatable = false, unique = true)
    private String secret;

    @Enumerated(EnumType.STRING)
    private Method2FA secretMethod;

    @JsonIgnore
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @JsonIgnore
    @Column(nullable = false)
    private Instant updatedAt;

    @JsonIgnore
    @Column(nullable = false)
    private boolean mailVerified = false;

    @ManyToOne
    @JoinColumn(name = "rId", referencedColumnName = "rId")
    private Role role;

    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Chat> chats;

    @JsonIgnore
    @OneToOne(cascade = CascadeType.REMOVE, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "pnId", referencedColumnName = "pnId")
    private PhoneNumber phoneNumber;

    @PrePersist
    private void prePersist() {
        this.uId = UUID.randomUUID();
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        this.secret = Base32.random();
        this.mailVerified = false;
    }

    @PreUpdate
    private void preUpdate() {
        this.updatedAt = Instant.now();
    }
}
