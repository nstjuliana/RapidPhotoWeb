package com.rapidphotoupload.infrastructure.persistence.jpa;

import com.rapidphotoupload.domain.uploadjob.UploadJob;
import com.rapidphotoupload.domain.uploadjob.UploadJobId;
import com.rapidphotoupload.domain.uploadjob.UploadJobRepository;
import com.rapidphotoupload.domain.uploadjob.UploadStatus;
import com.rapidphotoupload.domain.user.UserId;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * JPA adapter implementation of UploadJobRepository.
 * 
 * This adapter bridges the domain layer (UploadJob) and infrastructure layer (UploadJobJpaEntity).
 * It converts between domain objects and JPA entities, and wraps blocking JPA operations
 * in reactive types (Mono/Flux) using bounded elastic scheduler.
 * 
 * @author RapidPhotoUpload Team
 * @since 1.0.0
 */
@Repository
@Primary
public class UploadJobJpaAdapter implements UploadJobRepository {
    
    private final UploadJobJpaRepository jpaRepository;
    
    public UploadJobJpaAdapter(UploadJobJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }
    
    @Override
    public Mono<UploadJob> save(UploadJob uploadJob) {
        return Mono.fromCallable(() -> {
            UploadJobJpaEntity entity = toEntity(uploadJob);
            UploadJobJpaEntity saved = jpaRepository.save(entity);
            return toDomain(saved);
        }).subscribeOn(Schedulers.boundedElastic());
    }
    
    @Override
    public Mono<UploadJob> findById(UploadJobId id) {
        return Mono.fromCallable(() -> jpaRepository.findById(id.getValue()))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(optional -> optional.map(entity -> Mono.just(toDomain(entity)))
                        .orElse(Mono.empty()));
    }
    
    @Override
    public Flux<UploadJob> findByUserId(UserId userId) {
        return Mono.fromCallable(() -> jpaRepository.findByUserId(userId.getValue()))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(entities -> Flux.fromIterable(entities)
                        .map(this::toDomain));
    }
    
    @Override
    public Flux<UploadJob> findAll() {
        return Mono.fromCallable(() -> jpaRepository.findAll())
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(entities -> Flux.fromIterable(entities)
                        .map(this::toDomain));
    }
    
    @Override
    public Mono<Void> delete(UploadJobId id) {
        return Mono.fromRunnable(() -> jpaRepository.deleteById(id.getValue()))
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }
    
    /**
     * Converts a domain UploadJob entity to a JPA UploadJobJpaEntity.
     * 
     * @param uploadJob The domain upload job entity
     * @return The JPA entity representation
     */
    private UploadJobJpaEntity toEntity(UploadJob uploadJob) {
        UploadJobJpaEntity entity = new UploadJobJpaEntity();
        entity.setId(uploadJob.getId().getValue());
        entity.setUserId(uploadJob.getUserId().getValue());
        entity.setStatus(uploadJob.getStatus().name());
        entity.setTotalFiles(uploadJob.getTotalFiles());
        entity.setCompletedFiles(uploadJob.getCompletedFiles());
        return entity;
    }
    
    /**
     * Converts a JPA UploadJobJpaEntity to a domain UploadJob entity.
     * 
     * @param entity The JPA entity
     * @return The domain upload job entity
     */
    private UploadJob toDomain(UploadJobJpaEntity entity) {
        return new UploadJob(
                UploadJobId.of(entity.getId()),
                UserId.of(entity.getUserId()),
                UploadStatus.valueOf(entity.getStatus()),
                entity.getTotalFiles(),
                entity.getCompletedFiles(),
                entity.getCreatedAt()
        );
    }
}

