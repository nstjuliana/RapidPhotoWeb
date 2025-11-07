package com.rapidphotoupload.infrastructure.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for UserJpaEntity.
 * 
 * Provides standard CRUD operations and custom query methods for user entities.
 * 
 * @author RapidPhotoUpload Team
 * @since 1.0.0
 */
@Repository
public interface UserJpaRepository extends JpaRepository<UserJpaEntity, UUID> {
    
    /**
     * Finds a user by email address.
     * 
     * @param email The email address to search for
     * @return Optional containing the user if found
     */
    Optional<UserJpaEntity> findByEmail(String email);
}

