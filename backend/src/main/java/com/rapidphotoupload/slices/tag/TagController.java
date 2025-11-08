package com.rapidphotoupload.slices.tag;

import com.rapidphotoupload.domain.photo.PhotoId;
import com.rapidphotoupload.infrastructure.security.SecurityUtils;
import com.rapidphotoupload.slices.photo.PhotoDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * REST controller for tag management endpoints.
 * 
 * Provides endpoints for:
 * - Adding tags to a photo
 * - Removing tags from a photo
 * - Replacing all tags on a photo
 * 
 * All endpoints use reactive types (Mono) for non-blocking operations.
 * All endpoints require JWT authentication and verify photo ownership.
 * 
 * @author RapidPhotoUpload Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/photos/{photoId}/tags")
public class TagController {
    
    private final TagPhotoCommandHandler tagPhotoCommandHandler;
    
    public TagController(TagPhotoCommandHandler tagPhotoCommandHandler) {
        this.tagPhotoCommandHandler = tagPhotoCommandHandler;
    }
    
    /**
     * Adds tags to a photo.
     * 
     * Request body should contain a JSON object with a "tags" array:
     * { "tags": ["tag1", "tag2", "tag3"] }
     * 
     * User ID is extracted from JWT token in Authorization header.
     * 
     * @param photoId The photo ID (UUID)
     * @param request The tag request DTO containing tags to add
     * @return Mono containing updated PhotoDto
     */
    @PostMapping
    public Mono<ResponseEntity<PhotoDto>> addTags(
            @PathVariable String photoId,
            @RequestBody TagRequestDto request) {
        
        PhotoId photoIdObj;
        try {
            photoIdObj = PhotoId.of(photoId);
        } catch (IllegalArgumentException e) {
            return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).build());
        }
        
        return SecurityUtils.getCurrentUserId()
                .flatMap(userId -> {
                    TagPhotoCommand command = new TagPhotoCommand(
                            photoIdObj,
                            userId,
                            request.getTags(),
                            TagOperation.ADD
                    );
                    
                    return tagPhotoCommandHandler.handle(command);
                })
                .map(ResponseEntity::ok)
                .onErrorReturn(com.rapidphotoupload.shared.exceptions.EntityNotFoundException.class,
                        ResponseEntity.notFound().build())
                .onErrorReturn(com.rapidphotoupload.shared.exceptions.AuthenticationException.class,
                        ResponseEntity.status(HttpStatus.FORBIDDEN).build());
    }
    
    /**
     * Removes tags from a photo.
     * 
     * Request body should contain a JSON object with a "tags" array:
     * { "tags": ["tag1", "tag2"] }
     * 
     * User ID is extracted from JWT token in Authorization header.
     * 
     * @param photoId The photo ID (UUID)
     * @param request The tag request DTO containing tags to remove
     * @return Mono containing updated PhotoDto
     */
    @DeleteMapping
    public Mono<ResponseEntity<PhotoDto>> removeTags(
            @PathVariable String photoId,
            @RequestBody TagRequestDto request) {
        
        PhotoId photoIdObj;
        try {
            photoIdObj = PhotoId.of(photoId);
        } catch (IllegalArgumentException e) {
            return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).build());
        }
        
        return SecurityUtils.getCurrentUserId()
                .flatMap(userId -> {
                    TagPhotoCommand command = new TagPhotoCommand(
                            photoIdObj,
                            userId,
                            request.getTags(),
                            TagOperation.REMOVE
                    );
                    
                    return tagPhotoCommandHandler.handle(command);
                })
                .map(ResponseEntity::ok)
                .onErrorReturn(com.rapidphotoupload.shared.exceptions.EntityNotFoundException.class,
                        ResponseEntity.notFound().build())
                .onErrorReturn(com.rapidphotoupload.shared.exceptions.AuthenticationException.class,
                        ResponseEntity.status(HttpStatus.FORBIDDEN).build());
    }
    
    /**
     * Replaces all tags on a photo with new tags.
     * 
     * Request body should contain a JSON object with a "tags" array:
     * { "tags": ["newtag1", "newtag2"] }
     * 
     * User ID is extracted from JWT token in Authorization header.
     * 
     * @param photoId The photo ID (UUID)
     * @param request The tag request DTO containing new tags
     * @return Mono containing updated PhotoDto
     */
    @PutMapping
    public Mono<ResponseEntity<PhotoDto>> replaceTags(
            @PathVariable String photoId,
            @RequestBody TagRequestDto request) {
        
        PhotoId photoIdObj;
        try {
            photoIdObj = PhotoId.of(photoId);
        } catch (IllegalArgumentException e) {
            return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).build());
        }
        
        return SecurityUtils.getCurrentUserId()
                .flatMap(userId -> {
                    TagPhotoCommand command = new TagPhotoCommand(
                            photoIdObj,
                            userId,
                            request.getTags(),
                            TagOperation.REPLACE
                    );
                    
                    return tagPhotoCommandHandler.handle(command);
                })
                .map(ResponseEntity::ok)
                .onErrorReturn(com.rapidphotoupload.shared.exceptions.EntityNotFoundException.class,
                        ResponseEntity.notFound().build())
                .onErrorReturn(com.rapidphotoupload.shared.exceptions.AuthenticationException.class,
                        ResponseEntity.status(HttpStatus.FORBIDDEN).build());
    }
}

