package com.rapidphotoupload.slices.upload;

import com.rapidphotoupload.domain.photo.Photo;
import com.rapidphotoupload.domain.photo.PhotoId;
import com.rapidphotoupload.domain.photo.PhotoRepository;
import com.rapidphotoupload.domain.user.UserId;
import com.rapidphotoupload.shared.exceptions.EntityNotFoundException;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * REST controller for photo upload operations.
 * 
 * This controller provides endpoints for:
 * - Initiating photo uploads (POST /api/uploads)
 * - Getting upload status (GET /api/uploads/{photoId}/status)
 * - Reporting upload completion (POST /api/uploads/{photoId}/complete)
 * - Reporting upload failure (POST /api/uploads/{photoId}/fail)
 * 
 * All endpoints use reactive types (Mono) for non-blocking operations.
 * Authentication is currently mocked using x-user-id header (Phase 4 will add JWT).
 * 
 * @author RapidPhotoUpload Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/uploads")
public class UploadController {
    
    private static final Logger logger = LoggerFactory.getLogger(UploadController.class);
    
    /**
     * Header name for user ID (mocked authentication).
     */
    private static final String USER_ID_HEADER = "x-user-id";
    
    private final UploadPhotoCommandHandler uploadPhotoCommandHandler;
    private final GetUploadStatusQueryHandler getUploadStatusQueryHandler;
    private final PhotoRepository photoRepository;
    
    public UploadController(
            UploadPhotoCommandHandler uploadPhotoCommandHandler,
            GetUploadStatusQueryHandler getUploadStatusQueryHandler,
            PhotoRepository photoRepository) {
        this.uploadPhotoCommandHandler = uploadPhotoCommandHandler;
        this.getUploadStatusQueryHandler = getUploadStatusQueryHandler;
        this.photoRepository = photoRepository;
    }
    
    /**
     * Initiates a photo upload by generating a presigned URL.
     * 
     * @param requestDto The upload request containing file metadata
     * @param userIdHeader The user ID from header (mocked authentication)
     * @return Mono containing the upload response with presigned URL
     */
    @PostMapping
    public Mono<ResponseEntity<UploadResponseDto>> initiateUpload(
            @Valid @RequestBody Mono<UploadRequestDto> requestDto,
            @RequestHeader(value = USER_ID_HEADER, required = false) String userIdHeader) {
        
        logger.info("Received upload request");
        
        // Extract userId from header (mock authentication)
        if (userIdHeader == null || userIdHeader.isBlank()) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
        }
        
        UserId userId;
        try {
            userId = UserId.of(userIdHeader);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid user ID format: {}", userIdHeader);
            return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).build());
        }
        
        return requestDto
                .flatMap(dto -> {
                    // Convert DTO to command
                    UploadPhotoCommand command = new UploadPhotoCommand(
                            userId,
                            dto.getFilename(),
                            dto.getContentType(),
                            dto.getFileSize(),
                            dto.getTags()
                    );
                    
                    // Handle command
                    return uploadPhotoCommandHandler.handle(command);
                })
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response))
                .doOnError(error -> logger.error("Error initiating upload", error));
    }
    
    /**
     * Gets the upload status for a photo.
     * 
     * @param photoId The photo ID (UUID string)
     * @return Mono containing the upload status
     */
    @GetMapping("/{photoId}/status")
    public Mono<ResponseEntity<UploadStatusDto>> getUploadStatus(@PathVariable String photoId) {
        logger.debug("Getting upload status for photo: {}", photoId);
        
        PhotoId photoIdObj;
        try {
            photoIdObj = PhotoId.of(photoId);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid photo ID format: {}", photoId);
            return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).build());
        }
        
        GetUploadStatusQuery query = new GetUploadStatusQuery(photoIdObj);
        
        return getUploadStatusQueryHandler.handle(query)
                .map(ResponseEntity::ok)
                .onErrorReturn(EntityNotFoundException.class, ResponseEntity.notFound().build())
                .doOnError(error -> logger.error("Error getting upload status", error));
    }
    
    /**
     * Reports that a photo upload has completed successfully.
     * 
     * @param photoId The photo ID (UUID string)
     * @return Mono containing success response
     */
    @PostMapping("/{photoId}/complete")
    public Mono<ResponseEntity<Void>> reportUploadCompletion(@PathVariable String photoId) {
        logger.info("Reporting upload completion for photo: {}", photoId);
        
        PhotoId photoIdObj;
        try {
            photoIdObj = PhotoId.of(photoId);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid photo ID format: {}", photoId);
            return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).build());
        }
        
        return photoRepository.findById(photoIdObj)
                .switchIfEmpty(Mono.error(new EntityNotFoundException("Photo", photoId)))
                .flatMap(photo -> {
                    photo.markAsCompleted();
                    return photoRepository.save(photo);
                })
                .then(Mono.just(ResponseEntity.ok().<Void>build()))
                .onErrorReturn(EntityNotFoundException.class, ResponseEntity.notFound().build())
                .doOnError(error -> logger.error("Error reporting upload completion", error));
    }
    
    /**
     * Reports that a photo upload has failed.
     * 
     * @param photoId The photo ID (UUID string)
     * @param failureDto The failure details containing error message
     * @return Mono containing success response
     */
    @PostMapping("/{photoId}/fail")
    public Mono<ResponseEntity<Void>> reportUploadFailure(
            @PathVariable String photoId,
            @Valid @RequestBody Mono<UploadFailureDto> failureDto) {
        
        logger.info("Reporting upload failure for photo: {}", photoId);
        
        PhotoId photoIdObj;
        try {
            photoIdObj = PhotoId.of(photoId);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid photo ID format: {}", photoId);
            return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).build());
        }
        
        return failureDto
                .flatMap(dto -> photoRepository.findById(photoIdObj)
                        .switchIfEmpty(Mono.error(new EntityNotFoundException("Photo", photoId)))
                        .flatMap(photo -> {
                            photo.markAsFailed();
                            return photoRepository.save(photo);
                        }))
                .then(Mono.just(ResponseEntity.ok().<Void>build()))
                .onErrorReturn(EntityNotFoundException.class, ResponseEntity.notFound().build())
                .doOnError(error -> logger.error("Error reporting upload failure", error));
    }
}

