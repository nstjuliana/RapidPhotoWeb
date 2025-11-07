package com.rapidphotoupload.domain.uploadjob;

import java.util.Objects;
import java.util.UUID;

/**
 * Value object representing a unique identifier for an UploadJob entity.
 * 
 * This value object wraps a UUID to provide type safety and prevent primitive obsession.
 * It ensures that UploadJob IDs cannot be confused with other types of IDs (e.g., PhotoId, UserId).
 * 
 * The UploadJobId is immutable and provides factory methods for creation.
 * 
 * @author RapidPhotoUpload Team
 * @since 1.0.0
 */
public final class UploadJobId {
    
    private final UUID value;
    
    /**
     * Private constructor to enforce use of factory methods.
     * 
     * @param value The UUID value for this UploadJobId
     */
    private UploadJobId(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("UploadJobId value cannot be null");
        }
        this.value = value;
    }
    
    /**
     * Creates an UploadJobId from an existing UUID.
     * 
     * @param uuid The UUID to wrap
     * @return A new UploadJobId instance
     * @throws IllegalArgumentException if uuid is null
     */
    public static UploadJobId of(UUID uuid) {
        return new UploadJobId(uuid);
    }
    
    /**
     * Creates an UploadJobId from a UUID string.
     * 
     * @param uuidString The UUID string to parse
     * @return A new UploadJobId instance
     * @throws IllegalArgumentException if uuidString is null or invalid
     */
    public static UploadJobId of(String uuidString) {
        if (uuidString == null || uuidString.isBlank()) {
            throw new IllegalArgumentException("UploadJobId string cannot be null or blank");
        }
        try {
            return new UploadJobId(UUID.fromString(uuidString));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid UUID format for UploadJobId: " + uuidString, e);
        }
    }
    
    /**
     * Generates a new random UploadJobId.
     * 
     * @return A new UploadJobId with a randomly generated UUID
     */
    public static UploadJobId generate() {
        return new UploadJobId(UUID.randomUUID());
    }
    
    /**
     * Returns the UUID value of this UploadJobId.
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
        UploadJobId that = (UploadJobId) o;
        return Objects.equals(value, that.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}

