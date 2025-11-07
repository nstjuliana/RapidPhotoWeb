package com.rapidphotoupload.slices.photo;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Data Transfer Object for Photo entities.
 * 
 * This DTO represents a photo in API responses, including all necessary metadata
 * and a presigned download URL for accessing the photo from S3.
 * 
 * @author RapidPhotoUpload Team
 * @since 1.0.0
 */
public class PhotoDto {
    
    private String id;
    private String filename;
    private String s3Key;
    private LocalDateTime uploadDate;
    private Set<String> tags;
    private String status;
    private String downloadUrl;
    
    public PhotoDto() {
    }
    
    public PhotoDto(
            String id,
            String filename,
            String s3Key,
            LocalDateTime uploadDate,
            Set<String> tags,
            String status,
            String downloadUrl) {
        this.id = id;
        this.filename = filename;
        this.s3Key = s3Key;
        this.uploadDate = uploadDate;
        this.tags = tags;
        this.status = status;
        this.downloadUrl = downloadUrl;
    }
    
    // Getters and Setters
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getFilename() {
        return filename;
    }
    
    public void setFilename(String filename) {
        this.filename = filename;
    }
    
    public String getS3Key() {
        return s3Key;
    }
    
    public void setS3Key(String s3Key) {
        this.s3Key = s3Key;
    }
    
    public LocalDateTime getUploadDate() {
        return uploadDate;
    }
    
    public void setUploadDate(LocalDateTime uploadDate) {
        this.uploadDate = uploadDate;
    }
    
    public Set<String> getTags() {
        return tags;
    }
    
    public void setTags(Set<String> tags) {
        this.tags = tags;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getDownloadUrl() {
        return downloadUrl;
    }
    
    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }
}

