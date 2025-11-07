package com.rapidphotoupload.shared.exceptions;

/**
 * Exception thrown when domain validation fails.
 * 
 * This exception is used when business rules or validation constraints are violated.
 * 
 * @author RapidPhotoUpload Team
 * @since 1.0.0
 */
public class ValidationException extends DomainException {
    
    public ValidationException(String message) {
        super(message);
    }
    
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}

