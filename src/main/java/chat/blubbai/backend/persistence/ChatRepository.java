package chat.blubbai.backend.persistence;

import chat.blubbai.backend.model.Chat;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ChatRepository extends CrudRepository<Chat, UUID> {
}
