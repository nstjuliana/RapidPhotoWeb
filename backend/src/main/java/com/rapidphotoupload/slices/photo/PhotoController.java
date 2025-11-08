package com.rapidphotoupload.slices.photo;

import com.rapidphotoupload.domain.photo.PhotoId;
import com.rapidphotoupload.domain.user.UserId;
import com.rapidphotoupload.infrastructure.security.SecurityUtils;
import com.rapidphotoupload.infrastructure.storage.StorageAdapter;
import com.rapidphotoupload.shared.exceptions.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * REST controller for photo query endpoints.
 * 
 * Provides endpoints for:
 * - Listing photos with pagination and tag filtering
 * - Retrieving single photo details
 * - Getting presigned download URLs
 * 
 * All endpoints use reactive types (Mono/Flux) for non-blocking operations.
 * All endpoints require JWT authentication and filter results by authenticated user.
 * 
 * @author RapidPhotoUpload Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/photos")
public class PhotoController {
    
    private final ListPhotosQueryHandler listPhotosQueryHandler;
    private final GetPhotoQueryHandler getPhotoQueryHandler;
    private final StorageAdapter storageAdapter;
    
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;
    private static final long DOWNLOAD_URL_EXPIRATION_MINUTES = 60;
    
    public PhotoController(
            ListPhotosQueryHandler listPhotosQueryHandler,
            GetPhotoQueryHandler getPhotoQueryHandler,
            StorageAdapter storageAdapter) {
        this.listPhotosQueryHandler = listPhotosQueryHandler;
        this.getPhotoQueryHandler = getPhotoQueryHandler;
        this.storageAdapter = storageAdapter;
    }
    
    /**
     * Lists photos for the authenticated user with pagination and optional tag filtering.
     * 
     * User ID is extracted from JWT token in Authorization header.
     * 
     * Query parameters:
     * - page: Page number (0-indexed, default: 0)
     * - size: Page size (default: 20, max: 100)
     * - sortBy: Field to sort by (default: "uploadDate")
     * - tags: Comma-separated list of tags to filter by (AND logic)
     * 
     * @param page Page number (optional, default: 0)
     * @param size Page size (optional, default: 20)
     * @param sortBy Sort field (optional, default: "uploadDate")
     * @param tags Comma-separated tags (optional)
     * @return Flux of PhotoDto
     */
    @GetMapping
    public Flux<PhotoDto> listPhotos(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "sortBy", required = false) String sortBy,
            @RequestParam(value = "tags", required = false) String tags) {
        
        // Validate and normalize parameters
        final int normalizedPage = page < 0 ? 0 : page;
        final int normalizedSize = size < 1 ? DEFAULT_PAGE_SIZE : (size > MAX_PAGE_SIZE ? MAX_PAGE_SIZE : size);
        final String normalizedSortBy = sortBy;
        
        // Parse tags from comma-separated string
        final Set<String> normalizedTagSet;
        if (tags != null && !tags.isBlank()) {
            normalizedTagSet = Arrays.stream(tags.split(","))
                    .map(String::trim)
                    .filter(tag -> !tag.isBlank())
                    .collect(Collectors.toSet());
        } else {
            normalizedTagSet = null;
        }
        
        // Get userId from security context and create query
        return SecurityUtils.getCurrentUserId()
                .flatMapMany(userId -> {
                    ListPhotosQuery query = new ListPhotosQuery(
                            userId,
                            normalizedPage,
                            normalizedSize,
                            normalizedSortBy,
                            normalizedTagSet
                    );
                    
                    return listPhotosQueryHandler.handle(query);
                });
    }
    
    /**
     * Retrieves a single photo by ID.
     * 
     * Only returns photo if it belongs to the authenticated user.
     * 
     * @param photoId The photo ID (UUID)
     * @return Mono containing PhotoDto, or 404 if not found or not authorized
     */
    @GetMapping("/{photoId}")
    public Mono<ResponseEntity<PhotoDto>> getPhoto(@PathVariable String photoId) {
        PhotoId photoIdObj;
        try {
            photoIdObj = PhotoId.of(photoId);
        } catch (IllegalArgumentException e) {
            return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).build());
        }
        
        return SecurityUtils.getCurrentUserId()
                .flatMap(userId -> {
                    GetPhotoQuery query = new GetPhotoQuery(photoIdObj, userId);
                    return getPhotoQueryHandler.handle(query);
                })
                .map(ResponseEntity::ok)
                .onErrorReturn(EntityNotFoundException.class,
                        ResponseEntity.notFound().build())
                .onErrorReturn(com.rapidphotoupload.shared.exceptions.AuthenticationException.class,
                        ResponseEntity.status(HttpStatus.FORBIDDEN).build());
    }
    
    /**
     * Generates a presigned download URL for a photo.
     * 
     * Only returns URL if photo belongs to the authenticated user.
     * 
     * @param photoId The photo ID (UUID)
     * @return Mono containing download URL response
     */
    @GetMapping("/{photoId}/download")
    public Mono<ResponseEntity<DownloadUrlResponse>> getDownloadUrl(@PathVariable String photoId) {
        PhotoId photoIdObj;
        try {
            photoIdObj = PhotoId.of(photoId);
        } catch (IllegalArgumentException e) {
            return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).build());
        }
        
        return SecurityUtils.getCurrentUserId()
                .flatMap(userId -> {
                    GetPhotoQuery query = new GetPhotoQuery(photoIdObj, userId);
                    return getPhotoQueryHandler.handle(query);
                })
                .flatMap(photoDto -> 
                    storageAdapter.generatePresignedDownloadUrl(
                            photoDto.getS3Key(),
                            DOWNLOAD_URL_EXPIRATION_MINUTES
                    )
                    .map(url -> {
                        DownloadUrlResponse response = new DownloadUrlResponse();
                        response.setDownloadUrl(url);
                        response.setExpirationMinutes(DOWNLOAD_URL_EXPIRATION_MINUTES);
                        return ResponseEntity.ok(response);
                    })
                )
                .onErrorReturn(EntityNotFoundException.class,
                        ResponseEntity.notFound().build())
                .onErrorReturn(com.rapidphotoupload.shared.exceptions.AuthenticationException.class,
                        ResponseEntity.status(HttpStatus.FORBIDDEN).build());
    }
    
    /**
     * Response DTO for download URL endpoint.
     */
    public static class DownloadUrlResponse {
        private String downloadUrl;
        private long expirationMinutes;
        
        public String getDownloadUrl() {
            return downloadUrl;
        }
        
        public void setDownloadUrl(String downloadUrl) {
            this.downloadUrl = downloadUrl;
        }
        
        public long getExpirationMinutes() {
            return expirationMinutes;
        }
        
        public void setExpirationMinutes(long expirationMinutes) {
            this.expirationMinutes = expirationMinutes;
        }
    }
}

