package com.rapidphotoupload.slices.upload;

import com.rapidphotoupload.domain.photo.Photo;
import com.rapidphotoupload.domain.photo.PhotoRepository;
import com.rapidphotoupload.shared.exceptions.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.format.DateTimeFormatter;

/**
 * Query handler for retrieving upload status.
 * 
 * This handler processes GetUploadStatusQuery by:
 * 1. Finding the Photo by ID
 * 2. Mapping Photo entity to UploadStatusDto
 * 3. Returning the status information
 * 
 * All operations are reactive and non-blocking.
 * 
 * @author RapidPhotoUpload Team
 * @since 1.0.0
 */
@Component
public class GetUploadStatusQueryHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GetUploadStatusQueryHandler.class);
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    
    private final PhotoRepository photoRepository;
    
    public GetUploadStatusQueryHandler(PhotoRepository photoRepository) {
        this.photoRepository = photoRepository;
    }
    
    /**
     * Handles the get upload status query.
     * 
     * @param query The query containing the photo ID
     * @return Mono containing the upload status DTO
     * @throws EntityNotFoundException if photo is not found
     */
    public Mono<UploadStatusDto> handle(GetUploadStatusQuery query) {
        logger.debug("Handling get upload status query for photo: {}", query.getPhotoId());
        
        return photoRepository.findById(query.getPhotoId())
                .switchIfEmpty(Mono.error(new EntityNotFoundException("Photo", query.getPhotoId().toString())))
                .map(this::mapToDto)
                .doOnError(error -> logger.error("Error handling get upload status query", error));
    }
    
    /**
     * Maps a Photo entity to UploadStatusDto.
     * 
     * @param photo The photo entity
     * @return The upload status DTO
     */
    private UploadStatusDto mapToDto(Photo photo) {
        return new UploadStatusDto(
                photo.getId().toString(),
                photo.getStatus(),
                photo.getUploadDate().format(DATE_FORMATTER),
                null // Error message not stored in Photo entity yet
        );
    }
}

