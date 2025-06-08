package chat.blubbai.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity(name = "refresh_tokens")
public class RefreshToken {

    @Id
    @Column(nullable = false, updatable = false)
    @JsonIgnore
    private UUID id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "UUID", nullable = false, referencedColumnName = "UUID")
    private User user;

    @Column(nullable = false, unique = true, columnDefinition = "TEXT")
    private String token;

    @JsonIgnore
    @Column(nullable = false)
    private Instant expiresAt;

    @JsonIgnore
    @Column(nullable = false, updatable = false)
    private Instant issuedAt;

    @JsonIgnore
    @Column(nullable = false, updatable = false)
    private boolean revoked;


    @PrePersist
    private void prePersist() {
        this.id = UUID.randomUUID();
        this.issuedAt = Instant.now();
        this.revoked = false;
    }

}
