package chat.dvai.backend.service;

import chat.dvai.backend.model.PhoneNumber;
import chat.dvai.backend.persistence.PhoneNumberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PhoneNumberService {

    @Autowired
    private PhoneNumberRepository phoneNumberRepository;

    public PhoneNumber getPhoneNumberById(Integer id) {
        return phoneNumberRepository.findById(id).orElse(null);
    }

    public PhoneNumber createPhoneNumber(PhoneNumber phoneNumber) {
        if (phoneNumber.getPnId() != null) {
            throw new IllegalArgumentException("Phone number ID must be null for creation");
        }
        return phoneNumberRepository.save(phoneNumber);
    }

    public PhoneNumber updatePhoneNumber(PhoneNumber phoneNumber) {
        if (phoneNumber.getPnId() == null) {
            throw new IllegalArgumentException("Phone number ID must not be null for update");
        }
        return phoneNumberRepository.save(phoneNumber);
    }
}
