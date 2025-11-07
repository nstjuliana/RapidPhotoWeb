package com.rapidphotoupload.domain.photo;

import java.util.Objects;
import java.util.UUID;

/**
 * Value object representing a unique identifier for a Photo entity.
 * 
 * This value object wraps a UUID to provide type safety and prevent primitive obsession.
 * It ensures that Photo IDs cannot be confused with other types of IDs (e.g., UserId, UploadJobId).
 * 
 * The PhotoId is immutable and provides factory methods for creation.
 * 
 * @author RapidPhotoUpload Team
 * @since 1.0.0
 */
public final class PhotoId {
    
    private final UUID value;
    
    /**
     * Private constructor to enforce use of factory methods.
     * 
     * @param value The UUID value for this PhotoId
     */
    private PhotoId(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("PhotoId value cannot be null");
        }
        this.value = value;
    }
    
    /**
     * Creates a PhotoId from an existing UUID.
     * 
     * @param uuid The UUID to wrap
     * @return A new PhotoId instance
     * @throws IllegalArgumentException if uuid is null
     */
    public static PhotoId of(UUID uuid) {
        return new PhotoId(uuid);
    }
    
    /**
     * Creates a PhotoId from a UUID string.
     * 
     * @param uuidString The UUID string to parse
     * @return A new PhotoId instance
     * @throws IllegalArgumentException if uuidString is null or invalid
     */
    public static PhotoId of(String uuidString) {
        if (uuidString == null || uuidString.isBlank()) {
            throw new IllegalArgumentException("PhotoId string cannot be null or blank");
        }
        try {
            return new PhotoId(UUID.fromString(uuidString));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid UUID format for PhotoId: " + uuidString, e);
        }
    }
    
    /**
     * Generates a new random PhotoId.
     * 
     * @return A new PhotoId with a randomly generated UUID
     */
    public static PhotoId generate() {
        return new PhotoId(UUID.randomUUID());
    }
    
    /**
     * Returns the UUID value of this PhotoId.
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
        PhotoId photoId = (PhotoId) o;
        return Objects.equals(value, photoId.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}

