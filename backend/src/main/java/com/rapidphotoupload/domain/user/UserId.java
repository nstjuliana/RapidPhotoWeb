package com.rapidphotoupload.domain.user;

import java.util.Objects;
import java.util.UUID;

/**
 * Value object representing a unique identifier for a User entity.
 * 
 * This value object wraps a UUID to provide type safety and prevent primitive obsession.
 * It ensures that User IDs cannot be confused with other types of IDs (e.g., PhotoId, UploadJobId).
 * 
 * The UserId is immutable and provides factory methods for creation.
 * 
 * @author RapidPhotoUpload Team
 * @since 1.0.0
 */
public final class UserId {
    
    private final UUID value;
    
    /**
     * Private constructor to enforce use of factory methods.
     * 
     * @param value The UUID value for this UserId
     */
    private UserId(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("UserId value cannot be null");
        }
        this.value = value;
    }
    
    /**
     * Creates a UserId from an existing UUID.
     * 
     * @param uuid The UUID to wrap
     * @return A new UserId instance
     * @throws IllegalArgumentException if uuid is null
     */
    public static UserId of(UUID uuid) {
        return new UserId(uuid);
    }
    
    /**
     * Creates a UserId from a UUID string.
     * 
     * @param uuidString The UUID string to parse
     * @return A new UserId instance
     * @throws IllegalArgumentException if uuidString is null or invalid
     */
    public static UserId of(String uuidString) {
        if (uuidString == null || uuidString.isBlank()) {
            throw new IllegalArgumentException("UserId string cannot be null or blank");
        }
        try {
            return new UserId(UUID.fromString(uuidString));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid UUID format for UserId: " + uuidString, e);
        }
    }
    
    /**
     * Generates a new random UserId.
     * 
     * @return A new UserId with a randomly generated UUID
     */
    public static UserId generate() {
        return new UserId(UUID.randomUUID());
    }
    
    /**
     * Returns the UUID value of this UserId.
     * 
     * @return The wrapped UUID value
     */
    public UUID getValue() {
        return value;
    }
    
    /**
     * Returns the string representation of the UUID.
     * 
     * @return The UUID as a string
     */
    public String toString() {
        return value.toString();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserId userId = (UserId) o;
        return Objects.equals(value, userId.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}

