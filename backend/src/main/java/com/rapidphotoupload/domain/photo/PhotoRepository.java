package com.rapidphotoupload.domain.photo;

import com.rapidphotoupload.domain.user.UserId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Set;

/**
 * Repository interface for Photo domain entities.
 * 
 * This interface defines the contract for persisting and retrieving Photo entities.
 * It uses reactive types (Mono/Flux) to support non-blocking operations in a WebFlux environment.
 * 
 * Implementations of this interface are provided in the infrastructure layer (e.g., PhotoJpaAdapter).
 * 
 * @author RapidPhotoUpload Team
 * @since 1.0.0
 */
public interface PhotoRepository {
    
    /**
     * Saves a photo entity.
     * 
     * @param photo The photo to save
     * @return Mono containing the saved photo
     */
    Mono<Photo> save(Photo photo);
    
    /**
     * Finds a photo by its unique identifier.
     * 
     * @param id The photo ID to search for
     * @return Mono containing the photo if found, or Mono.empty() if not found
     */
    Mono<Photo> findById(PhotoId id);
    
    /**
     * Finds all photos belonging to a specific user.
     * 
     * @param userId The user ID to search for
     * @return Flux containing all photos for the user
     */
    Flux<Photo> findByUserId(UserId userId);
    
    /**
     * Finds photos belonging to a specific user with pagination support.
     * 
     * @param userId The user ID to search for
     * @param page The page number (0-indexed)
     * @param size The page size
     * @param sortBy The field to sort by (default: "uploadDate")
     * @return Flux containing paginated photos for the user
     */
    Flux<Photo> findByUserIdWithPagination(UserId userId, int page, int size, String sortBy);
    
    /**
     * Finds photos belonging to a specific user filtered by tags (AND logic).
     * Photos must have ALL specified tags to be included in results.
     * 
     * @param userId The user ID to search for
     * @param tags The set of tags to filter by (all tags must be present)
     * @param page The page number (0-indexed)
     * @param size The page size
     * @return Flux containing filtered and paginated photos
     */
    Flux<Photo> findByUserIdAndTags(UserId userId, Set<String> tags, int page, int size);
    
    /**
     * Counts total photos for a user (for pagination metadata).
     * 
     * @param userId The user ID to count photos for
     * @return Mono containing the total count
     */
    Mono<Long> countByUserId(UserId userId);
    
    /**
     * Counts photos for a user matching specific tags (for pagination metadata).
     * 
     * @param userId The user ID to count photos for
     * @param tags The set of tags to filter by
     * @return Mono containing the filtered count
     */
    Mono<Long> countByUserIdAndTags(UserId userId, Set<String> tags);
    
    /**
     * Finds all photos in the system.
     * 
     * @return Flux containing all photos
     */
    Flux<Photo> findAll();
    
    /**
     * Deletes a photo by its unique identifier.
     * 
     * @param id The photo ID to delete
     * @return Mono that completes when deletion is finished
     */
    Mono<Void> delete(PhotoId id);
}

