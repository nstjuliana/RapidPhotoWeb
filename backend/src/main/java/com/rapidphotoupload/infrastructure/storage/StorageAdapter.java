package com.rapidphotoupload.infrastructure.storage;

import reactor.core.publisher.Mono;

/**
 * Interface for storage operations, abstracting the underlying storage implementation.
 * 
 * This interface provides methods for generating presigned URLs for direct client uploads/downloads
 * and deleting objects from storage. Implementations can use S3, Azure Blob Storage, or other providers.
 * 
 * @author RapidPhotoUpload Team
 * @since 1.0.0
 */
public interface StorageAdapter {
    
    /**
     * Generates a presigned URL for uploading an object directly from the client.
     * 
     * @param key The storage key (path) where the object will be stored
     * @param contentType The MIME type of the object (e.g., "image/jpeg")
     * @param expirationMinutes How long the URL should be valid (in minutes)
     * @return Mono containing the presigned URL string
     */
    Mono<String> generatePresignedUploadUrl(String key, String contentType, long expirationMinutes);
    
    /**
     * Generates a presigned URL for downloading an object directly from storage.
     * 
     * @param key The storage key (path) of the object to download
     * @param expirationMinutes How long the URL should be valid (in minutes)
     * @return Mono containing the presigned URL string
     */
    Mono<String> generatePresignedDownloadUrl(String key, long expirationMinutes);
    
    /**
     * Deletes an object from storage.
     * 
     * @param key The storage key (path) of the object to delete
     * @return Mono that completes when deletion is finished
     */
    Mono<Void> deleteObject(String key);
}

