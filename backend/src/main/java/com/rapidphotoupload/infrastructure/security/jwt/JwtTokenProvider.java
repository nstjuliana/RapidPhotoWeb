package com.rapidphotoupload.infrastructure.security.jwt;

import com.rapidphotoupload.domain.user.UserId;
import com.rapidphotoupload.shared.exceptions.AuthenticationException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

/**
 * Service for generating and validating JWT tokens.
 * 
 * This service handles:
 * - Access token generation (15 minute expiration)
 * - Refresh token generation (7 day expiration)
 * - Token validation (signature and expiration)
 * - Token claim extraction (userId, email)
 * 
 * Uses HMAC-SHA256 algorithm for token signing.
 * 
 * @author RapidPhotoUpload Team
 * @since 1.0.0
 */
@Service
public class JwtTokenProvider {
    
    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;
    
    public JwtTokenProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        
        // Validate secret key
        if (jwtProperties.getSecret() == null || jwtProperties.getSecret().isBlank()) {
            throw new IllegalStateException("JWT secret must be configured");
        }
        
        // Create secret key from string
        byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }
    
    /**
     * Generates an access token for a user.
     * 
     * Access tokens are short-lived (15 minutes) and contain user ID and email.
     * 
     * @param userId The user ID to include in the token
     * @param email The user's email address
     * @return JWT access token string
     */
    public String generateAccessToken(UserId userId, String email) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtProperties.getAccessTokenExpirationMs());
        
        return Jwts.builder()
                .subject(userId.getValue().toString())
                .claim("email", email)
                .claim("type", "access")
                .issuedAt(now)
                .expiration(expiryDate)
                .issuer(jwtProperties.getIssuer())
                .signWith(secretKey)
                .compact();
    }
    
    /**
     * Generates a refresh token for a user.
     * 
     * Refresh tokens are long-lived (7 days) and contain only user ID.
     * 
     * @param userId The user ID to include in the token
     * @return JWT refresh token string
     */
    public String generateRefreshToken(UserId userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtProperties.getRefreshTokenExpirationMs());
        
        return Jwts.builder()
                .subject(userId.getValue().toString())
                .claim("type", "refresh")
                .issuedAt(now)
                .expiration(expiryDate)
                .issuer(jwtProperties.getIssuer())
                .signWith(secretKey)
                .compact();
    }
    
    /**
     * Validates a JWT token and returns the parsed claims.
     * 
     * @param token The JWT token string to validate
     * @return Claims object containing token data
     * @throws AuthenticationException if token is invalid, expired, or malformed
     */
    public Claims validateToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw new AuthenticationException("Token has expired");
        } catch (UnsupportedJwtException e) {
            throw new AuthenticationException("Token is unsupported");
        } catch (MalformedJwtException e) {
            throw new AuthenticationException("Token is malformed");
        } catch (SignatureException e) {
            throw new AuthenticationException("Invalid token signature");
        } catch (IllegalArgumentException e) {
            throw new AuthenticationException("Token is empty or null");
        } catch (Exception e) {
            throw new AuthenticationException("Token validation failed: " + e.getMessage());
        }
    }
    
    /**
     * Extracts the user ID from a JWT token.
     * 
     * @param token The JWT token string
     * @return UserId extracted from token subject claim
     * @throws AuthenticationException if token is invalid
     */
    public UserId getUserIdFromToken(String token) {
        Claims claims = validateToken(token);
        String userIdString = claims.getSubject();
        
        if (userIdString == null || userIdString.isBlank()) {
            throw new AuthenticationException("Token does not contain user ID");
        }
        
        try {
            return UserId.of(UUID.fromString(userIdString));
        } catch (IllegalArgumentException e) {
            throw new AuthenticationException("Invalid user ID format in token");
        }
    }
    
    /**
     * Extracts the email from a JWT token.
     * 
     * @param token The JWT token string
     * @return Email address from token claims
     * @throws AuthenticationException if token is invalid or email claim missing
     */
    public String getEmailFromToken(String token) {
        Claims claims = validateToken(token);
        String email = claims.get("email", String.class);
        
        if (email == null || email.isBlank()) {
            throw new AuthenticationException("Token does not contain email");
        }
        
        return email;
    }
    
    /**
     * Gets the expiration date from a token.
     * 
     * @param token The JWT token string
     * @return Date when the token expires
     * @throws AuthenticationException if token is invalid
     */
    public Date getExpirationDateFromToken(String token) {
        Claims claims = validateToken(token);
        return claims.getExpiration();
    }
    
    /**
     * Checks if a token is a refresh token.
     * 
     * @param token The JWT token string
     * @return true if token type claim is "refresh"
     */
    public boolean isRefreshToken(String token) {
        try {
            Claims claims = validateToken(token);
            String type = claims.get("type", String.class);
            return "refresh".equals(type);
        } catch (Exception e) {
            return false;
        }
    }
}

