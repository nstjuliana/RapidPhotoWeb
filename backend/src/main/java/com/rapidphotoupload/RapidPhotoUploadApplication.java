package com.rapidphotoupload;

import com.rapidphotoupload.config.DotenvConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for RapidPhotoUpload backend.
 * 
 * This Spring Boot application provides a reactive, non-blocking API for
 * handling high-volume photo uploads using Spring WebFlux, PostgreSQL, and AWS S3.
 * 
 * Architecture: Domain-Driven Design (DDD), CQRS, and Vertical Slice Architecture (VSA)
 */
@SpringBootApplication
public class RapidPhotoUploadApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(RapidPhotoUploadApplication.class);
        app.addInitializers(new DotenvConfig());
        app.run(args);
    }
}





