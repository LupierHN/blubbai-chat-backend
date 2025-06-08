package chat.blubbai.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Chat {
    @Id
    @Column(nullable = false, updatable = false)
    private UUID cId;
    @Column(nullable = false)
    private String title;
    private String description;
    @Column(nullable = false, updatable = false)
    private Instant created;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JsonIgnore
    @JoinColumn(name = "UUID", referencedColumnName = "UUID")
    private User user;

    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Message> messages;

    @PrePersist
    private void prePersist() {
        this.cId = UUID.randomUUID();
        this.created = Instant.now();
    }
}
