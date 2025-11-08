package com.rapidphotoupload.slices.photo;

import com.rapidphotoupload.domain.photo.Photo;
import com.rapidphotoupload.domain.photo.PhotoRepository;
import com.rapidphotoupload.infrastructure.storage.StorageAdapter;
import com.rapidphotoupload.shared.exceptions.AuthenticationException;
import com.rapidphotoupload.shared.exceptions.EntityNotFoundException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Query handler for retrieving a single photo by ID.
 * 
 * This handler processes GetPhotoQuery requests by:
 * 1. Finding the photo by ID in the repository
 * 2. Verifying the photo belongs to the authenticated user
 * 3. Converting Photo domain entity to PhotoDto
 * 4. Generating presigned download URL
 * 5. Throwing EntityNotFoundException if photo not found
 * 6. Throwing AuthenticationException if photo doesn't belong to user
 * 
 * @author RapidPhotoUpload Team
 * @since 1.0.0
 */
@Component
public class GetPhotoQueryHandler {
    
    private final PhotoRepository photoRepository;
    private final StorageAdapter storageAdapter;
    
    private static final long DOWNLOAD_URL_EXPIRATION_MINUTES = 60;
    
    public GetPhotoQueryHandler(
            PhotoRepository photoRepository,
            StorageAdapter storageAdapter) {
        this.photoRepository = photoRepository;
        this.storageAdapter = storageAdapter;
    }
    
    /**
     * Handles the get photo query.
     * 
     * @param query The query containing photoId and userId
     * @return Mono containing PhotoDto if found and authorized, or error if not found/unauthorized
     */
    public Mono<PhotoDto> handle(GetPhotoQuery query) {
        return photoRepository.findById(query.getPhotoId())
                .switchIfEmpty(Mono.error(new EntityNotFoundException(
                        "Photo",
                        query.getPhotoId().getValue().toString()
                )))
                .flatMap(photo -> {
                    // Verify photo belongs to authenticated user
                    if (!photo.getUserId().equals(query.getUserId())) {
                        return Mono.error(new AuthenticationException(
                                "Photo does not belong to authenticated user"));
                    }
                    return Mono.just(photo);
                })
                .flatMap(this::toDto);
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

