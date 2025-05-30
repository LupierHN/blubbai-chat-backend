package chat.blubbai.backend.persistence;

import chat.blubbai.backend.model.PhoneNumber;
import org.springframework.data.repository.CrudRepository;

public interface PhoneNumberRepository extends CrudRepository<PhoneNumber, Integer> {
}
