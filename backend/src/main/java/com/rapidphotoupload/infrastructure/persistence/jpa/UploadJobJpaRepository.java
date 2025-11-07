package com.rapidphotoupload.infrastructure.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for UploadJobJpaEntity.
 * 
 * Provides standard CRUD operations and custom query methods for upload job entities.
 * 
 * @author RapidPhotoUpload Team
 * @since 1.0.0
 */
@Repository
public interface UploadJobJpaRepository extends JpaRepository<UploadJobJpaEntity, UUID> {
    
    /**
     * Finds all upload jobs belonging to a specific user.
     * 
     * @param userId The user ID to search for
     * @return List of upload jobs for the user
     */
    List<UploadJobJpaEntity> findByUserId(UUID userId);
}

