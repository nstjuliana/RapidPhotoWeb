package com.rapidphotoupload.domain.photo;

import com.rapidphotoupload.domain.user.UserId;
import com.rapidphotoupload.domain.uploadjob.UploadJobId;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Photo domain entity representing a user-uploaded photo.
 * 
 * This entity contains the core domain logic for photos, including:
 * - Photo metadata (filename, upload date, tags)
 * - S3 storage key reference
 * - Upload status tracking
 * - Domain validation rules
 * - Business logic for tag management and status transitions
 * 
 * This is a pure domain entity with no infrastructure dependencies (no JPA annotations).
 * 
 * @author RapidPhotoUpload Team
 * @since 1.0.0
 */
public class Photo {
    
    private final PhotoId id;
    private final UserId userId;
    private final UploadJobId uploadJobId;
    private final String filename;
    private final String s3Key;
    private final LocalDateTime uploadDate;
    private final Set<String> tags;
    private String status;
    
    /**
     * Creates a new Photo entity.
     * 
     * @param id The unique identifier for this photo
     * @param userId The user who uploaded this photo
     * @param uploadJobId The upload job this photo belongs to (can be null)
     * @param filename The original filename of the photo
     * @param s3Key The S3 storage key where the photo is stored
     * @param uploadDate The date and time when the photo was uploaded
     * @param status The current upload status (e.g., "UPLOADING", "COMPLETED", "FAILED")
     */
    public Photo(
            PhotoId id,
            UserId userId,
            UploadJobId uploadJobId,
            String filename,
            String s3Key,
            LocalDateTime uploadDate,
            String status) {
        this.id = Objects.requireNonNull(id, "Photo ID cannot be null");
        this.userId = Objects.requireNonNull(userId, "User ID cannot be null");
        this.uploadJobId = uploadJobId; // Can be null for photos not part of a batch upload
        this.filename = validateFilename(filename);
        this.s3Key = validateS3Key(s3Key);
        this.uploadDate = Objects.requireNonNull(uploadDate, "Upload date cannot be null");
        this.tags = new HashSet<>();
        this.status = validateStatus(status);
    }
    
    /**
     * Creates a new Photo entity with tags.
     * 
     * @param id The unique identifier for this photo
     * @param userId The user who uploaded this photo
     * @param uploadJobId The upload job this photo belongs to (can be null)
     * @param filename The original filename of the photo
     * @param s3Key The S3 storage key where the photo is stored
     * @param uploadDate The date and time when the photo was uploaded
     * @param tags Initial set of tags for this photo
     * @param status The current upload status
     */
    public Photo(
            PhotoId id,
            UserId userId,
            UploadJobId uploadJobId,
            String filename,
            String s3Key,
            LocalDateTime uploadDate,
            Set<String> tags,
            String status) {
        this(id, userId, uploadJobId, filename, s3Key, uploadDate, status);
        if (tags != null) {
            this.tags.addAll(tags);
        }
    }
    
    /**
     * Validates the filename according to domain rules.
     * 
     * @param filename The filename to validate
     * @return The validated filename
     * @throws IllegalArgumentException if filename is invalid
     */
    private String validateFilename(String filename) {
        if (filename == null || filename.isBlank()) {
            throw new IllegalArgumentException("Filename cannot be null or blank");
        }
        if (filename.length() > 500) {
            throw new IllegalArgumentException("Filename cannot exceed 500 characters");
        }
        return filename.trim();
    }
    
    /**
     * Validates the S3 key according to domain rules.
     * 
     * @param s3Key The S3 key to validate
     * @return The validated S3 key
     * @throws IllegalArgumentException if S3 key is invalid
     */
    private String validateS3Key(String s3Key) {
        if (s3Key == null || s3Key.isBlank()) {
            throw new IllegalArgumentException("S3 key cannot be null or blank");
        }
        if (s3Key.length() > 1000) {
            throw new IllegalArgumentException("S3 key cannot exceed 1000 characters");
        }
        return s3Key.trim();
    }
    
    /**
     * Validates the status value.
     * 
     * @param status The status to validate
     * @return The validated status
     * @throws IllegalArgumentException if status is invalid
     */
    private String validateStatus(String status) {
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("Status cannot be null or blank");
        }
        return status.toUpperCase();
    }
    
    /**
     * Adds a tag to this photo.
     * 
     * Tags are normalized to lowercase and trimmed. Duplicate tags are ignored.
     * 
     * @param tag The tag to add
     * @throws IllegalArgumentException if tag is null or blank
     */
    public void addTag(String tag) {
        if (tag == null || tag.isBlank()) {
            throw new IllegalArgumentException("Tag cannot be null or blank");
        }
        String normalizedTag = tag.trim().toLowerCase();
        if (normalizedTag.length() > 100) {
            throw new IllegalArgumentException("Tag cannot exceed 100 characters");
        }
        tags.add(normalizedTag);
    }
    
    /**
     * Removes a tag from this photo.
     * 
     * @param tag The tag to remove
     * @return true if the tag was present and removed, false otherwise
     */
    public boolean removeTag(String tag) {
        if (tag == null || tag.isBlank()) {
            return false;
        }
        return tags.remove(tag.trim().toLowerCase());
    }
    
    /**
     * Checks if this photo has a specific tag.
     * 
     * @param tag The tag to check
     * @return true if the photo has this tag, false otherwise
     */
    public boolean hasTag(String tag) {
        if (tag == null || tag.isBlank()) {
            return false;
        }
        return tags.contains(tag.trim().toLowerCase());
    }
    
    /**
     * Marks this photo as completed.
     */
    public void markAsCompleted() {
        this.status = "COMPLETED";
    }
    
    /**
     * Marks this photo as failed.
     */
    public void markAsFailed() {
        this.status = "FAILED";
    }
    
    /**
     * Checks if this photo is in a completed state.
     * 
     * @return true if status is COMPLETED, false otherwise
     */
    public boolean isCompleted() {
        return "COMPLETED".equals(status);
    }
    
    /**
     * Checks if this photo is in a failed state.
     * 
     * @return true if status is FAILED, false otherwise
     */
    public boolean isFailed() {
        return "FAILED".equals(status);
    }
    
    // Getters
    
    public PhotoId getId() {
        return id;
    }
    
    public UserId getUserId() {
        return userId;
    }
    
    public UploadJobId getUploadJobId() {
        return uploadJobId;
    }
    
    public String getFilename() {
        return filename;
    }
    
    public String getS3Key() {
        return s3Key;
    }
    
    public LocalDateTime getUploadDate() {
        return uploadDate;
    }
    
    public Set<String> getTags() {
        return new HashSet<>(tags); // Return defensive copy
    }
    
    public String getStatus() {
        return status;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Photo photo = (Photo) o;
        return Objects.equals(id, photo.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "Photo{" +
                "id=" + id +
                ", userId=" + userId +
                ", filename='" + filename + '\'' +
                ", status='" + status + '\'' +
                ", tags=" + tags.size() +
                '}';
    }
}

