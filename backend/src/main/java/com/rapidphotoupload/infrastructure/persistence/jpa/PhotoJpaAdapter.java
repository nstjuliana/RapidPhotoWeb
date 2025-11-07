package com.rapidphotoupload.infrastructure.persistence.jpa;

import com.rapidphotoupload.domain.photo.Photo;
import com.rapidphotoupload.domain.photo.PhotoId;
import com.rapidphotoupload.domain.photo.PhotoRepository;
import com.rapidphotoupload.domain.uploadjob.UploadJobId;
import com.rapidphotoupload.domain.user.UserId;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

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
        return Mono.fromCallable(() -> jpaRepository.findById(id.getValue()))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(optional -> optional.map(entity -> Mono.just(toDomain(entity)))
                        .orElse(Mono.empty()));
    }
    
    @Override
    public Flux<Photo> findByUserId(UserId userId) {
        return Mono.fromCallable(() -> jpaRepository.findByUserId(userId.getValue()))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(entities -> Flux.fromIterable(entities)
                        .map(this::toDomain));
    }
    
    @Override
    public Flux<Photo> findAll() {
        return Mono.fromCallable(() -> jpaRepository.findAll())
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

