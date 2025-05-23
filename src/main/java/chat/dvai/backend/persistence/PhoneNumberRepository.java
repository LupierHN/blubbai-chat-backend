package chat.dvai.backend.persistence;

import chat.dvai.backend.model.PhoneNumber;
import org.springframework.data.repository.CrudRepository;

public interface PhoneNumberRepository extends CrudRepository<PhoneNumber, Integer> {
}
