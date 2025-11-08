package com.rapidphotoupload.infrastructure.persistence.jpa;

import com.rapidphotoupload.domain.photo.Photo;
import com.rapidphotoupload.domain.photo.PhotoId;
import com.rapidphotoupload.domain.photo.PhotoRepository;
import com.rapidphotoupload.domain.uploadjob.UploadJobId;
import com.rapidphotoupload.domain.user.UserId;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Set;

/**
 * JPA adapter implementation of PhotoRepository.
 * 
 * This adapter bridges the domain layer (Photo) and infrastructure layer (PhotoJpaEntity).
 * It converts between domain objects and JPA entities, and wraps blocking JPA operations
 * in reactive types (Mono/Flux) using bounded elastic scheduler.
 * 
 * @author RapidPhotoUpload Team
 * @since 1.0.0
 */
@Repository
@Primary
public class PhotoJpaAdapter implements PhotoRepository {
    
    private final PhotoJpaRepository jpaRepository;
    
    public PhotoJpaAdapter(PhotoJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }
    
    @Override
    public Mono<Photo> save(Photo photo) {
        return Mono.fromCallable(() -> {
            PhotoJpaEntity entity = toEntity(photo);
            PhotoJpaEntity saved = jpaRepository.save(entity);
            return toDomain(saved);
        }).subscribeOn(Schedulers.boundedElastic());
    }
    
    @Override
    public Mono<Photo> findById(PhotoId id) {
        return Mono.fromCallable(() -> {
                    var optional = jpaRepository.findById(id.getValue());
                    // Access tags while still in transaction context to avoid LazyInitializationException
                    return optional.map(entity -> {
                        // Force initialization of lazy collection while session is open
                        entity.getTags().size();
                        return entity;
                    });
                })
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(optional -> optional.map(entity -> Mono.just(toDomain(entity)))
                        .orElse(Mono.empty()));
    }
    
    @Override
    public Flux<Photo> findByUserId(UserId userId) {
        return Mono.fromCallable(() -> {
                    var entities = jpaRepository.findByUserId(userId.getValue());
                    // Force initialization of lazy collections while session is open
                    entities.forEach(entity -> entity.getTags().size());
                    return entities;
                })
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(entities -> Flux.fromIterable(entities)
                        .map(this::toDomain));
    }
    
    @Override
    public Flux<Photo> findByUserIdWithPagination(UserId userId, int page, int size, String sortBy) {
        return Mono.fromCallable(() -> {
                    // Normalize sort field (default to uploadDate)
                    String sortField = (sortBy == null || sortBy.isBlank()) ? "uploadDate" : sortBy;
                    // Ensure valid sort field (prevent SQL injection)
                    if (!sortField.matches("^[a-zA-Z_][a-zA-Z0-9_]*$")) {
                        sortField = "uploadDate";
                    }
                    
                    // Sort by primary field DESC, then by id DESC for consistent ordering
                    Sort sort = Sort.by(Sort.Direction.DESC, sortField)
                            .and(Sort.by(Sort.Direction.DESC, "id"));
                    Pageable pageable = PageRequest.of(page, size, sort);
                    
                    var pageResult = jpaRepository.findByUserId(userId.getValue(), pageable);
                    // Force initialization of lazy collections while session is open
                    pageResult.getContent().forEach(entity -> entity.getTags().size());
                    return pageResult.getContent();
                })
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(entities -> Flux.fromIterable(entities)
                        .map(this::toDomain));
    }
    
    @Override
    public Flux<Photo> findByUserIdAndTags(UserId userId, Set<String> tags, int page, int size) {
        if (tags == null || tags.isEmpty()) {
            // If no tags provided, fall back to regular pagination
            return findByUserIdWithPagination(userId, page, size, "uploadDate");
        }
        
        return Mono.fromCallable(() -> {
                    // Normalize tags to lowercase (matching domain logic)
                    Set<String> normalizedTags = tags.stream()
                            .map(tag -> tag.trim().toLowerCase())
                            .filter(tag -> !tag.isBlank())
                            .collect(java.util.stream.Collectors.toSet());
                    
                    if (normalizedTags.isEmpty()) {
                        return java.util.Collections.<PhotoJpaEntity>emptyList();
                    }
                    
                    // Sort by uploadDate DESC, then by id DESC for consistent ordering
                    Sort sort = Sort.by(Sort.Direction.DESC, "uploadDate")
                            .and(Sort.by(Sort.Direction.DESC, "id"));
                    Pageable pageable = PageRequest.of(page, size, sort);
                    
                    var pageResult = jpaRepository.findByUserIdAndTags(
                            userId.getValue(),
                            normalizedTags,
                            (long) normalizedTags.size(),
                            pageable
                    );
                    // Force initialization of lazy collections while session is open
                    pageResult.getContent().forEach(entity -> entity.getTags().size());
                    return pageResult.getContent();
                })
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(entities -> Flux.fromIterable(entities)
                        .map(this::toDomain));
    }
    
    @Override
    public Mono<Long> countByUserId(UserId userId) {
        return Mono.fromCallable(() -> jpaRepository.countByUserId(userId.getValue()))
                .subscribeOn(Schedulers.boundedElastic());
    }
    
    @Override
    public Mono<Long> countByUserIdAndTags(UserId userId, Set<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return countByUserId(userId);
        }
        
        return Mono.fromCallable(() -> {
                    // Normalize tags to lowercase (matching domain logic)
                    Set<String> normalizedTags = tags.stream()
                            .map(tag -> tag.trim().toLowerCase())
                            .filter(tag -> !tag.isBlank())
                            .collect(java.util.stream.Collectors.toSet());
                    
                    if (normalizedTags.isEmpty()) {
                        return jpaRepository.countByUserId(userId.getValue());
                    }
                    
                    return jpaRepository.countByUserIdAndTags(
                            userId.getValue(),
                            normalizedTags,
                            (long) normalizedTags.size()
                    );
                })
                .subscribeOn(Schedulers.boundedElastic());
    }
    
    @Override
    public Flux<Photo> findAll() {
        return Mono.fromCallable(() -> {
                    var entities = jpaRepository.findAll();
                    // Force initialization of lazy collections while session is open
                    entities.forEach(entity -> entity.getTags().size());
                    return entities;
                })
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(entities -> Flux.fromIterable(entities)
                        .map(this::toDomain));
    }
    
    @Override
    public Mono<Void> delete(PhotoId id) {
        return Mono.fromRunnable(() -> jpaRepository.deleteById(id.getValue()))
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }
    
    /**
     * Converts a domain Photo entity to a JPA PhotoJpaEntity.
     * 
     * @param photo The domain photo entity
     * @return The JPA entity representation
     */
    private PhotoJpaEntity toEntity(Photo photo) {
        PhotoJpaEntity entity = new PhotoJpaEntity();
        entity.setId(photo.getId().getValue());
        entity.setUserId(photo.getUserId().getValue());
        if (photo.getUploadJobId() != null) {
            entity.setUploadJobId(photo.getUploadJobId().getValue());
        }
        entity.setFilename(photo.getFilename());
        entity.setS3Key(photo.getS3Key());
        entity.setUploadDate(photo.getUploadDate());
        entity.setTags(photo.getTags());
        entity.setStatus(photo.getStatus());
        return entity;
    }
    
    /**
     * Converts a JPA PhotoJpaEntity to a domain Photo entity.
     * 
     * @param entity The JPA entity
     * @return The domain photo entity
     */
    private Photo toDomain(PhotoJpaEntity entity) {
        UploadJobId uploadJobId = entity.getUploadJobId() != null
                ? UploadJobId.of(entity.getUploadJobId())
                : null;
        
        return new Photo(
                PhotoId.of(entity.getId()),
                UserId.of(entity.getUserId()),
                uploadJobId,
                entity.getFilename(),
                entity.getS3Key(),
                entity.getUploadDate(),
                entity.getTags(),
                entity.getStatus()
        );
    }
}

