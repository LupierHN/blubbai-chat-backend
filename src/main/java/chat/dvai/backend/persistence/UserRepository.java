package chat.dvai.backend.persistence;

import chat.dvai.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends CrudRepository<User, Integer> {
    User findByUsername(String username);

    String getSecretByUId(Integer uId);

    String getPasswordByUId(Integer uId);
}
