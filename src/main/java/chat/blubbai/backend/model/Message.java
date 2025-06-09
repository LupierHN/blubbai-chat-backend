package chat.blubbai.backend.model;

import chat.blubbai.backend.model.enums.Sender;
import jakarta.persistence.Entity;
import jakarta.persistence.*;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Message {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID mId;

    @Column(nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Sender author;

    @Column(nullable = false, updatable = false)
    private Instant sendDate;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cId", referencedColumnName = "cId")
    private Chat chat;

    @PrePersist
    private void prePersist() {
        this.mId = UUID.randomUUID();
        this.sendDate = Instant.now();
    }
}
