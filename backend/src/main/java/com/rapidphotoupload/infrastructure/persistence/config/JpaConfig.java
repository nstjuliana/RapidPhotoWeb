package com.rapidphotoupload.infrastructure.persistence.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Configuration class for JPA auditing.
 * 
 * Enables automatic population of @CreatedDate and @LastModifiedDate fields
 * in JPA entities that use @EntityListeners(AuditingEntityListener.class).
 * 
 * @author RapidPhotoUpload Team
 * @since 1.0.0
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}

