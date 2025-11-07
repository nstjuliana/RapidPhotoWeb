package com.rapidphotoupload.shared.exceptions;

/**
 * Exception thrown when authentication fails.
 * 
 * This exception is used when:
 * - Invalid credentials are provided
 * - Token validation fails
 * - User is not authenticated
 * 
 * @author RapidPhotoUpload Team
 * @since 1.0.0
 */
public class AuthenticationException extends DomainException {
    
    public AuthenticationException(String message) {
        super(message);
    }
    
    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}

