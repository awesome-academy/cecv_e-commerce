package com.example.cecv_e_commerce.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Custom exception for situations representing a client error (e.g., invalid input,
 * duplicate data) that should result in a 400 Bad Request response.
 *
 * Extends RuntimeException to make it an unchecked exception.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }

    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
