package chat.dvai.backend.service;

// ...existing imports...

import chat.dvai.backend.model.PhoneNumber;
import chat.dvai.backend.persistence.PhoneNumberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * PhoneNumberService
 *
 * Provides business logic for managing phone numbers, including creation, retrieval, and update.
 */
@Service
public class PhoneNumberService {

    @Autowired
    private PhoneNumberRepository phoneNumberRepository;

    /**
     * Retrieve a phone number by its ID.
     * @param id Phone number ID.
     * @return PhoneNumber object or null if not found.
     */
    public PhoneNumber getPhoneNumberById(Integer id) {
        return phoneNumberRepository.findById(id).orElse(null);
    }

    /**
     * Create a new phone number entry.
     * @param phoneNumber PhoneNumber object to create (ID must be null).
     * @return The created PhoneNumber object.
     * @throws IllegalArgumentException if the ID is not null.
     */
    public PhoneNumber createPhoneNumber(PhoneNumber phoneNumber) {
        if (phoneNumber.getPnId() != null) {
            throw new IllegalArgumentException("Phone number ID must be null for creation");
        }
        return phoneNumberRepository.save(phoneNumber);
    }

    /**
     * Update an existing phone number entry.
     * @param phoneNumber PhoneNumber object to update (ID must not be null).
     * @return The updated PhoneNumber object.
     * @throws IllegalArgumentException if the ID is null.
     */
    public PhoneNumber updatePhoneNumber(PhoneNumber phoneNumber) {
        if (phoneNumber.getPnId() == null) {
            throw new IllegalArgumentException("Phone number ID must not be null for update");
        }
        return phoneNumberRepository.save(phoneNumber);
    }
}
