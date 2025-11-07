package com.rapidphotoupload.application.commands.upload;

import com.rapidphotoupload.domain.photo.Photo;
import com.rapidphotoupload.domain.photo.PhotoId;
import com.rapidphotoupload.domain.photo.PhotoRepository;
import com.rapidphotoupload.infrastructure.storage.StorageAdapter;
import com.rapidphotoupload.infrastructure.web.dto.UploadResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

/**
 * Command handler for uploading photos.
 * 
 * This handler orchestrates the upload flow:
 * 1. Validates the command (already validated in command constructor)
 * 2. Generates PhotoId and creates S3 key structure
 * 3. Creates Photo domain entity with PENDING status
 * 4. Generates presigned URL using StorageAdapter
 * 5. Saves Photo to repository
 * 6. Creates/updates UploadJob for batch tracking (optional)
 * 7. Returns UploadResponseDto with presigned URL
 * 
 * All operations are reactive and non-blocking, following WebFlux best practices.
 * 
 * @author RapidPhotoUpload Team
 * @since 1.0.0
 */
@Component
public class UploadPhotoCommandHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(UploadPhotoCommandHandler.class);
    
    /**
     * Default expiration time for presigned URLs (15 minutes).
     */
    private static final long DEFAULT_EXPIRATION_MINUTES = 15;
    
    private final PhotoRepository photoRepository;
    private final StorageAdapter storageAdapter;
    
    public UploadPhotoCommandHandler(
            PhotoRepository photoRepository,
            StorageAdapter storageAdapter) {
        this.photoRepository = photoRepository;
        this.storageAdapter = storageAdapter;
    }
    
    /**
     * Handles the upload photo command.
     * 
     * @param command The upload command containing photo metadata
     * @return Mono containing the upload response with presigned URL
     */
    public Mono<UploadResponseDto> handle(UploadPhotoCommand command) {
        logger.debug("Handling upload command for user: {}, filename: {}", 
                command.getUserId(), command.getFilename());
        
        // Generate PhotoId
        PhotoId photoId = PhotoId.generate();
        
        // Create S3 key structure: {userId}/{year}/{month}/{photoId}-{filename}
        String s3Key = buildS3Key(command.getUserId(), photoId, command.getFilename());
        
        // Create Photo domain entity with PENDING status
        LocalDateTime now = LocalDateTime.now();
        Photo photo = new Photo(
                photoId,
                command.getUserId(),
                null, // UploadJobId can be null for single uploads
                command.getFilename(),
                s3Key,
                now,
                command.getTags(),
                "PENDING"
        );
        
        // Generate presigned URL and save photo in parallel
        Mono<String> presignedUrlMono = storageAdapter.generatePresignedUploadUrl(
                s3Key,
                command.getContentType(),
                DEFAULT_EXPIRATION_MINUTES
        );
        
        Mono<Photo> savedPhotoMono = photoRepository.save(photo);
        
        // Combine both operations and build response
        return Mono.zip(presignedUrlMono, savedPhotoMono)
                .map(tuple -> {
                    String presignedUrl = tuple.getT1();
                    Photo savedPhoto = tuple.getT2();
                    
                    // Calculate expiration timestamp (current time + expiration minutes)
                    long expirationTime = System.currentTimeMillis() + 
                            (DEFAULT_EXPIRATION_MINUTES * 60 * 1000);
                    
                    logger.info("Generated presigned URL for photo: {}, user: {}", 
                            savedPhoto.getId(), savedPhoto.getUserId());
                    
                    return new UploadResponseDto(
                            savedPhoto.getId().toString(),
                            presignedUrl,
                            savedPhoto.getS3Key(),
                            expirationTime
                    );
                })
                .doOnError(error -> logger.error("Error handling upload command", error));
    }
    
    /**
     * Builds the S3 key structure: {userId}/{year}/{month}/{photoId}-{filename}
     * 
     * @param userId The user ID
     * @param photoId The photo ID
     * @param filename The original filename
     * @return The S3 key string
     */
    private String buildS3Key(com.rapidphotoupload.domain.user.UserId userId, PhotoId photoId, String filename) {
        LocalDateTime now = LocalDateTime.now();
        String year = String.valueOf(now.getYear());
        String month = String.format("%02d", now.getMonthValue());
        
        // Sanitize filename for S3 key (remove special characters, keep alphanumeric, dots, hyphens, underscores)
        String sanitizedFilename = filename.replaceAll("[^a-zA-Z0-9._-]", "_");
        
        return String.format("%s/%s/%s/%s-%s", 
                userId.toString(),
                year,
                month,
                photoId.toString(),
                sanitizedFilename);
    }
}

