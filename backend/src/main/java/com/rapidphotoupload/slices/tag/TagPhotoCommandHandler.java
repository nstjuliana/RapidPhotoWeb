package com.rapidphotoupload.slices.tag;

import com.rapidphotoupload.domain.photo.Photo;
import com.rapidphotoupload.domain.photo.PhotoId;
import com.rapidphotoupload.domain.photo.PhotoRepository;
import com.rapidphotoupload.infrastructure.storage.StorageAdapter;
import com.rapidphotoupload.slices.photo.PhotoDto;
import com.rapidphotoupload.shared.exceptions.EntityNotFoundException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Set;

/**
 * Command handler for tagging and untagging photos.
 * 
 * This handler processes TagPhotoCommand requests by:
 * 1. Loading the photo by ID from repository
 * 2. Applying the tag operation (ADD, REMOVE, or REPLACE) using domain methods
 * 3. Saving the updated photo
 * 4. Converting to PhotoDto with new download URL
 * 
 * @author RapidPhotoUpload Team
 * @since 1.0.0
 */
@Component
public class TagPhotoCommandHandler {
    
    private final PhotoRepository photoRepository;
    private final StorageAdapter storageAdapter;
    
    private static final long DOWNLOAD_URL_EXPIRATION_MINUTES = 60;
    
    public TagPhotoCommandHandler(
            PhotoRepository photoRepository,
            StorageAdapter storageAdapter) {
        this.photoRepository = photoRepository;
        this.storageAdapter = storageAdapter;
    }
    
    /**
     * Handles the tag photo command.
     * 
     * @param command The command containing photoId, tags, and operation
     * @return Mono containing updated PhotoDto
     */
    public Mono<PhotoDto> handle(TagPhotoCommand command) {
        return photoRepository.findById(command.getPhotoId())
                .switchIfEmpty(Mono.error(new EntityNotFoundException(
                        "Photo",
                        command.getPhotoId().getValue().toString()
                )))
                .flatMap(photo -> {
                    // Apply tag operation based on operation type
                    applyTagOperation(photo, command.getTags(), command.getOperation());
                    
                    // Save updated photo
                    return photoRepository.save(photo);
                })
                .flatMap(this::toDto);
    }
    
    /**
     * Applies the tag operation to the photo using domain methods.
     * 
     * @param photo The photo entity to modify
     * @param tags The tags to apply
     * @param operation The operation type (ADD, REMOVE, REPLACE)
     */
    private void applyTagOperation(Photo photo, Set<String> tags, TagOperation operation) {
        if (tags == null || tags.isEmpty()) {
            return; // No tags to apply
        }
        
        switch (operation) {
            case ADD:
                tags.forEach(photo::addTag);
                break;
            case REMOVE:
                tags.forEach(photo::removeTag);
                break;
            case REPLACE:
                // Remove all existing tags first
                Set<String> existingTags = photo.getTags();
                existingTags.forEach(tag -> photo.removeTag(tag));
                // Add new tags
                tags.forEach(photo::addTag);
                break;
        }
    }
    
    /**
     * Converts a Photo domain entity to PhotoDto with presigned download URL.
     * 
     * @param photo The domain photo entity
     * @return Mono containing PhotoDto
     */
    private Mono<PhotoDto> toDto(Photo photo) {
        return storageAdapter.generatePresignedDownloadUrl(
                        photo.getS3Key(),
                        DOWNLOAD_URL_EXPIRATION_MINUTES
                )
                .map(downloadUrl -> {
                    PhotoDto dto = new PhotoDto();
                    dto.setId(photo.getId().getValue().toString());
                    dto.setFilename(photo.getFilename());
                    dto.setS3Key(photo.getS3Key());
                    dto.setUploadDate(photo.getUploadDate());
                    dto.setTags(photo.getTags());
                    dto.setStatus(photo.getStatus());
                    dto.setDownloadUrl(downloadUrl);
                    return dto;
                });
    }
}

