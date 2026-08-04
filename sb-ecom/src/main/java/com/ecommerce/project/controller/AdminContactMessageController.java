package com.ecommerce.project.controller;

import com.ecommerce.project.payload.APIResponse;
import com.ecommerce.project.payload.ContactMessageResponse;
import com.ecommerce.project.service.ContactMessageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/contact-messages")
public class AdminContactMessageController {
    private final ContactMessageService contactMessageService;

    public AdminContactMessageController(ContactMessageService contactMessageService) {
        this.contactMessageService = contactMessageService;
    }

    @GetMapping
    public ResponseEntity<List<ContactMessageResponse>> getAllContactMessages() {
        return ResponseEntity.ok(contactMessageService.getAll());
    }

    @GetMapping("/{contactMessageId}")
    public ResponseEntity<ContactMessageResponse> getContactMessage(@PathVariable Long contactMessageId) {
        return ResponseEntity.ok(contactMessageService.getById(contactMessageId));
    }

    @DeleteMapping("/{contactMessageId}")
    public ResponseEntity<APIResponse> deleteContactMessage(@PathVariable Long contactMessageId) {
        contactMessageService.delete(contactMessageId);
        return ResponseEntity.ok(new APIResponse("Contact message deleted successfully.", true));
    }
}
