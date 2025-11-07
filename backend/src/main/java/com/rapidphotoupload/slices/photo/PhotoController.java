package com.rapidphotoupload.slices.photo;

import com.rapidphotoupload.domain.photo.PhotoId;
import com.rapidphotoupload.domain.user.UserId;
import com.rapidphotoupload.infrastructure.storage.StorageAdapter;
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
     * Lists photos for a user with pagination and optional tag filtering.
     * 
     * Query parameters:
     * - userId: Required user ID (UUID)
     * - page: Page number (0-indexed, default: 0)
     * - size: Page size (default: 20, max: 100)
     * - sortBy: Field to sort by (default: "uploadDate")
     * - tags: Comma-separated list of tags to filter by (AND logic)
     * 
     * @param userId The user ID (required)
     * @param page Page number (optional, default: 0)
     * @param size Page size (optional, default: 20)
     * @param sortBy Sort field (optional, default: "uploadDate")
     * @param tags Comma-separated tags (optional)
     * @return Flux of PhotoDto
     */
    @GetMapping
    public Flux<PhotoDto> listPhotos(
            @RequestParam("userId") String userId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "sortBy", required = false) String sortBy,
            @RequestParam(value = "tags", required = false) String tags) {
        
        // Validate and normalize parameters
        if (page < 0) {
            page = 0;
        }
        if (size < 1) {
            size = DEFAULT_PAGE_SIZE;
        }
        if (size > MAX_PAGE_SIZE) {
            size = MAX_PAGE_SIZE;
        }
        
        // Parse tags from comma-separated string
        Set<String> tagSet = null;
        if (tags != null && !tags.isBlank()) {
            tagSet = Arrays.stream(tags.split(","))
                    .map(String::trim)
                    .filter(tag -> !tag.isBlank())
                    .collect(Collectors.toSet());
        }
        
        // Create query
        ListPhotosQuery query = new ListPhotosQuery(
                UserId.of(userId),
                page,
                size,
                sortBy,
                tagSet
        );
        
        return listPhotosQueryHandler.handle(query);
    }
    
    /**
     * Retrieves a single photo by ID.
     * 
     * @param photoId The photo ID (UUID)
     * @return Mono containing PhotoDto, or 404 if not found
     */
    @GetMapping("/{photoId}")
    public Mono<ResponseEntity<PhotoDto>> getPhoto(@PathVariable String photoId) {
        GetPhotoQuery query = new GetPhotoQuery(PhotoId.of(photoId));
        
        return getPhotoQueryHandler.handle(query)
                .map(ResponseEntity::ok)
                .onErrorReturn(com.rapidphotoupload.shared.exceptions.EntityNotFoundException.class,
                        ResponseEntity.notFound().build());
    }
    
    /**
     * Generates a presigned download URL for a photo.
     * 
     * @param photoId The photo ID (UUID)
     * @return Mono containing download URL response
     */
    @GetMapping("/{photoId}/download")
    public Mono<ResponseEntity<DownloadUrlResponse>> getDownloadUrl(@PathVariable String photoId) {
        // First, verify photo exists by getting it
        GetPhotoQuery query = new GetPhotoQuery(PhotoId.of(photoId));
        
        return getPhotoQueryHandler.handle(query)
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
                .onErrorReturn(com.rapidphotoupload.shared.exceptions.EntityNotFoundException.class,
                        ResponseEntity.notFound().build());
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

