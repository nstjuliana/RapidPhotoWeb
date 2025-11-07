package com.rapidphotoupload.infrastructure.security;

import com.rapidphotoupload.domain.user.UserId;
import com.rapidphotoupload.shared.exceptions.AuthenticationException;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Mock authentication service for Phase 4 development.
 * 
 * This is a temporary, simple authentication service that stores tokens in memory.
 * It will be replaced with JWT-based authentication in Phase 8.
 * 
 * Features:
 * - Token generation using UUID
 * - Token validation
 * - Token invalidation (logout)
 * - In-memory storage using ConcurrentHashMap
 * 
 * Security Note: This is NOT production-ready. Tokens are stored in memory
 * and will be lost on server restart. No encryption or secure storage is used.
 * 
 * @author RapidPhotoUpload Team
 * @since 1.0.0
 */
@Service
public class MockAuthService {
    
    /**
     * In-memory token storage: token -> userId mapping
     */
    private final ConcurrentMap<String, UserId> tokenStore = new ConcurrentHashMap<>();
    
    /**
     * Generates a new authentication token for a user.
     * 
     * @param userId The user ID to generate token for
     * @return A new UUID-based token string
     */
    public String generateToken(UserId userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        
        String token = UUID.randomUUID().toString();
        tokenStore.put(token, userId);
        
        return token;
    }
    
    /**
     * Validates a token and returns the associated user ID.
     * 
     * @param token The token to validate
     * @return The UserId associated with the token
     * @throws DomainException if token is invalid or not found
     */
    public UserId validateToken(String token) {
        if (token == null || token.isBlank()) {
            throw new AuthenticationException("Token cannot be null or blank");
        }
        
        UserId userId = tokenStore.get(token);
        if (userId == null) {
            throw new AuthenticationException("Invalid or expired token");
        }
        
        return userId;
    }
    
    /**
     * Invalidates a token (logout).
     * 
     * @param token The token to invalidate
     */
    public void invalidateToken(String token) {
        if (token != null && !token.isBlank()) {
            tokenStore.remove(token);
        }
    }
    
    /**
     * Checks if a token is valid without throwing an exception.
     * 
     * @param token The token to check
     * @return true if token is valid, false otherwise
     */
    public boolean isTokenValid(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        return tokenStore.containsKey(token);
    }
}

