package chat.blubbai.backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.*;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer mId;
    private String content;
    private int author; // 0=user, 1=ai
    private Date sendDate;


    @ManyToOne
    @JoinColumn(name = "cId", referencedColumnName = "cId")
    private Chat chat;
}
