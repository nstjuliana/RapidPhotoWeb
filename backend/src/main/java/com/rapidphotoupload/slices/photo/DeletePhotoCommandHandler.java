package com.rapidphotoupload.slices.photo;

import com.rapidphotoupload.domain.photo.Photo;
import com.rapidphotoupload.domain.photo.PhotoId;
import com.rapidphotoupload.domain.photo.PhotoRepository;
import com.rapidphotoupload.infrastructure.storage.StorageAdapter;
import com.rapidphotoupload.shared.exceptions.AuthenticationException;
import com.rapidphotoupload.shared.exceptions.EntityNotFoundException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Command handler for deleting photos.
 * 
 * This handler processes DeletePhotoCommand requests by:
 * 1. Loading the photo by ID from repository
 * 2. Verifying the photo belongs to the authenticated user
 * 3. Deleting the photo file from S3 storage
 * 4. Deleting the photo record from the database
 * 
 * @author RapidPhotoUpload Team
 * @since 1.0.0
 */
@Component
public class DeletePhotoCommandHandler {
    
    private final PhotoRepository photoRepository;
    private final StorageAdapter storageAdapter;
    
    public DeletePhotoCommandHandler(
            PhotoRepository photoRepository,
            StorageAdapter storageAdapter) {
        this.photoRepository = photoRepository;
        this.storageAdapter = storageAdapter;
    }
    
    /**
     * Handles the delete photo command.
     * 
     * @param command The command containing photoId and userId
     * @return Mono that completes when deletion is finished
     */
    public Mono<Void> handle(DeletePhotoCommand command) {
        return photoRepository.findById(command.getPhotoId())
                .switchIfEmpty(Mono.error(new EntityNotFoundException(
                        "Photo",
                        command.getPhotoId().getValue().toString()
                )))
                .flatMap(photo -> {
                    // Verify photo belongs to authenticated user
                    if (!photo.getUserId().equals(command.getUserId())) {
                        return Mono.error(new AuthenticationException(
                                "Photo does not belong to authenticated user"));
                    }
                    
                    // Delete from S3 storage first
                    return storageAdapter.deleteObject(photo.getS3Key())
                            .then(Mono.just(photo));
                })
                .flatMap(photo -> {
                    // Delete from database
                    return photoRepository.delete(photo.getId());
                });
    }
}

