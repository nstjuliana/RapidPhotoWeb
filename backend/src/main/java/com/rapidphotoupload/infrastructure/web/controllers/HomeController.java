package com.rapidphotoupload.infrastructure.web.controllers;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Home controller providing API information at the root path.
 * 
 * This controller provides a simple response at the root endpoint
 * to help users discover available API endpoints.
 * 
 * @author RapidPhotoUpload Team
 * @since 1.0.0
 */
@RestController
public class HomeController {
    
    @GetMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Map<String, Object>> home() {
        return Mono.just(Map.of(
                "name", "RapidPhotoUpload API",
                "version", "1.0.0-SNAPSHOT",
                "description", "High-performance asynchronous photo upload system",
                "endpoints", Map.of(
                        "upload", "/api/uploads",
                        "status", "/api/uploads/{photoId}/status",
                        "health", "/actuator/health"
                ),
                "documentation", "See TESTING_GUIDE.md for API usage examples"
        ));
    }
}

