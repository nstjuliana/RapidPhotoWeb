package com.rapidphotoupload.shared.exceptions;

/**
 * Exception thrown when an entity is not found in the repository.
 * 
 * This exception is used when attempting to retrieve an entity by ID that does not exist.
 * 
 * @author RapidPhotoUpload Team
 * @since 1.0.0
 */
public class EntityNotFoundException extends DomainException {
    
    public EntityNotFoundException(String message) {
        super(message);
    }
    
    public EntityNotFoundException(String entityType, String identifier) {
        super(String.format("%s not found with identifier: %s", entityType, identifier));
    }
}

