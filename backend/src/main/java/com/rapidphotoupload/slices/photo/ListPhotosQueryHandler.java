package com.rapidphotoupload.slices.photo;

import com.rapidphotoupload.domain.photo.Photo;
import com.rapidphotoupload.domain.photo.PhotoRepository;
import com.rapidphotoupload.infrastructure.storage.StorageAdapter;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Set;

/**
 * Query handler for listing photos with pagination and optional tag filtering.
 * 
 * This handler processes ListPhotosQuery requests by:
 * 1. Retrieving photos from the repository (with optional tag filtering)
 * 2. Converting Photo domain entities to PhotoDto
 * 3. Generating presigned download URLs for each photo
 * 
 * @author RapidPhotoUpload Team
 * @since 1.0.0
 */
@Component
public class ListPhotosQueryHandler {
    
    private final PhotoRepository photoRepository;
    private final StorageAdapter storageAdapter;
    
    private static final long DOWNLOAD_URL_EXPIRATION_MINUTES = 60;
    
    public ListPhotosQueryHandler(
            PhotoRepository photoRepository,
            StorageAdapter storageAdapter) {
        this.photoRepository = photoRepository;
        this.storageAdapter = storageAdapter;
    }
    
    /**
     * Handles the list photos query.
     * 
     * @param query The query containing userId, pagination, and optional tag filters
     * @return Flux of PhotoDto with presigned download URLs
     */
    public Flux<PhotoDto> handle(ListPhotosQuery query) {
        Flux<Photo> photos;
        
        // Use tag filtering if tags are provided, otherwise use regular pagination
        if (query.getTags() != null && !query.getTags().isEmpty()) {
            photos = photoRepository.findByUserIdAndTags(
                    query.getUserId(),
                    query.getTags(),
                    query.getPage(),
                    query.getSize()
            );
        } else {
            photos = photoRepository.findByUserIdWithPagination(
                    query.getUserId(),
                    query.getPage(),
                    query.getSize(),
                    query.getSortBy()
            );
        }
        
        // Convert each photo to DTO with download URL
        return photos.flatMap(this::toDto);
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

