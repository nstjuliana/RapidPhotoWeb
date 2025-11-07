package com.rapidphotoupload.domain.user;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * User domain entity representing an application user.
 * 
 * This entity contains:
 * - User identification (ID, email)
 * - Authentication information (password hash)
 * - Account creation timestamp
 * - Basic validation rules for email format
 * 
 * This is a pure domain entity with no infrastructure dependencies (no JPA annotations).
 * Minimal implementation for Phase 2; will be extended in later phases.
 * 
 * @author RapidPhotoUpload Team
 * @since 1.0.0
 */
public class User {
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    
    private final UserId id;
    private final String email;
    private final String passwordHash;
    private final LocalDateTime createdAt;
    
    /**
     * Creates a new User entity.
     * 
     * @param id The unique identifier for this user
     * @param email The user's email address
     * @param passwordHash The hashed password for this user
     * @param createdAt The date and time when the user account was created
     * @throws IllegalArgumentException if email is invalid or passwordHash is null/blank
     */
    public User(UserId id, String email, String passwordHash, LocalDateTime createdAt) {
        this.id = Objects.requireNonNull(id, "User ID cannot be null");
        this.email = validateEmail(email);
        this.passwordHash = validatePasswordHash(passwordHash);
        this.createdAt = Objects.requireNonNull(createdAt, "Created date cannot be null");
    }
    
    /**
     * Validates the email address format.
     * 
     * @param email The email to validate
     * @return The validated email (trimmed)
     * @throws IllegalArgumentException if email is null, blank, or invalid format
     */
    private String validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be null or blank");
        }
        
        String trimmedEmail = email.trim().toLowerCase();
        
        if (trimmedEmail.length() > 255) {
            throw new IllegalArgumentException("Email cannot exceed 255 characters");
        }
        
        if (!EMAIL_PATTERN.matcher(trimmedEmail).matches()) {
            throw new IllegalArgumentException("Invalid email format: " + email);
        }
        
        return trimmedEmail;
    }
    
    /**
     * Validates the password hash.
     * 
     * @param passwordHash The password hash to validate
     * @return The validated password hash
     * @throws IllegalArgumentException if passwordHash is null or blank
     */
    private String validatePasswordHash(String passwordHash) {
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException("Password hash cannot be null or blank");
        }
        if (passwordHash.length() < 32) {
            throw new IllegalArgumentException("Password hash appears to be invalid (too short)");
        }
        return passwordHash;
    }
    
    // Getters
    
    public UserId getId() {
        return id;
    }
    
    public String getEmail() {
        return email;
    }
    
    public String getPasswordHash() {
        return passwordHash;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}

