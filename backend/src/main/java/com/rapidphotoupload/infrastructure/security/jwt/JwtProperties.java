package com.rapidphotoupload.infrastructure.security.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for JWT token generation and validation.
 * 
 * These properties are loaded from application.yml and can be overridden
 * with environment variables.
 * 
 * @author RapidPhotoUpload Team
 * @since 1.0.0
 */
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    
    /**
     * Secret key used to sign JWT tokens.
     * Must be set via environment variable JWT_SECRET in production.
     */
    private String secret;
    
    /**
     * Access token expiration time in milliseconds.
     * Default: 15 minutes (900000 ms)
     */
    private long accessTokenExpirationMs = 900_000L; // 15 minutes
    
    /**
     * Refresh token expiration time in milliseconds.
     * Default: 7 days (604800000 ms)
     */
    private long refreshTokenExpirationMs = 6_048_000_00L; // 7 days
    
    /**
     * Token issuer claim.
     * Default: rapid-photo-upload
     */
    private String issuer = "rapid-photo-upload";
    
    public String getSecret() {
        return secret;
    }
    
    public void setSecret(String secret) {
        this.secret = secret;
    }
    
    public long getAccessTokenExpirationMs() {
        return accessTokenExpirationMs;
    }
    
    public void setAccessTokenExpirationMs(long accessTokenExpirationMs) {
        this.accessTokenExpirationMs = accessTokenExpirationMs;
    }
    
    public long getRefreshTokenExpirationMs() {
        return refreshTokenExpirationMs;
    }
    
    public void setRefreshTokenExpirationMs(long refreshTokenExpirationMs) {
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
    }
    
    public String getIssuer() {
        return issuer;
    }
    
    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }
}

