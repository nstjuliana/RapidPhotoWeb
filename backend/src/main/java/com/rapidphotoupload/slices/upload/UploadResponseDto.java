package com.rapidphotoupload.slices.upload;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Data Transfer Object for photo upload response.
 * 
 * This DTO represents the response returned after initiating an upload.
 * It contains the presigned URL that the client will use to upload
 * the file directly to S3, along with metadata about the upload.
 * 
 * @author RapidPhotoUpload Team
 * @since 1.0.0
 */
public class UploadResponseDto {
    
    @JsonProperty("photoId")
    private String photoId;
    
    @JsonProperty("presignedUrl")
    private String presignedUrl;
    
    @JsonProperty("s3Key")
    private String s3Key;
    
    @JsonProperty("expirationTime")
    private Long expirationTime;
    
    public UploadResponseDto() {
    }
    
    public UploadResponseDto(String photoId, String presignedUrl, String s3Key, Long expirationTime) {
        this.photoId = photoId;
        this.presignedUrl = presignedUrl;
        this.s3Key = s3Key;
        this.expirationTime = expirationTime;
    }
    
    public String getPhotoId() {
        return photoId;
    }
    
    public void setPhotoId(String photoId) {
        this.photoId = photoId;
    }
    
    public String getPresignedUrl() {
        return presignedUrl;
    }
    
    public void setPresignedUrl(String presignedUrl) {
        this.presignedUrl = presignedUrl;
    }
    
    public String getS3Key() {
        return s3Key;
    }
    
    public void setS3Key(String s3Key) {
        this.s3Key = s3Key;
    }
    
    public Long getExpirationTime() {
        return expirationTime;
    }
    
    public void setExpirationTime(Long expirationTime) {
        this.expirationTime = expirationTime;
    }
}

