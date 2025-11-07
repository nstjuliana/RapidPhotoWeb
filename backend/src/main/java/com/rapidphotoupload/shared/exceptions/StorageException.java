package com.rapidphotoupload.shared.exceptions;

/**
 * Exception thrown when storage operations fail.
 * 
 * This exception is used when S3 or other storage operations encounter errors,
 * such as failed presigned URL generation or object deletion failures.
 * 
 * @author RapidPhotoUpload Team
 * @since 1.0.0
 */
public class StorageException extends DomainException {
    
    public StorageException(String message) {
        super(message);
    }
    
    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}

