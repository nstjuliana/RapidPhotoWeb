package com.rapidphotoupload.infrastructure.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for PhotoJpaEntity.
 * 
 * Provides standard CRUD operations and custom query methods for photo entities.
 * 
 * @author RapidPhotoUpload Team
 * @since 1.0.0
 */
@Repository
public interface PhotoJpaRepository extends JpaRepository<PhotoJpaEntity, UUID> {
    
    /**
     * Finds all photos belonging to a specific user.
     * 
     * @param userId The user ID to search for
     * @return List of photos for the user
     */
    List<PhotoJpaEntity> findByUserId(UUID userId);
    
    /**
     * Finds all photos belonging to a specific upload job.
     * 
     * @param uploadJobId The upload job ID to search for
     * @return List of photos for the upload job
     */
    List<PhotoJpaEntity> findByUploadJobId(UUID uploadJobId);
}

