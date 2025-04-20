package chat.dvai.backend.persistence;

import chat.dvai.backend.model.Role;
import chat.dvai.backend.model.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends CrudRepository<Role  , Integer> {
}
