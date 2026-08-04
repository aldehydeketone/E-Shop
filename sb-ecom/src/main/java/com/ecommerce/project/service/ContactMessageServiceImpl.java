package com.ecommerce.project.service;

import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.ContactMessage;
import com.ecommerce.project.payload.ContactMessageRequest;
import com.ecommerce.project.payload.ContactMessageResponse;
import com.ecommerce.project.repositories.ContactMessageRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class ContactMessageServiceImpl implements ContactMessageService {
    private final ContactMessageRepository contactMessageRepository;

    public ContactMessageServiceImpl(ContactMessageRepository contactMessageRepository) {
        this.contactMessageRepository = contactMessageRepository;
    }

    @Override
    public void save(ContactMessageRequest request) {
        ContactMessage contactMessage = new ContactMessage();
        contactMessage.setName(request.getName().trim());
        contactMessage.setEmail(request.getEmail().trim().toLowerCase());
        contactMessage.setMessage(request.getMessage().trim());
        contactMessage.setCreatedAt(LocalDateTime.now());
        contactMessageRepository.save(contactMessage);
    }

    @Override
    public List<ContactMessageResponse> getAll() {
        return contactMessageRepository.findAll().stream()
                .sorted(Comparator.comparing(ContactMessage::getCreatedAt).reversed())
                .map(this::toResponse)
                .toList();
    }

    @Override
    public ContactMessageResponse getById(Long contactMessageId) {
        return toResponse(findContactMessage(contactMessageId));
    }

    @Override
    public void delete(Long contactMessageId) {
        contactMessageRepository.delete(findContactMessage(contactMessageId));
    }

    private ContactMessage findContactMessage(Long contactMessageId) {
        return contactMessageRepository.findById(contactMessageId)
                .orElseThrow(() -> new ResourceNotFoundException("Contact message", "contactMessageId", contactMessageId));
    }

    private ContactMessageResponse toResponse(ContactMessage contactMessage) {
        return new ContactMessageResponse(
                contactMessage.getContactMessageId(),
                contactMessage.getName(),
                contactMessage.getEmail(),
                contactMessage.getMessage(),
                contactMessage.getCreatedAt()
        );
    }
}
