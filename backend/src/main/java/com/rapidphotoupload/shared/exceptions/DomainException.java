package com.rapidphotoupload.shared.exceptions;

/**
 * Base exception class for domain-related exceptions.
 * 
 * All domain exceptions should extend this class to provide a common exception hierarchy.
 * 
 * @author RapidPhotoUpload Team
 * @since 1.0.0
 */
public class DomainException extends RuntimeException {
    
    public DomainException(String message) {
        super(message);
    }
    
    public DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}

