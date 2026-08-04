package com.ecommerce.project.service;

import com.ecommerce.project.payload.ContactMessageRequest;
import com.ecommerce.project.payload.ContactMessageResponse;

import java.util.List;

public interface ContactMessageService {
    void save(ContactMessageRequest request);
    List<ContactMessageResponse> getAll();
    ContactMessageResponse getById(Long contactMessageId);
    void delete(Long contactMessageId);
}
