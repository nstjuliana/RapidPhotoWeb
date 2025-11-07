package com.rapidphotoupload.domain.photo;

import com.rapidphotoupload.domain.user.UserId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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

