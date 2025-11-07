package com.rapidphotoupload.infrastructure.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

/**
 * Data Transfer Object for photo upload requests.
 * 
 * This DTO represents the request body for initiating a photo upload.
 * It includes validation annotations to ensure data integrity before
 * processing the upload command.
 * 
 * @author RapidPhotoUpload Team
 * @since 1.0.0
 */
public class UploadRequestDto {
    
    /**
     * Maximum file size in bytes (50MB).
     */
    private static final long MAX_FILE_SIZE_BYTES = 52_428_800L;
    
    @NotBlank(message = "Filename is required")
    @JsonProperty("filename")
    private String filename;
    
    @NotBlank(message = "Content type is required")
    @JsonProperty("contentType")
    private String contentType;
    
    @NotNull(message = "File size is required")
    @Min(value = 1, message = "File size must be at least 1 byte")
    @Max(value = MAX_FILE_SIZE_BYTES, message = "File size cannot exceed 50MB")
    @JsonProperty("fileSize")
    private Long fileSize;
    
    @JsonProperty("tags")
    private Set<String> tags;
    
    public UploadRequestDto() {
        this.tags = new HashSet<>();
    }
    
    public UploadRequestDto(String filename, String contentType, Long fileSize, Set<String> tags) {
        this.filename = filename;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.tags = tags != null ? new HashSet<>(tags) : new HashSet<>();
    }
    
    public String getFilename() {
        return filename;
    }
    
    public void setFilename(String filename) {
        this.filename = filename;
    }
    
    public String getContentType() {
        return contentType;
    }
    
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
    
    public Long getFileSize() {
        return fileSize;
    }
    
    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }
    
    public Set<String> getTags() {
        return tags != null ? new HashSet<>(tags) : new HashSet<>();
    }
    
    public void setTags(Set<String> tags) {
        this.tags = tags != null ? new HashSet<>(tags) : new HashSet<>();
    }
}

