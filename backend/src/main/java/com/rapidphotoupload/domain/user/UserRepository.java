package com.rapidphotoupload.domain.user;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Repository interface for User domain entities.
 * 
 * This interface defines the contract for persisting and retrieving User entities.
 * It uses reactive types (Mono/Flux) to support non-blocking operations in a WebFlux environment.
 * 
 * Implementations of this interface are provided in the infrastructure layer (e.g., UserJpaAdapter).
 * 
 * @author RapidPhotoUpload Team
 * @since 1.0.0
 */
public interface UserRepository {
    
    /**
     * Saves a user entity.
     * 
     * @param user The user to save
     * @return Mono containing the saved user
     */
    Mono<User> save(User user);
    
    /**
     * Finds a user by its unique identifier.
     * 
     * @param id The user ID to search for
     * @return Mono containing the user if found, or Mono.empty() if not found
     */
    Mono<User> findById(UserId id);
    
    /**
     * Finds a user by email address.
     * 
     * @param email The email address to search for
     * @return Mono containing the user if found, or Mono.empty() if not found
     */
    Mono<User> findByEmail(String email);
    
    /**
     * Finds all users in the system.
     * 
     * @return Flux containing all users
     */
    Flux<User> findAll();
    
    /**
     * Deletes a user by its unique identifier.
     * 
     * @param id The user ID to delete
     * @return Mono that completes when deletion is finished
     */
    Mono<Void> delete(UserId id);
}

