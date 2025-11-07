package com.rapidphotoupload.domain.uploadjob;

import com.rapidphotoupload.domain.user.UserId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Repository interface for UploadJob domain entities.
 * 
 * This interface defines the contract for persisting and retrieving UploadJob entities.
 * It uses reactive types (Mono/Flux) to support non-blocking operations in a WebFlux environment.
 * 
 * Implementations of this interface are provided in the infrastructure layer (e.g., UploadJobJpaAdapter).
 * 
 * @author RapidPhotoUpload Team
 * @since 1.0.0
 */
public interface UploadJobRepository {
    
    /**
     * Saves an upload job entity.
     * 
     * @param uploadJob The upload job to save
     * @return Mono containing the saved upload job
     */
    Mono<UploadJob> save(UploadJob uploadJob);
    
    /**
     * Finds an upload job by its unique identifier.
     * 
     * @param id The upload job ID to search for
     * @return Mono containing the upload job if found, or Mono.empty() if not found
     */
    Mono<UploadJob> findById(UploadJobId id);
    
    /**
     * Finds all upload jobs belonging to a specific user.
     * 
     * @param userId The user ID to search for
     * @return Flux containing all upload jobs for the user
     */
    Flux<UploadJob> findByUserId(UserId userId);
    
    /**
     * Finds all upload jobs in the system.
     * 
     * @return Flux containing all upload jobs
     */
    Flux<UploadJob> findAll();
    
    /**
     * Deletes an upload job by its unique identifier.
     * 
     * @param id The upload job ID to delete
     * @return Mono that completes when deletion is finished
     */
    Mono<Void> delete(UploadJobId id);
}

