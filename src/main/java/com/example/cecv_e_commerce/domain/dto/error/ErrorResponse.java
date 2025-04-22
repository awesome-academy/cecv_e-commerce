package com.example.cecv_e_commerce.domain.dto.error;

import java.time.LocalDateTime;

public record ErrorResponse(int status, String message, LocalDateTime timestamp) {
    public ErrorResponse(int status, String message) {
        this(status, message, LocalDateTime.now());
    }
}
