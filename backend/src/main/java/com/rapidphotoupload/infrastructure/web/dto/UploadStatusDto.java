package com.rapidphotoupload.infrastructure.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Data Transfer Object for upload status responses.
 * 
 * This DTO represents the current status of a photo upload operation.
 * It includes the upload status, upload date, and optional error message
 * if the upload has failed.
 * 
 * @author RapidPhotoUpload Team
 * @since 1.0.0
 */
public class UploadStatusDto {
    
    @JsonProperty("photoId")
    private String photoId;
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("uploadDate")
    private String uploadDate;
    
    @JsonProperty("errorMessage")
    private String errorMessage;
    
    public UploadStatusDto() {
    }
    
    public UploadStatusDto(String photoId, String status, String uploadDate, String errorMessage) {
        this.photoId = photoId;
        this.status = status;
        this.uploadDate = uploadDate;
        this.errorMessage = errorMessage;
    }
    
    public String getPhotoId() {
        return photoId;
    }
    
    public void setPhotoId(String photoId) {
        this.photoId = photoId;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getUploadDate() {
        return uploadDate;
    }
    
    public void setUploadDate(String uploadDate) {
        this.uploadDate = uploadDate;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}

