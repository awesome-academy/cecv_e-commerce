package com.example.cecv_e_commerce.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Custom exception thrown when a requested resource (e.g., User, Product, Order)
 * is not found in the system.
 *
 * Annotated with @ResponseStatus(HttpStatus.NOT_FOUND) so that Spring MVC can
 * automatically map this exception to a 404 Not Found HTTP status code
 * if it's not caught by a specific @ExceptionHandler.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s : '%s'", resourceName, fieldName, fieldValue));
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
