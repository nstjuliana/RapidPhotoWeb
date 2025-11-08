package com.rapidphotoupload.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Web configuration for CORS and other web-related settings.
 * 
 * This configuration sets up CORS (Cross-Origin Resource Sharing) to allow
 * the web client (running on localhost:3000) to make requests to the backend API.
 * 
 * CORS configuration:
 * - Allowed origins: http://localhost:3000 (web client)
 * - Allowed methods: GET, POST, PUT, DELETE, OPTIONS, PATCH
 * - Allowed headers: Content-Type, Authorization, X-Requested-With
 * - Allow credentials: true
 * 
 * @author RapidPhotoUpload Team
 * @since 1.0.0
 */
@Configuration
public class WebConfig {
    
    /**
     * Creates a CORS web filter for Spring WebFlux.
     * 
     * @return Configured CorsWebFilter
     */
    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        
        // Allow web client origin
        corsConfig.setAllowedOrigins(List.of("http://localhost:3000"));
        
        // Allow all necessary HTTP methods
        corsConfig.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));
        
        // Allow necessary headers
        corsConfig.setAllowedHeaders(Arrays.asList(
                "Content-Type",
                "Authorization",
                "X-Requested-With"
        ));
        
        // Allow credentials (cookies, authorization headers)
        corsConfig.setAllowCredentials(true);
        
        // Cache preflight response for 1 hour
        corsConfig.setMaxAge(3600L);
        
        // Apply CORS configuration to all paths
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);
        
        return new CorsWebFilter(source);
    }
}

