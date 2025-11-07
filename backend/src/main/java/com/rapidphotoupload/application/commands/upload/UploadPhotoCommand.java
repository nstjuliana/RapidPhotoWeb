package com.rapidphotoupload.application.commands.upload;

import com.rapidphotoupload.domain.user.UserId;
import com.rapidphotoupload.shared.exceptions.ValidationException;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Command object for uploading a photo.
 * 
 * This command encapsulates all the data required to initiate a photo upload.
 * It follows the CQRS command pattern and includes validation logic to ensure
 * data integrity before processing.
 * 
 * The command validates:
 * - File size (max 50MB)
 * - Content type (must be an image type)
 * - Filename (not blank, valid characters)
 * 
 * @author RapidPhotoUpload Team
 * @since 1.0.0
 */
public class UploadPhotoCommand {
    
    /**
     * Maximum file size in bytes (50MB).
     */
    private static final long MAX_FILE_SIZE_BYTES = 52_428_800L;
    
    /**
     * Allowed image content types.
     */
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/gif",
            "image/webp"
    );
    
    private final UserId userId;
    private final String filename;
    private final String contentType;
    private final Long fileSize;
    private final Set<String> tags;
    
    /**
     * Creates a new UploadPhotoCommand with validation.
     * 
     * @param userId The user ID initiating the upload
     * @param filename The original filename of the photo
     * @param contentType The MIME type of the photo
     * @param fileSize The size of the file in bytes
     * @param tags Optional set of tags for the photo
     * @throws ValidationException if any validation fails
     */
    public UploadPhotoCommand(
            UserId userId,
            String filename,
            String contentType,
            Long fileSize,
            Set<String> tags) {
        this.userId = Objects.requireNonNull(userId, "User ID cannot be null");
        this.filename = validateFilename(filename);
        this.contentType = validateContentType(contentType);
        this.fileSize = validateFileSize(fileSize);
        this.tags = tags != null ? new HashSet<>(tags) : new HashSet<>();
    }
    
    /**
     * Validates the filename according to domain rules.
     * 
     * @param filename The filename to validate
     * @return The validated filename
     * @throws ValidationException if filename is invalid
     */
    private String validateFilename(String filename) {
        if (filename == null || filename.isBlank()) {
            throw new ValidationException("Filename cannot be null or blank");
        }
        String trimmed = filename.trim();
        if (trimmed.length() > 500) {
            throw new ValidationException("Filename cannot exceed 500 characters");
        }
        return trimmed;
    }
    
    /**
     * Validates the content type is an allowed image type.
     * 
     * @param contentType The content type to validate
     * @return The validated content type (normalized to lowercase)
     * @throws ValidationException if content type is invalid
     */
    private String validateContentType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            throw new ValidationException("Content type cannot be null or blank");
        }
        String normalized = contentType.toLowerCase().trim();
        if (!ALLOWED_CONTENT_TYPES.contains(normalized)) {
            throw new ValidationException(
                    "Content type must be one of: " + String.join(", ", ALLOWED_CONTENT_TYPES));
        }
        return normalized;
    }
    
    /**
     * Validates the file size is within acceptable limits.
     * 
     * @param fileSize The file size in bytes
     * @return The validated file size
     * @throws ValidationException if file size is invalid
     */
    private Long validateFileSize(Long fileSize) {
        if (fileSize == null) {
            throw new ValidationException("File size cannot be null");
        }
        if (fileSize < 1) {
            throw new ValidationException("File size must be at least 1 byte");
        }
        if (fileSize > MAX_FILE_SIZE_BYTES) {
            throw new ValidationException(
                    "File size cannot exceed " + (MAX_FILE_SIZE_BYTES / 1_048_576) + "MB");
        }
        return fileSize;
    }
    
    public UserId getUserId() {
        return userId;
    }
    
    public String getFilename() {
        return filename;
    }
    
    public String getContentType() {
        return contentType;
    }
    
    public Long getFileSize() {
        return fileSize;
    }
    
    public Set<String> getTags() {
        return new HashSet<>(tags); // Return defensive copy
    }
}

