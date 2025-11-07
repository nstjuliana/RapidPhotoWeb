package com.rapidphotoupload.infrastructure.persistence.jpa;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
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
     * Finds photos belonging to a specific user with pagination support.
     * 
     * @param userId The user ID to search for
     * @param pageable The pagination and sorting parameters
     * @return Page of photos for the user
     */
    Page<PhotoJpaEntity> findByUserId(UUID userId, Pageable pageable);
    
    /**
     * Finds photos belonging to a specific user that have ALL specified tags (AND logic).
     * 
     * This query uses a subquery to ensure photos have all tags in the provided set.
     * 
     * @param userId The user ID to search for
     * @param tags The set of tags that must all be present
     * @param tagCount The number of tags in the set (for validation)
     * @param pageable The pagination and sorting parameters
     * @return Page of photos matching the criteria
     */
    @Query("SELECT DISTINCT p FROM PhotoJpaEntity p " +
           "WHERE p.userId = :userId " +
           "AND (SELECT COUNT(DISTINCT t) FROM p.tags t WHERE t IN :tags) = :tagCount")
    Page<PhotoJpaEntity> findByUserIdAndTags(
            @Param("userId") UUID userId,
            @Param("tags") Set<String> tags,
            @Param("tagCount") long tagCount,
            Pageable pageable);
    
    /**
     * Counts photos belonging to a specific user.
     * 
     * @param userId The user ID to count photos for
     * @return Total count of photos for the user
     */
    long countByUserId(UUID userId);
    
    /**
     * Counts photos belonging to a specific user that have ALL specified tags.
     * 
     * @param userId The user ID to count photos for
     * @param tags The set of tags that must all be present
     * @param tagCount The number of tags in the set (for validation)
     * @return Count of photos matching the criteria
     */
    @Query("SELECT COUNT(DISTINCT p) FROM PhotoJpaEntity p " +
           "WHERE p.userId = :userId " +
           "AND (SELECT COUNT(DISTINCT t) FROM p.tags t WHERE t IN :tags) = :tagCount")
    long countByUserIdAndTags(
            @Param("userId") UUID userId,
            @Param("tags") Set<String> tags,
            @Param("tagCount") long tagCount);
    
    /**
     * Finds all photos belonging to a specific upload job.
     * 
     * @param uploadJobId The upload job ID to search for
     * @return List of photos for the upload job
     */
    List<PhotoJpaEntity> findByUploadJobId(UUID uploadJobId);
}

