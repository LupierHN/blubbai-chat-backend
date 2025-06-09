package chat.blubbai.backend.persistence;

import chat.blubbai.backend.model.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserRepository extends CrudRepository<User, UUID> {
    User findByUsername(String username);
    User findByUUID(UUID uId);
}