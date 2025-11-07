package com.rapidphotoupload.slices.photo;

import com.rapidphotoupload.domain.photo.Photo;
import com.rapidphotoupload.domain.photo.PhotoRepository;
import com.rapidphotoupload.infrastructure.storage.StorageAdapter;
import com.rapidphotoupload.shared.exceptions.EntityNotFoundException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Query handler for retrieving a single photo by ID.
 * 
 * This handler processes GetPhotoQuery requests by:
 * 1. Finding the photo by ID in the repository
 * 2. Converting Photo domain entity to PhotoDto
 * 3. Generating presigned download URL
 * 4. Throwing EntityNotFoundException if photo not found
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
     * @param query The query containing photoId
     * @return Mono containing PhotoDto if found, or error if not found
     */
    public Mono<PhotoDto> handle(GetPhotoQuery query) {
        return photoRepository.findById(query.getPhotoId())
                .switchIfEmpty(Mono.error(new EntityNotFoundException(
                        "Photo",
                        query.getPhotoId().getValue().toString()
                )))
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

