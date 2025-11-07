package com.rapidphotoupload.infrastructure.persistence.jpa;

import com.rapidphotoupload.domain.user.User;
import com.rapidphotoupload.domain.user.UserId;
import com.rapidphotoupload.domain.user.UserRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * JPA adapter implementation of UserRepository.
 * 
 * This adapter bridges the domain layer (User) and infrastructure layer (UserJpaEntity).
 * It converts between domain objects and JPA entities, and wraps blocking JPA operations
 * in reactive types (Mono/Flux) using bounded elastic scheduler.
 * 
 * @author RapidPhotoUpload Team
 * @since 1.0.0
 */
@Repository
@Primary
public class UserJpaAdapter implements UserRepository {
    
    private final UserJpaRepository jpaRepository;
    
    public UserJpaAdapter(UserJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }
    
    @Override
    public Mono<User> save(User user) {
        return Mono.fromCallable(() -> {
            UserJpaEntity entity = toEntity(user);
            UserJpaEntity saved = jpaRepository.save(entity);
            return toDomain(saved);
        }).subscribeOn(Schedulers.boundedElastic());
    }
    
    @Override
    public Mono<User> findById(UserId id) {
        return Mono.fromCallable(() -> jpaRepository.findById(id.getValue()))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(optional -> optional.map(entity -> Mono.just(toDomain(entity)))
                        .orElse(Mono.empty()));
    }
    
    @Override
    public Mono<User> findByEmail(String email) {
        return Mono.fromCallable(() -> jpaRepository.findByEmail(email))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(optional -> optional.map(entity -> Mono.just(toDomain(entity)))
                        .orElse(Mono.empty()));
    }
    
    @Override
    public Flux<User> findAll() {
        return Mono.fromCallable(() -> jpaRepository.findAll())
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(entities -> Flux.fromIterable(entities)
                        .map(this::toDomain));
    }
    
    @Override
    public Mono<Void> delete(UserId id) {
        return Mono.fromRunnable(() -> jpaRepository.deleteById(id.getValue()))
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }
    
    /**
     * Converts a domain User entity to a JPA UserJpaEntity.
     * 
     * @param user The domain user entity
     * @return The JPA entity representation
     */
    private UserJpaEntity toEntity(User user) {
        UserJpaEntity entity = new UserJpaEntity();
        entity.setId(user.getId().getValue());
        entity.setEmail(user.getEmail());
        entity.setPasswordHash(user.getPasswordHash());
        return entity;
    }
    
    /**
     * Converts a JPA UserJpaEntity to a domain User entity.
     * 
     * @param entity The JPA entity
     * @return The domain user entity
     */
    private User toDomain(UserJpaEntity entity) {
        return new User(
                UserId.of(entity.getId()),
                entity.getEmail(),
                entity.getPasswordHash(),
                entity.getCreatedAt()
        );
    }
}

