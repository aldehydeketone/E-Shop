package com.ecommerce.project.payload;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ContactMessageResponse {
    private Long contactMessageId;
    private String name;
    private String email;
    private String message;
    private LocalDateTime createdAt;
}
